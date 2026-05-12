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

package viewmodels.taxCalculation.selfAssessed

import config.CurrencyFormatter
import models.UserAnswers
import models.taxCalculation.*
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import utils.DateTimeFormats.parseDate
import utils.{TaxCalculationPenaltiesHelper, TimeMachine}

case class TotalAmountDueViewModel(totalAmountDueSummary: SummaryList)

object TotalAmountDueViewModel extends CurrencyFormatter {

  private val rightAligned = "govuk-!-text-align-right"

  def toViewModel(result:BigDecimal, answers: UserAnswers, timeMachine: TimeMachine)
                 (implicit messages: Messages): Either[BuildRequestError, TotalAmountDueViewModel] =
    for {
      fullReturn       <- answers.fullReturn.toRight(MissingFullReturnError)
      transaction      <- fullReturn.transaction.toRight(MissingAboutTheTransactionError)
      effectiveDateRaw <- transaction.effectiveDate.toRight(MissingTransactionAnswerError("effectiveDate"))
      effectiveDate    <- parseDate(effectiveDateRaw).left.map(_ => InvalidDateError(effectiveDateRaw))
      penalties         = TaxCalculationPenaltiesHelper.getPenalties(effectiveDate, timeMachine)
      sdltDue           = result
      total             = (result + penalties).toCurrency
    } yield TotalAmountDueViewModel(
      SummaryList(Seq(
        SummaryListRow(
          Key(Text(messages("taxCalculation.totalAmountDue.sdltDue"))),
          Value(Text(sdltDue.toCurrency), classes = rightAligned)
        ),
        SummaryListRow(
          Key(Text(messages("taxCalculation.totalAmountDue.penalties"))),
          Value(Text(penalties.toCurrency), classes = rightAligned)
        ),
        SummaryListRow(
          Key(Text(messages("taxCalculation.totalAmountDue.total"))),
          Value(Text(total), classes = rightAligned)
        )
      ))
    )
}