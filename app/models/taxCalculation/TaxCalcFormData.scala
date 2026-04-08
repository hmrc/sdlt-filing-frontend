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

import java.time.LocalDate

case class TaxCalcLeaseFormData(
  contractStartDateDay:   Option[String],
  contractStartDateMonth: Option[String],
  contractStartDateYear:  Option[String],
  contractEndDateDay:     Option[String],
  contractEndDateMonth:   Option[String],
  contractEndDateYear:    Option[String],
  startingRent:           Option[String],
  isAnnualRentOver1000:   Option[String],
  declaredNpv:            Option[BigDecimal]
)

case class TaxCalcTestFormData(
  propertyType:       String,
  interestCode:       String,
  effectiveDate:      LocalDate,
  totalConsideration: BigDecimal,
  isLinked:           Option[String],
  isNonUkResident:    Option[String],
  purchaserIsCompany: Option[String],
  isMultipleLand:     Option[String],
  firstTimeBuyer:     Option[String],
  taxReliefCode:      Option[Int],
  isPartialRelief:    Option[String],
  lease:              TaxCalcLeaseFormData
)
