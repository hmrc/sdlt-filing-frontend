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

package viewmodels.checkAnswers.purchaserAgent

import models.{CheckMode, UserAnswers}
import pages.purchaserAgent.{AddContactDetailsForPurchaserAgentPage, PurchaserAgentsContactDetailsPage}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object PurchaserAgentsContactDetailsSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] = {
    val label = messages("purchaserAgent.contactDetails.checkYourAnswersLabel")
    val changeRoute = controllers.purchaserAgent.routes.PurchaserAgentsContactDetailsController.onPageLoad(CheckMode).url

    (answers.get(PurchaserAgentsContactDetailsPage), answers.get(AddContactDetailsForPurchaserAgentPage)) match {
      case (Some(contactDetails), _) =>
        val value: String = (contactDetails.phoneNumber, contactDetails.emailAddress) match {
          case (Some(phone), Some(email)) =>
            HtmlFormat.escape(phone).toString + "<br/>" +
              HtmlFormat.escape(email).toString
          case (Some(phone), None) =>
            HtmlFormat.escape(phone).toString
          case (None, Some(email)) =>
            HtmlFormat.escape(email).toString
          case (None, None) => ""
        }

        Some(SummaryListRowViewModel(
          key = label,
          value = ValueViewModel(HtmlContent(value)),
          actions = Seq(
            ActionItemViewModel("site.change", changeRoute)
              .withVisuallyHiddenText(messages("purchaserAgent.contactDetails.change.hidden"))
          )
        ))
      case (None, Some(true)) =>
        val value = ValueViewModel(
          HtmlContent(
            s"""<a href="$changeRoute" class="govuk-link">${messages("purchaserAgent.checkYourAnswers.contactDetails.missing")}</a>""")
        )
        Some(SummaryListRowViewModel(
          key = label,
          value = value
        ))
      case _ => None
    }
  }
}
