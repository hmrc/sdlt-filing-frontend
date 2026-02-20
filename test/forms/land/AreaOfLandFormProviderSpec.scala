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

package forms.land

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class AreaOfLandFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "land.areaOfLand.error.required"
  val lengthKey = "land.areaOfLand.error.length"
  val invalidKey = "land.areaOfLand.error.invalid"
  val maxLength = 14

  val squareMetresUnit = "SQMETRE"
  val hectaresUnit = "HECTARES"

  ".value" - {

    val fieldName = "value"

    "when unit type is square metres" - {

      val form = new AreaOfLandFormProvider()(squareMetresUnit)

      behave like fieldThatBindsValidData(
        form,
        fieldName,
        oneOf(Seq("0", "12345678901234", "300"))
      )

      behave like fieldWithMaxLength(
        form,
        fieldName,
        maxLength = maxLength,
        lengthError = FormError(fieldName, lengthKey)
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, requiredKey)
      )

      "fail with invalid error with invalid values" in {
        val invalidSquareMetresValues = Seq("-1", "300.1", "1.001")
        invalidSquareMetresValues.foreach { v =>
          val result = form.bind(Map(fieldName -> v))
          result.errors must contain only FormError(fieldName, invalidKey, Seq.empty)
        }
      }

      "must convert whole numbers to 3 decimal places" in {
        val validWholeNumbers = Seq("300", "300.00", "300.0", "300.0000")
        validWholeNumbers.foreach { v =>
          val result = form.bind(Map(fieldName -> v))
          result.value mustEqual Some("300.000")
        }
      }
    }

    "when unit type is hectares" - {

      val form = new AreaOfLandFormProvider()(hectaresUnit)

      behave like fieldThatBindsValidData(
        form,
        fieldName,
        oneOf(Seq("0.000", "1234567890.234", "300.123", "0", "123.12", "300"))
      )

      behave like fieldWithMaxLength(
        form,
        fieldName,
        maxLength = maxLength,
        lengthError = FormError(fieldName, lengthKey)
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, requiredKey)
      )

      "fail with invalid error with invalid values" in {
        val invalidSquareMetresValues = Seq("-1", "1.1234", "test")
        invalidSquareMetresValues.foreach { v =>
          val result = form.bind(Map(fieldName -> v))
          result.errors must contain only FormError(fieldName, invalidKey, Seq.empty)
        }
      }

      "must convert whole numbers to 3 decimal places" in {
        val validWholeNumbers = Seq("300", "300.00", "300.0")
        validWholeNumbers.foreach { v =>
          val result = form.bind(Map(fieldName -> v))
          result.value mustEqual Some("300.000")
        }
      }

      "must convert other numbers to 3 decimal places" in {
        form.bind(Map(fieldName -> "1.12")).value mustEqual Some("1.120")
        form.bind(Map(fieldName -> "1.1")).value mustEqual Some("1.100")
      }
    }
  }
}
