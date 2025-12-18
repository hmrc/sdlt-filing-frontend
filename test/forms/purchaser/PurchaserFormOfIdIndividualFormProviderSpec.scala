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

class PurchaserFormOfIdIndividualFormProviderSpec extends StringFieldBehaviours {

  val form = new PurchaserFormOfIdIndividualFormProvider()()

  ".idNumberOrReference" - {

    val fieldName = "idNumberOrReference"
    val requiredKey = "purchaser.formOfIdIndividual.error.idNumberOrReference.required"
    val lengthKey = "purchaser.formOfIdIndividual.error.idNumberOrReference.length"
    val invalidKey = "purchaser.formOfIdIndividual.error.idNumberOrReference.invalid"
    val maxLength = 14

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must bind valid form data" in {
      val validValues = Seq(
        "Mr test",
        "Test Meow",
        "Company",
        "12346674",
        "(555) 123-4567"
      )

      validValues.foreach { number =>
        val result = form.bind(
          Map(
            fieldName -> number,
            "countryIssued" -> "Germany"
          )
        )
        result.errors mustBe empty
      }
    }

    "must not bind strings longer than 14 characters" in {
      val longValue = "a" * (maxLength + 1)
      val result = form.bind(
        Map(
          fieldName -> longValue,
          "countryIssued" -> "Germany"
        )
      )
      result.errors must contain(FormError(fieldName, lengthKey, Seq(maxLength)))
    }

    "must not bind empty strings" in {
      val result = form.bind(
        Map(
          fieldName -> "",
          "countryIssued" -> "Germany"
        )
      )
      result.errors must contain(FormError(fieldName, requiredKey))
    }

    "must not bind invalid values" in {
      val invalidValues = Seq(
        "Hello #world",
        "Price: $50",
        "A < B",
        "File \\ path",
        "José",
        "\"Line1\\nLine2\""
      )

      invalidValues.foreach { value =>
        val result = form.bind(
          Map(
            fieldName -> value,
            "countryIssued" -> "Germany"
          )
        )
        result.errors must contain(
          FormError(fieldName, invalidKey, Seq("[A-Za-z0-9 ~!@%&'()*+,\\-./:=?\\[\\]^_{}\\;]*"))
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
            "countryIssued" -> "Germany"
          )
        )
        result.errors must contain(
          FormError(fieldName, requiredKey)
        )
      }
    }
  }

  ".countryIssued" - {

    val fieldName = "countryIssued"
    val requiredKey = "purchaser.formOfIdIndividual.error.countryIssued.required"
    val lengthKey = "purchaser.formOfIdIndividual.error.countryIssued.length"
    val invalidKey = "purchaser.formOfIdIndividual.error.countryIssued.invalid"
    val maxLength = 28

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must bind valid form data" in {
      val validValues = Seq(
        "Mr test",
        "Test Meow",
        "Company",
        "12346674",
        "(555) 123-4567"
      )

      validValues.foreach { value =>
        val result = form.bind(
          Map(
            "idNumberOrReference" -> "123456",
            fieldName -> value
          )
        )
        result.errors mustBe empty
      }
    }

    "must not bind strings longer than 14 characters" in {
      val longValue = "a" * (maxLength + 1)
      val result = form.bind(
        Map(
          "idNumberOrReference" -> "123456",
          fieldName -> longValue
        )
      )
      result.errors must contain(FormError(fieldName, lengthKey, Seq(maxLength)))
    }


    "must not bind invalid values" in {
      val invalidValues = Seq(
        "Hello #world",
        "Price: $50",
        "A < B",
        "File \\ path",
        "José",
        "\"Line1\\nLine2\""
      )

      invalidValues.foreach { value =>
        val result = form.bind(
          Map(
            "idNumberOrReference" -> "123456",
            fieldName -> value
          )
        )
        result.errors must contain(
          FormError(fieldName, invalidKey, Seq("[A-Za-z0-9 ~!@%&'()*+,\\-./:=?\\[\\]^_{}\\;]*"))
        )
      }
    }
  }
}
