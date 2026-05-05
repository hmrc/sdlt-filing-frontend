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
import models.UserAnswers
import models.transaction.ReasonForRelief
import pages.transaction.*
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.*
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.transaction.*
import viewmodels.govuk.all.SummaryListViewModel
import views.html.transaction.TransactionCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class TransactionCheckYourAnswersController @Inject()(
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents,
  view: TransactionCheckYourAnswersView
)(implicit ex: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      for {
        result <- sessionRepository.get(request.userAnswers.id)
      } yield {

        val isReturnIdEmpty = result.exists(_.returnId.isEmpty)
        val isDataEmpty     = result.exists(_.data.value.isEmpty)

        if (isReturnIdEmpty) {
          Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
        } else {
          (isDataEmpty, result) match {
            case (true, _)                 => Redirect(controllers.preliminary.routes.BeforeStartReturnController.onPageLoad())
            case (_, Some(userAnswers))    => Ok(view(buildSummaryList(userAnswers)))
            case (false, None)             => Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
          }
        }
      }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      sessionRepository.get(request.userAnswers.id).map {
        case Some(_) => Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
        case None    => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
  }

  private def buildSummaryList(userAnswers: UserAnswers)(implicit request: RequestHeader) =
    SummaryListViewModel(
      rows = Seq(
        Some(TypeOfTransactionSummary.row(Some(userAnswers))),
        Some(TransactionEffectiveDateSummary.row(Some(userAnswers))),
        Some(TransactionAddDateOfContractSummary.row(userAnswers)),
        Option.when(contractDateKnown(userAnswers))(TransactionDateOfContractSummary.row(userAnswers)),
        Some(TotalConsiderationOfTransactionSummary.row(userAnswers)),
        Some(TransactionVatIncludedSummary.row(userAnswers)),
        Option.when(vatIncluded(userAnswers))(TransactionVatAmountSummary.row(userAnswers)),
        Some(TransactionFormsOfConsiderationSummary.row(userAnswers)),
        Some(TransactionLinkedTransactionsSummary.row(userAnswers)),
        Option.when(isLinkedTransaction(userAnswers))(TotalConsiderationOfLinkedTransactionSummary.row(userAnswers)),
        Some(PurchaserEligibleToClaimReliefSummary.row(userAnswers)),
        Option.when(isClaimingRelief(userAnswers))(ReasonForReliefSummary.row(userAnswers)),
        Option.when(isCharitiesRelief(userAnswers))(AddRegisteredCharityNumberSummary.row(userAnswers)),
        Option.when(knowsCharityNumber(userAnswers))(CharityRegisteredNumberSummary.row(userAnswers)),
        Some(TransactionPartialReliefSummary.row(userAnswers)),
        Option.when(isPartialRelief(userAnswers))(ClaimingPartialReliefAmountSummary.row(userAnswers))
      ).flatten
    )

  private def contractDateKnown(ua: UserAnswers): Boolean =
    ua.get(TransactionAddDateOfContractPage).contains(true)

  private def vatIncluded(ua: UserAnswers): Boolean =
    ua.get(TransactionVatIncludedPage).contains(true)

  private def isLinkedTransaction(ua: UserAnswers): Boolean =
    ua.get(TransactionLinkedTransactionsPage).contains(true)

  private def isClaimingRelief(ua: UserAnswers): Boolean =
    ua.get(PurchaserEligibleToClaimReliefPage).contains(true)

  private def isCharitiesRelief(ua: UserAnswers): Boolean =
    ua.get(ReasonForReliefPage).contains(ReasonForRelief.CharitiesRelief)

  private def knowsCharityNumber(ua: UserAnswers): Boolean =
    ua.get(AddRegisteredCharityNumberPage).contains(true)

  private def isPartialRelief(ua: UserAnswers): Boolean =
    ua.get(TransactionPartialReliefPage).contains(true)
}
