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

package viewmodels.checkAnswers.vendor

import base.SpecBase
import models.CheckMode
import pages.vendor.VendorRepresentedByAgentPage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent


class RepresentedByAnAgentSummarySpec extends SpecBase {

  "RepresentedByAnAgentSummary" - {

    "must return a SummaryListRow when VendorRepresentedByAgentPage is 'yes' and show change link" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers.set(VendorRepresentedByAgentPage, true).success.value

          val result = RepresentedByAnAgentSummary.row(Some(userAnswers))

          result.key.content.asHtml.toString() mustEqual msgs("vendor.checkYourAnswers.representedByAgent.label")

          val contentString = result.value.content.asHtml.toString()

          contentString mustEqual msgs("site.yes")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.vendor.routes.VendorRepresentedByAgentController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("transactionType.change.hidden")
        }
    }

    "must return a SummaryListRow when VendorRepresentedByAgentPage is 'no' and show change link" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers.set(VendorRepresentedByAgentPage, false).success.value

        val result = RepresentedByAnAgentSummary.row(Some(userAnswers))

        result.key.content.asHtml.toString() mustEqual msgs("vendor.checkYourAnswers.representedByAgent.label")

        val contentString = result.value.content.asHtml.toString()

        contentString mustEqual msgs("site.no")

        result.actions.get.items.size mustEqual 1
        result.actions.get.items.head.href mustEqual controllers.vendor.routes.VendorRepresentedByAgentController.onPageLoad(CheckMode).url
        result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
        result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("transactionType.change.hidden")
      }
    }

      "must return a summary list row with a link to add answer when data missing" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = RepresentedByAnAgentSummary.row(Some(emptyUserAnswers))

          result.key.content.asHtml.toString() mustEqual
            msgs("vendor.checkYourAnswers.representedByAgent.label")
          
          val html = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()

          html must include(controllers.vendor.routes.VendorRepresentedByAgentController.onPageLoad(CheckMode).url)

          html must include(msgs("vendor.checkYourAnswers.representedByAgent.agentMissing"))

          result.actions mustBe None
        }
      }
  }
}

