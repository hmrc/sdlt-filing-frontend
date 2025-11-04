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

package models

import play.api.libs.json.{Json, Reads}

case class PrelimSessionQuestions(
                                   purchaserIsIndividual: String,
                                   purchaserSurnameOrCompanyName: String,
                                   purchaserAddress: PrelimSessionAddress,
                                   transactionType: String
                          )
object PrelimSessionQuestions {
  implicit val reader: Reads[PrelimSessionQuestions] = Json.reads[PrelimSessionQuestions]

}

case class PrelimSessionAddress(
                                 houseNumber: Option[Int],
                                 line1: String,
                                 line2: Option[String],
                                 line3: Option[String],
                                 line4: Option[String],
                                 line5: Option[String],
                                 postcode: Option[String],
                                 country: Option[PrelimSessionCountry],
                                 addressValidated: Boolean,
                         )

object PrelimSessionAddress {
  implicit val reader: Reads[PrelimSessionAddress] = Json.reads[PrelimSessionAddress]
}


case class PrelimSessionCountry(
                           code: Option[String],
                           name: Option[String]
                         )
object PrelimSessionCountry {
  implicit val reader: Reads[PrelimSessionCountry] = Json.reads[PrelimSessionCountry]
}

