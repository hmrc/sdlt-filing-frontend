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
import models.vendor.VendorName
import pages.vendor.VendorOrCompanyNamePage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

class VendorOrCompanyNameSummarySpec extends SpecBase {

  "VendorOrCompanyNameSummary" - {

    "must return a SummaryListRow with VendorOrCompanyNamePage is set with change link" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers.set(VendorOrCompanyNamePage, VendorName(None, None, "Company")).success.value

        val result = VendorOrCompanyNameSummary.row(userAnswers).getOrElse(fail("Failed to get summary list row"))
        result.key.content.asHtml.toString() mustEqual msgs("vendorOrCompanyName.checkYourAnswersLabel")

        val contentString = result.value.content.asInstanceOf[Text].toString
        contentString must include("Company")

        result.actions.get.items.size mustEqual 1
        result.actions.get.items.head.href mustEqual controllers.vendor.routes.VendorOrCompanyNameController.onPageLoad(CheckMode).url
        result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
        result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("vendorOrCompanyName.change.hidden")
      }
    }

    "must return None when AddVendorAgentContactDetailsPage is not answered" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        VendorOrCompanyNameSummary.row(emptyUserAnswers) mustBe None
      }
    }
  }
}