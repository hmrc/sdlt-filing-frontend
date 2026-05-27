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

package controllers.taxCalculation.freeholdSelfAssessed

import controllers.actions.*
import controllers.taxCalculation.TaxCalculationErrorRecovery
import forms.taxCalculation.SdltCalculatedTotalAmountDueFormProvider
import models.taxCalculation.TaxCalculationFlow.FreeholdSelfAssessed
import models.taxCalculation.{BuildRequestError, MissingSelfAssessedAmountDueError, TotalAmountDueSummaryRowValues}
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.taxCalculation.freeholdSelfAssessed.{FreeholdSelfAssessedAmountPage, FreeholdSelfAssessedPenaltiesAndInterestPage, FreeholdSelfAssessedTotalAmountDuePage}
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import services.taxCalculation.SdltCalculationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{TaxCalculationPenaltiesHelper, TimeMachine}
import viewmodels.taxCalculation.selfAssessedViewModels.TotalAmountDueViewModel
import views.html.taxCalculation.selfAssessed.TotalAmountDueView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FreeholdSelfAssessedTotalAmountDueController @Inject()(
                                                              override val messagesApi: MessagesApi,
                                                              identify: IdentifierAction,
                                                              getData: DataRetrievalAction,
                                                              requireData: DataRequiredAction,
                                                              sdltCalculationService: SdltCalculationService,
                                                              sessionRepository: SessionRepository,
                                                              navigator: Navigator,
                                                              formProvider: SdltCalculatedTotalAmountDueFormProvider,
                                                              timeMachine: TimeMachine,
                                                              val controllerComponents: MessagesControllerComponents,
                                                              view: TotalAmountDueView,
                                                            )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging with TaxCalculationErrorRecovery {

  private val form: Form[String] = formProvider()

  private val postAction: Mode => Call = mode =>
    controllers.taxCalculation.freeholdSelfAssessed.routes.FreeholdSelfAssessedTotalAmountDueController.onSubmit(mode)

  private val sectionKey: String = "site.taxCalculation.freeholdSelfAssessed.section"

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      sdltCalculationService.whenInFlowAsync(FreeholdSelfAssessed) {
        Future.successful(constructViewModel(request.userAnswers)).map {
          case Right(viewModelRows) =>
            val viewModel = TotalAmountDueViewModel.toViewModel(viewModelRows)
            val prepared = request.userAnswers.get(FreeholdSelfAssessedTotalAmountDuePage).fold(form)(form.fill)
            Ok(view(prepared, viewModel, postAction(mode), sectionKey))

          case Left(err) =>
            logger.warn(s"[FreeholdSelfAssessedTotalAmountDueController][onPageLoad] failed: ${err.message}")
            Redirect(errorHandler(err))

        }
      }

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      sdltCalculationService.whenInFlowAsync(FreeholdSelfAssessed) {
        constructViewModel(request.userAnswers) match {
          case Right(viewModelRows) =>
            form.bindFromRequest().fold(
              formWithErrors =>
                val viewModel = TotalAmountDueViewModel.toViewModel(viewModelRows)
                Future.successful(BadRequest(view(formWithErrors, viewModel, postAction(mode), sectionKey))),
              amount =>
                val isPenaltyZero = TaxCalculationPenaltiesHelper.isPenaltyZero(viewModelRows.penalty)
                for {
                  updatedWithTotalAmount <- Future.fromTry(request.userAnswers.set(FreeholdSelfAssessedTotalAmountDuePage, amount))
                  updatedWithPenaltiesAndInterest <- 
                    if(isPenaltyZero) Future.fromTry(updatedWithTotalAmount.set(FreeholdSelfAssessedPenaltiesAndInterestPage, false))
                    else Future.successful(updatedWithTotalAmount)
                  _ <- sessionRepository.set(updatedWithPenaltiesAndInterest)
                } yield {
                  if (isPenaltyZero) Redirect(controllers.routes.IndexController.onPageLoad()) // TODO UPDATE TO REDIRECT TO CHECK YOUR ANSWERS CONTROLLER
                  else Redirect(navigator.nextPage(FreeholdSelfAssessedTotalAmountDuePage, mode, updatedWithPenaltiesAndInterest))
                }

            )
          case Left(err) =>
            logger.warn(s"[FreeholdSelfAssessedTotalAmountDueController][onPageSubmit] failed: ${err.message}")
            Future.successful(Redirect(errorHandler(err)))
        }

      }
  }


  private def constructViewModel(answers: UserAnswers): Either[BuildRequestError, TotalAmountDueSummaryRowValues] =

    for {
      sdltDue <- answers.get(FreeholdSelfAssessedAmountPage)
        .toRight(MissingSelfAssessedAmountDueError("Missing FreeholdSelfAssessedAmountPage"))
        .map(BigDecimal(_))

      viewSummaryRow <- TotalAmountDueViewModel.getTotalAmountDueSummaryRow(sdltDue, answers, timeMachine)
    } yield viewSummaryRow


}

