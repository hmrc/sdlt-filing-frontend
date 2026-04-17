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

import models.transaction.TransactionFormsOfConsiderationAnswers
import models.{CheckMode, UserAnswers}
import pages.transaction.TransactionFormsOfConsiderationPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object TransactionFormsOfConsiderationSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): SummaryListRow =

    val changeRoute = controllers.transaction.routes.TransactionFormsOfConsiderationController.onPageLoad(CheckMode).url
    val checkYourAnswersLabelMsg = messages("transaction.transactionFormsOfConsideration.checkYourAnswersLabel")
    val displayMissingMsgContent = messages("transaction.transactionFormsOfConsideration.missing")
    
    answers.get(TransactionFormsOfConsiderationPage).map {
      answersObject =>

        val selectedItems = TransactionFormsOfConsiderationAnswers.toSet(answersObject).toSeq
          .map(_.toString)

        val value = ValueViewModel(
          HtmlContent(
            selectedItems.map {
              answer => HtmlFormat.escape(messages(s"transaction.transactionFormsOfConsideration.$answer")).toString
            }
            .mkString(",<br>")
          )
        )

        SummaryListRowViewModel(
          key     = checkYourAnswersLabelMsg,
          value   = value,
          actions = Seq(
            ActionItemViewModel("site.change", changeRoute)
              .withVisuallyHiddenText(messages("transaction.transactionFormsOfConsideration.change.hidden"))
          )
        )
    }.getOrElse {

      val value = ValueViewModel(
        HtmlContent(
          s"""<a href="$changeRoute" class="govuk-link">$displayMissingMsgContent</a>""")
      )

      SummaryListRowViewModel(
        key = checkYourAnswersLabelMsg,
        value = value
      )
    }
}
