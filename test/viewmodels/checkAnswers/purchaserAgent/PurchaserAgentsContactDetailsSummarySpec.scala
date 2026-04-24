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

package viewmodels.checkAnswers.purchaserAgent

import base.SpecBase
import models.CheckMode
import models.purchaserAgent.PurchaserAgentsContactDetails
import pages.purchaserAgent.{AddContactDetailsForPurchaserAgentPage, PurchaserAgentsContactDetailsPage}
import play.api.i18n.Messages
import play.api.test.Helpers.running
import viewmodels.checkAnswers.summary.SummaryRowResult.{ Row, Missing }

class PurchaserAgentsContactDetailsSummarySpec extends SpecBase {

  "PurchaserAgentsContactDetailsSummary" - {

    "when contact details are present" - {

      "must return a summary list row with phone number only" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(
              PurchaserAgentsContactDetailsPage,
              PurchaserAgentsContactDetails(
                phoneNumber = Some("0123456789"),
                emailAddress = None
              )
            ).success.value

          val row =
            PurchaserAgentsContactDetailsSummary.row(userAnswers)
              .getOrElse(fail("Failed to get summary list row"))

          val result = row match {
            case Row(r) => r
            case _ => fail("Expected Row but got Missing")
          }

          result.key.content.asHtml.toString() mustEqual
            msgs("purchaserAgent.contactDetails.checkYourAnswersLabel")

          result.value.content.asHtml.toString() mustEqual "Tel: 0123456789"

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual
            controllers.purchaserAgent.routes.PurchaserAgentsContactDetailsController
              .onPageLoad(CheckMode).url

          result.actions.get.items.head.content.asHtml.toString() must include(
            msgs("site.change")
          )

          result.actions.get.items.head.visuallyHiddenText.value mustEqual
            msgs("purchaserAgent.contactDetails.change.hidden")
        }
      }

      "must return a summary list row with email address only" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(
              PurchaserAgentsContactDetailsPage,
              PurchaserAgentsContactDetails(
                phoneNumber = None,
                emailAddress = Some("test@example.com")
              )
            ).success.value

          val row =
            PurchaserAgentsContactDetailsSummary.row(userAnswers)
              .getOrElse(fail("Failed to get summary list row"))

          val result = row match {
            case Row(r) => r
            case _ => fail("Expected Row but got Missing")
          }

          result.value.content.asHtml.toString() mustEqual "Email: test@example.com"
        }
      }

      "must return a summary list row with phone number and email address separated by a line break" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(
              PurchaserAgentsContactDetailsPage,
              PurchaserAgentsContactDetails(
                phoneNumber = Some("0123456789"),
                emailAddress = Some("test@example.com")
              )
            ).success.value

          val row =
            PurchaserAgentsContactDetailsSummary.row(userAnswers)
              .getOrElse(fail("Failed to get summary list row"))

          val result = row match {
            case Row(r) => r
            case _ => fail("Expected Row but got Missing")
          }

          result.value.content.asHtml.toString() mustEqual
            "Tel: 0123456789<br/>Email: test@example.com"
        }
      }

      "must return a summary list row with an empty string when email and phone number are not set" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(
              PurchaserAgentsContactDetailsPage,
              PurchaserAgentsContactDetails(
                phoneNumber = None,
                emailAddress = None
              )
            ).success.value

          val row =
            PurchaserAgentsContactDetailsSummary.row(userAnswers)
              .getOrElse(fail("Failed to get summary list row"))

          val result = row match {
            case Row(r) => r
            case _ => fail("Expected Row but got Missing")
          }

          result.value.content.asHtml.toString() mustEqual ""
        }
      }
    }

    "when contact details are not present" - {

      "must return a Missing and redirect call to missing page when add contact details is true but contact details are missing" in {
        val userAnswers = emptyUserAnswers.set(AddContactDetailsForPurchaserAgentPage, true).success.value
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = PurchaserAgentsContactDetailsSummary.row(userAnswers).getOrElse(fail("Failed to get summary list row"))

          result match {
            case Missing(call) =>
              call mustEqual controllers.purchaserAgent.routes.PurchaserAgentsContactDetailsController.onPageLoad(CheckMode)

            case Row(_) =>
              fail("Expected Missing but got Row")
          }
        }
      }

      "must return None when add contact details is false" in {
        val userAnswers = emptyUserAnswers.set(AddContactDetailsForPurchaserAgentPage, false).success.value
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          PurchaserAgentsContactDetailsSummary.row(emptyUserAnswers) mustBe None
        }
      }
    }
  }
}
