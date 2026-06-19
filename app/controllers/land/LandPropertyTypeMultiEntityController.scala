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

import controllers.actions.*
import pages.land.LandOverviewRemovePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.crossflow.fields.CrossFlowValidationService
import services.land.LandService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.land.LandPropertyTypeMultiEntityView
import services.land.PopulateLandService
import models.CheckMode

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LandPropertyTypeMultiEntityController @Inject() (
                                                        override val messagesApi:  MessagesApi,
                                                        identify:                  IdentifierAction,
                                                        getData:                   DataRetrievalAction,
                                                        requireData:               DataRequiredAction,
                                                        sessionRepository:         SessionRepository,
                                                        crossFlow:                 CrossFlowValidationService,
                                                        landService:               LandService,
                                                        populateLandService:       PopulateLandService,
                                                        val controllerComponents:  MessagesControllerComponents,
                                                        view:                      LandPropertyTypeMultiEntityView
                                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {
  
  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      
      val nonCf6 = crossFlow.landFailuresExcluding(Set("Cf-6"), request.userAnswers)

      if (nonCf6.nonEmpty) {
        Future.successful(Redirect(
          controllers.land.routes.LandAuthorityCodeMultiEntityController.onPageLoad()
        ))
      } else {
        val cf6Failures = crossFlow.landFailuresOnly(Set("Cf-6"), request.userAnswers)

        cf6Failures match {

          case Nil =>
            Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad(None)))

          case lands =>
            val allLands = request.userAnswers.fullReturn
              .flatMap(_.land)
              .getOrElse(Seq.empty)

            val rows = landService.generateLandPropertyTypeRows(allLands, lands.map(_._1))

            val failure = lands.head._2.head

            Future.successful(Ok(view(
              rows         = rows,
              heading      = failure.headingKey,
              body         = failure.body,
              continueUrl  = controllers.routes.ReturnTaskListController.onPageLoad(None).url,
              problemCount = lands.size
            )))
        }
      }
  }

  def removeLand(landId: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      for {
        updatedAnswers <- Future.fromTry(request.userAnswers.set(LandOverviewRemovePage, landId))
        _              <- sessionRepository.set(updatedAnswers)
      } yield Redirect(controllers.land.routes.RemoveLandController.onPageLoad())
    }

  def updateLand(landId: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>

      val maybeLand = request.userAnswers.fullReturn
        .flatMap(_.land)
        .flatMap(_.find(_.landID.contains(landId)))

      maybeLand match {
        case Some(land) =>
          for {
            updatedAnswers <- Future.fromTry(populateLandService.populateLandInSession(land, request.userAnswers))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(controllers.land.routes.LandTypeOfPropertyController.onPageLoad(CheckMode))

        case None =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }
}