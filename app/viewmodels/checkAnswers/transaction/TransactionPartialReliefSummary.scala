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
import pages.transaction.TransactionPartialReliefPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object TransactionPartialReliefSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): SummaryListRow =
    
    val changeRoute = controllers.transaction.routes.TransactionPartialReliefController.onPageLoad(CheckMode).url
    val checkYourAnswersLabelMsg = messages("transaction.transactionPartialRelief.checkYourAnswersLabel")
    val displayMissingMsgContent = messages("transaction.transactionPartialRelief.missing")
    val hiddenMsg = messages("transaction.transactionPartialRelief.change.hidden")
    
    answers.get(TransactionPartialReliefPage).map {
      answer =>

        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key     = checkYourAnswersLabelMsg,
          value   = ValueViewModel(value),
          actions = Seq(
            ActionItemViewModel("site.change", changeRoute)
              .withVisuallyHiddenText(messages(hiddenMsg))
          )
        )
    }.getOrElse {
      val value = ValueViewModel(
        HtmlContent(
          s"""<a href="${changeRoute}" class="govuk-link">${messages(displayMissingMsgContent)}</a>""")
      )

      SummaryListRowViewModel(
        key = checkYourAnswersLabelMsg,
        value = value
      )
    }
}
