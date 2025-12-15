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

class PurchaserNationalInsuranceFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "purchaser.nationalInsurance.error.required"
  val lengthKey = "purchaser.nationalInsurance.error.length"
  val maxLength = 9
  val invalidKey = "purchaser.nationalInsurance.error.invalid"

  val form = new PurchaserNationalInsuranceFormProvider()()

  ".nationalInsuranceNumber" - {

    val fieldName = "nationalInsuranceNumber"

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must bind valid national insurance number form data" in {
      val validNino = Seq(
        "AA123456A",
        "AB987654C",
        "CE102938D",
        "GH564738A",
        "HJ837465B",
        "JK192837C",
        "LM564738D",
        "PR918273A",
        "TW112233B",
        "WX445566"
      )

      validNino.foreach { nino =>
        val result = form.bind(
          Map(fieldName -> nino)
        )
        result.errors mustBe empty
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

    "must not bind invalid national insurance values" in {
      val invalidNinos = Seq(
        "FY123456A",
        "GB987654B",
        "NK112233C",
        "TN554433A",
        "ZZ998877D",
        "A1234567A",
        "AA12345A",
        "AA1234567",
        "AB12C456D",
        "QZ123456E",
        " AA123456A",
        "AA123456A ",
        "A A123456A",
        "AA 123456A",
        "AA12 3456A",
        "AA123456 A",
        "A@123456A",
        "AA123!56A",
        "AA123456#",
        "1A123456A"
      )

      invalidNinos.foreach { nino =>
        val result = form.bind(
          Map(
            fieldName -> nino
          )
        )
        result.errors must contain(
          FormError(fieldName, invalidKey, Seq("^(?!FY)?(?!GB)(?!NK)(?!TN)(?!ZZ)[A-CEGHJ-PR-TW-Z][A-CEGHJ-NPR-TW-Z][0-9]{6}[A-D]?"))
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
