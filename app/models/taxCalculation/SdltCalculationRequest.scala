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

import play.api.libs.json.{Json, OWrites, Writes}

case class SdltCalculationRequest(
                                   holdingType:         HoldingTypes.Value,
                                   propertyType:        PropertyTypes.Value,
                                   effectiveDateDay:    Int,
                                   effectiveDateMonth:  Int,
                                   effectiveDateYear:   Int,
                                   nonUKResident:       Option[String],
                                   premium:             BigDecimal,
                                   highestRent:         BigDecimal,
                                   propertyDetails:     Option[PropertyDetails],
                                   leaseDetails:        Option[LeaseDetails],
                                   relevantRentDetails: Option[RelevantRentDetails],
                                   firstTimeBuyer:      Option[String],
                                   isLinked:            Option[Boolean],
                                   interestTransferred: Option[String],
                                   taxReliefDetails:    Option[TaxReliefDetails],
                                   isMultipleLand:      Option[Boolean],
                                   declaredNpv:         Option[BigDecimal]
)

object SdltCalculationRequest {
  implicit val writes: OWrites[SdltCalculationRequest] = Json.writes[SdltCalculationRequest]
}

case class PropertyDetails(
                            individual           : String,
                            twoOrMoreProperties  : Option[String],
                            replaceMainResidence : Option[String],
                            sharedOwnership      : Option[String],
                            currentValue         : Option[String]
                          )

object PropertyDetails {
  implicit val writes: OWrites[PropertyDetails] = Json.writes[PropertyDetails]
}

case class LeaseDetails(
                         startDateDay:   Int,
                         startDateMonth: Int,
                         startDateYear:  Int,
                         endDateDay:     Int,
                         endDateMonth:   Int,
                         endDateYear:    Int,
                         leaseTerm:      LeaseTerm,
                         year1Rent:      BigDecimal,
                         year2Rent:      Option[BigDecimal],
                         year3Rent:      Option[BigDecimal],
                         year4Rent:      Option[BigDecimal],
                         year5Rent:      Option[BigDecimal]
                       )

object LeaseDetails {
  implicit val writes: OWrites[LeaseDetails] = Json.writes[LeaseDetails]
}

case class LeaseTerm(
                      years:             Int,
                      days:              Int,
                      daysInPartialYear: Int
                    )

object LeaseTerm {
  implicit val writes: Writes[LeaseTerm] = Json.writes[LeaseTerm]
}

case class RelevantRentDetails(
                                contractPre201603:        Option[String],
                                contractVariedPost201603: Option[String],
                                relevantRent:             Option[BigDecimal]
                              )

object RelevantRentDetails {
  implicit val writes: Writes[RelevantRentDetails] = Json.writes[RelevantRentDetails]
}

case class TaxReliefDetails(
                             taxReliefCode:   Int,
                             isPartialRelief: Option[Boolean]
                           )

object TaxReliefDetails {
  implicit val writes: Writes[TaxReliefDetails] = Json.writes[TaxReliefDetails]
}
