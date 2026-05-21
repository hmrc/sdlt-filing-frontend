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

import models.{Lease, UserAnswers}
import pages.lease.{LeaseEndDatePage, LeaseStartDatePage, LeaseStartingRentEndDatePage}
import services.lease.LeaseDatesService.*

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LeaseDatesService {

  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MM yyyy")

  def leaseDatesValidation(userAnswers: UserAnswers): LeaseDatesValidationResult = {
    val lease: Option[Lease] =
      userAnswers.fullReturn
        .flatMap(_.lease)

    val leaseStartDate      = userAnswers.get(LeaseStartDatePage)
    val leaseEndDate        = userAnswers.get(LeaseEndDatePage)
    val startingRentEndDate = userAnswers.get(LeaseStartingRentEndDatePage)

    (leaseStartDate, leaseEndDate, startingRentEndDate) match {

      case (None, None, None) =>
        LeaseDateValid

      case (Some(leaseStart), Some(leaseEnd), _) if leaseEnd.isBefore(leaseStart) =>
        LeaseStartBeforeLeaseEndDate

      case (Some(leaseStart), _, Some(rentEndDate)) if leaseStart.isAfter(rentEndDate) =>
        LeaseStartBeforeRentEndDate

      case (_, Some(leaseEnd), Some(rentEndDate)) if rentEndDate.isAfter(leaseEnd) =>
        RentEndDateAfterLeaseEndDate

      case _ =>
        LeaseDateValid
    }
  }

  private def formatDate(date: String): LocalDate =
  def leaseEndDatesValidation(userAnswers: UserAnswers): LeaseEndDateValidationResult = {
    val lease: Option[Lease] =
      userAnswers.fullReturn
        .flatMap(_.lease)

    val leaseStartDate = userAnswers.get(LeaseStartDatePage)
    val leaseEndDate = userAnswers.get(LeaseEndDatePage)
    val startingRentEndDate = lease.flatMap(_.startingRentEndDate) //TODO: Post implementation of 3521 date will fetch from Page instead of fullreturn

    (leaseStartDate, leaseEndDate, startingRentEndDate) match {

      case (None, None, _) =>
        LeaseDatesEmptyInvalid

      case (Some(leaseStart), Some(leaseEnd), _) if leaseEnd.isBefore(leaseStart) =>
        LeaseEndDateBeforeLeaseStartDate

      case (_, Some(leaseEnd), Some(leaseRentStartEnd)) if leaseEnd.isBefore(formatDate(leaseRentStartEnd)) =>
        LeaseEndDateBeforeRentEndDate

      case _ =>
        LeaseEndDateValid
    }
  }

object LeaseDatesService {

  sealed trait LeaseDatesValidationResult

  case object LeaseDateValid extends LeaseDatesValidationResult

  case object LeaseStartBeforeRentEndDate extends LeaseDatesValidationResult

  case object RentEndDateAfterLeaseEndDate extends LeaseDatesValidationResult

  case object LeaseStartBeforeLeaseEndDate extends LeaseDatesValidationResult

  sealed trait LeaseEndDateValidationResult

  case object LeaseDatesEmptyInvalid extends LeaseEndDateValidationResult

  case object LeaseEndDateBeforeLeaseStartDate extends LeaseEndDateValidationResult

  case object  LeaseEndDateBeforeRentEndDate extends LeaseEndDateValidationResult

  case object LeaseEndDateValid extends LeaseEndDateValidationResult

}
