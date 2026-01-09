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
import forms.purchaser.PurchaserPartnershipUtrFormProvider
import models.purchaser.{PurchaserConfirmIdentity, WhoIsMakingThePurchase}
import models.{Mode, NormalMode}
import navigation.Navigator
import pages.purchaser.{NameOfPurchaserPage, PurchaserConfirmIdentityPage, PurchaserUTRPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.purchaser.PurchaserService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.purchaser.PurchaserPartnershipUtrView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PurchaserPartnershipUtrController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        sessionRepository: SessionRepository,
                                        navigator: Navigator,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: PurchaserPartnershipUtrFormProvider,
                                        purchaserService: PurchaserService,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: PurchaserPartnershipUtrView
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[String] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val purchaserNameOpt: Option[String] = request.userAnswers.get(NameOfPurchaserPage).map(_.fullName)
      val purchaserInformationOpt = request.userAnswers.get(PurchaserConfirmIdentityPage)

      purchaserService.continueIfAddingMainPurchaserWithPurchaserTypeCheck(
        purchaserType = WhoIsMakingThePurchase.Company,
        userAnswers = request.userAnswers,
        continueRoute = {
          (purchaserNameOpt, purchaserInformationOpt) match {
            case (Some(purchaserName), Some(purchaserInformationOpt)) if purchaserInformationOpt.equals(PurchaserConfirmIdentity.PartnershipUTR) =>
              val preparedForm = request.userAnswers.get(PurchaserUTRPage) match {
                case None => form
                case Some(value) => form.fill(value)
              }
              Ok(view(preparedForm, mode, purchaserName))

            case (None, _) => Redirect(controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode))

            case _ =>
              Redirect(controllers.purchaser.routes.PurchaserConfirmIdentityController.onPageLoad(NormalMode))
          }
        }
      )
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val purchaserName: Option[String] = request.userAnswers.get(NameOfPurchaserPage).map(_.fullName)

      purchaserName match {
        case Some(purchaserName) =>
          form.bindFromRequest().fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, mode, purchaserName))),

          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(PurchaserUTRPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(PurchaserUTRPage, mode, updatedAnswers))
          )

        case _ => Future.successful(Redirect(controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(mode)))
      }
  }
}
