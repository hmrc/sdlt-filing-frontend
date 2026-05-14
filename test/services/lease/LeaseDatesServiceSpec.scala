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
import pages.lease.LeaseStartDatePage

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

  val leaseWithLeaseStartDate = Lease(contractStartDate = Some("1/2/2006"))
  val leaesWithLeaseEndDate = Lease(contractEndDate = Some("1/2/2007"))
  val leaseWithstartingRentEndDate = Lease(startingRentEndDate = Some("1/1/2007"))

  val fullReturnWithLeaseStartDate = fullReturn.copy(lease = Some(leaseWithLeaseStartDate))
  val fullReturnWWithLeaseEndDate = fullReturn.copy(lease = Some(leaesWithLeaseEndDate))
  val fullReturnithstartingRentEndDate = fullReturn.copy(lease = Some(leaseWithstartingRentEndDate))


  "LeaseStartDatePageValidation" - {

    "must return Valid when no lease start date, end date and rent starting end date in full return" in {
      val result = service.leaseDatesValidation(emptyUserAnswers)
      result mustBe LeaseDateValid
    }

    "must return Valid when lease start date present and end date and rent starting end dates are not present in full return" in {
      emptyUserAnswers.copy(fullReturn = Some(fullReturnWithLeaseStartDate))
      val result = service.leaseDatesValidation(emptyUserAnswers)
      result mustBe LeaseDateValid
    }

    "must return Valid when lease end date present and start date and rent starting end dates are not present in full return" in {
      emptyUserAnswers.copy(fullReturn = Some(fullReturnWWithLeaseEndDate))
      val result = service.leaseDatesValidation(emptyUserAnswers)
      result mustBe LeaseDateValid
    }

    "must return Valid when rent starting end date present and lease start date and lease end dates are not present in full return" in {
      emptyUserAnswers.copy(fullReturn = Some(fullReturnithstartingRentEndDate))
      val result = service.leaseDatesValidation(emptyUserAnswers)
      result mustBe LeaseDateValid
    }

    "must return Valid when correct combination of lease start date, end date and rent starting end dates are present in full return" in {
      val leaseWithLeaseStartDate = Lease(contractEndDate = Some("1 02 2007"), startingRentEndDate = Some("1 10 2006"))
      val fullReturnWithLeaseValidDates = fullReturn.copy(lease = Some(leaseWithLeaseStartDate))
      val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnWithLeaseValidDates)).set(LeaseStartDatePage, LocalDate.of(2005, 10, 26)).success.value
      val result = service.leaseDatesValidation(userAnswers)
      result mustBe LeaseDateValid
    }

    "must return LeaseStartBeforeLeaseEndDate when lease start date is greater than lease end date are present in full return" in {
      val leaseWithLeaseStartDate = Lease(contractEndDate = Some("1 02 2005"), startingRentEndDate = Some("1 10 2007"))
      val fullReturnWithLeaseValidDates = fullReturn.copy(lease = Some(leaseWithLeaseStartDate))
      val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnWithLeaseValidDates)).set(LeaseStartDatePage, LocalDate.of(2006, 10, 26)).success.value
      val result = service.leaseDatesValidation(userAnswers)
      result mustBe LeaseStartBeforeLeaseEndDate
    }

    "must return LeaseStartBeforRentEndDate when lease start date is greater than rent starting end date are present in full return" in {
      val leaseWithLeaseStartDate = Lease(contractEndDate = Some("1 02 2008"), startingRentEndDate = Some("1 10 2006"))
      val fullReturnWithLeaseValidDates = fullReturn.copy(lease = Some(leaseWithLeaseStartDate))
      val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnWithLeaseValidDates)).set(LeaseStartDatePage, LocalDate.of(2006, 10, 26)).success.value
      val result = service.leaseDatesValidation(userAnswers)
      result mustBe LeaseStartBeforRentEndDate
    }
  }
}

