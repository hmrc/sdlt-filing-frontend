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

package models.purchaser

import play.api.libs.json.{Json, Reads}

import java.time.LocalDate

case class PurchaserSessionQuestions(purchaserCurrent: PurchaserCurrent)

object PurchaserSessionQuestions {
  implicit val reader: Reads[PurchaserSessionQuestions] = Json.reads[PurchaserSessionQuestions]
}

case class PurchaserCurrent(purchaserAndCompanyId: Option[PurchaserAndCompanyId] = None,
                            ConfirmNameOfThePurchaser: Option[ConfirmNameOfThePurchaser] = None,
                            whoIsMakingThePurchase: String,
                            nameOfPurchaser: NameOfPurchaser,
                            purchaserAddress: PurchaserSessionAddress,
                            addPurchaserPhoneNumber: Option[Boolean] = None,
                            enterPurchaserPhoneNumber: Option[String] = None,
                            doesPurchaserHaveNI: Option[DoesPurchaserHaveNI] = None,
                            nationalInsuranceNumber: Option[String] = None,
                            purchaserFormOfIdIndividual: Option[PurchaserFormOfIdIndividual] = None,
                            purchaserDateOfBirth: Option[LocalDate] = None,
                            purchaserConfirmIdentity: Option[PurchaserConfirmIdentity] = None,
                            registrationNumber: Option[String] = None,
                            purchaserUTRPage: Option[String] = None,
                            purchaserFormOfIdCompany: Option[CompanyFormOfId] = None,
                            purchaserTypeOfCompany: Option[PurchaserTypeOfCompanyAnswers] = None,
                            isPurchaserActingAsTrustee: Option[String] = None,
                            purchaserAndVendorConnected: Option[String] = None
)

object PurchaserCurrent {
  implicit val reader: Reads[PurchaserCurrent] = Json.reads[PurchaserCurrent]
}

case class PurchaserSessionAddress(
                                    houseNumber: Option[String] = None,
                                    line1: Option[String] = None,
                                    line2: Option[String] = None,
                                    line3: Option[String] = None,
                                    line4: Option[String] = None,
                                    line5: Option[String] = None,
                                    postcode: Option[String] = None,
                                    country: Option[PurchaserSessionCountry],
                                    addressValidated: Option[Boolean] = None
                                  )

object PurchaserSessionAddress {
  implicit val reader: Reads[PurchaserSessionAddress] = Json.reads[PurchaserSessionAddress]
}

case class PurchaserSessionCountry(
                                    code: Option[String] = None,
                                    name: Option[String] = None
                                  )

object PurchaserSessionCountry {
  implicit val reader: Reads[PurchaserSessionCountry] = Json.reads[PurchaserSessionCountry]
}

