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

class PurchaserCorporationTaxUTRFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "purchaser.corporationTaxUTR.error.required"
  val lengthKey = "purchaser.corporationTaxUTR.error.length"
  val invalidRegexKey = "purchaser.corporationTaxUTR.error.regex.invalid"
  val invalidKey = "purchaser.corporationTaxUTR.error.invalid"
  val minLength = 10
  val maxLength = 10

  val form = new PurchaserCorporationTaxUTRFormProvider()()

  ".value" - {

    val fieldName = "value"

    "must bind valid F15 UTR form data" in {
      val validUTR = Seq("1111111111","9570845180")

      validUTR.foreach { utr =>
        val result = form.bind(
          Map(fieldName -> utr)
        )
        result.errors mustBe empty
      }
    }

    "must not bind UTR values that do not pass F15 validation" in {
      val invalidUTRs = Seq("1234567899","5570845180")

      invalidUTRs.foreach { utr =>
        val result = form.bind(
          Map(
            fieldName -> utr
          )
        )
        result.errors must contain(
          FormError(fieldName, invalidKey)
        )
      }
    }

    "must not bind empty strings" in {
      val result = form.bind(
        Map(
          fieldName -> ""
        )
      )
      result.errors must contain(FormError(fieldName, requiredKey))
    }

    "must not bind UTR values longer than 10 digits" in {
      val longValue = "1" * (maxLength + 1)
      val result = form.bind(
        Map(
          fieldName -> longValue,
        )
      )
      result.errors must contain(FormError(fieldName, lengthKey, Seq(maxLength)))
    }

    "must not bind UTR values shorter than 10 digits" in {
      val shortValue = "1" * (minLength - 1)
      val result = form.bind(
        Map(
          fieldName -> shortValue,
        )
      )
      result.errors must contain(FormError(fieldName, lengthKey, Seq(minLength)))
    }

    "must not bind invalid format UTR values" in {
      val invalidUTRs = Seq(
        "AAAAAAAAAAAAA",
        "----EVEVRV-EVRV",
        "TEST",
        "cerc323fedce3",
        "       erer   "
      )

      invalidUTRs.foreach { utr =>
        val result = form.bind(
          Map(
            fieldName -> utr
          )
        )
        result.errors must contain(
          FormError(fieldName, invalidRegexKey, Seq("^[0-9]*$"))
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
          )
        )
        result.errors must contain(
          FormError(fieldName, requiredKey)
        )
      }
    }
  }
}
