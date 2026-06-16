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

package controllers.transaction

import controllers.actions.*
import models.{CheckMode, UserAnswers}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request, Result}
import repositories.SessionRepository
import services.crossflow.{CrossFlowFailure, Pages, ReturnSection}
import services.crossflow.fields.CrossFlowValidationService
import services.transaction.PopulateTransactionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transaction.TransactionReliefSingleEntityView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class TransactionSingleEntityController @Inject() (
                                                          override val messagesApi:    MessagesApi,
                                                          identify:                    IdentifierAction,
                                                          getData:                     DataRetrievalAction,
                                                          requireData:                 DataRequiredAction,
                                                          sessionRepository:           SessionRepository,
                                                          populateTransactionService:  PopulateTransactionService,
                                                          crossFlow:                   CrossFlowValidationService,
                                                          val controllerComponents:    MessagesControllerComponents,
                                                          view:                        TransactionReliefSingleEntityView
                                                        )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      populateFromTransaction(request.userAnswers)
  }

  private def populateFromTransaction(userAnswers: UserAnswers)(implicit request: Request[_]): Future[Result] =
    userAnswers.fullReturn.flatMap(_.transaction) match {

      case None =>
        Future.successful(Redirect(
          controllers.transaction.routes.TransactionBeforeYouStartController.onPageLoad()
        ))

      case Some(transaction) =>
        populateTransactionService.populateTransactionInSession(transaction, userAnswers) match {

          case Success(populated) =>
            sessionRepository.set(populated).map(_ => renderOrRedirect(populated))

          case Failure(_) =>
            Future.successful(Redirect(
              controllers.transaction.routes.TransactionBeforeYouStartController.onPageLoad()
            ))
        }
    }
  
  private def renderOrRedirect(userAnswers: UserAnswers)(implicit request: Request[_]): Result =
    crossFlow.failuresAffecting(ReturnSection.Transaction, userAnswers).headOption match {

      case Some(failure) =>
        Ok(view(
          headingKey  = failure.headingKey,
          body        = failure.body,
          ctaKey      = ctaKeyFor(failure),
          continueUrl = continueUrlFor(failure)
        ))

      case None =>
        Redirect(controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad())
    }

  private def continueUrlFor(failure: CrossFlowFailure): String = {
    val targets = failure.targets.map(_.page).toSet

    if      (targets.contains(Pages.LandPropertyType)) controllers.transaction.routes.ReasonForReliefController.onPageLoad(CheckMode).url
    else if (targets.contains(Pages.EffectiveDate))    controllers.transaction.routes.TransactionEffectiveDateController.onPageLoad(CheckMode).url
    else if (targets.contains(Pages.ContractDate))     controllers.transaction.routes.TransactionDateOfContractController.onPageLoad(CheckMode).url
    else                                               controllers.transaction.routes.ReasonForReliefController.onPageLoad(CheckMode).url
  }

  private def ctaKeyFor(failure: CrossFlowFailure): String = {
    val targets = failure.targets.map(_.page).toSet

    if      (targets.contains(Pages.LandPropertyType)) "crossflow.relief.cta.changePropertyType"
    else if (targets.contains(Pages.EffectiveDate))    "crossflow.relief.cta.changeEffectiveDate"
    else if (targets.contains(Pages.ContractDate))     "crossflow.relief.cta.changeContractDate"
    else                                               "crossflow.relief.cta.changeReliefReason"
  }
}