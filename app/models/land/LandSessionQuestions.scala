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

package models.land

import play.api.libs.json.{Json, Reads}

case class LandSessionQuestions(
                                 landId: Option[String] = None,
                                 propertyType: Option[String] = None,
                                 landInterestTransferredOrCreated: Option[String] = None,
                                 landAddress: LandSessionAddress,
                                 localAuthorityCode: String,
                                 titleNumber: Option[String] = None,
                                 landNlpgUprn: Option[String] = None,
                                 landSendingPlanByPost: Boolean,
                                 landMineralsOrMineralRights: Boolean,
                                 agriculturalOrDevelopmentalLand: Option[Boolean] = None,
                                 areaOfLand: Option[String] = None,
                                 areaUnit: Option[String] = None
                               )

object LandSessionQuestions {
  implicit val reader: Reads[LandSessionQuestions] = Json.reads[LandSessionQuestions]

}

case class LandSessionAddress(
                                         houseNumber: Option[String],
                                         line1: String,
                                         line2: Option[String],
                                         line3: Option[String],
                                         line4: Option[String],
                                         line5: Option[String],
                                         postcode: String,
                                         country: Option[LandSessionCountry],
                                         addressValidated: Boolean
                                       )

object LandSessionAddress {
  implicit val reader: Reads[LandSessionAddress] = Json.reads[LandSessionAddress]
}


case class LandSessionCountry(
                           code: Option[String],
                           name: Option[String]
                         )
object LandSessionCountry {
  implicit val reader: Reads[LandSessionCountry] = Json.reads[LandSessionCountry]
}

