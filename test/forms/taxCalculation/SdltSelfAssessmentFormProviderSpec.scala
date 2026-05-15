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

package forms.taxCalculation

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class SdltSelfAssessmentFormProviderSpec extends StringFieldBehaviours {

  private val requiredKey       = "taxCalculation.sdltSelfAssessment.error.required"
  private val invalidNumericKey = "taxCalculation.sdltSelfAssessment.error.invalidNumeric"
  private val invalidWholeKey   = "taxCalculation.sdltSelfAssessment.error.invalidWholeNumber"
  private val maxValueKey       = "taxCalculation.sdltSelfAssessment.error.maximum"

  private val form      = new SdltSelfAssessmentFormProvider()()
  private val fieldName = "value"

  ".value" - {

    behave like fieldThatBindsValidData(form, fieldName, oneOf(Seq("0", "10000", "1,000,000", "9999999999", "100.00")))

    behave like mandatoryField(form, fieldName, requiredError = FormError(fieldName, requiredKey))

    "reject non-numeric input" in {
      Seq("abc", "4/5", "ten thousand").foreach { v =>
        val result = form.bind(Map(fieldName -> v))
        result.errors must contain only FormError(fieldName, invalidNumericKey, Seq.empty)
      }
    }

    "reject negative values as non-numeric" in {
      val result = form.bind(Map(fieldName -> "-100"))
      result.errors must contain only FormError(fieldName, invalidNumericKey, Seq.empty)
    }

    "reject decimals other than .00" in {
      Seq("100.5", "8646.97", "100.000").foreach { v =>
        val result = form.bind(Map(fieldName -> v))
        result.errors must contain only FormError(fieldName, invalidWholeKey, Seq.empty)
      }
    }

    "reject values above £9,999,999,999" in {
      val result = form.bind(Map(fieldName -> "10000000000"))
      result.errors must contain only FormError(fieldName, maxValueKey, Seq.empty)
    }

    "normalise whole numbers to 2 decimal places" in {
      Seq("670", "670.0", "670.00").foreach { v =>
        form.bind(Map(fieldName -> v)).value mustEqual Some("670.00")
      }
    }

    "strip commas, spaces and £ before validating" in {
      form.bind(Map(fieldName -> "£ 1,000,000")).value mustEqual Some("1000000.00")
    }

    "normalise valid inputs to a value whose length does not exceed 16 characters" in {
      Seq("0", "10000", "1,000,000", "9999999999", "100.00").foreach { v =>
        form.bind(Map(fieldName -> v)).value.value.length must be <= 16
      }
    }
  }
}
