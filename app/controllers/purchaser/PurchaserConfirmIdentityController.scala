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
import forms.purchaser.PurchaserConfirmIdentityFormProvider
import models.purchaser.{PurchaserConfirmIdentity, WhoIsMakingThePurchase}
import models.{CheckMode, Mode, NormalMode}
import pages.purchaser.{NameOfPurchaserPage, PurchaserConfirmIdentityPage, WhoIsMakingThePurchasePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.purchaser.PurchaserService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.purchaser.PurchaserConfirmIdentityView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PurchaserConfirmIdentityController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       sessionRepository: SessionRepository,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: PurchaserConfirmIdentityFormProvider,
                                       purchaserService: PurchaserService,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: PurchaserConfirmIdentityView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[PurchaserConfirmIdentity] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      request.userAnswers.get(NameOfPurchaserPage) match {
        case None =>
          Redirect(controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode))

        case Some(purchaserName) =>
          val preparedForm = request.userAnswers.get(PurchaserConfirmIdentityPage) match {
            case None => form
            case Some(value) => form.fill(value)
          }
          purchaserService.checkPurchaserType(
            purchaserType = WhoIsMakingThePurchase.Company,
            userAnswers = request.userAnswers,
            continueRoute = Ok(view(preparedForm, mode, purchaserName.fullName)))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(NameOfPurchaserPage) match {
        case None =>
          Future.successful(Redirect(controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode)))

        case Some(purchaserName) =>
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, purchaserName.fullName))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(PurchaserConfirmIdentityPage, value))
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(purchaserService.confirmIdentityNextPage(value, mode))
          )
      }
  }
}
