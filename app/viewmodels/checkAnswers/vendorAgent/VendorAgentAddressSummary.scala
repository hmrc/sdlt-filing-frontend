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

import models.UserAnswers
import models.address.Address.toHtml
//import models.address.{Address, Country}
import pages.vendorAgent.VendorAgentAddressPage
import play.api.i18n.Messages
//import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object VendorAgentAddressSummary {
  def row(answers: UserAnswers)(implicit messages: Messages): SummaryListRow = {
    val changeRoute = controllers.vendorAgent.routes.VendorAgentAddressController.redirectToAddressLookupVendorAgent(Some("change")).url
    val label = messages("agent.checkYourAnswers.agentAddress.label")
    answers.get(VendorAgentAddressPage).map { answer =>

      SummaryListRowViewModel(
        key = label,
        value = ValueViewModel(HtmlContent(toHtml(answer))),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            changeRoute
          ).withVisuallyHiddenText(messages("agent.checkYourAnswers.agentAddress.label"))
        )
      )
    }.getOrElse {
      val value = ValueViewModel(
        HtmlContent(
          s"""<a href="$changeRoute" class="govuk-link">${messages("returnAgent.checkYourAnswers.address.missing")}</a>""")
      )
      SummaryListRowViewModel(
        key = label,
        value = value
      )
    }
  }
}


//val listOfAgentAddressDetails = List(
//        answer.line1,
//        answer.line2,
//        answer.line3,
//        answer.line4,
//        answer.line5,
//        answer.postcode,
//        answer.country
//      )
//
//      val list = listOfAgentAddressDetails.collect {
//        case Some(Country(Some(code), Some(name))) => name
//        case Some(detail) => detail
//        case detail => detail
//      }.filter(x => x != None)
//
//      val prelimAddressString = list.mkString(", ")
//
//      val value = ValueViewModel(
//        HtmlContent(HtmlFormat.escape(prelimAddressString))
//      )
//
//      SummaryListRowViewModel(
//        key = "agent.checkYourAnswers.agentAddress.label",
//        value = value,
//        actions = Seq(
//          ActionItemViewModel("site.change", changeRoute)
//            .withVisuallyHiddenText(messages("agent.checkYourAnswers.agentAddress"))
//        )
//      )
//    }.getOrElse{
//
//      val value = ValueViewModel(
//        HtmlContent(
//          s"""<a href="${controllers.vendorAgent.routes.VendorAgentAddressController.redirectToAddressLookupVendorAgent(Some("change")).url}" class="govuk-link">${messages("agent.checkYourAnswers.agentName.agentAddressMissing")}</a>""")
//      )
//
//      SummaryListRowViewModel(
//        key = "agent.checkYourAnswers.agentAddress.label",
//        value = value
//      )
//    }
//  }
//}
//
