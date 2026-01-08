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

class PurchaserAgentNameFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "purchaserAgent.name.error.required"
  val lengthKey = "purchaserAgent.name.error.length"
  val invalidKey = "purchaserAgent.name.error.invalid"
  val maxLength = 28

  val form = new PurchaserAgentNameFormProvider()()

  ".purchaserAgentName" - {

    val fieldName = "value"

    "must bind valid form data" in {
      val validNames = Seq(
        "Mr test",
        "Company test name",
        "Company are us",
        "Company@company.com",
        "(555) 123-4567"
      )

      validNames.foreach( validName =>
        val result = form.bind(Map(fieldName -> validName))
        result.errors must be(empty)
      )
    }

    "must not bind strings longer than 28 characters" in {
      val longName = "a" * 29
      val result = form.bind(Map(fieldName -> longName))
      result.errors must contain(FormError(fieldName, lengthKey, Seq(maxLength)))
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must reject invalid name formats" in {
      val invalidNames= Seq(
        "Hello #world",
        "Price: $50",
        "A < B",
        "File \\ path",
        "José",
        "\"Line1\\nLine2\""
      )

      invalidNames.foreach { invalidName =>
        val result = form.bind(Map(fieldName -> invalidName))
        result.errors must contain(
          FormError(fieldName, invalidKey, Seq("[A-Za-z0-9 ~!@%&'()*+,\\-./:=?\\[\\]^_{}\\;]*"))
        )
      }
    }
  }
}
