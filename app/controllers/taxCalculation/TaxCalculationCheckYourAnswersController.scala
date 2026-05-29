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
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents, Result}
import services.checkAnswers.CheckAnswersService
import services.taxCalculation.SdltCalculationService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.summary.SummaryRowResult
import viewmodels.checkAnswers.taxCalculation.*

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TaxCalculationCheckYourAnswersController @Inject()(
                                                         override val messagesApi: MessagesApi,
                                                         identify: IdentifierAction,
                                                         getData: DataRetrievalAction,
                                                         requireData: DataRequiredAction,
                                                         sdltCalculationService: SdltCalculationService,
                                                         checkAnswersService: CheckAnswersService,
                                                         val controllerComponents: MessagesControllerComponents,
                                                       )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val taskList: Call = controllers.routes.ReturnTaskListController.onPageLoad()

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(TaxCalculationFlowPage) match {
        case Some(FreeholdTaxCalculated) | Some(LeaseholdTaxCalculated) =>
          withCalculatedResult { result =>
            render(buildRowResults(result))
          }
        case Some(FreeholdSelfAssessed) | Some(LeaseholdSelfAssessed) =>
          Future.successful(
            render(buildRowResults(TaxCalculationResult(0, None, None, None, Nil)))
          )
        case _ =>
          Future.successful(Redirect(taskList))
      }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    // TODO: Must submit to BE
    _ => Redirect(taskList)
  }

  private def render(rows: Seq[SummaryRowResult]): Result =
    checkAnswersService.redirectOrRender(rows) match {
      case Left(call)         => Redirect(call)
      case Right(summaryList) =>
        // TODO: build the shared CYA view
        Ok
    }

  private def withCalculatedResult(onCalculated: TaxCalculationResult => Result)
                                  (implicit hc: HeaderCarrier, request: DataRequest[?]): Future[Result] =
    sdltCalculationService.calculateStampDutyLandTax(request.userAnswers).map {
      case Right(Calculated(result)) => onCalculated(result)
      case _                         => Redirect(taskList)
    }

  private def buildRowResults(result: TaxCalculationResult)(implicit request: DataRequest[?]): Seq[SummaryRowResult] = {

    val ua   = request.userAnswers
    val flow = ua.get(TaxCalculationFlowPage)

    Seq(
      // TODO: build the remaining Check Your Answers rows for each flow, one example kept below
      Option.when(flow.contains(FreeholdTaxCalculated))(CalculatedSdltDueSummary.row(result.totalTax.toString))
    ).flatten
  }
}
