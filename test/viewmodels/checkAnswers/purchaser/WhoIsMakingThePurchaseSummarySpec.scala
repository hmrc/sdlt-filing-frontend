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
import models.purchaser.WhoIsMakingThePurchase
import pages.purchaser.WhoIsMakingThePurchasePage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent

class WhoIsMakingThePurchaseSummarySpec extends SpecBase {

  "WhoIsMakingThePurchaseSummary" - {

    "when purchaser type is Individual" - {

      "must return a summary list row with Individual label" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Individual).success.value

          val result = WhoIsMakingThePurchaseSummary.row(userAnswers).getOrElse(fail("Failed to get summary list row"))

          result.key.content.asHtml.toString() mustEqual msgs("purchaser.whoIsMakingThePurchase.checkYourAnswersLabel")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent mustEqual msgs("purchaser.whoIsMakingThePurchase.Individual")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.purchaser.routes.WhoIsMakingThePurchaseController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("purchaser.whoIsMakingThePurchase.change.hidden")
        }
      }
    }

    "when purchaser type is Company" - {

      "must return a summary list row with Company label" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Company).success.value

          val result = WhoIsMakingThePurchaseSummary.row(userAnswers).getOrElse(fail("Failed to get summary list row"))

          result.key.content.asHtml.toString() mustEqual msgs("purchaser.whoIsMakingThePurchase.checkYourAnswersLabel")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent mustEqual msgs("purchaser.whoIsMakingThePurchase.Company")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.purchaser.routes.WhoIsMakingThePurchaseController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("purchaser.whoIsMakingThePurchase.change.hidden")
        }
      }
    }

    "when purchaser type is not present" - {

      "must return None" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = WhoIsMakingThePurchaseSummary.row(emptyUserAnswers)

          result mustBe None
        }
      }
    }

    "must use CheckMode for the change link" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers
          .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Individual).success.value

        val result = WhoIsMakingThePurchaseSummary.row(userAnswers).getOrElse(fail("Failed to get summary list row"))

        result.actions.get.items.head.href mustEqual controllers.purchaser.routes.WhoIsMakingThePurchaseController.onPageLoad(CheckMode).url
      }
    }

    "must properly escape messages in value" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers
          .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Company).success.value

        val result = WhoIsMakingThePurchaseSummary.row(userAnswers).getOrElse(fail("Failed to get summary list row"))

        val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
        htmlContent must not include "<script>"
        htmlContent must not include "&"
      }
    }

    "must handle both enum values correctly" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        implicit val msgs: Messages = messages(application)

        val individualAnswers = emptyUserAnswers
          .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Individual).success.value

        val companyAnswers = emptyUserAnswers
          .set(WhoIsMakingThePurchasePage, WhoIsMakingThePurchase.Company).success.value

        val individualResult = WhoIsMakingThePurchaseSummary.row(individualAnswers).getOrElse(fail("Failed to get summary list row"))
        val companyResult = WhoIsMakingThePurchaseSummary.row(companyAnswers).getOrElse(fail("Failed to get summary list row"))

        val individualContent = individualResult.value.content.asInstanceOf[HtmlContent].asHtml.toString()
        val companyContent = companyResult.value.content.asInstanceOf[HtmlContent].asHtml.toString()

        individualContent mustEqual msgs("purchaser.whoIsMakingThePurchase.Individual")
        companyContent mustEqual msgs("purchaser.whoIsMakingThePurchase.Company")

        individualContent must not equal companyContent
      }
    }
  }
}