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

package models.transaction

import models.{Transaction, UserAnswers}
import play.api.libs.json.{Json, OFormat}

import scala.concurrent.Future

case class UpdateTransactionRequest(
                                     storn: String,
                                     returnResourceRef: String,
                                     transaction: TransactionPayload
                                   )

object UpdateTransactionRequest {
  implicit val format: OFormat[UpdateTransactionRequest] = Json.format[UpdateTransactionRequest]

  def from(userAnswers: UserAnswers, transaction: Transaction): Future[UpdateTransactionRequest] =
    userAnswers.fullReturn match {
      case Some(fullReturn) =>
            Future.successful(UpdateTransactionRequest(
              storn = userAnswers.storn,
              returnResourceRef = fullReturn.returnResourceRef,
              TransactionPayload(
                claimingRelief = transaction.claimingRelief,
                reliefAmount = transaction.reliefAmount,
                reliefReason = transaction.reliefReason,
                reliefSchemeNumber = transaction.reliefSchemeNumber,
                isLinked = transaction.isLinked,
                totalConsiderLinked = transaction.totalConsiderationLinked,
                totalConsider = transaction.totalConsideration,
                considerBuild = transaction.considerationBuild,
                considerCash = transaction.considerationCash,
                considerContingent = transaction.considerationContingent,
                considerDebt = transaction.considerationDebt,
                considerEmploy = transaction.considerationEmploy,
                considerOther = transaction.considerationOther,
                considerLand = transaction.considerationLand,
                considerServices = transaction.considerationServices,
                considerSharesQtd = transaction.considerationSharesQTD,
                considerSharesUnqtd = transaction.considerationSharesUNQTD,
                considerVat = transaction.considerationVAT,
                includesChattel = transaction.includesChattel,
                includesGoodwill = transaction.includesGoodwill,
                includesOther = transaction.includesOther,
                includesStock = transaction.includesStock,
                usedAsFactory = transaction.usedAsFactory,
                usedAsHotel = transaction.usedAsHotel,
                usedAsIndustrial = transaction.usedAsIndustrial,
                usedAsOffice = transaction.usedAsOffice,
                usedAsOther = transaction.usedAsOther,
                usedAsShop = transaction.usedAsShop,
                usedAsWarehouse = transaction.usedAsWarehouse,
                contractDate = transaction.contractDate,
                isDependOnFutureEvent = transaction.isDependantOnFutureEvent,
                transactionDescription = transaction.transactionDescription,
                newTransactionDescription = transaction.newTransactionDescription,
                effectiveDate = transaction.effectiveDate,
                isLandExchanged = transaction.isLandExchanged,
                exLandHouseNumber = transaction.exchangedLandHouseNumber,
                exLandAddress1 = transaction.exchangedLandAddress1,
                exLandAddress2 = transaction.exchangedLandAddress2,
                exLandAddress3 = transaction.exchangedLandAddress3,
                exLandAddress4 = transaction.exchangedLandAddress4,
                exLandPostcode = transaction.exchangedLandPostcode,
                agreedDeferPay = transaction.agreedToDeferPayment,
                postTransactionRulingApplied = transaction.postTransRulingApplied,
                isPursuantToPreviousOption = transaction.isPursuantToPreviousOption,
                restAffectInt = transaction.restrictionsAffectInterest,
                restDetails = transaction.restrictionDetails,
                postTransactionRulingFollowed = transaction.postTransRulingFollowed,
                isPartOfSaleOfBusiness = transaction.isPartOfSaleOfBusiness,
                totalConsiderationOfBusiness = transaction.totalConsiderationBusiness
        )
            ))
      case None => Future.failed(new NoSuchElementException("Full return not found"))
    }
}

case class UpdateTransactionReturn(
                                    updated: Boolean
                                  )

object UpdateTransactionReturn {
  implicit val format: OFormat[UpdateTransactionReturn] = Json.format[UpdateTransactionReturn]
}

case class TransactionPayload(
                               claimingRelief: Option[String] = None,
                               reliefAmount: Option[String] = None,
                               reliefReason: Option[String] = None,
                               reliefSchemeNumber: Option[String] = None,
                               isLinked: Option[String] = None,
                               totalConsiderLinked: Option[String] = None,
                               totalConsider: Option[String] = None,
                               considerBuild: Option[String] = None,
                               considerCash: Option[String] = None,
                               considerContingent: Option[String] = None,
                               considerDebt: Option[String] = None,
                               considerEmploy: Option[String] = None,
                               considerOther: Option[String] = None,
                               considerLand: Option[String] = None,
                               considerServices: Option[String] = None,
                               considerSharesQtd: Option[String] = None,
                               considerSharesUnqtd: Option[String] = None,
                               considerVat: Option[String] = None,
                               includesChattel: Option[String] = None,
                               includesGoodwill: Option[String] = None,
                               includesOther: Option[String] = None,
                               includesStock: Option[String] = None,
                               usedAsFactory: Option[String] = None,
                               usedAsHotel: Option[String] = None,
                               usedAsIndustrial: Option[String] = None,
                               usedAsOffice: Option[String] = None,
                               usedAsOther: Option[String] = None,
                               usedAsShop: Option[String] = None,
                               usedAsWarehouse: Option[String] = None,
                               contractDate: Option[String] = None,
                               isDependOnFutureEvent: Option[String] = None,
                               transactionDescription: Option[String] = None,
                               newTransactionDescription: Option[String] = None,
                               effectiveDate: Option[String] = None,
                               isLandExchanged: Option[String] = None,
                               exLandHouseNumber: Option[String] = None,
                               exLandAddress1: Option[String] = None,
                               exLandAddress2: Option[String] = None,
                               exLandAddress3: Option[String] = None,
                               exLandAddress4: Option[String] = None,
                               exLandPostcode: Option[String] = None,
                               agreedDeferPay: Option[String] = None,
                               postTransactionRulingApplied: Option[String] = None,
                               isPursuantToPreviousOption: Option[String] = None,
                               restAffectInt: Option[String] = None,
                               restDetails: Option[String] = None,
                               postTransactionRulingFollowed: Option[String] = None,
                               isPartOfSaleOfBusiness: Option[String] = None,
                               totalConsiderationOfBusiness: Option[String] = None
                             )

object TransactionPayload {
  implicit val format: OFormat[TransactionPayload] = Json.format[TransactionPayload]
}
