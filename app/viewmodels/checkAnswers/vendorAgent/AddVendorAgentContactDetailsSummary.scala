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

package viewmodels.checkAnswers.vendorAgent

import models.{CheckMode, UserAnswers}
import pages.vendorAgent.{AddVendorAgentContactDetailsPage, AgentNamePage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object AddVendorAgentContactDetailsSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): SummaryListRow = {

    val changeRoute = controllers.vendorAgent.routes.AddVendorAgentContactDetailsController.onPageLoad(CheckMode).url
    val agentName = answers.get(AgentNamePage).getOrElse("the agent")
    val label = messages("vendorAgent.addVendorAgentContactDetails.checkYourAnswersLabel", agentName)

    answers.get(AddVendorAgentContactDetailsPage).map {
      answer =>

        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key = label,
          value = ValueViewModel(value),
          actions = Seq(
            ActionItemViewModel("site.change", changeRoute)
              .withVisuallyHiddenText(messages("vendorAgent.addVendorAgentContactDetails.change.hidden"))
          )
        )
    }.getOrElse {
      val value = ValueViewModel(
        HtmlContent(
          s"""<a href="$changeRoute" class="govuk-link">${messages("returnAgent.checkYourAnswers.addContactDetails.missing")}</a>""")
      )
      SummaryListRowViewModel(
        key = label,
        value = value
      )
    }
  }
}