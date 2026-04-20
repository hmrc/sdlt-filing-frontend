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

import javax.inject.Inject

class TotalConsiderationOfLinkedTransactionFormProvider @Inject() extends Mappings {

  private val maxValue = BigDecimal(9999999999L)

  def apply(totalConsiderationValidated: String => Boolean): Form[String] =
    Form(
      "value" -> wholeNumberCurrency(
        requiredKey = "transaction.totalConsiderationOfLinkedTransaction.error.required",
        invalidNumericKey = "transaction.totalConsiderationOfLinkedTransaction.error.invalidNumeric",
        invalidWholeNumberKey = "transaction.totalConsiderationOfLinkedTransaction.error.invalidWholeNumber",
        maxValueKey = "transaction.totalConsiderationOfLinkedTransaction.error.maximum",
        maxValue = maxValue
      ).verifying(isPredicateTrue(totalConsiderationValidated, "transaction.totalConsiderationOfLinkedTransaction.error.totalConsideration"))
    )
}
