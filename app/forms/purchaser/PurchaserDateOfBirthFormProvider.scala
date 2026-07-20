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

import forms.mappings.Mappings
import play.api.data.Form
import play.api.i18n.Messages
import utils.TimeMachine

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class PurchaserDateOfBirthFormProvider @Inject()(timeMachine: TimeMachine) extends Mappings {

  private def maxDateAllowed: LocalDate = timeMachine.today
  private val minDateAllowed: LocalDate = LocalDate.of(1900, 1, 1)
  private def dateFormatter = DateTimeFormatter.ofPattern("dd MM yyyy")

  def apply(purchaserName: String)(implicit messages: Messages): Form[LocalDate] =
    Form(
      "value" -> localDate(
        invalidKey = "purchaser.dateOfBirth.error.invalid",
        allRequiredKey = messages("purchaser.dateOfBirth.error.required.all", purchaserName),
        twoRequiredKey = "purchaser.dateOfBirth.error.required.two",
        requiredKey = messages("purchaser.dateOfBirth.error.required", purchaserName),
        dayRequiredKey = Some(messages("purchaser.dateOfBirth.error.required.day", purchaserName)),
        monthRequiredKey = Some(messages("purchaser.dateOfBirth.error.required.month", purchaserName)),
        yearRequiredKey = Some(messages("purchaser.dateOfBirth.error.required.year", purchaserName)),
        args = Seq(purchaserName)
      ).verifying(
        minDate(minDateAllowed, messages("purchaser.dateOfBirth.error.date.range.min", purchaserName), minDateAllowed.format(dateFormatter))
      ).verifying(
        maxDate(maxDateAllowed, messages("purchaser.dateOfBirth.error.date.range.max", purchaserName), maxDateAllowed.format(dateFormatter))
      )
    )
}