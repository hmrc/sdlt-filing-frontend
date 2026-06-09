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

class LeaseholdTaxCalculatedNpvSummarySpec extends SpecBase {

  private def resultWith(taxCalcs: CalculationDetails*): TaxCalculationResult =
    TaxCalculationResult(
      totalTax = 53523,
      resultHeading = None,
      resultHint = None,
      npv = None,
      taxCalcs = taxCalcs.toSeq
    )

  private def rentLine(taxDue: Int): CalculationDetails =
    CalculationDetails(TaxTypes.rent, CalcTypes.slice, taxDue, None, None, None, None, None, None)

  "LeaseholdTaxCalculatedNpvSummary" - {

    "must return a row with the NPV tax formatted as currency and no change link when a rent line is present" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val result = LeaseholdTaxCalculatedNpvSummary.row(resultWith(rentLine(17273))) match {
          case Some(Row(r)) => r
          case other        => fail(s"Expected Some(Row) but got $other")
        }

        result.key.content.asHtml.toString() mustEqual msgs("taxCalculation.taxDueOnNpv.checkYourAnswersLabel")
        result.value.content.asHtml.toString() mustEqual "£17,273"
        result.actions mustBe None
      }
    }

    "must return None (so the row is not rendered) when there is no rent line in the result" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        LeaseholdTaxCalculatedNpvSummary.row(resultWith()) mustBe None
      }
    }
  }
}
