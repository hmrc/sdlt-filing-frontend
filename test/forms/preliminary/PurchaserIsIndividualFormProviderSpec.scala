/*
 * Copyright 2025 HM Revenue & Customs
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

package forms.preliminary

import forms.behaviours.OptionFieldBehaviours
import forms.preliminary.PurchaserIsIndividualFormProvider
import models.prelimQuestions.BusinessOrIndividualRequest
import play.api.data.FormError

class PurchaserIsIndividualFormProviderSpec extends OptionFieldBehaviours{

  val form = new PurchaserIsIndividualFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "purchaserIsIndividual.error.required"
    val invalidKey = "purchaserIsIndividual.error.invalid"

    behave like optionsField[BusinessOrIndividualRequest](
      form,
      fieldName,
      validValues = BusinessOrIndividualRequest.values,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must not bind invalid values" in {
      val result = form.bind(Map(fieldName -> "InvalidOption"))
      result.errors must contain only FormError(fieldName, invalidKey)
    }
  }
}
