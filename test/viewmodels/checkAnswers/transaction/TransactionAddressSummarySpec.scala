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

package viewmodels.checkAnswers.transaction

import base.SpecBase
import models.address.{Address, Country}
import pages.transaction.TransactionAddressPage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import viewmodels.checkAnswers.summary.SummaryRowResult.{Row, Missing}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent


class TransactionAddressSummarySpec extends SpecBase {

  "TransactionAddressSummary" - {

    "when address data is present" - {

      "must return a summary list row with formatted address and change link" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val address = Address(
            line1 = "1 Test Street",
            line2 = Some("Test Area"),
            line3 = Some("Test Town"),
            line4 = Some("Test County"),
            line5 = None,
            postcode = Some("AB1 2CD"),
            country = Some(Country(Some("GB"), Some("United Kingdom")))
          )

          val userAnswers = emptyUserAnswers.set(TransactionAddressPage, address).success.value

          val row = TransactionAddressSummary.row(userAnswers)

          val result = row match {
            case Row(r) => r
            case _ => fail("Expected Row but got Missing")
          }

          result.key.content.asHtml.toString() mustEqual msgs("transaction.address.checkYourAnswersLabel")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent must include("1 Test Street")
          htmlContent must include("Test Area")
          htmlContent must include("Test Town")
          htmlContent must include("Test County")
          htmlContent must include("AB1 2CD")
          htmlContent must include("United Kingdom")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.transaction.routes.TransactionAddressController.redirectToAddressLookupTransaction(Some("change")).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("transaction.address.change.hidden")
        }
      }

      "must format address correctly with only mandatory fields" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val address = Address(
            line1 = "1 Test Street",
            line2 = None,
            line3 = None,
            line4 = None,
            line5 = None,
            postcode = None,
            country = None
          )

          val userAnswers = emptyUserAnswers.set(TransactionAddressPage, address).success.value

          val row = TransactionAddressSummary.row(userAnswers)
          val result = row match {
            case Row(r) => r
            case _ => fail("Expected Row but got Missing")
          }

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent must include("1 Test Street")
        }
      }
    }

    "when address data is not present" - {

      "must return a summary list row with a missing link" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val row = TransactionAddressSummary.row(emptyUserAnswers)

          row match {
            case Missing(call) =>
              call mustEqual controllers.transaction.routes.TransactionAddressController.redirectToAddressLookupTransaction(Some("change"))

            case Row(_) =>
              fail("Expected Missing but got Row")
          }
        }
      }
    }
  }
}