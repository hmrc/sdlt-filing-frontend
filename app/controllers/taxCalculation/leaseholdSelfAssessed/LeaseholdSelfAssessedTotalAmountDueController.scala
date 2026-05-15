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

package controllers.taxCalculation.leaseholdSelfAssessed

import controllers.actions.*
import controllers.taxCalculation.TaxCalculationErrorRecovery
import forms.taxCalculation.TotalAmountDueFormProvider
import models.taxCalculation.BuildRequestError
import models.taxCalculation.MissingTaxCalculationAnswerError
import models.taxCalculation.TaxCalculationFlow.LeaseholdSelfAssessed
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.taxCalculation.leaseholdSelfAssessed.{LeaseholdSelfAssessedNpvTaxPage, LeaseholdSelfAssessedPremiumPayableTaxPage, LeaseholdSelfAssessedTotalAmountDuePage}
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import services.taxCalculation.SdltCalculationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.TimeMachine
import viewmodels.taxCalculation.selfAssessedViewModels.TotalAmountDueViewModel
import views.html.taxCalculation.selfAssessed.TotalAmountDueView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LeaseholdSelfAssessedTotalAmountDueController @Inject()(
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
    controllers.taxCalculation.leaseholdSelfAssessed.routes.LeaseholdSelfAssessedTotalAmountDueController.onSubmit(mode)

  private val sectionKey: String = "site.taxCalculation.leaseholdSelfAssessed.caption"

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      sdltCalculationService.whenInFlowAsync(LeaseholdSelfAssessed) {
        Future.successful(constructViewModel(request.userAnswers)).map {
          case Right(viewModel) =>
            val prepared = request.userAnswers.get(LeaseholdSelfAssessedTotalAmountDuePage).fold(form)(form.fill)
            Ok(view(prepared, viewModel, postAction(mode), sectionKey))
          case Left(err) =>
            logger.warn(s"[LeaseholdSelfAssessedTotalAmountDueController][onPageLoad] failed: ${err.message}")
            Redirect(errorHandler(err))
        }
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      sdltCalculationService.whenInFlowAsync(LeaseholdSelfAssessed) {
        form.bindFromRequest().fold(
          formWithErrors =>
            Future.successful(constructViewModel(request.userAnswers)).map {
              case Right(viewModel) => BadRequest(view(formWithErrors, viewModel, postAction(mode), sectionKey))
              case Left(err) =>
                logger.warn(s"[LeaseholdSelfAssessedTotalAmountDueController][onSubmit] failed: ${err.message}")
                Redirect(errorHandler(err))
            },
          value =>
            for {
              updated <- Future.fromTry(request.userAnswers.set(LeaseholdSelfAssessedTotalAmountDuePage, value))
              _       <- sessionRepository.set(updated)
            } yield Redirect(navigator.nextPage(LeaseholdSelfAssessedTotalAmountDuePage, mode, updated))
        )
      }
  }

  private def constructViewModel(answers: UserAnswers)(implicit messages: Messages): Either[BuildRequestError, TotalAmountDueViewModel] =
    for {
      premiumTax <- answers
        .get(LeaseholdSelfAssessedPremiumPayableTaxPage)
        .toRight(MissingTaxCalculationAnswerError("premiumTax"))
      npvTax     <- answers
        .get(LeaseholdSelfAssessedNpvTaxPage)
        .toRight(MissingTaxCalculationAnswerError("npvTax"))
      totalTax = BigDecimal(premiumTax) + BigDecimal(npvTax)
      viewModel  <- TotalAmountDueViewModel.toViewModel(totalTax, answers, timeMachine)
    } yield viewModel

}
