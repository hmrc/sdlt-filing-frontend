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
import models.taxCalculation.{BuildRequestError, MissingSelfAssessedAmountDueError}
import models.taxCalculation.TaxCalculationFlow.FreeholdSelfAssessed
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.taxCalculation.freeholdSelfAssessed.{FreeholdSelfAssessedAmountPage, FreeholdSelfAssessedTotalAmountDuePage}
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import services.taxCalculation.SdltCalculationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.TimeMachine
import viewmodels.taxCalculation.selfAssessed.TotalAmountDueViewModel
import views.html.taxCalculation.selfAssessed.TotalAmountDueView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FreeholdSdltNotCalculatedTotalDueController @Inject()(
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
    controllers.taxCalculation.freeholdSelfAssessed.routes.FreeholdSdltNotCalculatedTotalDueController.onSubmit(mode)

  private val pageTitleKey: String = "taxCalculation.freeholdSelfAssessed.title"

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      sdltCalculationService.whenInFlowAsync(FreeholdSelfAssessed) {
        Future.successful(constructViewModel(request.userAnswers)).map {
          case Right(viewModel) =>
            val prepared = request.userAnswers.get(FreeholdSelfAssessedTotalAmountDuePage).fold(form)(form.fill)
            Ok(view(prepared, viewModel, postAction(mode), pageTitleKey))
          case Left(err) =>
            logger.warn(s"[FreeholdSdltCalculatedTotalDueController][onPageLoad] failed: ${err.message}")
            Redirect(errorHandler(err))

        }
      }

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      sdltCalculationService.whenInFlowAsync(FreeholdSelfAssessed) {
        form.bindFromRequest().fold(
          formWithErrors =>
            constructViewModel(request.userAnswers) match {
              case Right(viewModel) =>
                Future.successful(BadRequest(view(formWithErrors, viewModel, postAction(mode), pageTitleKey)))
              case Left(err) =>
                logger.warn(s"[FreeholdSdltCalculatedTotalDueController][onSubmit] failed: ${err.message}")
                Future.successful(Redirect(errorHandler(err)))
            },
          value =>
            for {
              updated <- Future.fromTry(request.userAnswers.set(FreeholdSelfAssessedTotalAmountDuePage, value))
              _ <- sessionRepository.set(updated)
            } yield Redirect(navigator.nextPage(FreeholdSelfAssessedTotalAmountDuePage, mode, updated))
        )
      }
  }


  private def constructViewModel(answers: UserAnswers)
                                (implicit messages: Messages): Either[BuildRequestError, TotalAmountDueViewModel] =

    for {
      sdltDue <- answers.get(FreeholdSelfAssessedAmountPage)
        .toRight(MissingSelfAssessedAmountDueError("Missing FreeholdSelfAssessedAmountPage"))
        .map(BigDecimal(_))

      viewModel <- TotalAmountDueViewModel.toViewModel(sdltDue, answers, timeMachine)
    } yield viewModel

}

