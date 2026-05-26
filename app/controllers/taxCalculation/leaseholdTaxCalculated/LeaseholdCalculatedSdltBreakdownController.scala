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

package controllers.taxCalculation.leaseholdTaxCalculated

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.taxCalculation.TaxCalculationErrorRecovery
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.taxCalculation.SdltCalculationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.taxCalculation.CalculationResultViewModel
import views.html.taxCalculation.CalculatedSdltBreakdownView

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import models.taxCalculation.CalculationOutcome.Calculated

@Singleton
class LeaseholdCalculatedSdltBreakdownController @Inject()(
                                                  val controllerComponents: MessagesControllerComponents,
                                                  view: CalculatedSdltBreakdownView,
                                                  sdltCalculationService: SdltCalculationService,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  identify: IdentifierAction
                                              )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging with TaxCalculationErrorRecovery {

  private val breakdownUrl: String = controllers.taxCalculation.leaseholdTaxCalculated.routes.LeaseholdCalculatedSdltBreakdownController.onPageLoad().url
  private val titleKey: String = "taxCalculation.calculation.leasehold.title"

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
      sdltCalculationService.calculateStampDutyLandTax(request.userAnswers)
        .map {
          case Right(Calculated(result)) =>
            CalculationResultViewModel.toViewModel(result, request.userAnswers) match {
              case Right(vm) => Ok(view(vm, breakdownUrl, titleKey))
              case Left(err) =>
                logger.warn(s"[LeaseholdCalculatedSdltBreakdownController] Failed to construct view model: ${err.message}")
                Redirect(errorHandler(err))
            }
          case Right(response) =>
            logger.warn(s"[LeaseholdCalculatedSdltBreakdownController] Failed to get a tax calculation result: $response")
            Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
          case Left(err) =>
            logger.warn(s"[LeaseholdCalculatedSdltBreakdownController] sdltc reported missing data: ${err.message}")
            Redirect(errorHandler(err))
        }
  }
}
