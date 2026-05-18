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

package viewmodels.checkAnswers.taxCalculation

import base.SpecBase
import play.api.i18n.Messages
import play.api.test.Helpers.running
import viewmodels.checkAnswers.summary.SummaryRowResult.Row

class CalculatedSdltDueSummarySpec extends SpecBase {

  "CalculatedSdltDueSummary" - {

    val sdltDue = "27,500.00"

    "when the tax due on NPV is present" - {

      "must return a summary list row with value" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val row = CalculatedSdltDueSummary.row(sdltDue)

          val result = row match {
            case Row(r) => r
            case _ => fail("Expected Row")
          }

          result.key.content.asHtml.toString() mustEqual msgs("taxCalculation.calculatedSdltDue.checkYourAnswersLabel")

          val valueHtml = result.value.content.asHtml.toString()
          valueHtml mustEqual "£27,500.00"
        }
      }
    }
  }
}
