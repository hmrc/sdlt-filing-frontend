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

import connectors.StampDutyLandTaxConnector
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.ReturnVersionUpdateRequest
import models.UserAnswers
import models.requests.DataRequest
import models.taxCalculation.CalculationOutcome.Calculated
import models.taxCalculation.TaxCalculationFlow.{FreeholdSelfAssessed, FreeholdTaxCalculated, LeaseholdSelfAssessed, LeaseholdTaxCalculated}
import models.taxCalculation.{TaxCalculationResult, UpdateTaxCalculationRequest}
import pages.taxCalculation.TaxCalculationFlowPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.checkAnswers.CheckAnswersService
import services.taxCalculation.SdltCalculationService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DateTimeFormats.parseDate
import utils.{TaxCalculationPenaltiesHelper, TimeMachine}
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
                                                         backendConnector: StampDutyLandTaxConnector,
                                                         timeMachine: TimeMachine,
                                                         val controllerComponents: MessagesControllerComponents
                                                       )(implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Logging with TaxCalculationErrorRecovery {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(TaxCalculationFlowPage) match {
        case Some(FreeholdTaxCalculated) | Some(LeaseholdTaxCalculated) =>
          withCalculatedResult(result => Future.successful(renderOrRedirect(buildRowResults(result))))
        case Some(FreeholdSelfAssessed) | Some(LeaseholdSelfAssessed) =>
          Future.successful(
            renderOrRedirect(buildRowResults(TaxCalculationResult(0, None, None, None, Nil)))
          )
        case None =>
          logger.warn("[TaxCalculationCheckYourAnswersController][onPageLoad] no tax calculation flow set in session, nothing to show; returning to the task list")
          Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad()))
      }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(TaxCalculationFlowPage) match {
        case Some(FreeholdTaxCalculated) | Some(LeaseholdTaxCalculated) =>
          withCalculatedResult(result => submitTaxCalculation(Some(result)))
        case Some(FreeholdSelfAssessed) | Some(LeaseholdSelfAssessed) =>
          submitTaxCalculation(None)
        case None =>
          logger.warn("[TaxCalculationCheckYourAnswersController][onSubmit] no tax calculation flow set in session, cannot submit; returning to the task list")
          Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad()))
      }
  }

  private def submitTaxCalculation(result: Option[TaxCalculationResult])
                                  (implicit hc: HeaderCarrier, request: DataRequest[?]): Future[Result] =
    penaltyFor(request.userAnswers) match {
      case None =>
        logger.warn("[TaxCalculationCheckYourAnswersController][submitTaxCalculation] return has no effective date so penalties cannot be determined; returning to the task list")
        Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad()))
      case Some(penalty) =>
        (for {
          versionRequest <- ReturnVersionUpdateRequest.from(request.userAnswers)
          versionReturn  <- backendConnector.updateReturnVersion(versionRequest)
          outcome        <- versionReturn.newVersion match {
                              case Some(_) => submitUpdate(result, penalty)
                              case None =>
                                logger.warn("[TaxCalculationCheckYourAnswersController][submitTaxCalculation] return version was not incremented so the tax calculation was not submitted; returning to the task list")
                                Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad()))
                            }
        } yield outcome).recover {
          case e =>
            logger.error(s"[TaxCalculationCheckYourAnswersController][submitTaxCalculation] tax calculation submit failed for ${returnContext(request.userAnswers)}: ${e.getMessage}", e)
            Redirect(routes.TaxCalculationCheckYourAnswersController.onPageLoad())
        }
    }

  private def submitUpdate(result: Option[TaxCalculationResult], penalty: BigDecimal)
                          (implicit hc: HeaderCarrier, request: DataRequest[?]): Future[Result] =
    for {
      taxCalcRequest <- UpdateTaxCalculationRequest.from(request.userAnswers, result, penalty)
      _               = logger.info(s"[TaxCalculationCheckYourAnswersController][submitUpdate] submitting tax calculation for ${returnContext(request.userAnswers)}: $taxCalcRequest")
      taxCalcReturn  <- backendConnector.updateTaxCalculationInfo(taxCalcRequest)
    } yield {
      if (taxCalcReturn.updated) {
        Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
      } else {
        logger.warn("[TaxCalculationCheckYourAnswersController][submitTaxCalculation] backend did not confirm the tax calculation update; returning to check your answers")
        Redirect(routes.TaxCalculationCheckYourAnswersController.onPageLoad())
      }
    }

  private def returnContext(userAnswers: UserAnswers): String =
    s"storn ${userAnswers.storn}, return ${userAnswers.fullReturn.map(_.returnResourceRef).getOrElse("unknown")}"

  private def penaltyFor(userAnswers: UserAnswers): Option[BigDecimal] =
    for {
      fullReturn    <- userAnswers.fullReturn
      transaction   <- fullReturn.transaction
      effectiveDate <- transaction.effectiveDate
      parsedDate    <- parseDate(effectiveDate).toOption
    } yield TaxCalculationPenaltiesHelper.getPenalties(parsedDate, timeMachine)

  private def renderOrRedirect(rows: Seq[SummaryRowResult]): Result =
    checkAnswersService.redirectOrRender(rows) match {
      case Left(call) => Redirect(call)
      case Right(_)   => Ok // TODO: render the shared Check Your Answers view once built
    }

  private def withCalculatedResult(onCalculated: TaxCalculationResult => Future[Result])
                                  (implicit hc: HeaderCarrier, request: DataRequest[?]): Future[Result] =
    sdltCalculationService.calculateStampDutyLandTax(request.userAnswers).flatMap {
      case Right(Calculated(result)) => onCalculated(result)
      case Right(response) =>
        logger.warn(s"[TaxCalculationCheckYourAnswersController][withCalculatedResult] expected a calculated tax result but sdltc returned $response; returning to the task list")
        Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad()))
      case Left(err) =>
        logger.warn(s"[TaxCalculationCheckYourAnswersController][withCalculatedResult] sdltc could not calculate the tax due, data is incomplete: ${err.message}")
        Future.successful(Redirect(errorHandler(err)))
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
