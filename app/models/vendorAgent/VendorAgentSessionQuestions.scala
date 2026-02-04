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

package models.vendorAgent

import play.api.libs.json.{Json, Reads}

case class VendorAgentSessionQuestions(
                                        vendorAgentName: String,
                                        vendorAgentAddress: VendorAgentSessionAddress,
                                        vendorAgentsContactDetails: Option[VendorAgentsContactDetails] = None,
                                        vendorAgentReference: Option[String] = None,
                                      )

object VendorAgentSessionQuestions {
  implicit val reader: Reads[VendorAgentSessionQuestions] = Json.reads[VendorAgentSessionQuestions]

}

case class VendorAgentSessionAddress(
                                      houseNumber: Option[Int],
                                      line1: String,
                                      line2: Option[String],
                                      line3: Option[String],
                                      line4: Option[String],
                                      line5: Option[String],
                                      postcode: String,
                                      country: Option[VendorAgentSessionCountry],
                                      addressValidated: Boolean
                                    )

object VendorAgentSessionAddress {
  implicit val reader: Reads[VendorAgentSessionAddress] = Json.reads[VendorAgentSessionAddress]
}


case class VendorAgentSessionCountry(
                                      code: Option[String],
                                      name: Option[String]
                                    )

object VendorAgentSessionCountry {
  implicit val reader: Reads[VendorAgentSessionCountry] = Json.reads[VendorAgentSessionCountry]
}

