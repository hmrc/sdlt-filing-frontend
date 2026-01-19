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

package viewmodels.checkAnswers.purchaser

import base.SpecBase
import models.CheckMode
import pages.purchaser.EnterPurchaserPhoneNumberPage
import play.api.i18n.Messages
import play.api.test.Helpers.running


class EnterPurchaserPhoneNumberSummarySpec extends SpecBase {

  "EnterPurchaserPhoneNumberSummary" - {

    "must return a SummaryListRow when EnterPurchaserPhoneNumberPage is set and change link" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers.set(EnterPurchaserPhoneNumberPage, "123456789").success.value

          val result = EnterPurchaserPhoneNumberSummary.row(userAnswers).getOrElse(fail("Failed to get summary list row"))

          result.key.content.asHtml.toString() mustEqual msgs("enterPurchaserPhoneNumber.checkYourAnswersLabel")

          val contentString = result.value.content.asHtml.toString()

          contentString mustEqual "123456789"

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.purchaser.routes.EnterPurchaserPhoneNumberController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("enterPurchaserPhoneNumber.change.hidden")
        }
    }

    "must return None when AddVendorAgentContactDetailsPage is not answered" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        EnterPurchaserPhoneNumberSummary.row(emptyUserAnswers) mustBe None
      }
    }
  }
}

