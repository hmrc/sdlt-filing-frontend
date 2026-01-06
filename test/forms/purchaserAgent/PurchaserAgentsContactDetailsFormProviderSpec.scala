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
//import models.purchaserAgent.PurchaserAgentsContactDetails
import play.api.data.FormError

class PurchaserAgentsContactDetailsFormProviderSpec extends StringFieldBehaviours {

  val form = new PurchaserAgentsContactDetailsFormProvider()()

  ".phoneNumber" - {

    val fieldName = "phoneNumber"
    val lengthKey = "purchaserAgent.contactDetails.error.agentPhoneNumber.length"
    val invalidKey = "purchaserAgent.contactDetails.error.agentPhoneNumber.invalid"
    val maxLength = 14
    val phoneRegex = "[A-Za-z0-9 \\~\\!\\@\\%\\&\\'\\(\\)\\*\\+,\\-\\.\\/\\:\\=\\?\\[\\]\\^\\_\\{\\}\\;]*"

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
        val result = form.bind(Map(fieldName -> number, "emailAddress" -> "test@example.com"))
        result.errors mustBe empty
        result.get.phoneNumber mustBe Some(number)
      }
    }

    "must bind empty strings as None if email is provided" in {
      val result = form.bind(Map(fieldName -> "", "emailAddress" -> "test@example.com"))
      result.errors mustBe empty
      result.get.phoneNumber mustBe None
    }

    "must bind when field is missing if email is provided" in {
      val result = form.bind(Map("emailAddress" -> "test@example.com"))
      result.errors mustBe empty
      result.get.phoneNumber mustBe None
    }

    "must bind string that is only whitespace as None if email is provided" in {
      val whiteSpace = Seq(" ", "  ", "\t", " \t")
      whiteSpace.foreach { space =>
        val result = form.bind(Map(fieldName -> space, "emailAddress" -> "test@example.com"))
        result.errors mustBe empty
        result.get.phoneNumber mustBe None
      }
    }

    "must not bind strings longer than 14 characters" in {
      val longNumber = "1" * (maxLength + 1)
      val result = form.bind(Map(fieldName -> longNumber, "emailAddress" -> "test@example.com"))
      result.errors must contain(FormError(fieldName, lengthKey, Seq(maxLength)))
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
        val result = form.bind(Map(fieldName -> number, "emailAddress" -> "test@example.com"))
        result.errors must contain(FormError(fieldName, invalidKey, Seq(phoneRegex)))
      }
    }
  }

  ".emailAddress" - {

    val fieldName = "emailAddress"
    val lengthKey = "purchaserAgent.contactDetails.error.agentEmailAddress.length"
    val invalidKey = "purchaserAgent.contactDetails.error.agentEmailAddress.invalid"
    val maxLength = 36
    val emailRegex = "^[^@|<>\"'`]+@[^@|<>\"'`]+$"

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
        val result = form.bind(Map(fieldName -> email, "phoneNumber" -> "123456789"))
        result.errors mustBe empty
        result.get.emailAddress mustBe Some(email)
      }
    }

    "must bind empty strings as None if phone is provided" in {
      val result = form.bind(Map(fieldName -> "", "phoneNumber" -> "123456789"))
      result.errors mustBe empty
      result.get.emailAddress mustBe None
    }

    "must bind when field is missing if phone is provided" in {
      val result = form.bind(Map("phoneNumber" -> "123456789"))
      result.errors mustBe empty
      result.get.emailAddress mustBe None
    }

    "must not bind strings longer than 36 characters" in {
      val longEmail = "a" * (maxLength + 1) + "@example.com"
      val result = form.bind(Map(fieldName -> longEmail, "phoneNumber" -> "123456789"))
      result.errors must contain(FormError(fieldName, lengthKey, Seq(maxLength)))
    }

    "must not bind invalid email address values" in {
      val invalidEmails = Seq(
        "test@@example.com",
        "te@<st@example.com>",
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
        val result = form.bind(Map(fieldName -> email, "phoneNumber" -> "123456789"))
        result.errors must contain(FormError(fieldName, invalidKey, Seq(emailRegex)))
      }
    }

    "must bind string that is only whitespace as None if phone is provided" in {
      val whiteSpace = Seq(" ", "  ", "\t", " \t")
      whiteSpace.foreach { space =>
        val result = form.bind(Map(fieldName -> space, "phoneNumber" -> "123456789"))
        result.errors mustBe empty
        result.get.emailAddress mustBe None
      }
    }
  }

  "oneRequired validation" - {

    "must fail when both phoneNumber and emailAddress are empty" in {
      val result = form.bind(Map("phoneNumber" -> "", "emailAddress" -> ""))
      result.errors must contain(FormError("", "purchaserAgent.contactDetails.error.oneRequired"))
    }

    "must fail when both phoneNumber and emailAddress are missing" in {
      val result = form.bind(Map.empty[String, String])
      result.errors must contain(FormError("", "purchaserAgent.contactDetails.error.oneRequired"))
    }
  }
}
