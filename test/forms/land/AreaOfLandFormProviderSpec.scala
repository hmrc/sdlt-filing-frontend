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

  val lengthKey           = "land.areaOfLand.error.length"
  val maxLength           = 14

  val squareMetresUnit    = "SquareMetres"
  val hectaresUnit        = "Hectares"

  val squareMetresRequired = s"land.areaOfLand.error.required.$squareMetresUnit"
  val hectaresRequired     = s"land.areaOfLand.error.required.$hectaresUnit"
  val squareMetresInvalid  = s"land.areaOfLand.error.invalidFormat.$squareMetresUnit"
  val hectaresInvalid      = s"land.areaOfLand.error.invalidFormat.$hectaresUnit"
  val invalidCharsKey      = "land.areaOfLand.error.invalidChars"

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
        requiredError = FormError(fieldName, squareMetresRequired)
      )

      "fail with invalid chars error for non-numeric input" in {
        Seq("-1", "1£2", "abc").foreach { v =>
          val result = form.bind(Map(fieldName -> v))
          result.errors must contain only FormError(fieldName, invalidCharsKey, Seq.empty)
        }
      }

      "fail with invalid format error for numbers in wrong format" in {
        Seq("300.1", "1.001").foreach { v =>
          val result = form.bind(Map(fieldName -> v))
          result.errors must contain only FormError(fieldName, squareMetresInvalid, Seq.empty)
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

      "must not bind strings longer than 14 characters" in {
        val longValue = "1" * 15
        val result = form.bind(Map(fieldName -> longValue))
        result.errors must contain(FormError(fieldName, lengthKey, Seq()))
      }

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, hectaresRequired)
      )

      "fail with invalid chars error for non-numeric input" in {
        Seq("-1", "test", "1£2").foreach { v =>
          val result = form.bind(Map(fieldName -> v))
          result.errors must contain only FormError(fieldName, invalidCharsKey, Seq.empty)
        }
      }

      "fail with invalid format error for numbers with too many decimal places" in {
        val result = form.bind(Map(fieldName -> "1.1234"))
        result.errors must contain only FormError(fieldName, hectaresInvalid, Seq.empty)
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
