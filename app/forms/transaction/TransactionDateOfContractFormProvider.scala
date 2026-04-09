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
import javax.inject.Inject

class TransactionDateOfContractFormProvider @Inject(timeMachine: TimeMachine) extends Mappings {
  
  private def maxDateAllowed: LocalDate = timeMachine.today
  private def minDateAllowed: LocalDate = LocalDate.of(1900, 1, 1)

  def apply()(implicit messages: Messages): Form[LocalDate] =
    Form(
      "value" -> localDate(
        invalidKey     = "transaction.transactionDateOfContract.error.invalid",
        allRequiredKey = "transaction.transactionDateOfContract.error.required.all",
        twoRequiredKey = "transaction.transactionDateOfContract.error.required.two",
        requiredKey    = "transaction.transactionDateOfContract.error.required"
      )
        .verifying(minDate(minDateAllowed, "transaction.transactionDateOfContract.error.date.range.min", minDateAllowed))
        .verifying(maxDate(maxDateAllowed, "transaction.transactionDateOfContract.error.date.range.max", maxDateAllowed))
    )
}
