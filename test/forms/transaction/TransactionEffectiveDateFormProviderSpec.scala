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

package forms.transaction

import forms.behaviours.DateBehaviours
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import utils.TimeMachine

import java.time.LocalDate

class TransactionEffectiveDateFormProviderSpec extends DateBehaviours with MockitoSugar{

  private implicit val messages: Messages = stubMessages()

  val mockTimeMachine: TimeMachine = mock[TimeMachine]

  when(mockTimeMachine.today).thenReturn(LocalDate.of(2025, 1, 1))

  private val form = new TransactionEffectiveDateFormProvider(mockTimeMachine)()

  def minDate = mockTimeMachine.today.minusYears(24)

  def maxDate = mockTimeMachine.today.plusYears(2)

  "TransactionEffectiveDateFormProvider" - {

    "bind a valid date within the allowed range" in {

      val result = form.bind(
        Map(
          "value.day" -> "10",
          "value.month" -> "3",
          "value.year" -> "2010"
        )
      )

      result.errors mustBe empty
      result.value mustBe Some(LocalDate.of(2010, 3, 10))
    }


    "bind the minimum allowed date" in {

      val result = form.bind(
        Map(
          "value.day" -> "1",
          "value.month" -> "11",
          "value.year" -> "2003"
        )
      )

      result.errors mustBe empty
    }

    "error when date is before the minimum allowed date" in {

      val result = form.bind(
        Map(
          "value.day" -> "31",
          "value.month" -> "10",
          "value.year" -> "2003"
        )
      )

      result.errors.map(_.message) must contain("transaction.transactionEffectiveDate.error.date.range.min")
    }


    "error when date is after the maximum allowed date" in {

      val result = form.bind(
        Map(
          "value.day" -> "9",
          "value.month" -> "3",
          "value.year" -> "2025"
        )
      )

      result.errors.map(_.message) must contain("transaction.transactionEffectiveDate.error.date.range.max")
    }

    "error when all date fields are missing" in {

      val result = form.bind(Map.empty[String, String])

      result.errors.map(_.message) must contain("transaction.transactionEffectiveDate.error.required.all")
    }


    "error when two date fields are missing" in {

      val result = form.bind(
        Map(
          "value.day" -> "10"
        )
      )

      result.errors.map(_.message) must contain("transaction.transactionEffectiveDate.error.required.two")
    }


    "error when one date field is missing" in {

      val result = form.bind(
        Map(
          "value.day" -> "10",
          "value.month" -> "3"
        )
      )

      result.errors.map(_.message) must contain("transaction.transactionEffectiveDate.error.required")
    }


    "error when date is invalid" in {

      val result = form.bind(
        Map(
          "value.day" -> "31",
          "value.month" -> "2",
          "value.year" -> "2010"
        )
      )

      result.errors.map(_.message) must contain("transaction.transactionEffectiveDate.error.invalid")
    }

  }
}
