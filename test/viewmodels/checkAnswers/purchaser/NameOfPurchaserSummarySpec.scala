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

package viewmodels.checkAnswers.purchaser

import base.SpecBase
import models.CheckMode
import models.purchaser.NameOfPurchaser
import pages.purchaser.NameOfPurchaserPage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent

class NameOfPurchaserSummarySpec extends SpecBase {

  "NameOfPurchaserSummary" - {

    "when purchaser is an individual with full name" - {

      "must return a summary list row with surname only" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
        running(application) {
          implicit val msgs: Messages = messages(application)

          val nameOfPurchaser = NameOfPurchaser(
            forename1 = Some("John"),
            forename2 = Some("Middle"),
            name = "Doe"
          )

          val userAnswers = emptyUserAnswers
            .set(NameOfPurchaserPage, nameOfPurchaser).success.value

          val result = NameOfPurchaserSummary.row(Some(userAnswers))

          result.key.content.asHtml.toString() mustEqual msgs("purchaser.nameOfThePurchaser.checkYourAnswersLabel")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent mustEqual "John Middle Doe"

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("purchaser.nameOfThePurchaser.change.hidden")
        }
      }
    }

    "when purchaser is an individual without middle name" - {

      "must return a summary list row with surname only" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
        running(application) {
          implicit val msgs: Messages = messages(application)

          val nameOfPurchaser = NameOfPurchaser(
            forename1 = Some("Jane"),
            forename2 = None,
            name = "Smith"
          )

          val userAnswers = emptyUserAnswers
            .set(NameOfPurchaserPage, nameOfPurchaser).success.value

          val result = NameOfPurchaserSummary.row(Some(userAnswers))

          result.key.content.asHtml.toString() mustEqual msgs("purchaser.nameOfThePurchaser.checkYourAnswersLabel")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent mustEqual "Jane Smith"

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("purchaser.nameOfThePurchaser.change.hidden")
        }
      }
    }

    "when purchaser is a company" - {

      "must return a summary list row with company name" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
        running(application) {
          implicit val msgs: Messages = messages(application)

          val nameOfPurchaser = NameOfPurchaser(
            forename1 = None,
            forename2 = None,
            name = "ACME Corporation"
          )

          val userAnswers = emptyUserAnswers
            .set(NameOfPurchaserPage, nameOfPurchaser).success.value

          val result = NameOfPurchaserSummary.row(Some(userAnswers))

          result.key.content.asHtml.toString() mustEqual msgs("purchaser.nameOfThePurchaser.checkYourAnswersLabel")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent mustEqual "ACME Corporation"

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("purchaser.nameOfThePurchaser.change.hidden")
        }
      }
    }

    "when name of purchaser is not present" - {

      "must return None" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = NameOfPurchaserSummary.row(Some(emptyUserAnswers))

          result.key.content.asHtml.toString() mustEqual msgs("purchaser.nameOfThePurchaser.checkYourAnswersLabel")
        }
      }
    }

    "must properly escape special characters in name" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        implicit val msgs: Messages = messages(application)

        val nameOfPurchaser = NameOfPurchaser(
          forename1 = None,
          forename2 = None,
          name = "O'Brien & Sons <Ltd>"
        )

        val userAnswers = emptyUserAnswers
          .set(NameOfPurchaserPage, nameOfPurchaser).success.value

        val result = NameOfPurchaserSummary.row(Some(userAnswers))

        val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
        htmlContent must include("O&#x27;Brien")
        htmlContent must include("&amp;")
        htmlContent must include("&lt;")
        htmlContent must include("&gt;")
      }
    }

    "must use CheckMode for the change link" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        implicit val msgs: Messages = messages(application)

        val nameOfPurchaser = NameOfPurchaser(
          forename1 = Some("John"),
          forename2 = None,
          name = "Doe"
        )

        val userAnswers = emptyUserAnswers
          .set(NameOfPurchaserPage, nameOfPurchaser).success.value

        val result = NameOfPurchaserSummary.row(Some(userAnswers))

        result.actions.get.items.head.href mustEqual controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(CheckMode).url
      }
    }

    "must handle names with only surname" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        implicit val msgs: Messages = messages(application)

        val nameOfPurchaser = NameOfPurchaser(
          forename1 = None,
          forename2 = None,
          name = "test"
        )

        val userAnswers = emptyUserAnswers
          .set(NameOfPurchaserPage, nameOfPurchaser).success.value

        val result = NameOfPurchaserSummary.row(Some(userAnswers))

        val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
        htmlContent mustEqual "test"
      }
    }
  }
}