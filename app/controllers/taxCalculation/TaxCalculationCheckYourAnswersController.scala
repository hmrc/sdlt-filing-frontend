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

package controllers.taxCalculation

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.requests.DataRequest
import models.taxCalculation.CalculationOutcome.Calculated
import models.taxCalculation.TaxCalculationFlow.{FreeholdSelfAssessed, FreeholdTaxCalculated, LeaseholdSelfAssessed, LeaseholdTaxCalculated}
import models.taxCalculation.TaxCalculationResult
import pages.taxCalculation.TaxCalculationFlowPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.checkAnswers.CheckAnswersService
import services.taxCalculation.SdltCalculationService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.TimeMachine
import viewmodels.checkAnswers.summary.SummaryRowResult
import viewmodels.checkAnswers.taxCalculation.*

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaxCalculationCheckYourAnswersController @Inject()(
                                                         override val messagesApi: MessagesApi,
                                                         identify: IdentifierAction,
                                                         getData: DataRetrievalAction,
                                                         requireData: DataRequiredAction,
                                                         sdltCalculationService: SdltCalculationService,
                                                         checkAnswersService: CheckAnswersService,
                                                         timeMachine: TimeMachine,
                                                         val controllerComponents: MessagesControllerComponents
                                                       )(implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Logging with TaxCalculationErrorRecovery {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(TaxCalculationFlowPage) match {
        case Some(FreeholdTaxCalculated) | Some(LeaseholdTaxCalculated) =>
          withCalculatedResult(
            result => renderOrRedirect(buildRowResults(result))
          )
        case Some(FreeholdSelfAssessed) | Some(LeaseholdSelfAssessed) =>
          Future.successful(
            renderOrRedirect(buildRowResults(TaxCalculationResult(0, None, None, None, Nil)))
          )
        case None =>
          logger.warn("[TaxCalculationCheckYourAnswersController] no tax calculation flow set")
          Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad()))
      }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    _ => Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
    // TODO: submit the tax calculation to the backend
  }

  private def renderOrRedirect(rows: Seq[SummaryRowResult]): Result =
    checkAnswersService.redirectOrRender(rows) match {
      case Left(call) => Redirect(call)
      case Right(_)   => Ok // TODO: render the shared Check Your Answers view once built
    }

  private def withCalculatedResult(onCalculated: TaxCalculationResult => Result)
                                  (implicit hc: HeaderCarrier, request: DataRequest[?]): Future[Result] =
    sdltCalculationService.calculateStampDutyLandTax(request.userAnswers).map {
      case Right(Calculated(result)) => onCalculated(result)
      case Right(response) =>
        logger.warn(s"[TaxCalculationCheckYourAnswersController] no calculated result: $response")
        Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
      case Left(err) =>
        logger.warn(s"[TaxCalculationCheckYourAnswersController] sdltc reported missing data: ${err.message}")
        Redirect(errorHandler(err))
    }

  private def buildRowResults(result: TaxCalculationResult)(implicit request: DataRequest[?]): Seq[SummaryRowResult] = {

    val ua   = request.userAnswers
    val flow = ua.get(TaxCalculationFlowPage)

    flow match {
      case Some(FreeholdTaxCalculated) => Seq(
        CalculatedSdltDueSummary.row(result.totalTax.toString),
        FreeholdTaxCalculatedSelfAssessedAmountSummary.row(Some(ua)),
        PenaltiesDueSummary.row(Some(ua), timeMachine),
        FreeholdTaxCalculatedTotalAmountDueSummary.row(Some(ua)),
        FreeholdTaxCalculatedDoesAmountIncludePenaltiesSummary.row(Some(ua))
      )
      // TODO: add the FreeholdSelfAssessed, LeaseholdTaxCalculated and LeaseholdSelfAssessed row sets (mirror the FreeholdTaxCalculated arm above)
      case _ => Nil
    }
  }
}
