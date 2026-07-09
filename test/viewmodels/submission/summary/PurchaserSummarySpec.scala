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
import constants.FullReturnConstants.{completeFullReturn, completePurchaser1}
import models.Purchaser
import play.api.i18n.Messages
import play.api.test.Helpers.running


class PurchaserSummarySpec extends SpecBase {

  "PurchaserSummary" - {

    ".getSummaryCards" - {

      "must return a summary list for a complete purchaser" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val fullReturn = completeFullReturn.copy(purchaser = Some(Seq(completePurchaser1)))

          val result = PurchaserSummary.getSummaryCards(fullReturn)

          result must not be empty
          result.get.length mustEqual 1
        }
      }

      "must return a sequence of summary lists for multiple purchasers" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        val purchaser2 = Purchaser(isCompany = Some("YES"), companyName = Some("Company name"), purchaserID = Some("PUR002"))
        val purchaser3 = Purchaser(isCompany = Some("NO"), surname = Some("Surname"), purchaserID = Some("PUR003"))

        running(application) {
          implicit val msgs: Messages = messages(application)

          val fullReturn = completeFullReturn.copy(purchaser = Some(Seq(completePurchaser1, purchaser2, purchaser3)))

          val result = PurchaserSummary.getSummaryCards(fullReturn)

          result must not be empty
          result.get.length mustEqual 3
        }
      }

      "must return None if no purchasers in return" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val fullReturn = completeFullReturn.copy(purchaser = None)

          val result = PurchaserSummary.getSummaryCards(fullReturn)

          result mustBe empty
        }
      }

      "must return None if no purchaser name" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val fullReturn = completeFullReturn.copy(
            purchaser = Some(Seq(completePurchaser1.copy(surname = None, forename1 = None, forename2 = None, companyName = None)))
          )

          val result = PurchaserSummary.getSummaryCards(fullReturn)

          result mustBe empty
        }
      }

      "must return None if no isCompany" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val fullReturn = completeFullReturn.copy(purchaser = Some(Seq(completePurchaser1.copy(isCompany = None))))

          val result = PurchaserSummary.getSummaryCards(fullReturn)

          result mustBe empty
        }
      }
    }
  }
}
