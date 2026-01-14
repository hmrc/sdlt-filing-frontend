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
import pages.purchaserAgent.AddContactDetailsForPurchaserAgentPage
import play.api.i18n.Messages
import play.api.test.Helpers.running


class AddContactDetailsForPurchaserAgentSpec extends SpecBase {

  "AddContactDetailsForPurchaserAgentSpec" - {

    "must return a SummaryListRow with 'yes' text and change link" in {
            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

            running(application) {
              implicit val msgs: Messages = messages(application)

              val userAnswers = emptyUserAnswers.set(AddContactDetailsForPurchaserAgentPage, true).success.value

              val result = AddContactDetailsForPurchaserAgentSummary.row(userAnswers).getOrElse(fail("Failed to get summary list row"))

              result.key.content.asHtml.toString() mustEqual msgs("purchaserAgent.addContactDetailsForPurchaserAgent.checkYourAnswersLabel")

              val contentString = result.value.content.asHtml.toString()

              contentString mustEqual msgs("purchaserAgent.addContactDetailsForPurchaserAgent.true")

              result.actions.get.items.size mustEqual 1
              result.actions.get.items.head.href mustEqual controllers.purchaserAgent.routes.AddContactDetailsForPurchaserAgentController.onPageLoad(CheckMode).url
              result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
              result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("purchaserAgent.addContactDetailsForPurchaserAgent.change.hidden")
            }
          }

    "must return a SummaryListRow with 'no' text and change link" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers.set(AddContactDetailsForPurchaserAgentPage, false).success.value

        val result = AddContactDetailsForPurchaserAgentSummary.row(userAnswers).getOrElse(fail("Failed to get summary list row"))

        result.key.content.asHtml.toString() mustEqual msgs("purchaserAgent.addContactDetailsForPurchaserAgent.checkYourAnswersLabel")

        val contentString = result.value.content.asHtml.toString()

        contentString mustEqual "purchaserAgent.addContactDetailsForPurchaserAgent.false"

        result.actions.get.items.size mustEqual 1

        result.actions.get.items.head.href mustEqual controllers.purchaserAgent.routes.AddContactDetailsForPurchaserAgentController.onPageLoad(CheckMode).url
        result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
        result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("purchaserAgent.addContactDetailsForPurchaserAgent.change.hidden")
      }
    }
  }
}

