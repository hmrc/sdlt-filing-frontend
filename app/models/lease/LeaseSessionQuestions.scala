/*
 * Copyright 2025 HM Revenue & Customs
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

package models.lease

import play.api.libs.json.{Json, Reads}

import java.time.LocalDate

case class LeaseSessionQuestions(
                                  typeOfLease: Option[TypeOfLease] = None,
                                  leaseStartDate: Option[LocalDate] = None,
                                  leaseEndDate: Option[LocalDate] = None,
                                  doesLeaseIncludeRentFreePeriod: Option[Boolean] = None,
                                  leaseEnterRentFreePeriod: Option[String] = None,
                                  annualStartingRent: Option[String] = None,
                                  leaseStartingRentEndDate: Option[LocalDate] = None,
                                  laterRent: Option[Boolean] = None,
                                  leaseThousandPoundsThreshold: Option[Boolean] = None,
                                  leaseIsVatPayable: Option[Boolean] = None,
                                  enterAnnualRentVat: Option[String] = None,
                                  leaseEnterTotalPremiumPayable: Option[String] = None,
                                  leaseNetPresentValue: Option[String] = None
                                )

object LeaseSessionQuestions {
  implicit val reader: Reads[LeaseSessionQuestions] = Json.reads[LeaseSessionQuestions]

}
