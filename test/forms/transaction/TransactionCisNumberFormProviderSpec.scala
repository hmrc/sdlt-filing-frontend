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

package forms.transaction

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class TransactionCisNumberFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "transaction.cisNumber.error.required"
  val lengthKey = "transaction.cisNumber.error.length"
  val invalidKey = "transaction.cisNumber.invalid"
  val maxLength = 14

  val form = new TransactionCisNumberFormProvider()()

  ".value" - {

    val fieldName = "value"
    "must bind valid form data" in {
      val validValues = Seq(
        "Mr test",
        "Test Meow",
        "Company",
        "12346674",
        "(555) 123-4567"
      )

      validValues.foreach(validRef =>
        val result = form.bind(Map(fieldName -> validRef))
        result.errors must be(empty)
      )
    }

    "must not bind strings longer than 14 characters" in {
      val longValue = "a" * 15
      val result = form.bind(Map(fieldName -> longValue))
      result.errors must contain(FormError(fieldName, lengthKey, Seq(maxLength)))
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must reject invalid strings" in {
      val invalidValues = Seq(
        "Hello #world",
        "Price: $50",
        "A < B",
        "File \\ path",
        "José",
        "\"Line1\\nLine2\""
      )

      invalidValues.foreach { invalidRef =>
        val result = form.bind(Map(fieldName -> invalidRef))
        result.errors must contain(
          FormError(fieldName, invalidKey, Seq("[A-Za-z0-9 \\~\\!\\@\\%\\&\\'\\(\\)\\*\\+,\\-\\.\\/\\:\\=\\?\\[\\]\\^\\_\\{\\}\\;]*"))
        )
      }
    }
  }
}
