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

package forms.submission

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class EmailConfirmationFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "submission.emailConfirmation.error.required"
  val lengthKey = "submission.emailConfirmation.error.length"
  val invalidCharsKey = "submission.emailConfirmation.error.invalid"
  val invalidFormatKey = "submission.emailConfirmation.error.invalidFormat"
  val maxLength = 36

  val form = new EmailConfirmationFormProvider()()

  ".value" - {

    val fieldName = "value"

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
        "a@b.com"
      )

      validEmails.foreach { email =>
        val result = form.bind(
          Map(fieldName -> email)
        )
        result.errors mustBe empty
        result.get mustBe email
      }
    }

    "must not bind email address values with invalid characters" in {
      val invalidCharEmails = Seq(
        "user@domain|com",
        "hello@domain>.com",
        "name@domain<.com",
        "quote\"@domain.com",
        "single'quote@domain.com",
        "`backtick`@domain.com"
      )

      invalidCharEmails.foreach { email =>
        val result = form.bind(
          Map(fieldName -> email)
        )
        result.errors must contain(
          FormError(fieldName, invalidCharsKey, Seq("^[^|<>\"'`]+$"))
        )
      }
    }

    "must not bind email address values with invalid format" in {
      val invalidFormatEmails = Seq(
        "test@@example.com",
        "te@st@example.com",
        "@missinglocal.com",
        "missingdomain@"
      )

      val invalidFormatKey = "submission.emailConfirmation.error.invalidFormat"

      invalidFormatEmails.foreach { email =>
        val result = form.bind(
          Map(fieldName -> email)
        )
        result.errors must contain(
          FormError(fieldName, invalidFormatKey, Seq("[^@|`'<>\"|`]+@[^@|`'<>\"]+"))
        )
      }
    }

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

  }
}
