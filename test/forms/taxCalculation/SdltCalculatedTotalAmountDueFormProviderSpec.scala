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

class SdltCalculatedTotalAmountDueFormProviderSpec extends StringFieldBehaviours {

  private val requiredKey           = "taxCalculation.totalAmountDue.error.required"
  private val invalidNumericKey     = "taxCalculation.totalAmountDue.error.invalidNumeric"
  private val invalidWholeNumberKey = "taxCalculation.totalAmountDue.error.invalidWholeNumber"
  private val maxValueKey           = "taxCalculation.totalAmountDue.error.maximum"

  private val form = new SdltCalculatedTotalAmountDueFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like mandatoryField(form, fieldName, requiredError = FormError(fieldName, requiredKey))

    behave like fieldThatBindsValidData(form, fieldName, oneOf(Seq("0", "300", "9999999999", "8,000,000")))

    "must fail with invalidNumeric for non-numeric input" in {
      Seq("abc", "-1", "$300").foreach { v =>
        form.bind(Map(fieldName -> v)).errors must contain only FormError(fieldName, invalidNumericKey, Seq.empty)
      }
    }

    "must fail with invalidWholeNumber for fractional input" in {
      Seq("300.1", "1.001").foreach { v =>
        form.bind(Map(fieldName -> v)).errors must contain only FormError(fieldName, invalidWholeNumberKey, Seq.empty)
      }
    }

    "must fail with maximum error when value exceeds £9,999,999,999" in {
      form.bind(Map(fieldName -> "10000000000")).errors must contain only FormError(fieldName, maxValueKey, Seq.empty)
    }

    "must normalise whole numbers to 2 decimal places" in {
      Seq("300", "300.0", "300.00").foreach { v =>
        form.bind(Map(fieldName -> v)).value mustEqual Some("300.00")
      }
    }
  }
}