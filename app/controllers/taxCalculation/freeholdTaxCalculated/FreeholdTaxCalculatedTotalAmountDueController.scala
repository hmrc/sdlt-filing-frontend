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

package controllers.taxCalculation.freeholdTaxCalculated

import controllers.actions.*
import controllers.taxCalculation.TaxCalculationErrorRecovery
import forms.taxCalculation.TotalAmountDueFormProvider
import models.Mode
import models.requests.DataRequest
import models.taxCalculation.CalculationOutcome.Calculated
import models.taxCalculation.TaxCalculationFlow.FreeholdTaxCalculated
import models.taxCalculation.{BuildRequestError, TaxCalculationResult, TotalAmountDueSummaryRowValues}
import navigation.Navigator
import pages.taxCalculation.freeholdTaxCalculated.{FreeholdTaxCalculatedPenaltiesAndInterestPage, FreeholdTaxCalculatedTotalAmountDuePage, FreeholdTaxCalculatedSelfAssessedAmountPage}
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.*
import repositories.SessionRepository
import services.taxCalculation.SdltCalculationService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{TaxCalculationPenaltiesHelper, TimeMachine}
import viewmodels.taxCalculation.TotalAmountDueViewModel
import views.html.taxCalculation.shared.TotalAmountDueView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FreeholdTaxCalculatedTotalAmountDueController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       statusCheck: CheckSubmissionStatusAction,
                                       sdltCalculationService: SdltCalculationService,
                                       sessionRepository: SessionRepository,
                                       navigator: Navigator,
                                       formProvider: TotalAmountDueFormProvider,
                                       timeMachine: TimeMachine,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: TotalAmountDueView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging with TaxCalculationErrorRecovery {

  private val form: Form[String] = formProvider()

  private val postAction: Mode => Call = mode =>
    controllers.taxCalculation.freeholdTaxCalculated.routes.FreeholdTaxCalculatedTotalAmountDueController.onSubmit(mode)

  private val sectionKey: String = "site.taxCalculation.freeholdSdltCalculated.section"

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen statusCheck).async {
    implicit request =>
      sdltCalculationService.whenInFlowAsync(FreeholdTaxCalculated) {
        withCalculatedResult { result =>
          constructViewRowModel(result) match {
            case Right(viewModelSummaryRowValues) =>
              val viewModel = TotalAmountDueViewModel.toViewModel(viewModelSummaryRowValues)
              val prepared = request.userAnswers.get(FreeholdTaxCalculatedTotalAmountDuePage).fold(form)(form.fill)
              Future.successful(Ok(view(prepared, viewModel, postAction(mode), sectionKey)))
            case Left(err) =>
              logger.warn(s"[FreeholdTaxCalculatedTotalAmountDueController][onPageLoad] failed: ${err.message}")
              Future.successful(Redirect(errorHandler(err)))
          }
        }
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen statusCheck).async {
    implicit request =>
      sdltCalculationService.whenInFlowAsync(FreeholdTaxCalculated) {
        withCalculatedResult { result =>
          constructViewRowModel(result) match {
            case Right(viewModelSummaryRowValues) =>
              form.bindFromRequest().fold(
                formWithErrors =>
                  val viewModel = TotalAmountDueViewModel.toViewModel(viewModelSummaryRowValues)
                  Future.successful(BadRequest(view(formWithErrors, viewModel, postAction(mode), sectionKey))),
                amount =>
                  val isPenaltyZero = TaxCalculationPenaltiesHelper.isPenaltyZero(viewModelSummaryRowValues.penalty)
                  for {
                    updatedWithTotalAmount <- Future.fromTry(request.userAnswers.set(FreeholdTaxCalculatedTotalAmountDuePage, amount))
                    updatedWithPenaltiesAndInterest <- if (isPenaltyZero) Future.fromTry(updatedWithTotalAmount.set(FreeholdTaxCalculatedPenaltiesAndInterestPage, false))
                    else Future.successful(updatedWithTotalAmount)
                    _ <- sessionRepository.set(updatedWithPenaltiesAndInterest)
                  } yield {
                     if (isPenaltyZero) Redirect(controllers.taxCalculation.routes.TaxCalculationCheckYourAnswersController.onPageLoad())
                     else Redirect(navigator.nextPage(FreeholdTaxCalculatedTotalAmountDuePage, mode, updatedWithPenaltiesAndInterest))
                  }
              )
            case Left(err) =>
              logger.warn(s"[FreeholdTaxCalculatedTotalAmountDueController][onSubmit] failed: ${err.message}")
              Future.successful(Redirect(errorHandler(err)))
          }

        }
      }
  }

  private def withCalculatedResult(onCalculated: TaxCalculationResult => Future[Result])
                                  (implicit hc: HeaderCarrier, request: DataRequest[?]): Future[Result] =
    sdltCalculationService.calculateStampDutyLandTax(request.userAnswers).flatMap {
      case Right(Calculated(result)) => onCalculated(result)
      case Right(response) =>
        logger.warn(s"[FreeholdTaxCalculatedTotalAmountDueController] Failed to get a tax calculation result: $response")
        Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad()))
      case Left(err) =>
        logger.warn(s"[FreeholdTaxCalculatedTotalAmountDueController] sdltc reported missing data: ${err.message}")
        Future.successful(Redirect(errorHandler(err)))
    }

  private def constructViewRowModel(result: TaxCalculationResult)
                            (implicit messages: Messages, request: DataRequest[?]): Either[BuildRequestError, TotalAmountDueSummaryRowValues] =
    TotalAmountDueViewModel.getTotalAmountDueSummaryRow(result, request.userAnswers, timeMachine, FreeholdTaxCalculatedSelfAssessedAmountPage)
}
