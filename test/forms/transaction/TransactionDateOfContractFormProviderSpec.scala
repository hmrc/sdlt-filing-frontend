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
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import utils.TimeMachine

import java.time.LocalDate

class TransactionDateOfContractFormProviderSpec extends DateBehaviours {

  private implicit val messages: Messages = stubMessages()
  private val time = TimeMachine()
  private val form = new TransactionDateOfContractFormProvider(time)()

  ".value" - {

    val maxDate = time.today
    val minDate = LocalDate.of(1900, 1, 1)

    val validData = datesBetween(
      min = minDate,
      max = maxDate
    )

    behave like dateField(form, "value", validData)

    behave like dateFieldWithMax(
      form = form,
      key = "value",
      max = maxDate,
      formError = FormError("value", "transaction.transactionDateOfContract.error.date.range.max", Seq(maxDate))
    )

    behave like dateFieldWithMin(
      form = form,
      key = "value",
      min = minDate,
      formError = FormError("value", "transaction.transactionDateOfContract.error.date.range.min", Seq(minDate))
    )

    behave like mandatoryDateField(form, "value", "transaction.transactionDateOfContract.error.required.all")
  }
}
