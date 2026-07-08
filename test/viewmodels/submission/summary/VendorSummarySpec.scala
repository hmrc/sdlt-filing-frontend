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
import constants.FullReturnConstants.{completeFullReturn, completeVendor}
import models.Vendor
import play.api.i18n.Messages
import play.api.test.Helpers.running


class VendorSummarySpec extends SpecBase {

  "VendorSummary" - {

    ".getSummaryCards" - {

      "must return a summary list for a complete vendor" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val fullReturn = completeFullReturn.copy(vendor = Some(Seq(completeVendor)))

          val result = VendorSummary.getSummaryCards(fullReturn)

          result must not be empty
          result.get.length mustEqual 1
        }
      }

      "must return a sequence of summary lists for multiple vendors" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        val vendor2 = Vendor(name = Some("Name 2"), vendorID = Some("VEN002"))
        val vendor3 = Vendor(name = Some("Name 3"), vendorID = Some("VEN003"))

        running(application) {
          implicit val msgs: Messages = messages(application)

          val fullReturn = completeFullReturn.copy(vendor = Some(Seq(completeVendor, vendor2, vendor3)))

          val result = VendorSummary.getSummaryCards(fullReturn)

          result must not be empty
          result.get.length mustEqual 3
        }
      }

      "must return None if no vendors in return" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val fullReturn = completeFullReturn.copy(vendor = None)

          val result = VendorSummary.getSummaryCards(fullReturn)

          result mustBe empty
        }
      }

      "must return None if no vendor name" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val fullReturn = completeFullReturn.copy(vendor = Some(Seq(completeVendor.copy(name = None, forename1 = None, forename2 = None))))

          val result = VendorSummary.getSummaryCards(fullReturn)

          result mustBe empty
        }
      }
    }
  }
}
