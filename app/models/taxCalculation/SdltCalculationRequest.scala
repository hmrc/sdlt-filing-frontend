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

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{Json, OWrites, Writes, __}
import RequestValidators.*

import java.time.LocalDate

case class SdltCalculationRequest(
                                   holdingType         : HoldingTypes.Value,
                                   propertyType        : PropertyTypes.Value,
                                   effectiveDate       : LocalDate,
                                   nonUKResident       : Option[Boolean],
                                   premium             : BigDecimal,
                                   highestRent         : BigDecimal,
                                   propertyDetails     : Option[PropertyDetails],
                                   leaseDetails        : Option[LeaseDetails],
                                   relevantRentDetails : Option[RelevantRentDetails],
                                   firstTimeBuyer      : Option[Boolean],
                                   isLinked            : Option[Boolean],
                                   interestTransferred : Option[String],
                                   taxReliefDetails    : Option[TaxReliefDetails],
                                   isMultipleLand      : Option[Boolean]
                                 )

object SdltCalculationRequest {

  implicit val writes: OWrites[SdltCalculationRequest] = (
    (__ \ "holdingType").write[HoldingTypes.Value] and
      (__ \ "propertyType").write[PropertyTypes.Value] and
      __.write[LocalDate](multiFieldDateWrites("effectiveDate")) and
      (__ \ "nonUKResident").writeNullable[Boolean](booleanToYesNoWrites) and
      (__ \ "premium").write[BigDecimal] and
      (__ \ "highestRent").write[BigDecimal] and
      (__ \ "propertyDetails").writeNullable[PropertyDetails] and
      (__ \ "leaseDetails").writeNullable[LeaseDetails] and
      (__ \ "relevantRentDetails").writeNullable[RelevantRentDetails] and
      (__ \ "firstTimeBuyer").writeNullable[Boolean](booleanToYesNoWrites) and
      (__ \ "isLinked").writeNullable[Boolean] and
      (__ \ "interestTransferred").writeNullable[String] and
      (__ \ "taxReliefDetails").writeNullable[TaxReliefDetails] and
      (__ \ "isMultipleLand").writeNullable[Boolean]
    )(Tuple.fromProductTyped(_))
}

case class PropertyDetails(
                            individual           : Boolean,
                            twoOrMoreProperties  : Option[Boolean],
                            replaceMainResidence : Option[Boolean],
                            sharedOwnership      : Option[Boolean],
                            currentValue         : Option[Boolean]
                          )

object PropertyDetails {

  implicit val writes: OWrites[PropertyDetails] = (
    (__ \ "individual").write[Boolean](booleanToYesNoWrites) and
      (__ \ "twoOrMoreProperties").writeNullable[Boolean](booleanToYesNoWrites) and
      (__ \ "replaceMainResidence").writeNullable[Boolean](booleanToYesNoWrites) and
      (__ \ "sharedOwnership").writeNullable[Boolean](booleanToYesNoWrites) and
      (__ \ "currentValue").writeNullable[Boolean](booleanToYesNoWrites)
    )(Tuple.fromProductTyped(_))
}

case class LeaseDetails(
                         startDate : LocalDate,
                         endDate   : LocalDate,
                         leaseTerm : LeaseTerm,
                         year1Rent : BigDecimal,
                         year2Rent : Option[BigDecimal],
                         year3Rent : Option[BigDecimal],
                         year4Rent : Option[BigDecimal],
                         year5Rent : Option[BigDecimal]
                       )

object LeaseDetails {
  implicit val writes: OWrites[LeaseDetails] = (
    __.write[LocalDate](multiFieldDateWrites("startDate")) and
      __.write[LocalDate](multiFieldDateWrites("endDate")) and
      (__ \ "leaseTerm").write[LeaseTerm] and
      (__ \ "year1Rent").write[BigDecimal] and
      (__ \ "year2Rent").writeNullable[BigDecimal] and
      (__ \ "year3Rent").writeNullable[BigDecimal] and
      (__ \ "year4Rent").writeNullable[BigDecimal] and
      (__ \ "year5Rent").writeNullable[BigDecimal]
    )(Tuple.fromProductTyped(_))
}

case class LeaseTerm(
                      years             : Int,
                      days              : Int,
                      daysInPartialYear : Int
                    )

object LeaseTerm {
  implicit val writes: Writes[LeaseTerm] = Json.writes[LeaseTerm]
}

case class RelevantRentDetails(
                                exchangedContractsBeforeMar16 : Option[Boolean],
                                contractChangedSinceMar16     : Option[Boolean],
                                relevantRent                  : Option[BigDecimal]
                              )

object RelevantRentDetails {
  implicit val writes: Writes[RelevantRentDetails] = (
    (__ \ "contractPre201603").writeNullable[Boolean](booleanToYesNoWrites) and
      (__ \ "contractVariedPost201603").writeNullable[Boolean](booleanToYesNoWrites) and
      (__ \ "relevantRent").writeNullable[BigDecimal]
    )(Tuple.fromProductTyped(_))
}

case class TaxReliefDetails(
                             taxReliefCode   : Int,
                             isPartialRelief : Option[Boolean]
                           )

object TaxReliefDetails {
  implicit val writes: Writes[TaxReliefDetails] = Json.writes[TaxReliefDetails]
}
