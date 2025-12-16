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
import forms.purchaser.PurchaserFormOfIdIndividualFormProvider
import models.purchaser.DoesPurchaserHaveNI
import models.Mode
import navigation.Navigator
import pages.purchaser.{DoesPurchaserHaveNIPage, NameOfPurchaserPage, PurchaserFormOfIdIndividualPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.purchaser.PurchaserFormOfIdIndividualView
import models.purchaser.WhoIsMakingThePurchase
import services.purchaser.PurchaserService

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PurchaserFormOfIdIndividualController @Inject()(
                                                       override val messagesApi: MessagesApi,
                                                       sessionRepository: SessionRepository,
                                                       navigator: Navigator,
                                                       identify: IdentifierAction,
                                                       getData: DataRetrievalAction,
                                                       requireData: DataRequiredAction,
                                                       formProvider: PurchaserFormOfIdIndividualFormProvider,
                                                       purchaserService: PurchaserService,
                                                       val controllerComponents: MessagesControllerComponents,
                                                       view: PurchaserFormOfIdIndividualView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      
      val maybePurchaserName: Option[String] = request.userAnswers.get(NameOfPurchaserPage).map(_.fullName)
      val maybeDoesPurchaserHaveNI: Option[DoesPurchaserHaveNI] = request.userAnswers.get(DoesPurchaserHaveNIPage)

      (maybePurchaserName, maybeDoesPurchaserHaveNI) match {
        case (Some(purchaserName), Some(doesPurchaserHaveNI)) if doesPurchaserHaveNI.equals(DoesPurchaserHaveNI.No) =>
          val preparedForm = request.userAnswers.get(PurchaserFormOfIdIndividualPage) match {
            case None => form
            case Some(value) => form.fill(value)
          }
          purchaserService.checkPurchaserTypeAndCompanyDetails(
            purchaserType = WhoIsMakingThePurchase.Individual,
            userAnswers = request.userAnswers,
            continueRoute = Ok(view(preparedForm, mode, purchaserName)))
          
      
        case (None, _) => Redirect(controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(mode))
        case (_, Some(doesPurchaserHaveNI)) if doesPurchaserHaveNI.equals(DoesPurchaserHaveNI.Yes) =>
          Redirect(controllers.purchaser.routes.PurchaserBeforeYouStartController.onPageLoad()) // TODO change to enter purchaser NI page
        case (_, _) => Redirect(controllers.purchaser.routes.DoesPurchaserHaveNIController.onPageLoad(mode))
      }
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
                updatedAnswers <- Future.fromTry(request.userAnswers.set(PurchaserFormOfIdIndividualPage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(PurchaserFormOfIdIndividualPage, mode, updatedAnswers))
          )
        case _ => Future.successful(Redirect(controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(mode)))
      }
  }
}
