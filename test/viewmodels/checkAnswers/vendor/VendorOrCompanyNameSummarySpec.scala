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
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}

class VendorOrCompanyNameSummarySpec extends SpecBase {

  "IndividualOrCompanyNameSummary" - {

    "when name is present" - {

      "must return a summary list row with agent name label" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)
          val vendorName = VendorName(None, None, "Doe")

          val userAnswers = emptyUserAnswers.set(VendorOrCompanyNamePage, vendorName).success.value

          val row = VendorOrCompanyNameSummary.row(Some(userAnswers))
          val result = row match {
            case Row(r) => r
            case _ => fail("Expected Row but got Missing")
          }

          result.key.content.asHtml.toString() mustEqual msgs("vendor.checkYourAnswers.vendorName.label")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent mustEqual "Doe"

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.vendor.routes.VendorOrCompanyNameController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("vendor.checkYourAnswers.vendorName.hidden")
        }
      }

      "must return a Missing and redirect call to missing page when data is not present" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = VendorOrCompanyNameSummary.row(Some(emptyUserAnswers))

          result match {
            case Missing(call) =>
              call mustEqual controllers.vendor.routes.VendorOrCompanyNameController.onPageLoad(CheckMode)

            case Row(_) =>
              fail("Expected Missing but got Row")
          }
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
        

        val row = VendorOrCompanyNameSummary.row(Some(userAnswers))
        val result = row match {
          case Row(r) => r
          case _ => fail("Expected Row but got Missing")
        }

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


        val row = VendorOrCompanyNameSummary.row(Some(userAnswers))
        val result = row match {
          case Row(r) => r
          case _ => fail("Expected Row but got Missing")
        }

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


        val row = VendorOrCompanyNameSummary.row(Some(userAnswers))
        val result = row match {
          case Row(r) => r
          case _ => fail("Expected Row but got Missing")
        }

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


        val row = VendorOrCompanyNameSummary.row(Some(userAnswers))
        val result = row match {
          case Row(r) => r
          case _ => fail("Expected Row but got Missing")
        }

        result.key.content.asHtml.toString() mustEqual msgs("vendor.checkYourAnswers.vendorName.label")

        val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
        htmlContent mustEqual "Bank plc"

        result.actions.get.items.size mustEqual 1
        result.actions.get.items.head.href mustEqual controllers.vendor.routes.VendorOrCompanyNameController.onPageLoad(CheckMode).url
        result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("vendor.checkYourAnswers.vendorName.hidden")
      }
    }
  }
}