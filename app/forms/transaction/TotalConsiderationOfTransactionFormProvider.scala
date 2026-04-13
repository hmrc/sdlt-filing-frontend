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

class TotalConsiderationOfTransactionFormProvider @Inject() extends Mappings {

  private val maxValue = BigDecimal(9999999999L)

  def apply(
             vatIncludedInTotalConsiderationValidated: String => Boolean,
             totalConsiderationValidated: String => Boolean
           ): Form[String] =
    Form(
      "value" -> wholeNumberCurrency(
        requiredKey = "transaction.totalConsiderationOfTransaction.error.required",
        invalidNumericKey = "transaction.totalConsiderationOfTransaction.error.invalidNumeric",
        invalidWholeNumberKey = "transaction.totalConsiderationOfTransaction.error.invalidWholeNumber",
        maxValueKey = "transaction.totalConsiderationOfTransaction.error.maximum",
        maxValue = maxValue
      )
        .verifying(firstError(
          isPredicateTrue(vatIncludedInTotalConsiderationValidated, "transaction.totalConsiderationOfTransaction.error.vatIncludedInTotalConsideration"),
          isPredicateTrue(totalConsiderationValidated, "transaction.totalConsiderationOfTransaction.error.totalConsideration")
        ))
    )
}
