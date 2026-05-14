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
import pages.lease.LeaseStartDatePage
import services.lease.LeaseDatesService.*

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LeaseDatesService {

  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MM yyyy")

  def leaseDatesValidation(userAnswers: UserAnswers): LeaseDatesValidationResult = {
    val lease: Option[Lease] =
      userAnswers.fullReturn
        .flatMap(_.lease)

    val leaseStartDate = userAnswers.get(LeaseStartDatePage)
    val leaseEndDate = lease.flatMap(_.contractEndDate)
    val startingRentEndDate = lease.flatMap(_.startingRentEndDate)
    
    (leaseStartDate, leaseEndDate, startingRentEndDate) match {

      case (None, _, _) | (_, None, _) | (_, _, None) =>
        LeaseDateValid

      case (Some(leaseStart), _, Some(leaseRentStartEnd)) if leaseStart.isAfter(formateDate(leaseRentStartEnd)) =>
        LeaseStartBeforRentEndDate

      case (Some(leaseStart), Some(leaseEnd),_) if formateDate(leaseEnd).isBefore(leaseStart) =>
        LeaseStartBeforeLeaseEndDate

      case _ =>
        LeaseDateValid
    }
  }

  private def formateDate(date: String) = {
    LocalDate.parse(date, formatter)
  }

}

object LeaseDatesService {

  sealed trait LeaseDatesValidationResult

  case object LeaseDateValid extends LeaseDatesValidationResult

  case object LeaseStartBeforRentEndDate extends LeaseDatesValidationResult

  case object LeaseStartBeforeLeaseEndDate extends LeaseDatesValidationResult
}
