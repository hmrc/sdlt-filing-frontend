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
import models.taxCalculation.{HoldingTypes, TaxCalculationFlow, TaxCalculationResult}

object TaxCalculationHelper {

  private val selfAssessedHeading: String = "Self-assessed"
  private val effectiveDateHeading: String = "Effective date is before 2012/03/22"

  def calculationResponseType(result: TaxCalculationResult): CalculationResultType =
    if (result.resultHeading.contains(selfAssessedHeading)) TaxNotCalculated
    else if(result.resultHeading.contains(effectiveDateHeading)) PreMarch2012Date
    else TaxCalculated

  def holdingType(answers: UserAnswers): Option[HoldingTypes.Value] =
    for {
      fullReturn  <- answers.fullReturn
      transaction <- fullReturn.transaction
      transDesc   <- transaction.transactionDescription
      holding     <- HoldingTypes.fromCode(transDesc)
    } yield holding

  def flowFor(answers: UserAnswers, result: TaxCalculationResult): Option[TaxCalculationFlow] =
    (holdingType(answers), calculationResponseType(result)) match {
      case (Some(HoldingTypes.freehold),  TaxCalculated)    => Some(TaxCalculationFlow.FreeholdTaxCalculated)
      case (Some(HoldingTypes.freehold),  TaxNotCalculated) => Some(TaxCalculationFlow.FreeholdSelfAssessed)
      case (Some(HoldingTypes.freehold),  PreMarch2012Date) => Some(TaxCalculationFlow.FreeholdSelfAssessed)
      case (Some(HoldingTypes.leasehold), TaxCalculated)    => Some(TaxCalculationFlow.LeaseholdTaxCalculated)
      case (Some(HoldingTypes.leasehold), TaxNotCalculated) => Some(TaxCalculationFlow.LeaseholdSelfAssessed)
      case (Some(HoldingTypes.leasehold), PreMarch2012Date) => Some(TaxCalculationFlow.LeaseholdSelfAssessed)
      case _                                                => None
    }

  sealed trait CalculationResultType
  case object TaxCalculated    extends CalculationResultType
  case object TaxNotCalculated extends CalculationResultType
  case object PreMarch2012Date extends CalculationResultType
}
