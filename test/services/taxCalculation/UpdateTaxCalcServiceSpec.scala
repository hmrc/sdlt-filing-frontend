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

package services.taxCalculation

import base.SpecBase
import constants.FullReturnConstants
import constants.FullReturnConstants.{completeFullReturn, completeTransaction}
import models.UserAnswers
import models.land.LandTypeOfProperty
import models.prelimQuestions.TransactionType
import models.taxCalculation.UpdateTaxCalculationRequest
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.land.LandTypeOfPropertyPage
import pages.lease.{LeaseNetPresentValuePage, LeaseStartDatePage}
import pages.transaction.{ClaimingPartialReliefAmountPage, TotalConsiderationOfTransactionPage, TypeOfTransactionPage}
import pages.ukResidency.NonUkResidentPurchaserPage

import java.time.LocalDate

class UpdateTaxCalcServiceSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  val service = new UpdateTaxCalcService()

  override def beforeEach(): Unit = {
    super.beforeEach()
  }

  "UpdateTaxCalcService" - {

    "updateTaxCalcRequest" - {

      "must return UpdateTaxCalculationRequest when fullReturn exists" in {
        val ua = UserAnswers(userAnswersId, storn = FullReturnConstants.completeFullReturn.stornId)
          .copy(fullReturn = Some(FullReturnConstants.completeFullReturn))

        val result = service.updateTaxCalcRequest(ua).futureValue

        result mustBe an[UpdateTaxCalculationRequest]
        result.stornId mustEqual FullReturnConstants.completeFullReturn.stornId
        result.returnResourceRef mustEqual FullReturnConstants.completeFullReturn.returnResourceRef
        result.amountPaid mustEqual FullReturnConstants.completeTaxCalculation.amountPaid
        result.includesPenalty mustEqual FullReturnConstants.completeTaxCalculation.includesPenalty
        result.taxDue mustEqual None
      }

      "must fail when fullReturn does not exist" in {
        val ua = UserAnswers(userAnswersId, storn = "TEST_STORN").copy(fullReturn = None)

        val result = service.updateTaxCalcRequest(ua)

        whenReady(result.failed) { exception =>
          exception.getMessage mustEqual "No full return found"
        }
      }
    }

    "transactionDataMatches" - {

      "must return true when transaction check fails and taxCalc exists" in {
        val fullReturn = FullReturnConstants.completeFullReturn.copy(
          transaction = Some(FullReturnConstants.completeTransaction.copy(transactionDescription = Some("F"))),
          taxCalculation = Some(FullReturnConstants.completeTaxCalculation)
        )
        val ua = UserAnswers(userAnswersId, storn = fullReturn.stornId)
          .copy(fullReturn = Some(fullReturn))
          .set(TypeOfTransactionPage, TransactionType.GrantOfLease).success.value

        service.transactionDataMatches(ua) mustEqual true
      }

      "must return false when all transaction checks pass and taxCalc exists" in {
        val fullReturn = FullReturnConstants.completeFullReturn.copy(
          transaction = Some(FullReturnConstants.completeTransaction.copy(transactionDescription = Some("F"))),
          taxCalculation = Some(FullReturnConstants.completeTaxCalculation)
        )
        val ua = UserAnswers(userAnswersId, storn = fullReturn.stornId)
          .copy(fullReturn = Some(fullReturn))
          .set(TypeOfTransactionPage, TransactionType.ConveyanceTransfer).success.value

        service.transactionDataMatches(ua) mustEqual false
      }

      "must return false when transaction checks fail but taxCalc doesn't exist" in {
        val fullReturn = FullReturnConstants.completeFullReturn.copy(
          transaction = Some(FullReturnConstants.completeTransaction.copy(transactionDescription = Some("F"))),
          taxCalculation = None
        )
        val ua = UserAnswers(userAnswersId, storn = fullReturn.stornId)
          .copy(fullReturn = Some(fullReturn))
          .set(TypeOfTransactionPage, TransactionType.GrantOfLease).success.value

        service.transactionDataMatches(ua) mustEqual false
      }
    }

    "leaseDataMatches" - {

      "must return true when lease check fails and taxCalc exists" in {
        val fullReturn = FullReturnConstants.completeFullReturn.copy(
          lease = Some(FullReturnConstants.completeLease.copy(contractStartDate = Some("01/01/2025"))),
          taxCalculation = Some(FullReturnConstants.completeTaxCalculation)
        )
        val ua = UserAnswers(userAnswersId, storn = fullReturn.stornId)
          .copy(fullReturn = Some(fullReturn))
          .set(LeaseStartDatePage, LocalDate.of(2025, 1, 2)).success.value

        service.leaseDataMatches(ua) mustEqual true
      }

      "must return false when all lease checks pass and taxCalc exists" in {
        val fullReturn = FullReturnConstants.completeFullReturn.copy(
          lease = Some(FullReturnConstants.completeLease.copy(contractStartDate = Some("01/01/2025"))),
          taxCalculation = Some(FullReturnConstants.completeTaxCalculation)
        )
        val ua = UserAnswers(userAnswersId, storn = fullReturn.stornId)
          .copy(fullReturn = Some(fullReturn))
          .set(LeaseStartDatePage, LocalDate.of(2025, 1, 1)).success.value

        service.leaseDataMatches(ua) mustEqual false
      }

      "must return false when lease checks fail but taxCalc doesn't exist" in {
        val fullReturn = FullReturnConstants.completeFullReturn.copy(
          lease = Some(FullReturnConstants.completeLease.copy(contractStartDate = Some("01/01/2025"))),
          taxCalculation = None
        )
        val ua = UserAnswers(userAnswersId, storn = fullReturn.stornId)
          .copy(fullReturn = Some(fullReturn))
          .set(LeaseStartDatePage, LocalDate.of(2025, 1, 2)).success.value

        service.leaseDataMatches(ua) mustEqual false
      }
    }
    "transactionDataMatches with BigDecimal comparison" - {

      "must handle totalConsideration with commas" in {
        val fullReturn = FullReturnConstants.completeFullReturn.copy(
          transaction = Some(FullReturnConstants.completeTransaction.copy(totalConsideration = Some("1,500,000.50"))),
          taxCalculation = Some(FullReturnConstants.completeTaxCalculation)
        )
        val ua = UserAnswers(userAnswersId, storn = fullReturn.stornId)
          .copy(fullReturn = Some(fullReturn))
          .set(TypeOfTransactionPage, TransactionType.ConveyanceTransfer).success.value
          .set(TotalConsiderationOfTransactionPage, "1500000.50").success.value

        service.transactionDataMatches(ua) mustEqual false
      }

      "must detect totalConsideration mismatch with different decimal places" in {
        val fullReturn = FullReturnConstants.completeFullReturn.copy(
          transaction = Some(FullReturnConstants.completeTransaction.copy(totalConsideration = Some("1500000.00"))),
          taxCalculation = Some(FullReturnConstants.completeTaxCalculation)
        )
        val ua = UserAnswers(userAnswersId, storn = fullReturn.stornId)
          .copy(fullReturn = Some(fullReturn))
          .set(TypeOfTransactionPage, TransactionType.ConveyanceTransfer).success.value
          .set(TotalConsiderationOfTransactionPage, "1500000.50").success.value

        service.transactionDataMatches(ua) mustEqual true
      }

      "must handle reliefAmount with commas and match correctly" in {
        val fullReturn = FullReturnConstants.completeFullReturn.copy(
          transaction = Some(FullReturnConstants.completeTransaction.copy(
            transactionDescription = Some("F"),
            reliefAmount = Some("50,000.75")
          )),
          taxCalculation = Some(FullReturnConstants.completeTaxCalculation)
        )
        val ua = UserAnswers(userAnswersId, storn = fullReturn.stornId)
          .copy(fullReturn = Some(fullReturn))
          .set(TypeOfTransactionPage, TransactionType.ConveyanceTransfer).success.value
          .set(ClaimingPartialReliefAmountPage, "50000.75").success.value

        service.transactionDataMatches(ua) mustEqual false
      }

      "must detect reliefAmount mismatch with commas" in {
        val fullReturn = FullReturnConstants.completeFullReturn.copy(
          transaction = Some(FullReturnConstants.completeTransaction.copy(
            transactionDescription = Some("F"),
            reliefAmount = Some("50,000.75")
          )),
          taxCalculation = Some(FullReturnConstants.completeTaxCalculation)
        )
        val ua = UserAnswers(userAnswersId, storn = fullReturn.stornId)
          .copy(fullReturn = Some(fullReturn))
          .set(TypeOfTransactionPage, TransactionType.ConveyanceTransfer).success.value
          .set(ClaimingPartialReliefAmountPage, "60000.75").success.value

        service.transactionDataMatches(ua) mustEqual true
      }
    }

    "leaseDataMatches with BigDecimal comparison" - {

      "must handle netPresentValue with commas and match correctly" in {

        val ua: UserAnswers = emptyUserAnswers.copy(
          fullReturn = Some(completeFullReturn.copy(
            lease = Some(FullReturnConstants.completeLease.copy(netPresentValue = Some("95,000.00")))
            ,
            taxCalculation = Some(FullReturnConstants.completeTaxCalculation),

              transaction = Some(completeTransaction.copy(
              transactionDescription = Some("L")))))).set(LeaseNetPresentValuePage, "95000.00").success.value

        service.leaseDataMatches(ua) mustEqual false
      }

      "must detect netPresentValue mismatch with commas" in {
        val fullReturn = FullReturnConstants.completeFullReturn.copy(
          lease = Some(FullReturnConstants.completeLease.copy(netPresentValue = Some("95,000.50"))),
          taxCalculation = Some(FullReturnConstants.completeTaxCalculation)
        )
        val ua = UserAnswers(userAnswersId, storn = fullReturn.stornId)
          .copy(fullReturn = Some(fullReturn))
          .set(LeaseStartDatePage, LocalDate.of(2025, 1, 1)).success.value
          .set(LeaseNetPresentValuePage, "95,000.00").success.value

        service.leaseDataMatches(ua) mustEqual true
      }

      "must handle netPresentValue without commas" in {
        val ua: UserAnswers = emptyUserAnswers.copy(
          fullReturn = Some(completeFullReturn.copy(
            lease = Some(FullReturnConstants.completeLease.copy(netPresentValue = Some("95000.00")))
            ,
            taxCalculation = Some(FullReturnConstants.completeTaxCalculation),

            transaction = Some(completeTransaction.copy(
              transactionDescription = Some("L")))))).set(LeaseNetPresentValuePage, "95000.00").success.value

        service.leaseDataMatches(ua) mustEqual false
      }
    }

    "residencyDataMatches" - {

      "must return true when residency check fails and taxCalc exists" in {
        val fullReturn = FullReturnConstants.completeFullReturn.copy(
          residency = Some(FullReturnConstants.completeResidency.copy(isNonUkResidents = Some("yes"))),
          taxCalculation = Some(FullReturnConstants.completeTaxCalculation)
        )
        val ua = UserAnswers(userAnswersId, storn = fullReturn.stornId)
          .copy(fullReturn = Some(fullReturn))
          .set(NonUkResidentPurchaserPage, false).success.value

        service.residencyDataMatches(ua) mustEqual true
      }

      "must return false when residency check passes and taxCalc exists" in {
        val fullReturn = FullReturnConstants.completeFullReturn.copy(
          residency = Some(FullReturnConstants.completeResidency.copy(isNonUkResidents = Some("yes"))),
          taxCalculation = Some(FullReturnConstants.completeTaxCalculation)
        )
        val ua = UserAnswers(userAnswersId, storn = fullReturn.stornId)
          .copy(fullReturn = Some(fullReturn))
          .set(NonUkResidentPurchaserPage, true).success.value

        service.residencyDataMatches(ua) mustEqual false
      }

      "must return false when residency check fails but taxCalc doesn't exist" in {
        val fullReturn = FullReturnConstants.completeFullReturn.copy(
          residency = Some(FullReturnConstants.completeResidency.copy(isNonUkResidents = Some("yes"))),
          taxCalculation = None
        )
        val ua = UserAnswers(userAnswersId, storn = fullReturn.stornId)
          .copy(fullReturn = Some(fullReturn))
          .set(NonUkResidentPurchaserPage, false).success.value

        service.residencyDataMatches(ua) mustEqual false
      }
    }

    "landDataMatches" - {

      "must return true when land check fails and taxCalc exists" in {
        val fullReturn = FullReturnConstants.completeFullReturn.copy(
          land = Some(Seq(FullReturnConstants.completeLand.copy(propertyType = Some("01")))),
          returnInfo = Some(FullReturnConstants.completeReturnInfo.copy(mainLandID = Some("LND001"))),
          taxCalculation = Some(FullReturnConstants.completeTaxCalculation)
        )
        val ua = UserAnswers(userAnswersId, storn = fullReturn.stornId)
          .copy(fullReturn = Some(fullReturn))
          .set(LandTypeOfPropertyPage, LandTypeOfProperty.Mixed).success.value

        service.landDataMatches(ua) mustEqual true
      }

      "must return false when all land checks pass and taxCalc exists" in {
        val fullReturn = FullReturnConstants.completeFullReturn.copy(
          land = Some(Seq(FullReturnConstants.completeLand.copy(propertyType = Some("01")))),
          returnInfo = Some(FullReturnConstants.completeReturnInfo.copy(mainLandID = Some("LND001"))),
          taxCalculation = Some(FullReturnConstants.completeTaxCalculation)
        )
        val ua = UserAnswers(userAnswersId, storn = fullReturn.stornId)
          .copy(fullReturn = Some(fullReturn))
          .set(LandTypeOfPropertyPage, LandTypeOfProperty.Residential).success.value

        service.landDataMatches(ua) mustEqual false
      }

      "must return false when land checks fail but taxCalc doesn't exist" in {
        val fullReturn = FullReturnConstants.completeFullReturn.copy(
          land = Some(Seq(FullReturnConstants.completeLand.copy(propertyType = Some("01")))),
          returnInfo = Some(FullReturnConstants.completeReturnInfo.copy(mainLandID = Some("LND001"))),
          taxCalculation = None
        )
        val ua = UserAnswers(userAnswersId, storn = fullReturn.stornId)
          .copy(fullReturn = Some(fullReturn))
          .set(LandTypeOfPropertyPage, LandTypeOfProperty.Mixed).success.value

        service.landDataMatches(ua) mustEqual false
      }
    }
  }
}