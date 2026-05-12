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

package controllers.taxCalculation.leaseholdSelfAssessed

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.taxCalculation.leaseholdSelfAssessed.LeaseholdSelfAssessedPremiumPayableTaxFormProvider
import navigation.Navigator
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.taxCalculation.leaseholdSelfAssessed.LeaseholdSelfAssessedPremiumPayableTaxView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import models.Mode
import models.taxCalculation.TaxCalculationFlow.LeaseholdSelfAssessed
import pages.taxCalculation.leaseholdSelfAssessed.LeaseholdSelfAssessedPremiumPayableTaxPage
import services.taxCalculation.SdltCalculationService


@Singleton
class LeaseholdSelfAssessedPremiumPayableTaxController @Inject()(
                                                                  val controllerComponents: MessagesControllerComponents,
                                                                  navigator: Navigator,
                                                                  view: LeaseholdSelfAssessedPremiumPayableTaxView,
                                                                  formProvider: LeaseholdSelfAssessedPremiumPayableTaxFormProvider,
                                                                  sdltCalculationService: SdltCalculationService,
                                                                  sessionRepository: SessionRepository,
                                                                  getData: DataRetrievalAction,
                                                                  requireData: DataRequiredAction,
                                                                  identify: IdentifierAction
                                        )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {
  val form: Form[String] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    sdltCalculationService.whenInFlow(LeaseholdSelfAssessed) {
      val premiumPayable = request.userAnswers.fullReturn.flatMap(_.lease.flatMap(_.totalPremiumPayable))

      premiumPayable match {
        case Some(premiumPayable) =>
          val preparedForm = request.userAnswers.get(LeaseholdSelfAssessedPremiumPayableTaxPage) match {
            case None => form
            case Some(value) => form.fill(value)
          }
          Ok(view(preparedForm, premiumPayable, mode))

        case None =>
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    sdltCalculationService.whenInFlowAsync(LeaseholdSelfAssessed) {
      val premiumPayable = request.userAnswers.fullReturn.flatMap(_.lease.flatMap(_.totalPremiumPayable))

      premiumPayable match {
        case Some(premiumPayable) =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, premiumPayable, mode))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(
                    request.userAnswers.set(LeaseholdSelfAssessedPremiumPayableTaxPage, value)
                  )
                  _ <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(LeaseholdSelfAssessedPremiumPayableTaxPage, mode, updatedAnswers))
            )
        case None =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }
  }
}
