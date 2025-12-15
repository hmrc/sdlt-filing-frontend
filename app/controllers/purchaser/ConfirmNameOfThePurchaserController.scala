/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.purchaser

import controllers.actions.*
import forms.purchaser.ConfirmNameOfThePurchaserFormProvider
import models.*
import navigation.Navigator
import pages.purchaser.ConfirmNameOfThePurchaserPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.purchaser.PurchaserService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.purchaser.ConfirmNameOfThePurchaserView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class ConfirmNameOfThePurchaserController @Inject()(
                                                     override val messagesApi: MessagesApi,
                                                     sessionRepository: SessionRepository,
                                                     navigator: Navigator,
                                                     identify: IdentifierAction,
                                                     getData: DataRetrievalAction,
                                                     requireData: DataRequiredAction,
                                                     formProvider: ConfirmNameOfThePurchaserFormProvider,
                                                     val controllerComponents: MessagesControllerComponents,
                                                     purchaserService: PurchaserService,
                                                     view: ConfirmNameOfThePurchaserView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val purchaserOpt: Option[Purchaser] = request.userAnswers.fullReturn
        .flatMap(_.purchaser)
        .flatMap(_.headOption)

      purchaserOpt match {
        case Some(purchaser) if purchaser.address1.isEmpty && (purchaser.surname.isDefined || purchaser.companyName.isDefined) =>
          val preparedForm = request.userAnswers.get(ConfirmNameOfThePurchaserPage) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          val isCompany = purchaser.companyName.isDefined
          val name = purchaser.companyName.orElse(purchaser.surname).getOrElse("")

          sessionRepository.set(request.userAnswers.copy(data = Json.obj())).map { _ =>
            Ok(view(preparedForm, mode, name, isCompany))
          }

        case _ =>
          Future.successful(Redirect(controllers.purchaser.routes.WhoIsMakingThePurchaseController.onPageLoad(NormalMode)))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          val purchaserOpt: Option[Purchaser] = request.userAnswers.fullReturn
            .flatMap(_.purchaser)
            .flatMap(_.headOption)

          val isCompany = purchaserOpt.flatMap(_.companyName).isDefined
          val name = purchaserOpt
            .flatMap(p => p.companyName.orElse(p.surname))
            .getOrElse("")

          sessionRepository.set(request.userAnswers).map { _ =>
            BadRequest(view(formWithErrors, mode, name, isCompany))
          }
        },
        value =>
          purchaserService.populatePurchaserNameInSession(
            purchaserCheck = value.toString,
            userAnswers = request.userAnswers
          ) match {
            case Success(updatedAnswers) =>
              sessionRepository.set(updatedAnswers).map { _ =>
                if (value.toString == "yes") {
                  //TODO - update to use navigator?
                  Redirect(controllers.purchaser.routes.PurchaserAddressController.redirectToAddressLookupPurchaser())
                } else {
                  Redirect(controllers.purchaser.routes.WhoIsMakingThePurchaseController.onPageLoad(NormalMode))
                }
              }
            case Failure(exception) =>
              Future.successful(InternalServerError("Failed to update session"))
          }
      )
  }
}
