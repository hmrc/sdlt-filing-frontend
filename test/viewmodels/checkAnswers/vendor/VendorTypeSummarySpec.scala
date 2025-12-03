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
import controllers.routes
import models.CheckMode
import models.prelimQuestions.TransactionType
import models.vendor.whoIsTheVendor
import pages.vendor.WhoIsTheVendorPage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent

class VendorTypeSummarySpec extends SpecBase {

  "VendorTypeSummary" - {

    "when Vendor type is present" - {

      "must return a summary list row with Vendor type value and change link" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)
          

          val userAnswers = emptyUserAnswers.set(WhoIsTheVendorPage, whoIsTheVendor.Company).success.value

          val result = VendorTypeSummary.row(Some(userAnswers))

          result.key.content.asHtml.toString() mustEqual msgs("vendor.checkYourAnswers.vendorType.label")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()

          htmlContent mustEqual msgs("vendor.checkYourAnswers.Company")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.vendor.routes.WhoIsTheVendorController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("vendor.checkYourAnswers.hidden")
        }
      }

      "must display the correct message for different Vendor types" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val individualOrCompany = Seq(
            whoIsTheVendor.Company,
            whoIsTheVendor.Individual
          )

          individualOrCompany.foreach { individualOrCompany =>
            val userAnswers = emptyUserAnswers
              .set(WhoIsTheVendorPage, individualOrCompany).success.value

            val result = VendorTypeSummary.row(Some(userAnswers))

            val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
            // Verify it equals the resolved message for this transaction type
            htmlContent mustEqual msgs(s"vendor.checkYourAnswers.$individualOrCompany")
          }
        }
      }

    }

    "when Vendor type is not present" - {

      "must return a summary list row with a link to enter Vendor type" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = VendorTypeSummary.row(Some(emptyUserAnswers))

          result.key.content.asHtml.toString() mustEqual msgs("vendor.checkYourAnswers.vendorType.label")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent must include("govuk-link")
          htmlContent must include(controllers.vendor.routes.WhoIsTheVendorController.onPageLoad(CheckMode).url)
          htmlContent must include(msgs("vendor.checkYourAnswers.vendorTypeMissing"))

          result.actions mustBe None
        }
      }

      "must return a summary list row with a link when UserAnswers is None" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = VendorTypeSummary.row(None)

          result.key.content.asHtml.toString() mustEqual msgs("vendor.checkYourAnswers.vendorType.label")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent must include("govuk-link")
          htmlContent must include(controllers.vendor.routes.WhoIsTheVendorController.onPageLoad(CheckMode).url)
          htmlContent must include(msgs("vendor.checkYourAnswers.vendorTypeMissing"))

          result.actions mustBe None
        }
      }
    }

    "must use CheckMode for the change link" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers
          .set(WhoIsTheVendorPage, whoIsTheVendor.Company).success.value

        val result = VendorTypeSummary.row(Some(userAnswers))

        result.actions.get.items.head.href mustEqual controllers.vendor.routes.WhoIsTheVendorController.onPageLoad(CheckMode).url
      }
    }
  }
}