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

package forms.vendor

import forms.behaviours.StringFieldBehaviours
import forms.vendor.VendorAgentsReferenceFormProvider
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class VendorAgentsReferenceFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "vendorAgentsReference.error.required"
  val lengthKey = "vendorAgentsReference.error.length"
  val invalidKey = "vendorAgentsReference.error.invalid"
  val maxLength = 14
  val agentName = "Name"

  implicit val messages: Messages = stubMessages()

  val form = new VendorAgentsReferenceFormProvider()(agentName)

  ".agentReference" - {

    val fieldName = "agentReference"

    "must bind valid form data" in {
      val validNames = Seq(
        "Mr test",
        "Test Meow",
        "Company",
        "12346674",
        "(555) 123-4567"
      )

      validNames.foreach( validName =>
        val result = form.bind(Map(fieldName -> validName))
        result.errors must be(empty)
      )
    }

    "must not bind strings longer than 14 characters" in {
      val longName = "a" * 15
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
        val result = form.bind(Map("agentReference" -> invalidName))
        result.errors must contain(
          FormError("agentReference", invalidKey, Seq("[A-Za-z0-9 ~!@%&'()*+,\\-./:=?\\[\\]^_{}\\;]*"))
        )
      }
    }
  }
}
