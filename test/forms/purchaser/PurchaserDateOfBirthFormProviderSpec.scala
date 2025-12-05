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

class PurchaserDateOfBirthFormProviderSpec extends DateBehaviours with MockitoSugar{

  private implicit val messages: Messages = stubMessages()

  val mockTimeMachine: TimeMachine = mock[TimeMachine]

  when(mockTimeMachine.today).thenReturn(LocalDate.of(2025, 1, 1))

  private val form = new PurchaserDateOfBirthFormProvider(mockTimeMachine)()

  def minDate = mockTimeMachine.today.minusYears(130)

  def maxDate = mockTimeMachine.today

  private def dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

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
      formError = FormError("value", "PurchaserDateOfBirth.error.date.range.max", Seq(maxDate.format(dateFormatter)))
    )

    behave like dateFieldWithMin(
      form = form,
      key = "value",
      min = minDate,
      formError = FormError("value", "PurchaserDateOfBirth.error.date.range.min", Seq(minDate.format(dateFormatter)))
    )

    behave like mandatoryDateField(form, "value", "purchaserDateOfBirth.error.required.all")
  }
}
