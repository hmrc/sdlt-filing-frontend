/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.libs.json.*

case class CalculationResponse(result: Seq[Result])

object CalculationResponse {
  implicit val reads: Reads[CalculationResponse] = Json.reads[CalculationResponse]
}

case class Result(
                   totalTax      : Int,
                   resultHeading : Option[String],
                   resultHint    : Option[String],
                   npv           : Option[Int],
                   taxCalcs      : Seq[CalculationDetails]
                 )

object Result {
  implicit val reads: Reads[Result] = Json.reads[Result]
}

case class CalculationDetails(
                               taxType       : TaxTypes.Value,
                               calcType      : CalcTypes.Value,
                               taxDue        : Int,
                               detailHeading : Option[String],
                               bandHeading   : Option[String],
                               detailFooter  : Option[String],
                               rate          : Option[Int],
                               rateFraction  : Option[Int],
                               slices        : Option[Seq[SliceDetails]]
                             )

object CalculationDetails {
  implicit val reads: Reads[CalculationDetails] = Json.reads[CalculationDetails]
}

case class SliceDetails(
                         from   : Int,
                         to     : Option[Int],
                         rate   : Int,
                         taxDue : Int
                       )

object SliceDetails {
  implicit val reads: Reads[SliceDetails] = Json.reads[SliceDetails]
}
