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

import models.lease.*
import models.prelimQuestions.TransactionType
import models.prelimQuestions.TransactionType.GrantOfLease
import models.{Lease, UserAnswers}
import pages.lease.*

import java.time.LocalDate
import scala.util.{Failure, Success, Try}

class PopulateLeaseService {

  def populateLeaseInSession(lease: Lease, userAnswers: UserAnswers): Try[UserAnswers] =
    for {
      withTypeOfLease                           <- typeOfLeasePage(lease, userAnswers)
      withLeaseStartDate                        <- leaseStartDatePage(lease, withTypeOfLease)
      withLeaseEndDate                          <- leaseEndDatePage(lease, withLeaseStartDate)
      withRentFreePeriod                        <- rentFreePeriodPages(lease, withLeaseEndDate)
      withAnnualStartingRent                    <- annualStartingRentPage(lease, withRentFreePeriod)
      withLeaseStartingRentEndDate              <- leaseStartingRentEndDatePage(lease, withAnnualStartingRent)
      withLaterRent                             <- laterRentPage(lease, withLeaseStartingRentEndDate)
      withLeaseThousandPoundsThreshold          <- leaseThousandPoundsThresholdPage(lease, withLaterRent)
      withAnnualRentVat                         <- annualRentVatPages(lease, withLeaseThousandPoundsThreshold)
      finalAnswers                              <- grantOfLeasePages(lease, withAnnualRentVat)
    } yield finalAnswers

  private def typeOfLeasePage(lease: Lease, userAnswers: UserAnswers): Try[UserAnswers] =
    TypeOfLease.parse(lease.leaseType) match {
      case Some(leaseType) => userAnswers.set(TypeOfLeasePage, leaseType)
      case None            => Failure(new IllegalStateException("Lease is missing required lease type"))
    }

  private def leaseStartDatePage(lease: Lease, userAnswers: UserAnswers): Try[UserAnswers] =
    lease.contractStartDate match {
      case Some(dateStr) => Try(LocalDate.parse(dateStr)).flatMap(userAnswers.set(LeaseStartDatePage, _))
      case None          => Failure(new IllegalStateException("Lease is missing required contract start date"))
    }

  private def leaseEndDatePage(lease: Lease, userAnswers: UserAnswers): Try[UserAnswers] =
    lease.contractEndDate match {
      case Some(dateStr) => Try(LocalDate.parse(dateStr)).flatMap(userAnswers.set(LeaseEndDatePage, _))
      case None          => Failure(new IllegalStateException("Lease is missing required contract end date"))
    }

  private def rentFreePeriodPages(lease: Lease, userAnswers: UserAnswers): Try[UserAnswers] =
    lease.rentFreePeriod match {
      case Some(monthStr) =>
        for {
          withDoesLeaseIncludeRentFreePeriod <- userAnswers.set(DoesLeaseIncludeRentFreePeriodPage, true)
          finalAnswers                       <- withDoesLeaseIncludeRentFreePeriod.set(LeaseEnterRentFreePeriodPage, monthStr)
        } yield finalAnswers
      case None =>
        userAnswers.set(DoesLeaseIncludeRentFreePeriodPage, false)
    }

  private def annualStartingRentPage(lease: Lease, userAnswers: UserAnswers): Try[UserAnswers] =
    lease.startingRent match {
      case Some(startingRent) => userAnswers.set(AnnualStartingRentPage, startingRent)
      case None               => Failure(new IllegalStateException("Lease is missing required starting rent"))
    }

  private def leaseStartingRentEndDatePage(lease: Lease, userAnswers: UserAnswers): Try[UserAnswers] =
    lease.startingRentEndDate match {
      case Some(dateStr) => Try(LocalDate.parse(dateStr)).flatMap(userAnswers.set(LeaseStartingRentEndDatePage, _))
      case None          => Failure(new IllegalStateException("Lease is missing required starting rent end date"))
    }

  private def laterRentPage(lease: Lease, userAnswers: UserAnswers): Try[UserAnswers] =
    lease.laterRentKnown match {
      case Some(str) if str.equalsIgnoreCase("yes") => userAnswers.set(LaterRentPage, true)
      case Some(str) if str.equalsIgnoreCase("no") => userAnswers.set(LaterRentPage, false)
      case _ => Failure(new IllegalStateException("Lease is missing required later rent known"))
    }

  private def leaseThousandPoundsThresholdPage(lease: Lease, userAnswers: UserAnswers): Try[UserAnswers] =
    lease.isAnnualRentOver1000 match {
      case Some(str) if str.equalsIgnoreCase("yes") => userAnswers.set(LeaseThousandPoundsThresholdPage, true)
      case Some(str) if str.equalsIgnoreCase("no") => userAnswers.set(LeaseThousandPoundsThresholdPage, false)
      case _ => Failure(new IllegalStateException("Lease is missing required is annual rent over 1000"))
  }

  private def annualRentVatPages(lease: Lease, userAnswers: UserAnswers): Try[UserAnswers] =
    lease.VATAmount match {
      case Some(vatAmount) =>
        for {
          withAnnualRentVat <- userAnswers.set(LeaseIsVatPayablePage, true)
          finalAnswers      <- withAnnualRentVat.set(EnterAnnualRentVatPage, vatAmount)
        } yield finalAnswers
      case None =>
        userAnswers.set(LeaseIsVatPayablePage, false)
    }

  private def grantOfLeasePages(lease: Lease, userAnswers: UserAnswers): Try[UserAnswers] = {
    val isGrantOfLease = TransactionType.parse(
      userAnswers.fullReturn.flatMap(_.transaction).flatMap(_.transactionDescription)
    ).contains(GrantOfLease)

    if (!isGrantOfLease) {
      Success(userAnswers)
    } else {
      for {
        withTotalPremiumPayable <- leaseEnterTotalPremiumPayablePage(lease, userAnswers)
        finalAnswers <- leaseNetPresentValuePage(lease, withTotalPremiumPayable)
      } yield finalAnswers
    }
  }

  private def leaseEnterTotalPremiumPayablePage(lease: Lease, userAnswers: UserAnswers): Try[UserAnswers] = {
    lease.totalPremiumPayable match {
      case Some(premiumAmount) => userAnswers.set(LeaseEnterTotalPremiumPayablePage, premiumAmount)
      case None => Failure(new IllegalStateException("Lease is missing required total premium payable"))
    }
  }

  private def leaseNetPresentValuePage(lease :Lease, userAnswers: UserAnswers): Try[UserAnswers] =
    lease.netPresentValue match {
      case Some(npv) => userAnswers.set(LeaseNetPresentValuePage, npv)
      case None => Failure(new IllegalStateException("Lease is missing required net present value"))
    }
}
