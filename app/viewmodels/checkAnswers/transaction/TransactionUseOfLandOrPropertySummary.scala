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

import models.transaction.TransactionUseOfLandOrPropertyAnswers
import models.{CheckMode, UserAnswers}
import pages.transaction.TransactionUseOfLandOrPropertyPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.checkAnswers.summary.SummaryRowResult
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object TransactionUseOfLandOrPropertySummary {

  def row(answers: UserAnswers)(implicit messages: Messages): SummaryRowResult = {
    val changeRoute = controllers.transaction.routes.TransactionUseOfLandOrPropertyController.onPageLoad(CheckMode)
    val label = messages("transaction.transactionUseOfLandOrProperty.checkYourAnswersLabel")
    val displayHiddenMsg = messages("transaction.transactionUseOfLandOrProperty.change.hidden")

    answers.get(TransactionUseOfLandOrPropertyPage).map { answersObject =>
      val selectedItems = TransactionUseOfLandOrPropertyAnswers.toSet(answersObject).toSeq.sortBy(_.order)
        .map(_.toString)

      val value = ValueViewModel(
        HtmlContent(
          selectedItems.map { answer =>
              HtmlFormat.escape(messages(s"transaction.transactionUseOfLandOrProperty.$answer")).toString
            }
            .mkString(",<br>")
        )
      )

      Row(
        SummaryListRowViewModel(
          key     = label,
          value   = value,
          actions = Seq(
            ActionItemViewModel("site.change", changeRoute.url)
              .withVisuallyHiddenText(displayHiddenMsg)
          )
        )
      )
    }.getOrElse {
      Missing(changeRoute)
    }
  }
}