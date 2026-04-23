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
import pages.taxCalculation.IsSelfAssessedPage

class TaxCalculationHelperSpec extends AnyFreeSpec with Matchers {

  private def result(heading: Option[String]): TaxCalculationResult =
    TaxCalculationResult(totalTax = 0, resultHeading = heading, resultHint = None, npv = None, taxCalcs = Seq.empty)

  private def answersWith(transactionType: Option[TransactionType], isSelfAssessed: Option[Boolean]): UserAnswers = {
    val base = UserAnswers("id", storn = "TESTSTORN")
    val withType = transactionType.fold(base)(t => base.set(TransactionTypePage, t).success.value)
    isSelfAssessed.fold(withType)(b => withType.set(IsSelfAssessedPage, b).success.value)
  }

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

  "isLeasehold" - {

    "must return true when TransactionTypePage is GrantOfLease" in {
      TaxCalculationHelper.isLeasehold(answersWith(Some(TransactionType.GrantOfLease), None)) mustBe true
    }

    "must return false when TransactionTypePage is ConveyanceTransfer" in {
      TaxCalculationHelper.isLeasehold(answersWith(Some(TransactionType.ConveyanceTransfer), None)) mustBe false
    }

    "must return false when TransactionTypePage is not set" in {
      TaxCalculationHelper.isLeasehold(answersWith(None, None)) mustBe false
    }
  }

  "isLeaseholdAndSelfAssessed" - {

    "must return true when transaction is leasehold and IsSelfAssessedPage is true" in {
      TaxCalculationHelper.isLeaseholdAndSelfAssessed(answersWith(Some(TransactionType.GrantOfLease), Some(true))) mustBe true
    }

    "must return false when transaction is leasehold and IsSelfAssessedPage is false" in {
      TaxCalculationHelper.isLeaseholdAndSelfAssessed(answersWith(Some(TransactionType.GrantOfLease), Some(false))) mustBe false
    }

    "must return false when transaction is leasehold and IsSelfAssessedPage is unset" in {
      TaxCalculationHelper.isLeaseholdAndSelfAssessed(answersWith(Some(TransactionType.GrantOfLease), None)) mustBe false
    }

    "must return false when transaction is not leasehold but IsSelfAssessedPage is true" in {
      TaxCalculationHelper.isLeaseholdAndSelfAssessed(answersWith(Some(TransactionType.ConveyanceTransfer), Some(true))) mustBe false
    }
  }
}
