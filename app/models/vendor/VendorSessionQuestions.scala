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

package models.vendor

import play.api.libs.json.{Json, Reads}


case class VendorSessionQuestions(vendorCurrent: VendorCurrent)

object VendorSessionQuestions {
  implicit val reader: Reads[VendorSessionQuestions] = Json.reads[VendorSessionQuestions]
}


case class VendorCurrent(
                          vendorID: Option[String] = None,
                          whoIsTheVendor: String,
                          vendorOrCompanyName: VendorName,
                          vendorAddress: VendorSessionAddress
                        )

object VendorCurrent {
  implicit val reader: Reads[VendorCurrent] = Json.reads[VendorCurrent]
}


case class VendorSessionAddress(
                                 houseNumber: Option[String] = None,
                                 line1: String,
                                 line2: Option[String] = None,
                                 line3: Option[String] = None,
                                 line4: Option[String] = None,
                                 line5: Option[String] = None,
                                 postcode: String,
                                 country: Option[VendorSessionCountry],
                                 addressValidated: Option[Boolean] = None
                               )

object VendorSessionAddress {
  implicit val reader: Reads[VendorSessionAddress] = Json.reads[VendorSessionAddress]
}


case class VendorSessionCountry(
                                 code: Option[String] = None,
                                 name: Option[String] = None
                               )
object VendorSessionCountry {
  implicit val reader: Reads[VendorSessionCountry] = Json.reads[VendorSessionCountry]
}
