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
import constants.FullReturnConstants.{completeFullReturn, completeLand}
import models.Land
import play.api.i18n.Messages
import play.api.test.Helpers.running


class LandSummarySpec extends SpecBase {

  "LandSummary" - {

    ".getSummaryCards" - {

      "must return a summary list for a complete land" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val fullReturn = completeFullReturn.copy(land = Some(Seq(completeLand)))

          val result = LandSummary.getSummaryCards(fullReturn)

          result must not be empty
          result.get.length mustEqual 1
        }
      }

      "must return a sequence of summary lists for multiple lands" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        val land2 = Land(address1 = Some("Address 2"))
        val land3 = Land(address1 = Some("Address 3"))

        running(application) {
          implicit val msgs: Messages = messages(application)

          val fullReturn = completeFullReturn.copy(land = Some(Seq(completeLand, land2, land3)))

          val result = LandSummary.getSummaryCards(fullReturn)

          result must not be empty
          result.get.length mustEqual 3
        }
      }

      "must return None if no lands in return" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val fullReturn = completeFullReturn.copy(land = None)

          val result = LandSummary.getSummaryCards(fullReturn)

          result mustBe empty
        }
      }

      "must return None if no land address 1" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val fullReturn = completeFullReturn.copy(land = Some(Seq(completeLand.copy(address1 = None))))

          val result = LandSummary.getSummaryCards(fullReturn)

          result mustBe empty
        }
      }
    }
  }
}
