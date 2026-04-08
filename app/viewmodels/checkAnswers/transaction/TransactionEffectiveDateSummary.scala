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
import pages.transaction.TransactionEffectiveDatePage
import play.api.i18n.{Lang, Messages}
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.DateTimeFormats.dateTimeFormat
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object TransactionEffectiveDateSummary  {

  def row(answers: Option[UserAnswers])(implicit messages: Messages): SummaryListRow =

    val changeRoute = controllers.transaction.routes.TransactionEffectiveDateController.onPageLoad(CheckMode).url
    val checkYourAnswersLabelMsg = messages("transaction.transactionEffectiveDate.checkYourAnswersLabel")
    val displayMissingMsgContent = messages("transaction.transactionEffectiveDate.missing")
    
    answers.flatMap(_.get(TransactionEffectiveDatePage)).map {
      answer =>

        implicit val lang: Lang = messages.lang

        SummaryListRowViewModel(
          key     = checkYourAnswersLabelMsg,
          value   = ValueViewModel(answer.format(dateTimeFormat())),
          actions = Seq(
            ActionItemViewModel("site.change", changeRoute)
              .withVisuallyHiddenText(messages("transaction.transactionEffectiveDate.change.hidden"))
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
