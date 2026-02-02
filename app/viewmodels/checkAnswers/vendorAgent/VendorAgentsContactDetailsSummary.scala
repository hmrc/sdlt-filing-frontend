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
import pages.vendorAgent.VendorAgentsContactDetailsPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object VendorAgentsContactDetailsSummary {

  def row(answers: Option[UserAnswers])(implicit messages: Messages): SummaryListRow =
    answers.flatMap(_.get(VendorAgentsContactDetailsPage)).map { answer =>
      val value: Option[String] = (answer.phoneNumber, answer.emailAddress) match {
        case (Some(phone), Some(email)) =>
          Some(HtmlFormat.escape(phone).toString + "<br/>" + HtmlFormat.escape(email).toString)
        case (_, Some(email)) =>
          Some(HtmlFormat.escape(email).toString)
        case (Some(phone), _) =>
          Some(HtmlFormat.escape(phone).toString)
        case _ =>
          None
      }

      value match {
        case Some(details) =>
          SummaryListRowViewModel(
            key = "vendorAgent.vendorAgentsContactDetails.checkYourAnswersLabel",
            value = ValueViewModel(HtmlContent(details)),
            actions = Seq(
              ActionItemViewModel("site.change", controllers.vendorAgent.routes.VendorAgentsContactDetailsController.onPageLoad(CheckMode).url)
                .withVisuallyHiddenText(messages("vendorAgent.vendorAgentsContactDetails.change.hidden"))
            )
          )
        case _ =>
          SummaryListRowViewModel(
            key = "vendorAgent.vendorAgentsContactDetails.checkYourAnswersLabel",
            value = ValueViewModel(HtmlContent("-")),
            actions = Seq(
              ActionItemViewModel("site.change", controllers.vendorAgent.routes.VendorAgentsContactDetailsController.onPageLoad(CheckMode).url)
                .withVisuallyHiddenText(messages("vendorAgent.vendorAgentsContactDetails.change.hidden"))
            )
          )
      }
    }.getOrElse {

      val value = ValueViewModel(
        HtmlContent(
          s"""<a href="${controllers.vendorAgent.routes.VendorAgentCheckYourAnswersController.onPageLoad().url}" class="govuk-link">${messages("agent.checkYourAnswers.agentContactDetails.agentDetailsMissing")}</a>""")
      )

      SummaryListRowViewModel(
        key = "vendorAgent.vendorAgentsContactDetails.checkYourAnswersLabel",
        value = value
      )
    }
}
