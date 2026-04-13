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

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class TotalConsiderationOfTransactionFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "transaction.totalConsiderationOfTransaction.error.required"
  val invalidNumericKey = "transaction.totalConsiderationOfTransaction.error.invalidNumeric"
  val invalidWholeNumberKey = "transaction.totalConsiderationOfTransaction.error.invalidWholeNumber"
  val maxValueKey = "transaction.totalConsiderationOfTransaction.error.maximum"

  val form = new TotalConsiderationOfTransactionFormProvider()(_ => true, _ => true)
  val formWithInvalidVat = new TotalConsiderationOfTransactionFormProvider()(_ => false, _ => true)
  val formWithInvalidTotalLinkedConsideration = new TotalConsiderationOfTransactionFormProvider()(_ => true, _ => false)

  ".value" - {

    val fieldName = "value"

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      oneOf(Seq("0.00", "123456789.00", "300", "0", "100.0"))
    )

    "fail with invalid numeric error with invalid values" in {
      val invalidNumericValues = Seq("-1", "abc", "$1.00")
      invalidNumericValues.foreach { v =>
        val result = form.bind(Map(fieldName -> v))
        result.errors must contain only FormError(fieldName, invalidNumericKey, Seq.empty)
      }
    }

    "fail with invalid whole number error with invalid values" in {
      val invalidWholeNumbers = Seq("300.1", "1.001", "0.0001")
      invalidWholeNumbers.foreach { v =>
        val result = form.bind(Map(fieldName -> v))
        result.errors must contain only FormError(fieldName, invalidWholeNumberKey, Seq.empty)
      }
    }

    "fail with maximum error when value exceeds max" in {
      val result = form.bind(Map(fieldName -> "10000000000"))
      result.errors must contain only FormError(fieldName, maxValueKey, Seq.empty)
    }

    "must convert whole numbers to 2 decimal places" in {
      val validWholeNumbers = Seq("300", "300.0", "300.00")
      validWholeNumbers.foreach { v =>
        val result = form.bind(Map(fieldName -> v))
        result.value mustEqual Some("300.00")
      }
    }

    "must fail with vat included in total consideration error when the predicate is false" in {
      val result = formWithInvalidVat.bind(Map(fieldName -> "300"))
      result.errors must contain only FormError(fieldName, "transaction.totalConsiderationOfTransaction.error.vatIncludedInTotalConsideration", Seq.empty)
    }

    "must fail with total consideration error when the predicate is false" in {
      val result = formWithInvalidTotalLinkedConsideration.bind(Map(fieldName -> "300"))
      result.errors must contain only FormError(fieldName, "transaction.totalConsiderationOfTransaction.error.totalConsideration", Seq.empty)
    }
  }
}
