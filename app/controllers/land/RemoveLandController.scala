/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.land

import connectors.StampDutyLandTaxConnector
import controllers.actions.*
import forms.land.RemoveLandFormProvider
import models.land.DeleteLandRequest
import models.requests.DataRequest
import models.{Land, ReturnVersionUpdateRequest}
import pages.land.{LandOverviewRemovePage, RemoveLandPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.*
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.land.RemoveLandView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@Singleton
class RemoveLandController @Inject() (
                                       override val messagesApi: MessagesApi,
                                       identify:                 IdentifierAction,
                                       getData:                  DataRetrievalAction,
                                       requireData:              DataRequiredAction,
                                       statusCheck:              CheckSubmissionStatusAction,
                                       formProvider:             RemoveLandFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       backendConnector:         StampDutyLandTaxConnector,
                                       view:                     RemoveLandView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val actions = identify andThen getData andThen requireData andThen statusCheck

  private val landOverview: Result       = Redirect(controllers.land.routes.LandOverviewController.onPageLoad())
  private val journeyRecovery: Result    = Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
  private val updateVersionError: Result = Redirect(controllers.routes.UpdateReturnVersionErrorController.onPageLoad())

  private def landToRemove(request: DataRequest[_]): Option[Land] =
    request.userAnswers.get(LandOverviewRemovePage).flatMap { removeLandId =>
      request.userAnswers.fullReturn
        .flatMap(_.land)
        .flatMap(_.find(_.landID.contains(removeLandId)))
    }

  def onPageLoad(): Action[AnyContent] = actions { implicit request =>
    request.userAnswers.get(LandOverviewRemovePage) match {
      case None => landOverview
      case Some(_) =>
        landToRemove(request) match {
          case None => journeyRecovery
          case Some(land) =>
            val form     = formProvider(land.address1)
            val prepared = request.userAnswers.get(RemoveLandPage).fold(form)(form.fill)
            Ok(view(prepared, land.address1.getOrElse("")))
        }
    }
  }

  def onSubmit(): Action[AnyContent] = actions.async { implicit request =>
    request.userAnswers.get(LandOverviewRemovePage) match {
      case None => Future.successful(landOverview)
      case Some(_) =>
        landToRemove(request).filter(_.landResourceRef.isDefined) match {
          case None => Future.successful(journeyRecovery)
          case Some(land) =>
            formProvider(land.address1).bindFromRequest().fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, land.address1.getOrElse("")))),
              confirmed =>
                if (confirmed) removeLand(land)
                else Future.successful(landOverview)
            )
        }
    }
  }

  private def removeLand(land: Land)(implicit request: DataRequest[_]): Future[Result] = {
    val addressLine1 = land.address1.getOrElse("")

    (for {
      updateReturnVersionRequest <- ReturnVersionUpdateRequest.from(request.userAnswers)
      versionResult <-
        backendConnector
          .updateReturnVersion(updateReturnVersionRequest)
          .map(Right(_))
          .recover { case NonFatal(_) => Left(updateVersionError) }
      result <- versionResult match {
        case Left(error)                                => Future.successful(error)
        case Right(version) if version.newVersion.isEmpty => Future.successful(landOverview)
        case Right(_) =>
          for {
            deleteLandRequest <- DeleteLandRequest.from(request.userAnswers, land.landResourceRef.get)
            _                 <- backendConnector.deleteLand(deleteLandRequest)
          } yield landOverview.flashing("landDeleted" -> addressLine1)
      }
    } yield result).recover { case _ => landOverview }
  }
}