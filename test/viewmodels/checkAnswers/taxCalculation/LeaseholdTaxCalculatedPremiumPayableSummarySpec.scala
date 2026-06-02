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
import models.taxCalculation.{CalculationDetails, TaxCalculationResult, TaxTypes, CalcTypes}
import play.api.i18n.Messages
import play.api.test.Helpers.running
import viewmodels.checkAnswers.summary
import viewmodels.checkAnswers.summary.SummaryRowResult
import viewmodels.checkAnswers.summary.SummaryRowResult.Row

class LeaseholdTaxCalculatedPremiumPayableSummarySpec extends SpecBase {

  private val emptyTestResult = TaxCalculationResult(
    totalTax = 5000,
    resultHeading = None,
    resultHint = None,
    npv = None,
    taxCalcs = Nil
  )

  "LeaseholdTaxCalculatedPremiumPayableSummary" - {

    "when there is premium payable tax" - {
      "must return a summary list row with values" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val row = LeaseholdTaxCalculatedPremiumPayableSummary.row(emptyTestResult.copy(taxCalcs = Seq(
            CalculationDetails(TaxTypes.premium, CalcTypes.slab,  5000, None, None, None, Some(5), None, None),
            CalculationDetails(TaxTypes.rent,    CalcTypes.slice, 3000, None, None, None, Some(1), None, None)
          )))

          val result = row match {
            case Some(Row(r)) => r
            case _ => fail("Failed to retrieve Row")
          }

          result.key.content.asHtml.toString() mustEqual msgs("taxCalculation.leaseholdTaxCalculated.premiumPayable.checkYourAnswers")
          result.value.content.asHtml.toString() mustEqual "£5,000"
        }
      }
    }

    "when there is no premium payable tax" - {
      "must not return summary list row" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val row = LeaseholdTaxCalculatedPremiumPayableSummary.row(emptyTestResult)

          row mustBe None
        }
      }
    }
  }
}
