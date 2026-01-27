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
import models.purchaser.PurchaserFormOfIdIndividual
import pages.purchaser.PurchaserFormOfIdIndividualPage
import play.api.i18n.Messages
import play.api.test.Helpers.running

class PurchaserFormOfIdIndividualSummarySpec extends SpecBase {

  "PurchaserFormOfIdIndividualSummary" - {

    "when ID number and country issued are present" - {

      "must return a summary list row with ID number and country issued" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(PurchaserFormOfIdIndividualPage, PurchaserFormOfIdIndividual("123456", "Germany")).success.value

          val result = PurchaserFormOfIdIndividualSummary.row(Some(userAnswers))

          result.key.content.asHtml.toString() mustEqual msgs("purchaser.formOfIdIndividual.checkYourAnswersLabel")

          val valueHtml = result.value.content.asHtml.toString()
          valueHtml must include("123456")
          valueHtml must include("Germany")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.purchaser.routes.PurchaserFormOfIdIndividualController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("purchaser.formOfIdIndividual.change.hidden")
        }
      }

      "must use CheckMode for the change link" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(PurchaserFormOfIdIndividualPage, PurchaserFormOfIdIndividual("123456", "Germany")).success.value

          val result = PurchaserFormOfIdIndividualSummary.row(Some(userAnswers))

          result.actions.get.items.head.href mustEqual controllers.purchaser.routes.PurchaserFormOfIdIndividualController.onPageLoad(CheckMode).url
        }
      }
    }
  }
}