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

import forms.mappings.Mappings
import play.api.data.Form
import play.api.i18n.Messages
import utils.TimeMachine

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class TransactionEffectiveDateFormProvider @Inject(timeMachine: TimeMachine) extends Mappings {

  private val formatter = DateTimeFormatter.ofPattern("d MM yyyy")
  private val  minDateAllowed : LocalDate =  LocalDate.parse("1 11 2003", formatter)
  private def maxDateAllowed: LocalDate = timeMachine.today

  def apply()(implicit messages: Messages): Form[LocalDate] =
    Form(
      "value" -> localDate(
        invalidKey     = "transaction.transactionEffectiveDate.error.invalid",
        allRequiredKey = "transaction.transactionEffectiveDate.error.required.all",
        twoRequiredKey = "transaction.transactionEffectiveDate.error.required.two",
        requiredKey    = "transaction.transactionEffectiveDate.error.required"
      ).verifying(minDate(minDateAllowed, "transaction.transactionEffectiveDate.error.date.range.min", minDateAllowed))
       .verifying(maxDate(maxDateAllowed, "transaction.transactionEffectiveDate.error.date.range.max", maxDateAllowed.format(formatter)))

    )
}
