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

package forms.taxCalculation.leaseholdSelfAssessed

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class LeaseholdSelfAssessedPremiumPayableTaxFormProviderSpec extends StringFieldBehaviours {

  val emptyValue = "taxCalculation.leaseholdSelfAssessed.premiumPayable.error.emptyValue"
  val invalidNumeric = "taxCalculation.leaseholdSelfAssessed.premiumPayable.error.invalidNumeric"
  val invalidWholeNumber = "taxCalculation.leaseholdSelfAssessed.premiumPayable.error.invalidWholeNumber"
  val maxValue = "taxCalculation.leaseholdSelfAssessed.premiumPayable.error.maxValue"
  val maxLengthText = "taxCalculation.leaseholdSelfAssessed.premiumPayable.error.maxLength"
  val maxLengthNum = 16

  val form = new LeaseholdSelfAssessedPremiumPayableTaxFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      oneOf(Seq("0.00", "4", "8,000,000", "1000000.00", "100.0"))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, emptyValue)
    )
    

    "fail valid numeric validation" in {
      val invalid = Seq("abc", "-234567", "4/5")
      invalid.foreach { i =>
        val result = form.bind(Map(fieldName -> i))
        result.errors must contain only FormError(fieldName, invalidNumeric, Seq.empty)
      }
    }

    "fail valid whole number validation" in {
      val invalid = Seq("00.000", "8646.97", "100000000.9876")
      invalid.foreach { i =>
        val result = form.bind(Map(fieldName -> i))
        result.errors must contain only FormError(fieldName, invalidWholeNumber, Seq.empty)
      }
    }

    "fail max value error validation" in {
      val result = form.bind(Map(fieldName -> "100000000000"))
      result.errors must contain only FormError(fieldName, maxValue, Seq.empty)
    }

    "round to 2 decimal places" in {
      val rounding = Seq("670", "670.0", "670.00")
      rounding.foreach { r =>
        val result = form.bind(Map(fieldName -> r))
        result.value mustEqual Some("670.00")
      }
    }
  }
}
