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

package pages.taxCalculation.leaseholdTaxCalculated

import base.SpecBase
import pages.QuestionPage
import play.api.libs.json.JsPath

class LeaseholdTaxCalculatedPagesSpec extends SpecBase {

  private val pages: Seq[(QuestionPage[Boolean], String)] = Seq(
    LeaseholdTaxCalculatedSdltPage                 -> "leaseholdTaxCalculatedSdlt",
    LeaseholdTaxCalculatedSelfAssessedAmountPage   -> "leaseholdTaxCalculatedSelfAssessedAmount",
    LeaseholdTaxCalculatedTotalAmountDuePage       -> "leaseholdTaxCalculatedTotalAmountDue",
    LeaseholdTaxCalculatedPenaltiesAndInterestPage -> "leaseholdTaxCalculatedPenaltiesAndInterest"
  )

  "Leasehold tax-calculated pages" - {

    "must use the expected toString for each page" in {
      pages.foreach { case (page, expected) =>
        page.toString mustBe expected
      }
    }

    "must nest each page under the 'taxCalculationCurrent' key" in {
      pages.foreach { case (page, expected) =>
        page.path mustBe (JsPath \ "taxCalculationCurrent" \ expected)
      }
    }

    "must round-trip a Boolean value via UserAnswers for each page" in {
      pages.foreach { case (page, _) =>
        val updated = emptyUserAnswers.set(page, true).success.value
        updated.get(page) mustBe Some(true)
      }
    }
  }
}
