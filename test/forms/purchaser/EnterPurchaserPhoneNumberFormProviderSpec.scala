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

class EnterPurchaserPhoneNumberFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "purchaser.enterPhoneNumber.error.required"
  val lengthKey = "purchaser.enterPhoneNumber.error.length"
  val invalidMsgKey = "purchaser.enterPhoneNumber.error.invalid"
  val maxLength = 14

  val form = new EnterPurchaserPhoneNumberFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(Seq("+987654321", "(987)654-32", "987-654-3210", "9876543210"))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must not bind strings longer than max length" in {
      val tooLong = "1234567890123456"
      val result = form.bind(Map(fieldName -> tooLong))
      result.errors must contain only FormError(fieldName, lengthKey, Seq(maxLength))
    }

    "must not bind strings with invalid characters" in {
      val result = form.bind(Map(fieldName -> "123|456"))
      result.errors.map(_.message) must contain(invalidMsgKey)
    }
    }
  }


