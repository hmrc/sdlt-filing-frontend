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

import config.CurrencyFormatter.IntToCurrency
import controllers.actions.*
import controllers.taxCalculation.TaxCalculationErrorRecovery
import play.api.Logging
import models.taxCalculation.TaxCalculationFlow.FreeholdTaxCalculated
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.taxCalculation.SdltCalculationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.taxCalculation.freeholdTaxCalculated.FreeholdCalculatedSdltDueView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class FreeholdCalculatedSdltDueController @Inject()(
                                                    override val messagesApi: MessagesApi,
                                                    identify: IdentifierAction,
                                                    getData: DataRetrievalAction,
                                                    requireData: DataRequiredAction,
                                                    sdltCalculationService: SdltCalculationService,
                                                    val controllerComponents: MessagesControllerComponents,
                                                    view: FreeholdCalculatedSdltDueView
                                                  )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging with TaxCalculationErrorRecovery {

  val sectionKey = "site.taxCalculation.freeholdSdltCalculated.section"

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      sdltCalculationService.whenInFlowAsync(FreeholdTaxCalculated) {
        sdltCalculationService
          .calculateStampDutyLandTax(request.userAnswers)
          .map {
            case Right(result) =>
              val formattedSdltDue = result.totalTax.toCurrency
              Ok(view(formattedSdltDue, sectionKey))
            case Left(err) =>
              logger.warn(s"[FreeholdCalculatedSdltDueController] sdltc reported missing data: ${err.message}")
              Redirect(errorHandler(err))
          }
      }
  }
  
}
