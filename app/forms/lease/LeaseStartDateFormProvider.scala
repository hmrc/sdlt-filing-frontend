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

class LeaseStartDateFormProvider @Inject() extends Mappings {
  private val formatter = DateTimeFormatter.ofPattern("d MM yyyy")
  private val minDateAllowed: LocalDate = LocalDate.parse("1 01 1500", formatter)

  def apply()(implicit messages: Messages): Form[LocalDate] =
    Form(
      "value" -> localDate(
        invalidKey     = "lease.leaseStartDate.error.invalid",
        allRequiredKey = "lease.leaseStartDate.error.required.all",
        twoRequiredKey = "lease.leaseStartDate.error.required.two",
        requiredKey    = "lease.leaseStartDate.error.required"
      ).verifying(minDate(minDateAllowed, "lease.leaseStartDate.error.date.range.min", minDateAllowed))
    )
}
