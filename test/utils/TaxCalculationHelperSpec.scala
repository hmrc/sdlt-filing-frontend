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

import models.UserAnswers
import models.prelimQuestions.TransactionType
import models.taxCalculation.TaxCalculationResult
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.preliminary.TransactionTypePage

class TaxCalculationHelperSpec extends AnyFreeSpec with Matchers {

  private def result(heading: Option[String]): TaxCalculationResult =
    TaxCalculationResult(totalTax = 0, resultHeading = heading, resultHint = None, npv = None, taxCalcs = Seq.empty)

  "isSelfAssessedResponse" - {

    "must return true when resultHeading equals 'self-assessed'" in {
      TaxCalculationHelper.isSelfAssessedResponse(result(Some("self-assessed"))) mustBe true
    }

    "must return false when resultHeading is a different string" in {
      TaxCalculationHelper.isSelfAssessedResponse(result(Some("calculated"))) mustBe false
    }

    "must return false when resultHeading is None" in {
      TaxCalculationHelper.isSelfAssessedResponse(result(None)) mustBe false
    }

    "must return false when resultHeading merely contains 'self-assessed' as a substring" in {
      TaxCalculationHelper.isSelfAssessedResponse(result(Some("self-assessed result"))) mustBe false
    }

    "must return false when the case does not match (different capitalisation)" in {
      TaxCalculationHelper.isSelfAssessedResponse(result(Some("Self-Assessed"))) mustBe false
    }
  }
}
