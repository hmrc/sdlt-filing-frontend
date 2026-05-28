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
import models.UserAnswers
import models.requests.DataRequest
import models.taxCalculation.CalculationOutcome.Calculated
import models.taxCalculation.TaxCalculationFlow.{FreeholdSelfAssessed, FreeholdTaxCalculated, LeaseholdSelfAssessed, LeaseholdTaxCalculated}
import models.taxCalculation.TaxCalculationResult
import pages.taxCalculation.TaxCalculationFlowPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents, Request, Result}
import services.checkAnswers.CheckAnswersService
import services.taxCalculation.SdltCalculationService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.TimeMachine
import viewmodels.checkAnswers.summary.SummaryRowResult
import viewmodels.checkAnswers.taxCalculation.*
import views.html.taxCalculation.shared.TaxCalculationCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TaxCalculationCheckYourAnswersController @Inject()(
                                                         override val messagesApi: MessagesApi,
                                                         identify: IdentifierAction,
                                                         getData: DataRetrievalAction,
                                                         requireData: DataRequiredAction,
                                                         sdltCalculationService: SdltCalculationService,
                                                         checkAnswersService: CheckAnswersService,
                                                         timeMachine: TimeMachine,
                                                         val controllerComponents: MessagesControllerComponents,
                                                         view: TaxCalculationCheckYourAnswersView
                                                       )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val onSubmitCall: Call =
    controllers.taxCalculation.routes.TaxCalculationCheckYourAnswersController.onSubmit()

  private val taskList: Call = controllers.routes.ReturnTaskListController.onPageLoad()

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(TaxCalculationFlowPage) match {
        case Some(FreeholdTaxCalculated) | Some(LeaseholdTaxCalculated) =>
          withCalculatedResult { result =>
            render("site.taxCalculation.caption", buildRowResults(result, request.userAnswers))
          }
        case Some(FreeholdSelfAssessed) | Some(LeaseholdSelfAssessed) =>
          Future.successful(render(
            "site.taxCalculation.caption",
            buildRowResults(TaxCalculationResult(0, None, None, None, Seq.empty), request.userAnswers)
          ))
        case _ =>
          Future.successful(Redirect(taskList))
      }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    _ => Redirect("/")
  }

  private def render(sectionKey: String, rows: Seq[SummaryRowResult])(implicit request: DataRequest[?]): Result =
    checkAnswersService.redirectOrRender(rows) match {
      case Left(call)         => Redirect(call)
      case Right(summaryList) => Ok(view(summaryList, sectionKey, onSubmitCall))
    }

  private def withCalculatedResult(onCalculated: TaxCalculationResult => Result)
                                  (implicit hc: HeaderCarrier, request: DataRequest[?]): Future[Result] =
    sdltCalculationService.calculateStampDutyLandTax(request.userAnswers).map {
      case Right(Calculated(result)) => onCalculated(result)
      case _                         => Redirect(taskList)
    }

  private def buildRowResults(result: TaxCalculationResult, ua: UserAnswers)(implicit request: Request[_]): Seq[SummaryRowResult] = {

    val flow = ua.get(TaxCalculationFlowPage)
    
    Seq(
      Option.when(flow.contains(FreeholdSelfAssessed))(FreeholdSelfAssessedAmountSummary.row(Some(ua))),
      Option.when(flow.contains(FreeholdSelfAssessed))(PenaltiesDueSummary.row(Some(ua), timeMachine)),
      Option.when(flow.contains(FreeholdSelfAssessed))(FreeholdSelfAssessedTotalAmountDueSummary.row(Some(ua))),
      Option.when(flow.contains(FreeholdSelfAssessed))(FreeholdSelfAssessedDoesAmountIncludePenaltiesSummary.row(Some(ua))),
      Option.when(flow.contains(FreeholdTaxCalculated))(CalculatedSdltDueSummary.row(result.totalTax.toString)),
      Option.when(flow.contains(FreeholdTaxCalculated))(FreeholdTaxCalculatedSelfAssessedAmountSummary.row(Some(ua))),
      Option.when(flow.contains(FreeholdTaxCalculated))(PenaltiesDueSummary.row(Some(ua), timeMachine)),
      Option.when(flow.contains(FreeholdTaxCalculated))(FreeholdTaxCalculatedTotalAmountDueSummary.row(Some(ua))),
      Option.when(flow.contains(FreeholdTaxCalculated))(FreeholdTaxCalculatedDoesAmountIncludePenaltiesSummary.row(Some(ua))),
      Option.when(flow.contains(LeaseholdSelfAssessed))(PremiumPayableTaxSummary.row(Some(ua))),
      Option.when(flow.contains(LeaseholdSelfAssessed))(TaxDueOnNpvSummary.row(ua)),
      Option.when(flow.contains(LeaseholdSelfAssessed))(PenaltiesDueSummary.row(Some(ua), timeMachine)),
      Option.when(flow.contains(LeaseholdSelfAssessed))(LeaseholdSelfAssessedTotalAmountDueSummary.row(Some(ua))),
      Option.when(flow.contains(LeaseholdSelfAssessed))(LeaseholdSelfAssessedDoesAmountIncludePenaltiesSummary.row(Some(ua))),
      Option.when(flow.contains(LeaseholdTaxCalculated))(CalculatedSdltDueSummary.row(result.totalTax.toString)),
      Option.when(flow.contains(LeaseholdTaxCalculated))(LeaseholdTaxCalculatedSelfAssessedAmountSummary.row(Some(ua))),
      Option.when(flow.contains(LeaseholdTaxCalculated))(PenaltiesDueSummary.row(Some(ua), timeMachine)),
      Option.when(flow.contains(LeaseholdTaxCalculated))(LeaseholdTaxCalculatedTotalAmountDueSummary.row(Some(ua))),
      Option.when(flow.contains(LeaseholdTaxCalculated))(LeaseholdTaxCalculatedDoesAmountIncludePenaltiesSummary.row(Some(ua)))
    ).flatten
  }
}
