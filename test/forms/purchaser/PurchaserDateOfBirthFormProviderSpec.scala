/*
 * Copyright 2025 HM Revenue & Customs
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

package forms.purchaser

import forms.behaviours.DateBehaviours
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import utils.TimeMachine

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PurchaserDateOfBirthFormProviderSpec extends DateBehaviours with MockitoSugar {

  private implicit val messages: Messages = stubMessages()

  val mockTimeMachine: TimeMachine = mock[TimeMachine]

  when(mockTimeMachine.today).thenReturn(LocalDate.of(2025, 1, 1))

  private val form = new PurchaserDateOfBirthFormProvider(mockTimeMachine)("Doe")
  private val formWithDifferentName = new PurchaserDateOfBirthFormProvider(mockTimeMachine)("Smith")

  def minDate = LocalDate.of(1900, 1, 1)

  def maxDate = mockTimeMachine.today

  private def dateFormatter = DateTimeFormatter.ofPattern("dd MM yyyy")

  ".value" - {

    val validData = datesBetween(
      min = minDate,
      max = maxDate
    )

    behave like dateField(form, "value", validData)

    behave like dateFieldWithMax(
      form = form,
      key = "value",
      max = maxDate,
      formError = FormError("value", "purchaser.dateOfBirth.error.date.range.max", Seq(maxDate.format(dateFormatter)))
    )

    behave like dateFieldWithMin(
      form = form,
      key = "value",
      min = minDate,
      formError = FormError("value", "purchaser.dateOfBirth.error.date.range.min", Seq(minDate.format(dateFormatter)))
    )

    behave like mandatoryDateField(
      form,
      "value",
      messages("purchaser.dateOfBirth.error.required.all", "Doe"),
      errorArgs = Seq("Doe")
    )

    "must fail to bind a date with missing day using day-specific key" in {
      val data = Map(
        "value.day" -> "",
        "value.month" -> "1",
        "value.year" -> "2000"
      )

      val result = form.bind(data)

      result.hasErrors mustBe true
      result.errors.head.message must include("day")
    }

    "must fail to bind a date with missing month using month-specific key" in {
      val data = Map(
        "value.day" -> "1",
        "value.month" -> "",
        "value.year" -> "2000"
      )

      val result = form.bind(data)

      result.hasErrors mustBe true
      result.errors.head.message must include("month")
    }

    "must fail to bind a date with missing year using year-specific key" in {
      val data = Map(
        "value.day" -> "1",
        "value.month" -> "1",
        "value.year" -> ""
      )

      val result = form.bind(data)

      result.hasErrors mustBe true
      result.errors.head.message must include("year")
    }

    "must fail to bind a date with two fields missing" in {
      val data = Map(
        "value.day" -> "",
        "value.month" -> "",
        "value.year" -> "2000"
      )

      val result = form.bind(data)

      result.hasErrors mustBe true
      result.errors.head.key mustBe "value"
      result.errors.head.message mustBe "purchaser.dateOfBirth.error.required.two"
      result.errors.head.args must contain("Doe")
    }

    "must fail to bind a date with invalid date" in {
      val data = Map(
        "value.day" -> "31",
        "value.month" -> "2",
        "value.year" -> "2020"
      )

      val result = form.bind(data)

      result.hasErrors mustBe true
      result.errors.head.key mustBe "value"
    }

    "must unbind a valid date" in {
      val date = LocalDate.of(2000, 5, 15)

      val filledForm = form.fill(date)

      filledForm("value.day").value.value mustEqual "15"
      filledForm("value.month").value.value mustEqual "5"
      filledForm("value.year").value.value mustEqual "2000"
    }

    "must use correct purchaser name in all required error messages" in {
      val dataWithoutDay = Map(
        "value.day" -> "",
        "value.month" -> "1",
        "value.year" -> "2000"
      )

      val resultWithSmith = formWithDifferentName.bind(dataWithoutDay)

      resultWithSmith.hasErrors mustBe true
      resultWithSmith.errors.head.key mustBe "value"
      resultWithSmith.errors.head.message must include("purchaser.dateOfBirth.error.required.day")
    }

    "must use correct purchaser name in all required error messages for two missing fields" in {
      val dataWithoutDayAndMonth = Map(
        "value.day" -> "",
        "value.month" -> "",
        "value.year" -> "2000"
      )

      val resultWithSmith = formWithDifferentName.bind(dataWithoutDayAndMonth)

      resultWithSmith.hasErrors mustBe true
      resultWithSmith.errors.head.key mustBe "value"
      resultWithSmith.errors.head.message must include("purchaser.dateOfBirth.error.required.two")
    }

    "must use correct purchaser name in max date error message" in {
      val futureDate = maxDate.plusDays(1)
      val data = Map(
        "value.day" -> futureDate.getDayOfMonth.toString,
        "value.month" -> futureDate.getMonthValue.toString,
        "value.year" -> futureDate.getYear.toString
      )

      val resultWithSmith = formWithDifferentName.bind(data)

      resultWithSmith.hasErrors mustBe true
      resultWithSmith.errors.head.key mustBe "value"
      resultWithSmith.errors.head.message must include("purchaser.dateOfBirth.error.date.range.max")
    }
  }
}