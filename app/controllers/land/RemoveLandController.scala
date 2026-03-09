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
import models.{Land, ReturnVersionUpdateRequest}
import pages.land.RemoveLandPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.land.RemoveLandView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveLandController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: RemoveLandFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         backendConnector: StampDutyLandTaxConnector,
                                         view: RemoveLandView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      //TODO update to take landResourceRef from user answers overview page DTR-2498.
      val landResourceRef = "LND-REF-001"
      // request.userAnswers.get(LandOverviewRemovePage).map { landResourceRef =>

      val maybeReturnLandToRemove = request.userAnswers.fullReturn.flatMap(_.land.flatMap(_.find(_.landResourceRef.exists( _.trim == landResourceRef.trim))))
      val addressLine1 = maybeReturnLandToRemove.flatMap(_.address1).getOrElse("")

      maybeReturnLandToRemove match {
        case None =>
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())

        case Some(land) =>
          val preparedForm = request.userAnswers.get(RemoveLandPage) match {
            case None => form
            case Some(value) => form.fill(value)
          }
          Ok(view(preparedForm, addressLine1))
      }//TODO remove bellow comments post overview page DTR-2498 implementation.
       /*.getOrElse(
      Redirect(controllers.land.routes.LandOverviewController.onPageLoad())
    )*/
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      //TODO update to take ID from user answers overview page DTR-2498
      val landResourceRef = "LND-REF-001"
      // request.userAnswers.get(LandOverviewRemovePage).map { landResourceRef =>
      val maybeLandToDelete: Option[Land] = for {
        fullReturn <- request.userAnswers.fullReturn
        allLands <- fullReturn.land
        returnLandToDelete <- allLands.find(_.landResourceRef.exists(_.trim == landResourceRef.trim))
      } yield returnLandToDelete

      maybeLandToDelete match {
        case None =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

        case Some(maybeLandToDelete) =>
          val addressLine1 = maybeLandToDelete.address1.getOrElse("")
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, addressLine1))),

            value =>
              if (value) {
                (for {
                  updateReturnVersionRequest <- ReturnVersionUpdateRequest.from(request.userAnswers)
                  returnVersion <- backendConnector.updateReturnVersion(updateReturnVersionRequest)
                  deleteLandRequest <- DeleteLandRequest.from(request.userAnswers, landResourceRef) if returnVersion.newVersion.isDefined
                  deleteLandReturn <- backendConnector.deleteLand(deleteLandRequest) if returnVersion.newVersion.isDefined
                } yield {
                  //Redirect(controllers.land.routes.LandOverviewController.onPageLoad()).flashing("landDeleted" -> addressLine1)
                  Redirect(controllers.land.routes.RemoveLandController.onPageLoad()).flashing("landDeleted" -> addressLine1) //TODO update to land  overview page DTR-2498
                }).recover {
                case _ =>
                 // Redirect(controllers.land.routes.LandOverviewController.onPageLoad())
                  Redirect(controllers.routes.ReturnTaskListController.onPageLoad()) //TODO update to land  overview page DTR-2498

              }
              } else {
                //Future.successful(Redirect(controllers.land.routes.LandOverviewController.onPageLoad()))
                Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad())) //TODO update to land overview page DTR-2498
              }
          )
      }//TODO remove bellow comments post overview page DTR-2498 implementation.
    /*.getOrElse(
   Redirect(controllers.land.routes.LandOverviewController.onPageLoad())
 )*/
  }
}
