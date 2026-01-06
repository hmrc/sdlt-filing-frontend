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
import forms.purchaser.PurchaserCorporationTaxUTRFormProvider
import models.purchaser.{PurchaserConfirmIdentity, WhoIsMakingThePurchase}
import models.{Mode, NormalMode}
import navigation.Navigator
import pages.purchaser.{NameOfPurchaserPage, PurchaserConfirmIdentityPage, PurchaserCorporationTaxUTRPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.purchaser.PurchaserService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.purchaser.PurchaserCorporationTaxUTRView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PurchaserCorporationTaxUTRController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        sessionRepository: SessionRepository,
                                        navigator: Navigator,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        purchaserService: PurchaserService,
                                        formProvider: PurchaserCorporationTaxUTRFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: PurchaserCorporationTaxUTRView
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val userAnswers = request.userAnswers
      val nameOfPurchaser: Option[String] = userAnswers.get(NameOfPurchaserPage).map(_.fullName)
      val purchaserConfirmIdentity: Option[PurchaserConfirmIdentity] = userAnswers.get(PurchaserConfirmIdentityPage)

      purchaserService.continueIfAddingMainPurchaserWithPurchaserTypeCheck(
        WhoIsMakingThePurchase.Company,
        userAnswers,
        continueRoute = (nameOfPurchaser, purchaserConfirmIdentity) match {
          case (Some(purchaserName), Some(identity)) if identity == PurchaserConfirmIdentity.CorporationTaxUTR =>
            val preparedForm = request.userAnswers.get(PurchaserCorporationTaxUTRPage) match {
              case None => form
              case Some(formValue) => form.fill(formValue)
            }
            Ok(view(preparedForm, purchaserName, mode))

          case (Some(purchaserName), _) => Redirect(controllers.purchaser.routes.PurchaserConfirmIdentityController.onPageLoad(NormalMode))
          case (None, _) => Redirect(controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode))
        }
      )
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val userAnswers = request.userAnswers
      val nameOfPurchaser: Option[String] = userAnswers.get(NameOfPurchaserPage).map(_.fullName)

      nameOfPurchaser match {
        case None => Future.successful(Redirect(controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode)))

        case Some(purchaserName) =>
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, purchaserName, mode))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(PurchaserCorporationTaxUTRPage, value))
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(PurchaserCorporationTaxUTRPage, mode, updatedAnswers))
          )
      }
  }
}
