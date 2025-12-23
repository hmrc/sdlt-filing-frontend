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
import forms.purchaser.PurchaserNationalInsuranceFormProvider
import models.purchaser.DoesPurchaserHaveNI
import models.{Mode, NormalMode}
import navigation.Navigator
import pages.purchaser.{DoesPurchaserHaveNIPage, NameOfPurchaserPage, PurchaserNationalInsurancePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.purchaser.PurchaserNationalInsuranceView
import services.purchaser.PurchaserService
import models.purchaser.WhoIsMakingThePurchase

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PurchaserNationalInsuranceController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        sessionRepository: SessionRepository,
                                        navigator: Navigator,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: PurchaserNationalInsuranceFormProvider,
                                        purchaserService: PurchaserService,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: PurchaserNationalInsuranceView
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[String] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val userAnswers = request.userAnswers

      val purchaserName: Option[String] = userAnswers
        .get(NameOfPurchaserPage)
        .map(_.fullName)

      val doesPurchaserHaveNi = request.userAnswers.get(DoesPurchaserHaveNIPage)

      (purchaserName, doesPurchaserHaveNi) match {
        case (Some(purchaserName), Some(doesPurchaserHaveNi)) if doesPurchaserHaveNi.equals(DoesPurchaserHaveNI.Yes) =>
          val preparedForm = request.userAnswers.get(PurchaserNationalInsurancePage) match {
            case None => form
            case Some(value) => form.fill(value)
          }
          purchaserService.checkPurchaserTypeAndCompanyDetails(
            purchaserType = WhoIsMakingThePurchase.Individual,
            userAnswers = request.userAnswers,
            continueRoute = Ok(view(preparedForm, mode, purchaserName)))
          
        case (None, _) => Redirect(controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode))

        case (_, Some(doesPurchaserHaveNI)) if doesPurchaserHaveNI.equals(DoesPurchaserHaveNI.No) =>
          Redirect(controllers.purchaser.routes.PurchaserFormOfIdIndividualController.onPageLoad(NormalMode))

        case (_, _) => Redirect(controllers.purchaser.routes.DoesPurchaserHaveNIController.onPageLoad(mode))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val purchaserName: Option[String] = request.userAnswers
        .get(NameOfPurchaserPage)
        .map(_.fullName)

      purchaserName match {
        case Some(purchaserName) =>
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, purchaserName))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(PurchaserNationalInsurancePage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(PurchaserNationalInsurancePage, mode, updatedAnswers)))

        case _ =>
          Future.successful(Redirect(controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(mode)))
      }
  }
}
