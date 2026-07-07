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
import constants.FullReturnConstants.{completeFullReturn, completeReturnAgent, completeReturnAgentVendor}
import play.api.i18n.Messages
import play.api.test.Helpers.running


class VendorAgentSummarySpec extends SpecBase {

  "VendorAgentSummary" - {

    ".getSummaryCard" - {

      "must return a summary list for a complete vendorAgent" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val fullReturn = completeFullReturn.copy(returnAgent = Some(Seq(completeReturnAgentVendor)))

          val result = VendorAgentSummary.getSummaryCard(fullReturn)

          result must not be empty
        }
      }

      "must return None if no vendorAgent in return" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val fullReturn = completeFullReturn.copy(returnAgent = None)

          val result = VendorAgentSummary.getSummaryCard(fullReturn)

          result mustBe empty
        }
      }

      "must return None if no return agent of type Vendor in return" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val fullReturn = completeFullReturn.copy(returnAgent = Some(Seq(completeReturnAgent)))

          val result = VendorAgentSummary.getSummaryCard(fullReturn)

          result mustBe empty
        }
      }

      "must return None if no name" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val fullReturn = completeFullReturn.copy(returnAgent = Some(Seq(completeReturnAgentVendor.copy(name = None))))

          val result = VendorAgentSummary.getSummaryCard(fullReturn)

          result mustBe empty
        }
      }
    }
  }
}
