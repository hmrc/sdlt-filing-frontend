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
import forms.purchaser.CompanyFormOfIdFormProvider
import models.purchaser.{CompanyFormOfId, PurchaserConfirmIdentity, WhoIsMakingThePurchase}
import models.{Mode, NormalMode}
import navigation.Navigator
import pages.purchaser.{CompanyFormOfIdPage, NameOfPurchaserPage, PurchaserConfirmIdentityPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.purchaser.PurchaserService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.purchaser.CompanyFormOfIdView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CompanyFormOfIdController @Inject()(
                                      override val messagesApi: MessagesApi,
                                      sessionRepository: SessionRepository,
                                      navigator: Navigator,
                                      identify: IdentifierAction,
                                      getData: DataRetrievalAction,
                                      requireData: DataRequiredAction,
                                      formProvider: CompanyFormOfIdFormProvider,
                                      purchaserService: PurchaserService,
                                      val controllerComponents: MessagesControllerComponents,
                                      view: CompanyFormOfIdView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[CompanyFormOfId] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val maybePurchaserName: Option[String] = request.userAnswers.get(NameOfPurchaserPage).map(_.fullName)
      val maybePurchaserInfo = request.userAnswers.get(PurchaserConfirmIdentityPage)

      purchaserService.continueIfAddingMainPurchaserWithPurchaserTypeCheck(
        purchaserType = WhoIsMakingThePurchase.Company,
        userAnswers = request.userAnswers,
        continueRoute = {
          (maybePurchaserName, maybePurchaserInfo) match {
            case (Some(purchaserName), Some(purchaserInfo)) if purchaserInfo.equals(PurchaserConfirmIdentity.AnotherFormOfID) =>
              val preparedForm = request.userAnswers.get(CompanyFormOfIdPage) match {
                case None => form
                case Some(value) => form.fill(value)
              }
              Ok(view(preparedForm, mode, purchaserName))

            case (None, _) => Redirect(controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode))

            case _ => Redirect(controllers.purchaser.routes.PurchaserConfirmIdentityController.onPageLoad(NormalMode))
          }
        }
      )
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val maybePurchaserName: Option[String] = request.userAnswers.get(NameOfPurchaserPage).map(_.fullName)

      maybePurchaserName match {
        case Some(purchaserName) =>
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, purchaserName))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(CompanyFormOfIdPage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(CompanyFormOfIdPage, mode, updatedAnswers))
          )

        case _ => Future.successful(Redirect(controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(mode)))

      }
  }
}
