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
import models.CheckMode
import models.vendor.{VendorName, whoIsTheVendor}
import pages.vendor.{VendorOrCompanyNamePage, WhoIsTheVendorPage}
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent

class IndividualOrCompanyNameSummarySpec extends SpecBase {

  "IndividualOrCompanyNameSummary" - {

    "when name is present" - {

      "must return a summary list row with agent name label" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)
          val vendorName = VendorName(None, None, "Doe")

          val userAnswers = emptyUserAnswers.set(VendorOrCompanyNamePage, vendorName).success.value

          val result = IndividualOrCompanyNameSummary.row(Some(userAnswers))

          result.key.content.asHtml.toString() mustEqual msgs("vendor.checkYourAnswers.vendorName.label")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent mustEqual "Doe"

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.vendor.routes.VendorOrCompanyNameController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("vendor.checkYourAnswers.vendorName.hidden")
        }
      }

      "must return a summary list row with a link to enter name when userAnswers is empty" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = IndividualOrCompanyNameSummary.row(Some(emptyUserAnswers))

          result.key.content.asHtml.toString() mustEqual msgs("vendor.checkYourAnswers.vendorName.label")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent must include("govuk-link")
          htmlContent must include(controllers.vendor.routes.VendorOrCompanyNameController.onPageLoad(CheckMode).url)
          htmlContent must include(msgs("vendor.checkYourAnswers.vendorName.nameMissing"))

          result.actions mustBe None
        }
      }
    }

    "must return a summary list when vendor is Individual and display first & last name correctly" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      val vendorName = VendorName(Some("John"), None, "Doe")
      
      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers
          .set(WhoIsTheVendorPage, whoIsTheVendor.Individual).success.value
         .set(VendorOrCompanyNamePage, vendorName).success.value
        

        val result = IndividualOrCompanyNameSummary.row(Some(userAnswers))

        result.key.content.asHtml.toString() mustEqual msgs("vendor.checkYourAnswers.vendorName.label")

        val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
        htmlContent mustEqual "John Doe"

        result.actions.get.items.size mustEqual 1
        result.actions.get.items.head.href mustEqual controllers.vendor.routes.VendorOrCompanyNameController.onPageLoad(CheckMode).url
        result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
        result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("vendor.checkYourAnswers.vendorName.hidden")
      }
    }

    "must return a summary list when vendor is Individual and display first, second & last name correctly" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      val vendorName = VendorName(Some("John"), Some("Smith"), "Doe")

      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers
          .set(WhoIsTheVendorPage, whoIsTheVendor.Individual).success.value
          .set(VendorOrCompanyNamePage, vendorName).success.value


        val result = IndividualOrCompanyNameSummary.row(Some(userAnswers))

        result.key.content.asHtml.toString() mustEqual msgs("vendor.checkYourAnswers.vendorName.label")

        val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
        htmlContent mustEqual "John Smith Doe"

        result.actions.get.items.size mustEqual 1
        result.actions.get.items.head.href mustEqual controllers.vendor.routes.VendorOrCompanyNameController.onPageLoad(CheckMode).url
        result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("vendor.checkYourAnswers.vendorName.hidden")
      }
    }

    "must return a summary list when vendor is Individual and display second & last name correctly" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      val vendorName = VendorName(None, Some("Smith"), "Doe")

      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers
          .set(WhoIsTheVendorPage, whoIsTheVendor.Individual).success.value
          .set(VendorOrCompanyNamePage, vendorName).success.value


        val result = IndividualOrCompanyNameSummary.row(Some(userAnswers))

        result.key.content.asHtml.toString() mustEqual msgs("vendor.checkYourAnswers.vendorName.label")

        val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
        htmlContent mustEqual "Smith Doe"

        result.actions.get.items.size mustEqual 1
        result.actions.get.items.head.href mustEqual controllers.vendor.routes.VendorOrCompanyNameController.onPageLoad(CheckMode).url
        result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("vendor.checkYourAnswers.vendorName.hidden")
      }
    }

    "must return a summary list when vendor is Company and display company name correctly" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      val vendorName = VendorName(None, None, "Bank plc")

      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers
          .set(WhoIsTheVendorPage, whoIsTheVendor.Company).success.value
          .set(VendorOrCompanyNamePage, vendorName).success.value


        val result = IndividualOrCompanyNameSummary.row(Some(userAnswers))

        result.key.content.asHtml.toString() mustEqual msgs("vendor.checkYourAnswers.vendorName.label")

        val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
        htmlContent mustEqual "Bank plc"

        result.actions.get.items.size mustEqual 1
        result.actions.get.items.head.href mustEqual controllers.vendor.routes.VendorOrCompanyNameController.onPageLoad(CheckMode).url
        result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("vendor.checkYourAnswers.vendorName.hidden")
      }
    }

    "must use CheckMode for the change link" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      val vendorName = VendorName(None, None, "Brown")
      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers
          .set(VendorOrCompanyNamePage, vendorName).success.value

        val result = IndividualOrCompanyNameSummary.row(Some(userAnswers))

        result.actions.get.items.head.href mustEqual controllers.vendor.routes.VendorOrCompanyNameController.onPageLoad(CheckMode).url
      }
    }
  }
}