/*
 * Copyright 2025 HM Revenue & Customs
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

package viewmodels.checkAnswers.vendor

import models.{CheckMode, UserAnswers}
import pages.vendor.VendorRepresentedByAgentPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object RepresentedByAnAgentSummary  {

  def row(answers: Option[UserAnswers])(implicit messages: Messages): SummaryListRow =
    answers.flatMap(_.get(VendorRepresentedByAgentPage)).map {
      answer =>

        val answerText = if (answer) {
          messages("site.yes")
        } else {
          messages("site.no")
        }

        val value = ValueViewModel(
          HtmlContent(answerText)
        )

        SummaryListRowViewModel(
          key     = "vendor.checkYourAnswers.representedByAgent.label",
          value   = value,
          actions = Seq(
            ActionItemViewModel("site.change", controllers.vendor.routes.VendorRepresentedByAgentController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("transactionType.change.hidden"))
          )
        )
    }.getOrElse{

      val value = ValueViewModel(
        HtmlContent(
          s"""<a href="${controllers.vendor.routes.VendorRepresentedByAgentController.onPageLoad(CheckMode).url}" class="govuk-link">${messages("vendor.checkYourAnswers.representedByAgent.agentMissing")}</a>""")
      )

      SummaryListRowViewModel(
        key = "vendor.checkYourAnswers.representedByAgent.label",
        value = value
      )
    }
}
