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

package viewmodels.checkAnswers.preliminary

import base.SpecBase
import controllers.preliminary.routes
import models.address.{Address, Country}
import pages.preliminary.PurchaserAddressPage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.checkAnswers.summary.SummaryRowResult.{ Row, Missing }

class PrelimAddressSummarySpec extends SpecBase {

  "PrelimAddressSummary" - {

    "when address data is present" - {

      "must return a summary list row with formatted address and change link" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val address = Address(
            line1 = "123 Test Street",
            line2 = Some("Test Area"),
            line3 = Some("Test Town"),
            line4 = Some("Test County"),
            line5 = None,
            postcode = Some("AA1 1AA"),
            country = Some(Country(Some("GB"), Some("United Kingdom")))
          )

          val userAnswers = emptyUserAnswers.set(PurchaserAddressPage, address).success.value

          val row = PrelimAddressSummary.row(Some(userAnswers))

          val result = row match{
            case Row(r) => r
            case _ => fail("Expected Row but got Missing")
          }

          result.key.content.asHtml.toString() mustEqual msgs("purchaser.address.checkYourAnswersLabel")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent must include("123 Test Street")
          htmlContent must include("Test Area")
          htmlContent must include("Test Town")
          htmlContent must include("Test County")
          htmlContent must include("AA1 1AA")
          htmlContent must include("United Kingdom")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual routes.PrelimAddressController.redirectToAddressLookup(Some("change")).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("purchaser.address.change.hidden")
        }
      }

      "must format address correctly with only mandatory fields" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val address = Address(
            line1 = "123 Test Street",
            line2 = None,
            line3 = None,
            line4 = None,
            line5 = None,
            postcode = None,
            country = None
          )

          val userAnswers = emptyUserAnswers.set(PurchaserAddressPage, address).success.value

          val row = PrelimAddressSummary.row(Some(userAnswers))

          val result = row match {
            case Row(r) => r
            case _ => fail("Expected Row but got Missing")
          }

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent mustEqual "123 Test Street"
        }
      }

      "must filter out None values from address display" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val address = Address(
            line1 = "123 Test Street",
            line2 = Some("Test Area"),
            line3 = None,
            line4 = Some("Test County"),
            line5 = None,
            postcode = Some("AA1 1AA"),
            country = None
          )

          val userAnswers = emptyUserAnswers.set(PurchaserAddressPage, address).success.value

          val row = PrelimAddressSummary.row(Some(userAnswers))

          val result = row match{
            case Row(r) => r
            case _ => fail("Expected Row but got Missing")
          }

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent mustEqual "123 Test Street, Test Area, Test County, AA1 1AA"
        }
      }

      "must extract country name from Country object" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val address = Address(
            line1 = "123 Test Street",
            line2 = None,
            line3 = None,
            line4 = None,
            line5 = None,
            postcode = None,
            country = Some(Country(Some("FR"), Some("France")))
          )

          val userAnswers = emptyUserAnswers.set(PurchaserAddressPage, address).success.value

          val row = PrelimAddressSummary.row(Some(userAnswers))

          val result = row match{
            case Row(r) => r
            case _ => fail("Expected Row but got Missing")
          }

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent must include("France")
          htmlContent mustNot include("FR")
        }
      }

      "must properly escape special characters in address" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val address = Address(
            line1 = "123 Test & Street <script>",
            line2 = None,
            line3 = None,
            line4 = None,
            line5 = None,
            postcode = None,
            country = None
          )

          val userAnswers = emptyUserAnswers.set(PurchaserAddressPage, address).success.value

          val row = PrelimAddressSummary.row(Some(userAnswers))

          val result = row match{
            case Row(r) => r
            case _ => fail("Expected Row but got Missing")
          }

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent must include("&amp;")
          htmlContent must include("&lt;")
          htmlContent must include("&gt;")
        }
      }
    }

    "when address data is not present" - {

      "must return a Missing and redirect call to missing page when address data is missing" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = PrelimAddressSummary.row(Some(emptyUserAnswers))

          result match {
            case Missing(call) =>
              call mustEqual controllers.preliminary.routes.PrelimAddressController.redirectToAddressLookup(Some("change"))

            case Row(_) =>
              fail("Expected Missing but got Row")
          }
        }
      }

      "must return a Missing and redirect call to missing page when UserAnswers is None" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = PrelimAddressSummary.row(None)

          result match {
            case Missing(call) =>
              call mustEqual controllers.preliminary.routes.PrelimAddressController.redirectToAddressLookup(Some("change"))

            case Row(_) =>
              fail("Expected Missing but got Row")
          }
        }
      }
    }

    "must handle all address fields populated" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val address = Address(
          line1 = "123 Test Street",
          line2 = Some("Apartment 4B"),
          line3 = Some("Test Area"),
          line4 = Some("Test Town"),
          line5 = Some("Test County"),
          postcode = Some("AA1 1AA"),
          country = Some(Country(Some("GB"), Some("United Kingdom")))
        )

        val userAnswers = emptyUserAnswers.set(PurchaserAddressPage, address).success.value

        val row = PrelimAddressSummary.row(Some(userAnswers))

        val result = row match{
          case Row(r) => r
          case _ => fail("Expected Row but got Missing")
        }

        val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
        htmlContent must include("123 Test Street")
        htmlContent must include("Apartment 4B")
        htmlContent must include("Test Area")
        htmlContent must include("Test Town")
        htmlContent must include("Test County")
        htmlContent must include("AA1 1AA")
        htmlContent must include("United Kingdom")
      }
    }
  }
}