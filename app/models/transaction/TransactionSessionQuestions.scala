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

package models.transaction

import play.api.libs.json.{Json, Reads}

import java.time.LocalDate

case class TransactionSessionQuestions(
                                        purchaserEligibleToClaimRelief: Option[Boolean] = None,
                                        claimingPartialReliefAmount: Option[String] = None,
                                        reasonForRelief: Option[ReasonForRelief] = None,
                                        transactionCisNumber: Option[String] = None,
                                        charityRegisteredNumber: Option[String] = None,
                                        isLinked: Option[Boolean] = None,
                                        totalConsiderationOfLinkedTransaction: Option[String] = None,
                                        totalConsiderationOfTransaction: Option[BigDecimal] = None,
                                        transactionFormsOfConsideration: Option[TransactionFormsOfConsiderationAnswers],
                                        transactionVatAmount: Option[String] = None,
                                        transactionSaleOfBusinessAssets: Option[TransactionSaleOfBusinessAssetsAnswers] = None,
                                        transactionUseOfLandOrProperty: Option[TransactionUseOfLandOrPropertyAnswers] = None,
                                        transactionDateOfContract: Option[String] = None,
                                        transactionAddDateOfContract: Option[Boolean] = None,
                                        considerationsAffectedUncertain: Option[Boolean] = None,
                                        typeOfTransaction: Option[String] = None,
                                        transactionEffectiveDate: Option[LocalDate] = None,
                                        isLandOrPropertyExchanged: Option[Boolean] = None,
                                        transactionAddress: TransactionSessionAddress,
                                        transactionDeferringPayment: Option[Boolean] = None,
                                        cap1OrNsbc: Option[Boolean] = None,
                                        transactionExercisingAnOption: Option[Boolean] = None,
                                        transactionRestrictionsCovenantsAndConditions: Option[Boolean] = None,
                                        descriptionOfRestrictions: Option[String] = None,
                                        transactionRulingFollowed: Option[TransactionRulingFollowed] = None,
                                        saleOfBusiness: Option[Boolean] = None,
                                        totalAssetsConsideration: Option[String] = None
                               )

object TransactionSessionQuestions {
  implicit val reader: Reads[TransactionSessionQuestions] = Json.reads[TransactionSessionQuestions]

}

case class TransactionSessionAddress(
                                         houseNumber: Option[String],
                                         line1: String,
                                         line2: Option[String],
                                         line3: Option[String],
                                         line4: Option[String],
                                         line5: Option[String],
                                         postcode: String,
                                         country: Option[TransactionSessionCountry],
                                         addressValidated: Boolean
                                       )

object TransactionSessionAddress {
  implicit val reader: Reads[TransactionSessionAddress] = Json.reads[TransactionSessionAddress]
}


case class TransactionSessionCountry(
                           code: Option[String],
                           name: Option[String]
                         )
object TransactionSessionCountry {
  implicit val reader: Reads[TransactionSessionCountry] = Json.reads[TransactionSessionCountry]
}

