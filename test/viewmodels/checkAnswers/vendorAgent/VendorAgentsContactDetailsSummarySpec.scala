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

package viewmodels.checkAnswers.vendorAgent

import base.SpecBase
import models.CheckMode
import models.vendorAgent.VendorAgentsContactDetails
import pages.vendorAgent.VendorAgentsContactDetailsPage
import play.api.i18n.Messages
import play.api.test.Helpers.running

class VendorAgentsContactDetailsSummarySpec extends SpecBase {

  "VendorAgentsContactDetailsSummary" - {

    "when both phone number and email address are present" - {

      "must return a summary list row with phone number and email address" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(VendorAgentsContactDetailsPage, VendorAgentsContactDetails(Some("123456"), Some("test@example.com"))).success.value

          val result = VendorAgentsContactDetailsSummary.row(Some(userAnswers))

          result.key.content.asHtml.toString() mustEqual msgs("vendorAgent.vendorAgentsContactDetails.checkYourAnswersLabel")

          val valueHtml = result.value.content.asHtml.toString()
          valueHtml must include("123456")
          valueHtml must include("<br/>")
          valueHtml must include("test@example.com")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.vendorAgent.routes.VendorAgentsContactDetailsController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("vendorAgent.vendorAgentsContactDetails.change.hidden")
        }
      }

      "must use CheckMode for the change link" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(VendorAgentsContactDetailsPage, VendorAgentsContactDetails(Some("123456"), Some("test@example.com"))).success.value

          val result = VendorAgentsContactDetailsSummary.row(Some(userAnswers))

          result.actions.get.items.head.href mustEqual controllers.vendorAgent.routes.VendorAgentsContactDetailsController.onPageLoad(CheckMode).url
        }
      }
    }

    "when only phone number is present" - {

      "must return a summary list row with only phone number" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(VendorAgentsContactDetailsPage, VendorAgentsContactDetails(Some("123456"), None)).success.value

          val result = VendorAgentsContactDetailsSummary.row(Some(userAnswers))

          result.key.content.asHtml.toString() mustEqual msgs("vendorAgent.vendorAgentsContactDetails.checkYourAnswersLabel")

          val valueHtml = result.value.content.asHtml.toString()
          valueHtml must include("123456")
          valueHtml mustNot include("<br/>")
          valueHtml mustNot include("test@example.com")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.vendorAgent.routes.VendorAgentsContactDetailsController.onPageLoad(CheckMode).url
        }
      }
    }

    "when only email address is present" - {

      "must return a summary list row with only email address" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(VendorAgentsContactDetailsPage, VendorAgentsContactDetails(None, Some("test@example.com"))).success.value

          val result = VendorAgentsContactDetailsSummary.row(Some(userAnswers))

          result.key.content.asHtml.toString() mustEqual msgs("vendorAgent.vendorAgentsContactDetails.checkYourAnswersLabel")

          val valueHtml = result.value.content.asHtml.toString()
          valueHtml must include("test@example.com")
          valueHtml mustNot include("<br/>")
          valueHtml mustNot include("123456")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.vendorAgent.routes.VendorAgentsContactDetailsController.onPageLoad(CheckMode).url
        }
      }
    }

    "when both fields are None" - {

      "must return a summary list row with dash" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(VendorAgentsContactDetailsPage, VendorAgentsContactDetails(None, None)).success.value

          val result = VendorAgentsContactDetailsSummary.row(Some(userAnswers))

          result.key.content.asHtml.toString() mustEqual msgs("vendorAgent.vendorAgentsContactDetails.checkYourAnswersLabel")

          val valueHtml = result.value.content.asHtml.toString()
          valueHtml must include("-")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.vendorAgent.routes.VendorAgentsContactDetailsController.onPageLoad(CheckMode).url
        }
      }
    }

    "when contact details page is not answered" - {

      "must return a summary list row with missing link" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = VendorAgentsContactDetailsSummary.row(Some(emptyUserAnswers))

          result.key.content.asHtml.toString() mustEqual msgs("vendorAgent.vendorAgentsContactDetails.checkYourAnswersLabel")

          val valueHtml = result.value.content.asHtml.toString()
          valueHtml must include(controllers.vendorAgent.routes.VendorAgentsContactDetailsController.onPageLoad(CheckMode).url)
          valueHtml must include(msgs("vendorAgent.checkYourAnswers.agentContactDetails.agentDetailsMissing"))
          valueHtml must include("govuk-link")


          result.actions mustBe None
        }
      }
    }

    "when userAnswers is None" - {

      "must return a summary list row with missing link" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = VendorAgentsContactDetailsSummary.row(None)

          result.key.content.asHtml.toString() mustEqual msgs("vendorAgent.vendorAgentsContactDetails.checkYourAnswersLabel")

          val valueHtml = result.value.content.asHtml.toString()
          valueHtml must include(controllers.vendorAgent.routes.VendorAgentsContactDetailsController.onPageLoad(CheckMode).url)
          valueHtml must include(msgs("vendorAgent.checkYourAnswers.agentContactDetails.agentDetailsMissing"))
        }
      }
    }
  }
}