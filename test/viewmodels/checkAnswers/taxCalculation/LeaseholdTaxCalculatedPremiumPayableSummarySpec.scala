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
import models.taxCalculation.{CalcTypes, CalculationDetails, TaxCalculationResult, TaxTypes}
import play.api.i18n.Messages
import play.api.test.Helpers.running
import viewmodels.checkAnswers.summary.SummaryRowResult.Row

class LeaseholdTaxCalculatedPremiumPayableSummarySpec extends SpecBase {

  private def resultWithPremium(taxDue: Int): TaxCalculationResult =
    TaxCalculationResult(
      totalTax = 53523,
      resultHeading = None,
      resultHint = None,
      npv = None,
      taxCalcs = Seq(CalculationDetails(TaxTypes.premium, CalcTypes.slab, taxDue, None, None, None, None, None, None))
    )

  "LeaseholdTaxCalculatedPremiumPayableSummary" - {

    "must return a summary list row with the premium tax formatted as currency and no change link" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val row = LeaseholdTaxCalculatedPremiumPayableSummary.row(resultWithPremium(36250))

        val result = row match {
          case Row(r) => r
          case _      => fail("Expected Row")
        }

        result.key.content.asHtml.toString() mustEqual msgs("taxCalculation.leaseholdSelfAssessed.premiumPayable.checkYourAnswers")
        result.value.content.asHtml.toString() mustEqual "£36,250"
        result.actions mustBe None
      }
    }
  }
}
