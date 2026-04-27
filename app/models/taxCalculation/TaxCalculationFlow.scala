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

package models.taxCalculation

import models.{Enumerable, WithName}

sealed trait TaxCalculationFlow

object TaxCalculationFlow extends Enumerable.Implicits {

  case object FreeholdTaxCalculated  extends WithName("FreeholdTaxCalculated")  with TaxCalculationFlow
  case object FreeholdSelfAssessed   extends WithName("FreeholdSelfAssessed")   with TaxCalculationFlow
  case object LeaseholdTaxCalculated extends WithName("LeaseholdTaxCalculated") with TaxCalculationFlow
  case object LeaseholdSelfAssessed  extends WithName("LeaseholdSelfAssessed")  with TaxCalculationFlow

  val values: Seq[TaxCalculationFlow] = Seq(
    FreeholdTaxCalculated, FreeholdSelfAssessed, LeaseholdTaxCalculated, LeaseholdSelfAssessed
  )

  implicit val enumerable: Enumerable[TaxCalculationFlow] =
    Enumerable(values.map(v => v.toString -> v)*)
}
