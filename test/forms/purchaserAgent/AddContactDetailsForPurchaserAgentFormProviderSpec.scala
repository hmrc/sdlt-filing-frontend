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

package forms.purchaserAgent

import forms.behaviours.OptionFieldBehaviours
import play.api.data.FormError

class AddContactDetailsForPurchaserAgentFormProviderSpec extends OptionFieldBehaviours {

  val form = new AddContactDetailsForPurchaserAgentFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "purchaserAgent.addContactDetailsForPurchaserAgent.error.required"

    "must bind true and false values correctly" in {
      form.bind(Map(fieldName -> "true")).get mustBe true
      form.bind(Map(fieldName -> "false")).get mustBe false
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
