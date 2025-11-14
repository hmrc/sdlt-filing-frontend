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

import base.SpecBase
import models.{CheckMode, UserAnswers}
import pages.vendor.{AgentNamePage, VendorRepresentedByAgentPage}
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.VendorRepresentedByAgentSummary
import controllers.routes


class VendorRepresentedByAgentSummarySpec extends SpecBase {

  "VendorRepresentedByAgentSummary" - {

    "must return a SummaryListRow with 'yes' text and change link" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers.set(VendorRepresentedByAgentPage, true).success.value

        val result = VendorRepresentedByAgentSummary.row(userAnswers).getOrElse(fail("Failed to get summary list row"))

        result.key.content.asHtml.toString() mustEqual msgs("vendorRepresentedByAgent.checkYourAnswersLabel")

        val contentString = result.value.content.asInstanceOf[Text].asHtml.toString()

        contentString mustEqual msgs("site.yes")

        result.actions.get.items.size mustEqual 1
        result.actions.get.items.head.href mustEqual controllers.vendor.routes.VendorRepresentedByAgentController.onPageLoad(CheckMode).url
        result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
        result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("vendorRepresentedByAgent.change.hidden")
      }
    }

    "must return a SummaryListRow with 'no' text and change link" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers.set(VendorRepresentedByAgentPage, false).success.value

        val result = VendorRepresentedByAgentSummary.row(userAnswers).getOrElse(fail("Failed to get summary list row"))

        result.key.content.asHtml.toString() mustEqual msgs("vendorRepresentedByAgent.checkYourAnswersLabel")

        val contentString = result.value.content.asInstanceOf[Text].asHtml.toString()

        contentString mustEqual msgs("site.no")

        result.actions.get.items.size mustEqual 1
        result.actions.get.items.head.href mustEqual controllers.vendor.routes.VendorRepresentedByAgentController.onPageLoad(CheckMode).url
        result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
        result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("vendorRepresentedByAgent.change.hidden")
      }
    }

    }
  }