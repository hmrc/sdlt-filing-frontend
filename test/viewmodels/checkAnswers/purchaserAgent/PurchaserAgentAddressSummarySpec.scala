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

package viewmodels.checkAnswers.purchaserAgent

import base.SpecBase
import models.address.{Address, Country}
import pages.purchaserAgent.PurchaserAgentAddressPage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}

class PurchaserAgentAddressSummarySpec extends SpecBase {

  "PurchaserAgentAddressSummary" - {

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

          val userAnswers = emptyUserAnswers.set(PurchaserAgentAddressPage, address).success.value

          val row = PurchaserAgentAddressSummary.row(userAnswers)

          val result = row match {
            case Row(r) => r
            case _ => fail("Expected Row but got Missing")
          }

          result.key.content.asHtml.toString() mustEqual msgs("purchaserAgent.address.checkYourAnswersLabel")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent must include("123 Test Street")
          htmlContent must include("Test Area")
          htmlContent must include("Test Town")
          htmlContent must include("Test County")
          htmlContent must include("AA1 1AA")
          htmlContent must include("United Kingdom")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.purchaserAgent.routes.PurchaserAgentAddressController.redirectToAddressLookupPurchaserAgent(Some("change")).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("purchaserAgent.address.change.hidden")
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

          val userAnswers = emptyUserAnswers.set(PurchaserAgentAddressPage, address).success.value

          val row = PurchaserAgentAddressSummary.row(userAnswers)

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

          val userAnswers = emptyUserAnswers.set(PurchaserAgentAddressPage, address).success.value

          val row = PurchaserAgentAddressSummary.row(userAnswers)

          val result = row match {
            case Row(r) => r
            case _ => fail("Expected Row but got Missing")
          }

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent mustEqual "123 Test Street<br>Test Area<br>Test County<br>AA1 1AA"
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

          val userAnswers = emptyUserAnswers.set(PurchaserAgentAddressPage, address).success.value

          val row = PurchaserAgentAddressSummary.row(userAnswers)

          val result = row match {
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

          val userAnswers = emptyUserAnswers.set(PurchaserAgentAddressPage, address).success.value

          val row = PurchaserAgentAddressSummary.row(userAnswers)

          val result = row match {
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

      "must return a Missing and redirect call to missing page when data is not present" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = PurchaserAgentAddressSummary.row(emptyUserAnswers)

          result match {
            case Missing(call) =>
              call mustEqual controllers.purchaserAgent.routes.PurchaserAgentAddressController.redirectToAddressLookupPurchaserAgent(Some("change"))

            case Row(_) =>
              fail("Expected Missing but got Row")
          }
        }
      }
    }
  }
}