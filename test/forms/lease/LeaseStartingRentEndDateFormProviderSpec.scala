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

package forms.lease

import forms.behaviours.DateBehaviours
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

import java.time.LocalDate

class LeaseStartingRentEndDateFormProviderSpec extends DateBehaviours {

  private implicit val messages: Messages = stubMessages()

  private val form = new LeaseStartingRentEndDateFormProvider()()

  "LeaseStartingRentEndDateFormProvider" - {

    "bind a valid date" in {

      val result = form.bind(
        Map(
          "value.day"   -> "27",
          "value.month" -> "3",
          "value.year"  -> "2023"
        )
      )

      result.errors mustBe empty
      result.value mustBe Some(LocalDate.of(2023, 3, 27))
    }

    "bind the minimum allowed date (1 January 1900)" in {

      val result = form.bind(
        Map(
          "value.day"   -> "1",
          "value.month" -> "1",
          "value.year"  -> "1900"
        )
      )

      result.errors mustBe empty
    }

    "error when date is before 1 January 1900" in {

      val result = form.bind(
        Map(
          "value.day"   -> "31",
          "value.month" -> "12",
          "value.year"  -> "1899"
        )
      )

      result.errors.map(_.message) must contain("lease.leaseStartingRentEndDate.error.date.range.min")
    }

    "error when all date fields are missing" in {

      val result = form.bind(Map.empty[String, String])

      result.errors.map(_.message) must contain("lease.leaseStartingRentEndDate.error.required.all")
    }

    "error when two date fields are missing" in {

      val result = form.bind(
        Map(
          "value.day" -> "27"
        )
      )

      result.errors.map(_.message) must contain("lease.leaseStartingRentEndDate.error.required.two")
    }

    "error when one date field is missing" in {

      val result = form.bind(
        Map(
          "value.day"   -> "27",
          "value.month" -> "3"
        )
      )

      result.errors.map(_.message) must contain("lease.leaseStartingRentEndDate.error.required")
    }

    "error when date is invalid" in {

      val result = form.bind(
        Map(
          "value.day"   -> "31",
          "value.month" -> "2",
          "value.year"  -> "2023"
        )
      )

      result.errors.map(_.message) must contain("lease.leaseStartingRentEndDate.error.invalid")
    }
  }
}
