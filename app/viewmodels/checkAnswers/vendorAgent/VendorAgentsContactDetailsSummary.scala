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
import pages.vendorAgent.{AddVendorAgentContactDetailsPage, VendorAgentsContactDetailsPage}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object VendorAgentsContactDetailsSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] = {
    val label = messages("vendorAgent.vendorAgentsContactDetails.checkYourAnswersLabel")
    val changeRoute = controllers.vendorAgent.routes.VendorAgentsContactDetailsController.onPageLoad(CheckMode).url

    (answers.get(VendorAgentsContactDetailsPage), answers.get(AddVendorAgentContactDetailsPage)) match {
      case (Some(contactDetails), _) =>
        val value: String = (contactDetails.phoneNumber, contactDetails.emailAddress) match {
          case (Some(phone), Some(email)) =>
            "Tel: " + HtmlFormat.escape(phone).toString + "<br/>" + "Email: " + HtmlFormat.escape(email).toString
          case (_, Some(email)) =>
            "Email: " + HtmlFormat.escape(email).toString
          case (Some(phone), _) =>
            "Tel: " + HtmlFormat.escape(phone).toString
          case (None, None) => ""
        }

        Some(SummaryListRowViewModel(
          key = label,
          value = ValueViewModel(HtmlContent(value)),
          actions = Seq(
            ActionItemViewModel("site.change", changeRoute)
              .withVisuallyHiddenText(messages("vendorAgent.addVendorAgentContactDetails.change.hidden"))
          )
        ))

      case (None, Some(true)) =>
        val value = ValueViewModel(
          HtmlContent(
            s"""<a href="$changeRoute" class="govuk-link">${messages("returnAgent.checkYourAnswers.contactDetails.missing")}</a>""")
        )
        Some(SummaryListRowViewModel(
          key = label,
          value = value
        ))
      case _ => None
    }
  }
}
