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
import models.{Mode, UserAnswers}
import models.taxCalculation.BuildRequestError
import models.taxCalculation.TaxCalculationFlow.FreeholdTaxCalculated
import navigation.Navigator
import pages.taxCalculation.freeholdTaxCalculated.{FreeholdTaxCalculatedSelfAssessedAmountPage, FreeholdTaxCalculatedTotalAmountDuePage}
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import services.taxCalculation.SdltCalculationService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.TimeMachine
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

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      sdltCalculationService.whenInFlowAsync(FreeholdTaxCalculated) {
        constructViewModel(request.userAnswers).map {
          case Right(viewModel) =>
            val prepared = request.userAnswers.get(FreeholdTaxCalculatedTotalAmountDuePage).fold(form)(form.fill)
            Ok(view(prepared, viewModel, postAction(mode), sectionKey))
          case Left(err) =>
            logger.warn(s"[FreeholdTaxCalculatedTotalAmountDueController][onPageLoad] failed: ${err.message}")
            Redirect(errorHandler(err))
        }
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      sdltCalculationService.whenInFlowAsync(FreeholdTaxCalculated) {
        form.bindFromRequest().fold(
          formWithErrors =>
            constructViewModel(request.userAnswers).map {
              case Right(viewModel) => BadRequest(view(formWithErrors, viewModel, postAction(mode), sectionKey))
              case Left(err) =>
                logger.warn(s"[FreeholdTaxCalculatedTotalAmountDueController][onSubmit] failed: ${err.message}")
                Redirect(errorHandler(err))
            },
          value =>
            for {
              updated <- Future.fromTry(request.userAnswers.set(FreeholdTaxCalculatedTotalAmountDuePage, value))
              _       <- sessionRepository.set(updated)
            } yield Redirect(navigator.nextPage(FreeholdTaxCalculatedTotalAmountDuePage, mode, updated))
        )
      }
  }

  private def constructViewModel(answers: UserAnswers)
                                (implicit hc: HeaderCarrier, messages: Messages): Future[Either[BuildRequestError, TotalAmountDueViewModel]] =
    sdltCalculationService.calculateStampDutyLandTax(answers).map { sdltcResult =>
      for {
        result    <- sdltcResult
        viewModel <- TotalAmountDueViewModel.toViewModel(result, answers, timeMachine, FreeholdTaxCalculatedSelfAssessedAmountPage)
      } yield viewModel
    }
}
