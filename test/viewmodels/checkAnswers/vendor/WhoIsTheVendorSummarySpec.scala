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
import models.vendor.whoIsTheVendor
import pages.vendor.WhoIsTheVendorPage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent

class WhoIsTheVendorSummarySpec extends SpecBase {

  "WhoIsTheVendorSummary" - {

    "must return a SummaryListRow with WhoIsTheVendorPage is set and with change link" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers.set(WhoIsTheVendorPage, whoIsTheVendor.Company).success.value

        val result = WhoIsTheVendorSummary.row(userAnswers).getOrElse(fail("Failed to get summary list row"))
        result.key.content.asHtml.toString() mustEqual msgs("vendor.whoIsTheVendor.checkYourAnswersLabel")

        val contentString = result.value.content.asInstanceOf[HtmlContent].toString
        contentString must include(msgs(s"vendor.whoIsTheVendor.${whoIsTheVendor.Company}"))

        result.actions.get.items.size mustEqual 1
        result.actions.get.items.head.href mustEqual controllers.vendor.routes.WhoIsTheVendorController.onPageLoad(CheckMode).url
        result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
        result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("vendor.whoIsTheVendor.change.hidden")
      }
    }

    "must return None when AddVendorAgentContactDetailsPage is not answered" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        WhoIsTheVendorSummary.row(emptyUserAnswers) mustBe None
      }
    }
  }
}