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

import models.UserAnswers
import models.address.{Address, Country}
import pages.vendor.VendorAgentAddressPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object AgentAddressSummary {
  def row(answers: Option[UserAnswers])(implicit messages: Messages): SummaryListRow =
    answers.flatMap(_.get(VendorAgentAddressPage)).map { answer =>
      
      val listOfAgentAddressDetails = List(
        answer.line1,
        answer.line2,
        answer.line3,
        answer.line4,
        answer.line5,
        answer.postcode,
        answer.country
      )

      val list = listOfAgentAddressDetails.collect {
        case Some(Country(Some(code), Some(name))) => name
        case Some(detail) => detail
        case detail => detail
      }.filter(x => x != None)

      val prelimAddressString = list.mkString(", ")

      val value = ValueViewModel(
        HtmlContent(HtmlFormat.escape(prelimAddressString))
      )

      SummaryListRowViewModel(
        key = "vendor.checkYourAnswers.agentAddress.label",
        value = value,
        actions = Seq(
          ActionItemViewModel("site.change", controllers.vendor.routes.VendorAgentAddressController.redirectToAddressLookupVendorAgent(Some("change")).url)
            .withVisuallyHiddenText(messages("vendor.checkYourAnswers.agentAddress"))
        )
      )
    }.getOrElse{

      val value = ValueViewModel(
        HtmlContent(
          s"""<a href="${controllers.vendor.routes.VendorAgentAddressController.redirectToAddressLookupVendorAgent(Some("change")).url}" class="govuk-link">${messages("vendor.checkYourAnswers.agentName.agentAddressMissing")}</a>""")
      )
      
      SummaryListRowViewModel(
        key = "vendor.checkYourAnswers.agentAddress.label",
        value = value
      )
    }
}
