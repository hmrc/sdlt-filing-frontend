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

import controllers.actions.*
import forms.taxCalculation.ConfirmEffectiveDateOfTransactionFormProvider
import models.{CheckMode, NormalMode, UserAnswers}
import models.taxCalculation.BuildRequestError
import navigation.Navigator
import pages.taxCalculation.ConfirmEffectiveDateOfTransactionPage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.transaction.PopulateTransactionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.EffectiveDateHelper
import views.html.taxCalculation.ConfirmEffectiveDateOfTransactionYesNoView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class ConfirmEffectiveDateOfTransactionController @Inject()(override val messagesApi: MessagesApi,
                                                            identify: IdentifierAction,
                                                            getData: DataRetrievalAction,
                                                            requireData: DataRequiredAction,
                                                            navigator: Navigator,
                                                            sessionRepository: SessionRepository,
                                                            populateTransactionService: PopulateTransactionService,
                                                            val controllerComponents: MessagesControllerComponents,
                                                            formProvider: ConfirmEffectiveDateOfTransactionFormProvider,
                                                            view: ConfirmEffectiveDateOfTransactionYesNoView
                                                           )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging with TaxCalculationErrorRecovery {


  private val form: Form[Boolean] = formProvider()

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      EffectiveDateHelper.getEffectiveDate(request.userAnswers) match {
        case Right(effectiveDate) =>
          val preparedForm = request.userAnswers.get(ConfirmEffectiveDateOfTransactionPage).fold(form)(form.fill)
          Ok(view(preparedForm, effectiveDate))
        case Left(error) =>
          logger.warn(s"[ConfirmEffectiveDateOfTransactionController][onPageLoad] failed: ${error.message}")
          Redirect(errorHandler(error))
      }

  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      EffectiveDateHelper.getEffectiveDate(request.userAnswers) match {
        case Right(effectiveDate) =>
          form.bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, effectiveDate))),
              value =>
                for {
                  updatedUserAnswers <- Future.fromTry(request.userAnswers.set(ConfirmEffectiveDateOfTransactionPage, value))
                  _                   = logger.info(s"[ConfirmEffectiveDateOfTransactionController][onSubmit] returnId=${request.userAnswers.returnId.getOrElse("unknown")}: effective date '$effectiveDate' confirmed=$value (${if (value) "proceeding to tax calculation" else "going to transaction journey to amend the date"})")
                  answersToPersist    = if (value) updatedUserAnswers else populateTransaction(updatedUserAnswers)
                  _                  <- sessionRepository.set(answersToPersist)
                } yield {
                  if (value) {
                    Redirect(navigator.nextPage(ConfirmEffectiveDateOfTransactionPage, NormalMode, updatedUserAnswers))
                  } else {
                    Redirect(controllers.transaction.routes.TransactionEffectiveDateController.onPageLoad(CheckMode))
                  }
                }
            )
        case Left(error) =>
          logger.warn(s"[ConfirmEffectiveDateOfTransactionController][onSubmit] failed: ${error.message}")
          Future.successful(Redirect(errorHandler(error)))
      }
  }

  private def populateTransaction(userAnswers: UserAnswers): UserAnswers = {
    val returnRef = userAnswers.returnId.getOrElse("unknown")
    userAnswers.fullReturn.flatMap(_.transaction) match {
      case Some(transaction) =>
        populateTransactionService.populateTransactionInSession(transaction, userAnswers) match {
          case Success(populated) =>
            logger.info(s"[ConfirmEffectiveDateOfTransactionController][populateTransaction] returnId=$returnRef: populated transaction into session ahead of the transaction journey")
            populated
          case Failure(e) =>
            logger.warn(s"[ConfirmEffectiveDateOfTransactionController][populateTransaction] returnId=$returnRef: failed to populate transaction into session, the transaction journey will have no session: ${e.getMessage}")
            userAnswers
        }
      case None =>
        logger.warn(s"[ConfirmEffectiveDateOfTransactionController][populateTransaction] returnId=$returnRef: no transaction on the full return to populate into session")
        userAnswers
    }
  }
}
