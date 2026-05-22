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

package viewmodels.checkAnswers.lease

import base.SpecBase
import models.CheckMode
import pages.lease.LeaseEnterTotalPremiumPayablePage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}

class LeaseEnterTotalPremiumPayableSummarySpec extends SpecBase {

  "LeaseEnterTotalPremiumPayableSummary" - {

    "when the total premium payable is present" - {

      "must return a summary list row with value and change link" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val value = "100.00"

          val userAnswers = emptyUserAnswers
            .set(LeaseEnterTotalPremiumPayablePage, value).success.value

          val row = LeaseEnterTotalPremiumPayableSummary.row(userAnswers)

          val result = row match {
            case Row(r) => r
            case _ => fail("Expected Row but got Missing")
          }

          result.key.content.asHtml.toString() mustEqual msgs("lease.enterTotalPremiumPayable.checkYourAnswersLabel")

          val valueHtml = result.value.content.asHtml.toString()
          valueHtml mustEqual "£100.00"

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.lease.routes.LeaseEnterTotalPremiumPayableController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("lease.enterTotalPremiumPayable.change.hidden")
        }
      }
    }

    "when the total premium payable is not present" - {

      "must return a Missing and redirect call to missing page" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers

          val result = LeaseEnterTotalPremiumPayableSummary.row(userAnswers)

          result match {
            case Missing(call) =>
              call mustEqual controllers.lease.routes.LeaseEnterTotalPremiumPayableController.onPageLoad(CheckMode)

            case Row(_) =>
              fail("Expected Missing but got Row")
          }
        }
      }
    }
  }
}
