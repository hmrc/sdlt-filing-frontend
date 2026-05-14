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

import forms.behaviours.IntFieldBehaviours
import play.api.data.FormError

class LeaseEnterRentFreePeriodFormProviderSpec extends IntFieldBehaviours {

  val requiredKey = "lease.enterRentFreePeriod.error.required"
  val maxValueKey = "lease.enterRentFreePeriod.error.maxValue"
  val minValueKey = "lease.enterRentFreePeriod.error.minValue"
  val invalidKey = "lease.enterRentFreePeriod.error.invalid"

  val form = new LeaseEnterRentFreePeriodFormProvider()()

  ".value" - {

    val fieldName = "value"

    val minimum = 1
    val maximum = 99

    val validDataGenerator = intsInRangeWithCommas(minimum, maximum)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like intField(
      form,
      fieldName,
      nonNumericError = FormError(fieldName, invalidKey),
      wholeNumberError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must not bind a value above the maximum" in {
      val result = form.bind(Map(fieldName -> "100")).apply(fieldName)
      result.errors must contain(FormError(fieldName, maxValueKey, Seq(maximum)))
    }

    "must not bind a value below the minimum" in {
      val result = form.bind(Map(fieldName -> "0")).apply(fieldName)
      result.errors must contain(FormError(fieldName, minValueKey, Seq(minimum)))
    }
  }
}
