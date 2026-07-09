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

package viewmodels.submission.summary

import base.SpecBase
import constants.FullReturnConstants.{completeFullReturn, completeLease}
import models.Lease
import play.api.i18n.Messages
import play.api.test.Helpers.running


class LeaseSummarySpec extends SpecBase {

  "LeaseSummary" - {

    ".getSummaryCard" - {

      "must return a summary list for a complete lease" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val fullReturn = completeFullReturn.copy(lease = Some(completeLease))

          val result = LeaseSummary.getSummaryCard(fullReturn)

          result must not be empty
        }
      }

      "must return None if no lease in return" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val fullReturn = completeFullReturn.copy(lease = None)

          val result = LeaseSummary.getSummaryCard(fullReturn)

          result mustBe empty
        }
      }

      "must return None if no lease type" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val fullReturn = completeFullReturn.copy(lease = Some(completeLease.copy(leaseType = None)))

          val result = LeaseSummary.getSummaryCard(fullReturn)

          result mustBe empty
        }
      }
    }
  }
}
