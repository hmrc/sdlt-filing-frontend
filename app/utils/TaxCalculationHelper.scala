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
import models.taxCalculation.CalculationOutcome.{Calculated, PreMarch2012, SelfAssessed}
import models.taxCalculation.{CalculationOutcome, HoldingTypes, TaxCalculationFlow}

object TaxCalculationHelper {

  def holdingType(answers: UserAnswers): Option[HoldingTypes.Value] =
    for {
      fullReturn  <- answers.fullReturn
      transaction <- fullReturn.transaction
      transDesc   <- transaction.transactionDescription
      holding     <- HoldingTypes.fromCode(transDesc)
    } yield holding

  def flowFor(answers: UserAnswers, outcome: CalculationOutcome): Option[TaxCalculationFlow] =
    (holdingType(answers), outcome) match {
      case (Some(HoldingTypes.freehold),  Calculated(_))   => Some(TaxCalculationFlow.FreeholdTaxCalculated)
      case (Some(HoldingTypes.freehold),  SelfAssessed)    => Some(TaxCalculationFlow.FreeholdSelfAssessed)
      case (Some(HoldingTypes.freehold),  PreMarch2012)    => Some(TaxCalculationFlow.FreeholdSelfAssessed)
      case (Some(HoldingTypes.leasehold), Calculated(_))   => Some(TaxCalculationFlow.LeaseholdTaxCalculated)
      case (Some(HoldingTypes.leasehold), SelfAssessed)    => Some(TaxCalculationFlow.LeaseholdSelfAssessed)
      case (Some(HoldingTypes.leasehold), PreMarch2012)    => Some(TaxCalculationFlow.LeaseholdSelfAssessed)
      case _                                               => None
    }
}
