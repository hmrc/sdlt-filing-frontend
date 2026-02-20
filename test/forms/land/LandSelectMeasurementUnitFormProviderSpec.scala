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

package forms.land

import forms.behaviours.OptionFieldBehaviours
import models.land.LandSelectMeasurementUnit
import play.api.data.FormError

class LandSelectMeasurementUnitFormProviderSpec extends OptionFieldBehaviours {

  val form = new LandSelectMeasurementUnitFormProvider()()
  val requiredKey = "land.landSelectMeasurementUnit.error.required"

  ".value" - {

    val fieldName = "value"

    behave like optionsField[LandSelectMeasurementUnit](
      form,
      fieldName,
      validValues  = LandSelectMeasurementUnit.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
