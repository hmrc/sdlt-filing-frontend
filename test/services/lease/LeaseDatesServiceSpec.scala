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
import models.{FullReturn, Lease}
import pages.lease.{LeaseStartDatePage, LeaseStartingRentEndDatePage, LeaseEndDatePage}

import java.time.LocalDate

import org.scalatest.matchers.must.Matchers
import services.lease.LeaseDatesService.*

class LeaseDatesServiceSpec extends SpecBase with Matchers {
  private val service = LeaseDatesService()

  val fullReturn = FullReturn(
    stornId = "1",
    returnResourceRef = "ref",
    lease = Some(Lease(contractStartDate = Some("1/2/2006")))
  )

  "LeaseStartDatePageValidation" - {

    "leaseDatesValidation method" - {


      "must return Valid when no lease start date, end date and rent starting end date are present" in {
        val result = service.leaseDatesValidation(emptyUserAnswers)
        result mustBe LeaseDateValid
      }

      "must return Valid when only lease start date is present" in {
        val userAnswers = emptyUserAnswers.set(LeaseStartDatePage, LocalDate.of(2006, 2, 1)).success.value
        val result = service.leaseDatesValidation(userAnswers)
        result mustBe LeaseDateValid
      }

      "must return Valid when only lease end date is present" in {
        val userAnswers = emptyUserAnswers.set(LeaseEndDatePage, LocalDate.of(2007, 2, 1)).success.value
        val result = service.leaseDatesValidation(userAnswers)
        result mustBe LeaseDateValid
      }

      "must return Valid when only rent starting end date is present" in {
        val userAnswers = emptyUserAnswers.set(LeaseStartingRentEndDatePage, LocalDate.of(2007, 1, 1)).success.value
        val result = service.leaseDatesValidation(userAnswers)
        result mustBe LeaseDateValid
      }

      "must return Valid when correct combination of lease start date, end date and rent starting end dates are present" in {
        val userAnswers = emptyUserAnswers.set(LeaseStartDatePage, LocalDate.of(2005, 10, 26)).success.value
          .set(LeaseStartingRentEndDatePage, LocalDate.of(2006, 10, 1)).success.value
        val result = service.leaseDatesValidation(userAnswers)
        result mustBe LeaseDateValid
      }

      "must return RentEndDateAfterLeaseEndDate when rent end date is after the lease end date" in {

        val userAnswers = emptyUserAnswers.set(LeaseStartDatePage, LocalDate.of(2005, 10, 26)).success.value
          .set(LeaseEndDatePage, LocalDate.of(2007, 2, 1)).success.value
          .set(LeaseStartingRentEndDatePage, LocalDate.of(2008, 2, 1)).success.value

        val result = service.leaseDatesValidation(userAnswers)
        result mustBe RentEndDateAfterLeaseEndDate
      }

      "must return LeaseStartBeforeRentEndDate when lease start date is after rent starting end date" in {
        val userAnswers = emptyUserAnswers.set(LeaseStartDatePage, LocalDate.of(2006, 10, 26)).success.value
          .set(LeaseStartingRentEndDatePage, LocalDate.of(2005, 10, 1)).success.value

        val result = service.leaseDatesValidation(userAnswers)
        result mustBe LeaseStartBeforeRentEndDate
      }

      "must return LeaseStartBeforeLeaseEndDate when lease end date is before lease start date (inconsistent backend data)" in {
        val userAnswers = emptyUserAnswers.set(LeaseStartDatePage, LocalDate.of(2006, 10, 26)).success.value
          .set(LeaseEndDatePage, LocalDate.of(2005, 2, 1)).success.value

        val result = service.leaseDatesValidation(userAnswers)
        result mustBe LeaseStartBeforeLeaseEndDate
      }
    }

    "leaseEndDatesValidation method" - {

      "must return LeaseEndDateValid when lease end date is not set but other dates are present" in {

        val userAnswers = emptyUserAnswers.set(LeaseStartDatePage, LocalDate.of(2026, 10, 26)).success.value
          .set(LeaseStartingRentEndDatePage, LocalDate.of(2008, 2,1)).success.value
        val result = service.leaseEndDatesValidation(userAnswers)
        result mustBe LeaseEndDateValid
      }

      "must return Invalid when lease start date present and end date are not present in full return" in {
        val leaseWithLeaseStartDate = Lease(startingRentEndDate = Some("1 02 2008"))
        val fullReturnWithLeaseValidDates = fullReturn.copy(lease = Some(leaseWithLeaseStartDate))
        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnWithLeaseValidDates)).set(LeaseStartingRentEndDatePage, LocalDate.of(2008, 2,1)).success.value
        val result = service.leaseEndDatesValidation(userAnswers)
        result mustBe LeaseDatesEmptyInvalid
      }

      "must return LeaseEndDateBeforeLeaseStartDate validation message when lease end date is before lease start date" in {
        val userAnswers = emptyUserAnswers.set(LeaseStartDatePage, LocalDate.of(2006, 10, 26)).success.value
          .set(LeaseEndDatePage, LocalDate.of(2005, 10, 26)).success.value
          .set(LeaseStartingRentEndDatePage, LocalDate.of(2007, 7, 1)).success.value
        val result = service.leaseEndDatesValidation(userAnswers)
        result mustBe LeaseEndDateBeforeLeaseStartDate
      }

      "must return LeaseEndDateBeforeRentEndDate when lease end date is before rent starting end date in full return" in {

        val userAnswers = emptyUserAnswers.set(LeaseEndDatePage, LocalDate.of(2005, 10, 26)).success.value
          .set(LeaseStartingRentEndDatePage, LocalDate.of(2008, 2,1)).success.value
        val result = service.leaseEndDatesValidation(userAnswers)
        result mustBe LeaseEndDateBeforeRentEndDate
      }
    }

  }
}
