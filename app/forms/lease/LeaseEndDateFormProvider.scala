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

import forms.mappings.Mappings
import play.api.data.Form
import play.api.i18n.Messages

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class LeaseEndDateFormProvider @Inject() extends Mappings {

  private val formatter = DateTimeFormatter.ofPattern("d MM yyyy")
  private val minDateAllowed: LocalDate = LocalDate.parse("1 12 2003", formatter)
  private val maxDateAllowed: LocalDate = LocalDate.parse("31 12 9999", formatter)

  def apply()(implicit messages: Messages): Form[LocalDate] =
    Form(
      "value" -> localDate(
        invalidKey = "lease.leaseEndDate.error.invalid",
        allRequiredKey = "lease.leaseEndDate.error.required.all",
        twoRequiredKey = "lease.leaseEndDate.error.required.two",
        requiredKey = "lease.leaseEndDate.error.required"
      ).verifying(minDate(minDateAllowed, "lease.leaseEndDate.error.date.range.min", minDateAllowed))
       .verifying(maxDate(maxDateAllowed, "lease.leaseEndDate.error.date.range.max", maxDateAllowed))
    )
}

