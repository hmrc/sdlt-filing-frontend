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
import play.api.data.FormError

class PurchaserPartnershipUtrFormProviderSpec extends StringFieldBehaviours {

  val form = new PurchaserPartnershipUtrFormProvider()()

  ".partnershipUniqueTaxpayerReference" - {

    val fieldName = "partnershipUniqueTaxpayerReference"
    val requiredKey = "purchaser.partnershipUtr.error.required"
    val lengthKey = "purchaser.partnershipUtr.error.length"
    val invalidKey = "purchaser.partnershipUtr.error.invalid"
    val invalidRegexKey = "purchaser.partnershipUtr.error.regex.invalid"
    val maxLength = 10
    val minLength = 10

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must bind valid form data" in {
      val validUtr = Seq (
        "1111111111",
        "2222222222",
        "3333333333",
        "9570845180"
      )

      validUtr.foreach { number =>
        val result = form.bind(
          Map(
            fieldName -> number
          )
        )
        result.errors mustBe empty
      }
    }

    "must not bind strings longer than 13 digits" in {
      val maxValue = "1" * (maxLength + 1)
        val result = form.bind(
          Map(
            fieldName -> maxValue
          )
        )
        result.errors must contain(FormError(fieldName, lengthKey, Seq(maxLength)))
    }

    "must not bind strings shorter than 10 digits" in {
      val minValue = "1" * (minLength - 1)
        val result = form.bind(
          Map(
            fieldName -> minValue
          )
        )
        result.errors must contain(FormError(fieldName, lengthKey, Seq(minLength)))
    }

    "must not bind empty strings" in {
      val result = form.bind(
        Map(
          fieldName -> ""
        )
      )
      result.errors must contain(FormError(fieldName, requiredKey))
    }

    "must not bind invalid regex values" in {
      val invalidValues = Seq(
        "1234567890#",
        "1234567890a",
        "a1234567890#",
        "12345f67890#",
        "12345   67890"
      )

      invalidValues.foreach { value =>
        val result = form.bind(
          Map(
            fieldName -> value,
          )
        )
        result.errors must contain(
          FormError(fieldName, invalidRegexKey, Seq("^[0-9]*$"))
        )
      }
    }

    "must not bind values that fail validateUtr check" in {
      val invalidValues = Seq(
        "1234567890",
        "1234567854",
        "9235478904",
        "1298756958"
      )

      invalidValues.foreach { value =>
        val result = form.bind(
          Map(
            fieldName -> value,
          )
        )
        result.errors must contain(
          FormError(fieldName, invalidKey)
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
            fieldName -> space
          )
        )
        result.errors must contain(
          FormError(fieldName, requiredKey)
        )
      }
    }
  }
}
