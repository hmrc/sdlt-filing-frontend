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
import forms.vendor.VendorAgentsContactDetailsFormProvider
import play.api.data.FormError

class VendorAgentsContactDetailsFormProviderSpec extends StringFieldBehaviours {

  val form = new VendorAgentsContactDetailsFormProvider()()

  ".phoneNumber" - {

    val fieldName = "phoneNumber"
    val requiredKey = "vendorAgentsContactDetails.error.agentPhoneNumber.required"
    val lengthKey = "vendorAgentsContactDetails.error.agentPhoneNumber.length"
    val invalidKey = "vendorAgentsContactDetails.error.agentPhoneNumber.invalid"
    val maxLength = 14


    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must bind valid phone number form data" in {
      val validNumbers = Seq(
        "1234567890",
        "12345678912345",
        "1",
        "ABC123",
        "A1B2C3!",
        "9876-5432",
        "Test_Value",
        "Hello.World+!@",
        "A+B=C",
        "(Agent)42",
        "Value:100%",
        "OK;GO"
      )

      validNumbers.foreach { number =>
        val result = form.bind(
          Map(
          fieldName -> number,
          "emailAddress" -> "test@example.com"
          )
        )
        result.errors mustBe empty
      }
    }

    "must not bind strings longer than 14 characters" in {
      val longNumber = "1" * (maxLength + 1)
      val result = form.bind(
        Map(
          fieldName -> longNumber,
          "emailAddress" -> "test@example.com"
        )
      )
      result.errors must contain(FormError(fieldName, lengthKey, Seq(maxLength)))
    }

    "must not bind empty strings" in {
      val result = form.bind(
        Map(
          fieldName -> "",
          "emailAddress" -> "test@example.com"
        )
      )
      result.errors must contain(FormError(fieldName, requiredKey))
    }

    "must not bind invalid phone number values" in {
      val invalidNumbers = Seq(
        "123456789#",
        "123$4567",
        "12€34",
        "abc©def",
        "987~^`",
        "phone🙂",
        "num>value",
        "num<value",
        "hello|world",
        "test\"",
        "back\\slash"
      )

      invalidNumbers.foreach { number =>
        val result = form.bind(
          Map(
            fieldName -> number,
            "emailAddress" -> "test@example.com"
          )
        )
        result.errors must contain(
          FormError(fieldName, invalidKey, Seq("[A-Za-z0-9 \\~\\!\\@\\%\\&\\'\\(\\)\\*\\+,\\-\\.\\/\\:\\=\\?\\[\\]\\^\\_\\{\\}\\;]*"))
        )
      }
    }

    "must not bind string that is only whitespace or tabs" in {
      val whiteSpace = Seq(
        " ",
        "            ",
        "\t",
        " \t"
      )

      whiteSpace.foreach { space =>
        val result = form.bind(
          Map(
            fieldName -> space,
            "emailAddress" -> "test@example.com"
          )
        )
        result.errors must contain(
          FormError(fieldName, requiredKey)
        )
      }
    }
  }

  ".emailAddress" - {

    val fieldName = "emailAddress"
    val requiredKey = "vendorAgentsContactDetails.error.agentEmailAddress.required"
    val lengthKey = "vendorAgentsContactDetails.error.agentEmailAddress.length"
    val invalidKey = "vendorAgentsContactDetails.error.agentEmailAddress.invalid"
    val maxLength = 36


    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must bind valid email address form data" in {
      val validEmails = Seq(
        "test@example.com",
        "user.name@domain.co.uk",
        "hello+world@sub.domain.com",
        "simple123@numbers.net",
        "UPPERCASE@EXAMPLE.COM",
        "name_with_underscores@domain.org",
        "dots.in.name@domain.io",
        "hyphen-name@domain-name.com",
        "a@b"
      )

      validEmails.foreach { email =>
        val result = form.bind(
          Map(
            fieldName -> email,
            "phoneNumber" -> "123456789"
          )
        )
        result.errors mustBe empty
      }
    }

    "must not bind strings longer than 36 characters" in {
      val longEmail = "1" * (maxLength + 1)
      val result = form.bind(
        Map(
          fieldName -> longEmail,
          "phoneNumber" -> "123456789"
        )
      )
      result.errors must contain(FormError(fieldName, lengthKey, Seq(maxLength)))
    }

    "must not bind empty strings" in {
      val result = form.bind(
        Map(
          fieldName -> "",
          "phoneNumber" -> "123456789"
        )
      )
      result.errors must contain(FormError(fieldName, requiredKey))
    }

    "must not bind invalid email address values" in {
      val invalidEmails = Seq(
        "test@@example.com",
        "te@st@example.com",
        "user@domain|com",
        "hello@domain>.com",
        "name@domain<.com",
        "quote\"@domain.com",
        "single'quote@domain.com",
        "`backtick`@domain.com",
        "@missinglocal.com",
        "missingdomain@"
      )

      invalidEmails.foreach { email =>
        val result = form.bind(
          Map(
            fieldName -> email,
            "phoneNumber" -> "123456789"
          )
        )
        result.errors must contain(
          FormError(fieldName, invalidKey, Seq("^[^@|<>\"'`]+@[^@|<>\"'`]+$"))
        )
      }
    }

    "must not bind string that is only whitespace or tabs" in {
      val whiteSpace = Seq(
        " ",
        "            ",
        "\t",
        " \t"
      )

      whiteSpace.foreach { space =>
        val result = form.bind(
          Map(
            fieldName -> space,
            "phoneNumber" -> "123456789"
          )
        )
        result.errors must contain(
          FormError(fieldName, requiredKey)
        )
      }
    }
  }
}
