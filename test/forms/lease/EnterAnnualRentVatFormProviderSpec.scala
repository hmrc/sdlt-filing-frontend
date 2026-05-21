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

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class EnterAnnualRentVatFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "lease.enterAnnualRentVat.error.required"
  val lengthKey = "lease.enterAnnualRentVat.error.length"
  val invalidNumericKey = "lease.enterAnnualRentVat.error.invalidNumeric"
  val invalidWholeNumberKey = "lease.enterAnnualRentVat.error.invalidWholeNumber"
  val maxValueKey = "lease.enterAnnualRentVat.error.maximum"
  val maxLength = 14

  val form = new EnterAnnualRentVatFormProvider()()

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
      oneOf(Seq("0.00", "12345678.00", "300", "0", "100.0"))
    )

    "must fail with invalid numeric error with invalid values" in {
      val invalidNumericValues = Seq("-1", "abc", "$1.00")
      invalidNumericValues.foreach { v =>
        val result = form.bind(Map(fieldName -> v))
        result.errors must contain only FormError(fieldName, invalidNumericKey, Seq.empty)
      }
    }

    "must fail with invalid whole number error with invalid values" in {
      val invalidWholeNumbers = Seq("300.1", "1.001", "0.0001")
      invalidWholeNumbers.foreach { v =>
        val result = form.bind(Map(fieldName -> v))
        result.errors must contain only FormError(fieldName, invalidWholeNumberKey, Seq.empty)
      }
    }

    "must fail with maximum error when value exceeds max" in {
      val result = form.bind(Map(fieldName -> "9999999999"))
      result.errors must contain only FormError(fieldName, maxValueKey, Seq.empty)
    }

    "must convert whole numbers to 2 decimal places" in {
      val validWholeNumbers = Seq("300", "300.0", "300.00")
      validWholeNumbers.foreach { v =>
        val result = form.bind(Map(fieldName -> v))
        result.value mustEqual Some("300.00")
      }
    }

  }
}
