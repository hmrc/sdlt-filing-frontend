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

package utils

import base.SpecBase
import models.taxCalculation.{HoldingTypes, TaxCalculationFlow, TaxCalculationResult}
import models.{FullReturn, Transaction, UserAnswers}
import utils.TaxCalculationHelper.*

class TaxCalculationHelperSpec extends SpecBase {

  private def resultWithHeading(heading: Option[String]): TaxCalculationResult =
    TaxCalculationResult(totalTax = 0, resultHeading = heading, resultHint = None, npv = None, taxCalcs = Seq.empty)

  private def answersWith(transactionDescription: String): UserAnswers =
    emptyUserAnswers.copy(fullReturn = Some(FullReturn(
      stornId           = "TESTSTORN",
      returnResourceRef = "REF001",
      transaction       = Some(Transaction(transactionDescription = Some(transactionDescription)))
    )))

  "calculationResponseType" - {

    "must return TaxNotCalculated when resultHeading equals 'Self-assessed'" in {
      calculationResponseType(resultWithHeading(Some("Self-assessed"))) mustBe TaxNotCalculated
    }

    "must return TaxCalculated when resultHeading is None" in {
      calculationResponseType(resultWithHeading(None)) mustBe TaxCalculated
    }

    "must return TaxCalculated when resultHeading is anything other than the exact 'Self-assessed' string" in {
      calculationResponseType(resultWithHeading(Some("calculated"))) mustBe TaxCalculated
    }
  }

  "holdingType" - {

    "must return Some(freehold) for transactionDescription F (conveyance transfer)" in {
      holdingType(answersWith("F")) mustBe Some(HoldingTypes.freehold)
    }

    "must return Some(freehold) for transactionDescription A (conveyance transfer lease)" in {
      holdingType(answersWith("A")) mustBe Some(HoldingTypes.freehold)
    }

    "must return Some(freehold) for transactionDescription O (other transaction)" in {
      holdingType(answersWith("O")) mustBe Some(HoldingTypes.freehold)
    }

    "must return Some(leasehold) for transactionDescription L (grant of lease)" in {
      holdingType(answersWith("L")) mustBe Some(HoldingTypes.leasehold)
    }

    "must return None when no FullReturn is present" in {
      holdingType(emptyUserAnswers) mustBe None
    }

    "must return None when the transaction description is unrecognised" in {
      holdingType(answersWith("X")) mustBe None
    }
  }

  "flowFor" - {

    "must return Some(FreeholdTaxCalculated) for freehold and a tax-calculated result" in {
      flowFor(answersWith("F"), resultWithHeading(None)) mustBe Some(TaxCalculationFlow.FreeholdTaxCalculated)
    }

    "must return Some(FreeholdSelfAssessed) for freehold and a Self-assessed result" in {
      flowFor(answersWith("F"), resultWithHeading(Some("Self-assessed"))) mustBe Some(TaxCalculationFlow.FreeholdSelfAssessed)
    }

    "must return Some(LeaseholdTaxCalculated) for leasehold and a tax-calculated result" in {
      flowFor(answersWith("L"), resultWithHeading(None)) mustBe Some(TaxCalculationFlow.LeaseholdTaxCalculated)
    }

    "must return Some(LeaseholdSelfAssessed) for leasehold and a Self-assessed result" in {
      flowFor(answersWith("L"), resultWithHeading(Some("Self-assessed"))) mustBe Some(TaxCalculationFlow.LeaseholdSelfAssessed)
    }

    "must return None when no FullReturn is present" in {
      flowFor(emptyUserAnswers, resultWithHeading(None)) mustBe None
    }
  }
}
