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

package viewmodels.checkAnswers.transaction

import models.{CheckMode, UserAnswers}
import pages.transaction.{TransactionAddDateOfContractPage, TransactionDateOfContractPage}
import play.api.i18n.{Lang, Messages}
import utils.DateTimeFormats.dateTimeFormat
import viewmodels.checkAnswers.summary.SummaryRowResult
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object TransactionDateOfContractSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryRowResult] = {
    val changeRoute = controllers.transaction.routes.TransactionDateOfContractController.onPageLoad(CheckMode)
    val label = messages("transaction.transactionDateOfContract.checkYourAnswersLabel")

    (answers.get(TransactionDateOfContractPage), answers.get(TransactionAddDateOfContractPage)) match {
      case (Some(date), _) =>
        implicit val lang: Lang = messages.lang
        Some(Row(
          SummaryListRowViewModel(
            key     = label,
            value   = ValueViewModel(date.format(dateTimeFormat())),
            actions = Seq(
              ActionItemViewModel("site.change", changeRoute.url)
                .withVisuallyHiddenText(messages("transaction.transactionDateOfContract.change.hidden"))
            )
          )
        ))
      case (None, Some(true)) =>
        Some(Missing(changeRoute))
      case _ =>
        None
    }
  }
}
