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

package services.lease

import base.SpecBase
import constants.FullReturnConstants.{completeFullReturn, completeTransaction}
import models.{Lease, UserAnswers}
import org.scalatestplus.mockito.MockitoSugar
import pages.lease.*
import models.lease.*

import java.time.LocalDate


class PopulateLeaseServiceSpec extends SpecBase with MockitoSugar {

  val service = new PopulateLeaseService()

  val userAnswers: UserAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")

  val leaseComplete = Lease(
    leaseType = Some("R"),
    contractStartDate = Some("01/01/2024"),
    contractEndDate = Some("01/01/2025"),
    rentFreePeriod = Some("50"),
    startingRent = Some("50.00"),
    startingRentEndDate = Some("31/12/2024"),
    laterRentKnown = Some("yes"),
    isAnnualRentOver1000 = Some("yes"),
    VATAmount = Some("60.00"),
    totalPremiumPayable = Some("80.00"),
    netPresentValue = Some("100.00")
  )

  val leaseCompleteNo = Lease(
    leaseType = Some("R"),
    contractStartDate = Some("01/01/2024"),
    contractEndDate = Some("01/01/2025"),
    rentFreePeriod = Some("50"),
    startingRent = Some("50.00"),
    startingRentEndDate = Some("31/12/2024"),
    laterRentKnown = Some("no"),
    isAnnualRentOver1000 = Some("no"),
    VATAmount = Some("60.00"),
    totalPremiumPayable = Some("80.00"),
    netPresentValue = Some("100.00")
  )

  val userAnswersTransactionTypeL: UserAnswers = emptyUserAnswers.copy(
    fullReturn = Some(completeFullReturn.copy(
      transaction = Some(completeTransaction.copy(
        transactionDescription = Some("L"))))))

  "PopulateLeaseService" - {

    ".populateLeaseInSession" - {

      "must populate TypeOfLeasePage when a valid leaseType is provided" in {
        val result = service.populateLeaseInSession(leaseComplete, emptyUserAnswers)

        result.isSuccess mustBe true
        result.get.get(TypeOfLeasePage) mustBe Some(TypeOfLease.R)
      }

      "must return a Failure when lease type is missing" in {

        val lease = leaseComplete.copy(leaseType = None)
        val result = service.populateLeaseInSession(lease, emptyUserAnswers)

        result.isFailure mustBe true
        result.failed.get.getMessage mustBe "Lease is missing required lease type"
      }

      "must populate leaseStartDatePage when a valid date is provided" in {
        val result = service.populateLeaseInSession(leaseComplete, emptyUserAnswers)

        result.isSuccess mustBe true
        result.get.get(LeaseStartDatePage) mustBe Some(LocalDate.of(2024, 01, 01))
      }

      "must return a Failure when leaseStartDate is missing" in {

        val lease = leaseComplete.copy(contractStartDate = None)
        val result = service.populateLeaseInSession(lease, emptyUserAnswers)

        result.isFailure mustBe true
        result.failed.get.getMessage mustBe "Lease is missing required contract start date"
      }

      "must populate leaseEndDatePage when a valid date is provided" in {
        val result = service.populateLeaseInSession(leaseComplete, emptyUserAnswers)

        result.isSuccess mustBe true
        result.get.get(LeaseEndDatePage) mustBe Some(LocalDate.of(2025, 01, 01))
      }

      "must return a Failure when leaseEndDate is missing" in {

        val lease = leaseComplete.copy(contractEndDate = None)
        val result = service.populateLeaseInSession(lease, emptyUserAnswers)

        result.isFailure mustBe true
        result.failed.get.getMessage mustBe "Lease is missing required contract end date"
      }

      "must populate rentFreePeriodPages when a valid answers are provided" in {
        val result = service.populateLeaseInSession(leaseComplete, emptyUserAnswers)

        result.isSuccess mustBe true
        result.get.get(DoesLeaseIncludeRentFreePeriodPage) mustBe Some(true)
        result.get.get(LeaseEnterRentFreePeriodPage) mustBe Some("50")
      }

      "must set DoesLeaseIncludeRentFreePeriodPage to false when rentFreePeriod is missing" in {

        val lease = leaseComplete.copy(rentFreePeriod = None)
        val result = service.populateLeaseInSession(lease, emptyUserAnswers)

        result.isSuccess mustBe true
        result.get.get(DoesLeaseIncludeRentFreePeriodPage) mustBe Some(false)
      }

      "must populate annualStartingRentPage when a valid rent is provided" in {
        val result = service.populateLeaseInSession(leaseComplete, emptyUserAnswers)

        result.isSuccess mustBe true
        result.get.get(AnnualStartingRentPage) mustBe Some("50.00")
      }

      "must return a Failure when annualStartingRent is missing" in {

        val lease = leaseComplete.copy(startingRent = None)
        val result = service.populateLeaseInSession(lease, emptyUserAnswers)

        result.isFailure mustBe true
        result.failed.get.getMessage mustBe "Lease is missing required starting rent"
      }

      "must populate leaseStartingRentEndDatePage when a valid date is provided" in {
        val result = service.populateLeaseInSession(leaseComplete, emptyUserAnswers)

        result.isSuccess mustBe true
        result.get.get(LeaseStartingRentEndDatePage) mustBe Some(LocalDate.of(2024, 12, 31))
      }

      "must return a Failure when leaseStartingRentEndDate is missing" in {

        val lease = leaseComplete.copy(startingRentEndDate = None)
        val result = service.populateLeaseInSession(lease, emptyUserAnswers)

        result.isFailure mustBe true
        result.failed.get.getMessage mustBe "Lease is missing required starting rent end date"
      }

      "must populate laterRentPage when a valid YES is provided" in {
        val result = service.populateLeaseInSession(leaseComplete, emptyUserAnswers)

        result.isSuccess mustBe true
        result.get.get(LaterRentPage) mustBe Some(true)
      }

      "must populate laterRentPage when a valid NO is provided" in {
        val result = service.populateLeaseInSession(leaseCompleteNo, emptyUserAnswers)

        result.isSuccess mustBe true
        result.get.get(LaterRentPage) mustBe Some(false)
      }

      "must return a Failure when laterRent is missing" in {

        val lease = leaseComplete.copy(laterRentKnown = None)
        val result = service.populateLeaseInSession(lease, emptyUserAnswers)

        result.isFailure mustBe true
        result.failed.get.getMessage mustBe "Lease is missing required later rent known"
      }

      "must populate leaseThousandPoundsThresholdPage when a YES is provided" in {
        val result = service.populateLeaseInSession(leaseComplete, emptyUserAnswers)

        result.isSuccess mustBe true
        result.get.get(LeaseThousandPoundsThresholdPage) mustBe Some(true)
      }

      "must populate leaseThousandPoundsThresholdPage when a valid NO is provided" in {
        val result = service.populateLeaseInSession(leaseCompleteNo, emptyUserAnswers)

        result.isSuccess mustBe true
        result.get.get(LeaseThousandPoundsThresholdPage) mustBe Some(false)
      }

      "must return None when leaseThousandPoundsThreshold is missing" in {

        val lease = leaseComplete.copy(isAnnualRentOver1000 = None)
        val result = service.populateLeaseInSession(lease, emptyUserAnswers)

        result.isFailure mustBe false
      }

      "must populate annualRentVatPages when a valid answers are provided" in {
        val result = service.populateLeaseInSession(leaseComplete, emptyUserAnswers)

        result.isSuccess mustBe true
        result.get.get(LeaseIsVatPayablePage) mustBe Some(true)
        result.get.get(EnterAnnualRentVatPage) mustBe Some("60.00")
      }

      "must set LeaseIsVatPayablePage to false when vatAmount is missing" in {

        val lease = leaseComplete.copy(VATAmount = None)
        val result = service.populateLeaseInSession(lease, emptyUserAnswers)

        result.isSuccess mustBe true
        result.get.get(LeaseIsVatPayablePage) mustBe Some(false)
      }

      "must populate grantOfLeasePages when a valid answers are provided" in {

        val result = service.populateLeaseInSession(leaseComplete, userAnswersTransactionTypeL)

        result.isSuccess mustBe true
        result.get.get(LeaseEnterTotalPremiumPayablePage) mustBe Some("80.00")
        result.get.get(LeaseNetPresentValuePage) mustBe Some("100.00")
      }

      "must return a Failure when leaseEnterTotalPremiumPayable is missing" in {

        val lease = leaseComplete.copy(totalPremiumPayable = None)
        val result = service.populateLeaseInSession(lease, userAnswersTransactionTypeL)

        result.isFailure mustBe true
        result.failed.get.getMessage mustBe "Lease is missing required total premium payable"
      }

      "must return a Failure when leaseNetPresentValuePage is missing" in {

        val lease = leaseComplete.copy(netPresentValue = None)
        val result = service.populateLeaseInSession(lease, userAnswersTransactionTypeL)

        result.isFailure mustBe true
        result.failed.get.getMessage mustBe "Lease is missing required net present value"
      }

    }
  }
}