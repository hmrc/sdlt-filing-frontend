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
import pages.preliminary.TransactionTypePage

object TaxCalculationHelper {

  private val SELF_ASSESSED_HINT_TEXT: String = "self-assessed"

  def isSelfAssessedResponse(taxCalculationResult: TaxCalculationResult): Boolean =
    taxCalculationResult
      .resultHeading
      .contains(SELF_ASSESSED_HINT_TEXT)

  def isLeasehold(answers: UserAnswers): Boolean =
    answers.get(TransactionTypePage).contains(TransactionType.GrantOfLease)

  def isLeaseholdAndSelfAssessed(answers: UserAnswers, result: TaxCalculationResult): Boolean =
    isLeasehold(answers) && isSelfAssessedResponse(result)
}
