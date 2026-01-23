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

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class PurchaserAgentReferenceFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "purchaserAgent.reference.error.required"
  val lengthKey = "purchaserAgent.reference.error.length"
  val invalidKey = "purchaserAgent.reference.error.invalid"
  val maxLength = 14

  val form = new PurchaserAgentReferenceFormProvider()()

  ".value" - {

    val fieldName = "value"

    "must bind valid form data" in {
      val validRefs = Seq(
        "Mr test",
        "Test Meow",
        "Company",
        "12346674",
        "(555) 123-4567"
      )

      validRefs.foreach(validRef =>
        val result = form.bind(Map(fieldName -> validRef))
        result.errors must be(empty)
      )
    }

    "must not bind strings longer than 14 characters" in {
      val longRef = "a" * 15
      val result = form.bind(Map(fieldName -> longRef))
      result.errors must contain(FormError(fieldName, lengthKey, Seq(maxLength)))
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must reject invalid name formats" in {
      val invalidRefs = Seq(
        "Hello #world",
        "Price: $50",
        "A < B",
        "File \\ path",
        "José",
        "\"Line1\\nLine2\""
      )

      invalidRefs.foreach { invalidRef =>
        val result = form.bind(Map(fieldName -> invalidRef))
        result.errors must contain(
          FormError(fieldName, invalidKey, Seq("[A-Za-z0-9 ~!@%&'()*+,\\-./:=?\\[\\]^_{};]*"))
        )
      }
    }
  }
}
