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

package forms.purchaser

import forms.behaviours.StringFieldBehaviours
import org.scalacheck.Gen
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class EnterPurchaserPhoneNumberFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "purchaser.enterPhoneNumber.error.required"
  val lengthKey = "purchaser.enterPhoneNumber.error.length"
  val invalidMsgKey = "purchaser.enterPhoneNumber.error.invalid"
  val maxLength = 14

  implicit val messages: Messages = stubMessages()
  val form = new EnterPurchaserPhoneNumberFormProvider()("Doe")

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(Seq("+987654321", "9876543210"))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must strip spaces, hyphens and brackets before storing" in {
      val inputs = Seq(
        "(987)654-32"    -> "987654 32".replace(" ", ""),
        "987-654-3210"   -> "9876543210",
        "+44 808 157 0192" -> "+448081570192"
      )
      inputs.foreach { case (input, expected) =>
        val result = form.bind(Map(fieldName -> input))
        result.errors mustBe empty
        result.get mustBe expected
      }
    }

    "must not bind strings longer than max length after stripping" in {
      val tooLong = "1234567890123456"
      val result = form.bind(Map(fieldName -> tooLong))
      result.errors must contain only FormError(fieldName, lengthKey, Seq(maxLength))
    }

    "must allow a phone number that exceeds 14 characters raw but is within limit after stripping" in {
      val longWithSpaces = "+44 808 157 0192"
      val result = form.bind(Map(fieldName -> longWithSpaces))
      result.errors mustBe empty
      result.get mustBe "+448081570192"
    }

    "must not bind strings with invalid characters" in {
      val result = form.bind(Map(fieldName -> "123|456"))
      result.errors.map(_.message) must contain(invalidMsgKey)
    }
  }
}


