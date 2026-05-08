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

package controllers.taxCalculation.freeholdTaxCalculated

import controllers.actions.*
import controllers.taxCalculation.TaxCalculationErrorRecovery
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.taxCalculation.SdltCalculationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.taxCalculation.CalculationResultViewModel
import views.html.taxCalculation.freeholdTaxCalculated.FreeholdCalculatedSdltBreakdownView

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class FreeholdCalculatedSdltBreakdownController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       sdltCalculationService: SdltCalculationService,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: FreeholdCalculatedSdltBreakdownView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging with TaxCalculationErrorRecovery {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      sdltCalculationService
        .calculateStampDutyLandTax(request.userAnswers)
        .map {
          case Right(result) =>
            CalculationResultViewModel.toViewModel(result, request.userAnswers) match {
              case Right(vm) => Ok(view(vm))
              case Left(err) =>
                logger.warn(s"[FreeholdCalculatedSdltBreakdownController] Failed to construct view model: ${err.message}")
                Redirect(errorHandler(err))
            }
          case Left(err) =>
            logger.warn(s"[FreeholdCalculatedSdltBreakdownController] sdltc reported missing data: ${err.message}")
            Redirect(errorHandler(err))
        }
  }
}
