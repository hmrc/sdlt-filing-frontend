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
import forms.purchaser.AddPurchaserPhoneNumberFormProvider
import models.purchaser.WhoIsMakingThePurchase
import models.{CheckMode, Mode, NormalMode}
import navigation.Navigator
import pages.purchaser.{AddPurchaserPhoneNumberPage, EnterPurchaserPhoneNumberPage, NameOfPurchaserPage, WhoIsMakingThePurchasePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.purchaser.PurchaserService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.purchaser.AddPurchaserPhoneNumberView

import scala.util.Success
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddPurchaserPhoneNumberController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   sessionRepository: SessionRepository,
                                                   navigator: Navigator,
                                                   identify: IdentifierAction,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   formProvider: AddPurchaserPhoneNumberFormProvider,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   view: AddPurchaserPhoneNumberView,
                                                   purchaserService: PurchaserService
                                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      request.userAnswers.get(NameOfPurchaserPage) match {
        case None =>
          Redirect(controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode))

        case Some(purchaser) =>
          val purchaserName = purchaser.fullName

          val preparedForm = request.userAnswers.get(AddPurchaserPhoneNumberPage) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          purchaserService.continueIfAddingMainPurchaserToRoute(
            userAnswers = request.userAnswers,
            continueRoute = Ok(view(preparedForm, mode, purchaserName)),
            mode = mode,
            journeyJumpRoute = Redirect(controllers.purchaser.routes.IsPurchaserActingAsTrusteeController.onPageLoad(mode))
          )
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      request.userAnswers.get(NameOfPurchaserPage) match {
        case None =>
          Future.successful(
            Redirect(controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(mode))
          )

        case Some(purchaser) =>
          val purchaserName: String = purchaser.fullName

          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, purchaserName))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(AddPurchaserPhoneNumberPage, value))
                removePhoneNumber <- Future.fromTry {
                  if !value then updatedAnswers.remove(EnterPurchaserPhoneNumberPage)
                  else Success(updatedAnswers)
                }
                _ <- sessionRepository.set(removePhoneNumber)
              } yield {
                (value, removePhoneNumber.get(WhoIsMakingThePurchasePage)) match {

                  case (true, _) =>
                    Redirect(navigator.nextPage(AddPurchaserPhoneNumberPage, mode, removePhoneNumber))

                  case (false, Some(WhoIsMakingThePurchase.Individual)) => if (mode == CheckMode) {
                Redirect(controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad())
                }
                else {
                Redirect(controllers.purchaser.routes.DoesPurchaserHaveNIController.onPageLoad(mode))
                }
                case (false, Some(WhoIsMakingThePurchase.Company)) => if (mode == CheckMode) {

                    Redirect(controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad())
                  }
                  else {
                    Redirect(controllers.purchaser.routes.PurchaserConfirmIdentityController.onPageLoad(mode))

                  }
                  case _ =>
                    Redirect(controllers.purchaser.routes.WhoIsMakingThePurchaseController.onPageLoad(mode))
                }
              }
          )
      }
  }
}