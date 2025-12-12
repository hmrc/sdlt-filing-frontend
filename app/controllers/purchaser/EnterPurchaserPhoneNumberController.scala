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
import forms.purchaser.EnterPurchaserPhoneNumberFormProvider
import models.purchaser.{NameOfPurchaser, WhoIsMakingThePurchase}
import models.{Mode, NormalMode}
import navigation.Navigator
import pages.purchaser.{EnterPurchaserPhoneNumberPage, NameOfPurchaserPage, WhoIsMakingThePurchasePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.purchaser.EnterPurchaserPhoneNumberView
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EnterPurchaserPhoneNumberController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        sessionRepository: SessionRepository,
                                        navigator: Navigator,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: EnterPurchaserPhoneNumberFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        val view: EnterPurchaserPhoneNumberView
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      request.userAnswers.get(NameOfPurchaserPage) match {
        case None =>
          Redirect(controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode))

        case Some(purchaser) =>
          val purchaserName = purchaser.fullName

          val preparedForm = request.userAnswers.get(EnterPurchaserPhoneNumberPage) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, mode, purchaserName))
      }
  }


  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      request.userAnswers.get(NameOfPurchaserPage) match {
        case None =>
          Future.successful(Redirect(controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode)))

        case Some(purchaser) =>
          val purchaserName = purchaser.fullName

          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, purchaserName))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(EnterPurchaserPhoneNumberPage, value))
                _ <- sessionRepository.set(updatedAnswers)
              } yield {
                updatedAnswers.get(WhoIsMakingThePurchasePage) match {
                  case Some(WhoIsMakingThePurchase.Individual) =>
                    Redirect(navigator.nextPage(EnterPurchaserPhoneNumberPage, mode, updatedAnswers))
                  case Some(WhoIsMakingThePurchase.Company) =>
                    Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()) // TODO: DTR-1603 when built
                  case None =>
                    Redirect(controllers.purchaser.routes.WhoIsMakingThePurchaseController.onPageLoad(mode))
                }
              }
          )
      }
  }
}