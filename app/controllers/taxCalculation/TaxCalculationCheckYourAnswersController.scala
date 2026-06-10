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
import models.taxCalculation.TaxCalculationFlow
import models.taxCalculation.TaxCalculationFlow.{FreeholdSelfAssessed, FreeholdTaxCalculated, LeaseholdSelfAssessed, LeaseholdTaxCalculated}
import models.taxCalculation.{TaxCalculationResult, UpdateTaxCalculationRequest}
import pages.taxCalculation.TaxCalculationFlowPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsObject
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.checkAnswers.CheckAnswersService
import services.taxCalculation.{PopulateTaxCalculationService, SdltCalculationService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DateTimeFormats.parseDate
import utils.{TaxCalculationHelper, TaxCalculationPenaltiesHelper, TimeMachine}
import viewmodels.checkAnswers.summary.SummaryRowResult
import viewmodels.checkAnswers.taxCalculation.*
import views.html.taxCalculation.shared.CheckYourAnswersView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class TaxCalculationCheckYourAnswersController @Inject()(
                                                         override val messagesApi: MessagesApi,
                                                         identify: IdentifierAction,
                                                         getData: DataRetrievalAction,
                                                         requireData: DataRequiredAction,
                                                         sdltCalculationService: SdltCalculationService,
                                                         checkAnswersService: CheckAnswersService,
                                                         backendConnector: StampDutyLandTaxConnector,
                                                         populateTaxCalculationService: PopulateTaxCalculationService,
                                                         sessionRepository: SessionRepository,
                                                         timeMachine: TimeMachine,
                                                         view: CheckYourAnswersView,
                                                         val controllerComponents: MessagesControllerComponents
                                                       )(implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Logging with TaxCalculationErrorRecovery {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      sessionRepository.get(request.userAnswers.id).flatMap {
        case None =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

        case Some(userAnswers) =>
          if (userAnswers.returnId.isEmpty) {
            Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad()))
          } else {
            logger.info(s"[TaxCalculationCheckYourAnswersController][onPageLoad] returnId=${userAnswers.returnId.getOrElse("unknown")}: rendering CYA, flow in session=${userAnswers.get(TaxCalculationFlowPage)}")
            userAnswers.get(TaxCalculationFlowPage) match {
              case Some(flow) => populateOrRender(userAnswers, flow)
              case None       => deriveFlow(userAnswers)
            }
          }
      }
  }

  private def deriveFlow(userAnswers: UserAnswers)(implicit hc: HeaderCarrier, request: DataRequest[?]): Future[Result] =
    sdltCalculationService.calculateStampDutyLandTax(userAnswers).flatMap {
      case Right(calculationResult) =>
        TaxCalculationHelper.flowFor(userAnswers, calculationResult) match {
          case Some(flow) =>
            logger.info(s"[TaxCalculationCheckYourAnswersController][deriveFlow] returnId=${userAnswers.returnId.getOrElse("unknown")}: derived flow=$flow from calculation outcome=$calculationResult")
            for {
              updated <- Future.fromTry(userAnswers.set(TaxCalculationFlowPage, flow))
              _       <- sessionRepository.set(updated)
              result  <- populateOrRender(updated, flow)
            } yield result
          case None =>
            logger.warn("[TaxCalculationCheckYourAnswersController][onPageLoad] could not determine the tax calculation flow, returning to the task list")
            Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad()))
        }
      case Left(err) =>
        logger.warn(s"[TaxCalculationCheckYourAnswersController][onPageLoad] sdltc could not calculate the tax due: ${err.message}")
        Future.successful(Redirect(errorHandler(err)))
    }

  private def populateOrRender(userAnswers: UserAnswers, flow: TaxCalculationFlow)(implicit request: DataRequest[?]): Future[Result] = {
    val sectionEmpty =
      (userAnswers.data \ "taxCalculationCurrent").asOpt[JsObject].forall(_.values.isEmpty)

    userAnswers.fullReturn.flatMap(_.taxCalculation) match {
      case Some(taxCalculation) if sectionEmpty =>
        populateTaxCalculationService.populateTaxCalculationInSession(taxCalculation, flow, userAnswers) match {
          case Success(populated) => sessionRepository.set(populated).flatMap(_ => renderForFlow(populated, flow))
          case Failure(_) =>
            logger.warn("[TaxCalculationCheckYourAnswersController][populateOrRender] could not populate the tax calculation section from the full return, returning to the start of the journey")
            Future.successful(Redirect(routes.TaxCalculationBeforeYouStartController.onPageLoad()))
        }
      case _ =>
        renderForFlow(userAnswers, flow)
    }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      sessionRepository.get(request.userAnswers.id).flatMap {
        case None =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

        case Some(userAnswers) =>
          logger.info(s"[TaxCalculationCheckYourAnswersController][onSubmit] returnId=${userAnswers.returnId.getOrElse("unknown")}: submitting tax calculation, flow=${userAnswers.get(TaxCalculationFlowPage)}")
          userAnswers.get(TaxCalculationFlowPage) match {
            case Some(FreeholdTaxCalculated) | Some(LeaseholdTaxCalculated) =>
              withCalculatedResult(userAnswers, result =>
                updateTaxCalculation(userAnswers, Some(result))
              )
            case Some(FreeholdSelfAssessed) | Some(LeaseholdSelfAssessed) =>
              updateTaxCalculation(userAnswers, None)
            case None =>
              logger.warn("[TaxCalculationCheckYourAnswersController][onSubmit] no tax calculation flow set in session, cannot submit, returning to the task list")
              Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad()))
          }
      }
  }

  private def renderForFlow(userAnswers: UserAnswers, flow: TaxCalculationFlow)(implicit request: DataRequest[?]): Future[Result] =
    flow match {
      case FreeholdTaxCalculated | LeaseholdTaxCalculated =>
        withCalculatedResult(userAnswers, result =>
          Future.successful(
            renderOrRedirect(
              buildRowResults(result, userAnswers)
            )
          )
        )
      case FreeholdSelfAssessed | LeaseholdSelfAssessed =>
        Future.successful(
          renderOrRedirect(
            buildRowResults(TaxCalculationResult(0, None, None, None, Nil), userAnswers))
        )
    }

  private def updateTaxCalculation(userAnswers: UserAnswers, result: Option[TaxCalculationResult])
                                  (implicit hc: HeaderCarrier, request: DataRequest[?]): Future[Result] =
    penaltyFor(userAnswers) match {
      case None =>
        logger.warn("[TaxCalculationCheckYourAnswersController][updateTaxCalculation] return has no effective date so penalties cannot be determined, returning to the task list")
        Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad()))
      case Some(penalty) =>
        val returnRef = userAnswers.returnId.getOrElse("unknown")
        for {
          versionRequest <- ReturnVersionUpdateRequest.from(userAnswers)
          _               = logger.info(s"[TaxCalculationCheckYourAnswersController][updateTaxCalculation] returnId=$returnRef: bumping return version, currentVersion=${versionRequest.currentVersion}")
          versionReturn  <- backendConnector.updateReturnVersion(versionRequest)
          _               = logger.info(s"[TaxCalculationCheckYourAnswersController][updateTaxCalculation] returnId=$returnRef: return version response, newVersion=${versionReturn.newVersion}")
          taxCalcRequest <- UpdateTaxCalculationRequest.from(userAnswers, result, penalty) if versionReturn.newVersion.isDefined
          _               = logger.info(s"[TaxCalculationCheckYourAnswersController][updateTaxCalculation] returnId=$returnRef: submitting tax calculation to BE: $taxCalcRequest")
          taxCalcReturn  <- backendConnector.updateTaxCalculationInfo(taxCalcRequest) if versionReturn.newVersion.isDefined
          _               = logger.info(s"[TaxCalculationCheckYourAnswersController][updateTaxCalculation] returnId=$returnRef: tax calculation submit response, updated=${taxCalcReturn.updated}")
        } yield {
          if (taxCalcReturn.updated) {
            Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
          } else {
            Redirect(routes.TaxCalculationCheckYourAnswersController.onPageLoad())
          }
        }
    }

  private def penaltyFor(userAnswers: UserAnswers): Option[BigDecimal] =
    for {
      fullReturn    <- userAnswers.fullReturn
      transaction   <- fullReturn.transaction
      effectiveDate <- transaction.effectiveDate
      parsedDate    <- parseDate(effectiveDate).toOption
    } yield TaxCalculationPenaltiesHelper.getPenalties(parsedDate, timeMachine)

  private def renderOrRedirect(rows: Seq[SummaryRowResult])
                              (implicit request: DataRequest[?]): Result =
    checkAnswersService.redirectOrRender(rows) match {
      case Left(call) => Redirect(call)
      case Right(summaryList) => Ok(view(summaryList))
    }

  private def withCalculatedResult(userAnswers: UserAnswers, onCalculated: TaxCalculationResult => Future[Result])
                                  (implicit hc: HeaderCarrier): Future[Result] =
    sdltCalculationService.calculateStampDutyLandTax(userAnswers).flatMap {
      case Right(Calculated(result)) => onCalculated(result)
      case Right(response) =>
        logger.warn(s"[TaxCalculationCheckYourAnswersController][withCalculatedResult] expected a calculated tax result but sdltc returned $response, returning to the task list")
        Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad()))
      case Left(err) =>
        logger.warn(s"[TaxCalculationCheckYourAnswersController][withCalculatedResult] sdltc could not calculate the tax due, data is incomplete: ${err.message}")
        Future.successful(Redirect(errorHandler(err)))
    }

  private[taxCalculation] def buildRowResults(result: TaxCalculationResult, ua: UserAnswers)(implicit request: DataRequest[?]): Seq[SummaryRowResult] = {

    val flow = ua.get(TaxCalculationFlowPage)

    flow match {
      case Some(FreeholdTaxCalculated) => Seq(
        CalculatedSdltDueSummary.row(result.totalTax.toString),
        FreeholdTaxCalculatedSelfAssessedAmountSummary.row(Some(ua)),
        PenaltiesDueSummary.row(Some(ua), timeMachine),
        FreeholdTaxCalculatedTotalAmountDueSummary.row(Some(ua)),
        FreeholdTaxCalculatedDoesAmountIncludePenaltiesSummary.row(Some(ua))
      )
      case Some(LeaseholdSelfAssessed) => Seq(
        PremiumPayableTaxSummary.row(Some(ua)),
        TaxDueOnNpvSummary.row(ua),
        PenaltiesDueSummary.row(Some(ua), timeMachine),
        LeaseholdSelfAssessedTotalAmountDueSummary.row(Some(ua)),
        LeaseholdSelfAssessedDoesAmountIncludePenaltiesSummary.row(Some(ua))
      )
      case Some(FreeholdSelfAssessed) =>
        Seq(
          FreeholdSelfAssessedAmountSummary.row(Some(ua)),
          PenaltiesDueSummary.row(Some(ua), timeMachine),
          FreeholdSelfAssessedTotalAmountDueSummary.row(Some(ua)),
          FreeholdSelfAssessedDoesAmountIncludePenaltiesSummary.row(Some(ua))
        )
      case Some(LeaseholdTaxCalculated) =>
        Seq(
          LeaseholdTaxCalculatedPremiumPayableSummary.row(result),
          LeaseholdTaxCalculatedNpvSummary.row(result),
          Some(CalculatedSdltDueSummary.row(result.totalTax.toString)),
          Some(LeaseholdTaxCalculatedSelfAssessedAmountSummary.row(Some(ua))),
          Some(PenaltiesDueSummary.row(Some(ua), timeMachine)),
          Some(LeaseholdTaxCalculatedTotalAmountDueSummary.row(Some(ua))),
          Some(LeaseholdTaxCalculatedDoesAmountIncludePenaltiesSummary.row(Some(ua)))
        ).flatten
      case _ =>
        logger.error(s"[TaxCalculationCheckYourAnswersController][buildRowResults]: Failed to match tax calculation flow.")
        Nil
    }
  }
}
