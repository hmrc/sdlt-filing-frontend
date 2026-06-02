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
import org.scalacheck.Gen

class LandTitleNumberFormProviderSpec extends StringFieldBehaviours {

  val requiredKey       = "land.titleNumber.error.required"
  val lengthKey         = "land.titleNumber.error.length"
  val minLengthKey      = "land.titleNumber.error.minLength"
  val invalidKey        = "land.titleNumber.error.invalid"
  val invalidFormatKey  = "land.titleNumber.error.invalidFormat"

  val maxLength = 14

  val form = new LandTitleNumberFormProvider()()

  private val validStrings: Gen[String] =
    for {
      prefixLen <- Gen.choose(1, 3)
      prefix    <- Gen.listOfN(prefixLen, Gen.alphaChar).map(_.mkString)
      digitLen  <- Gen.choose(1, maxLength - prefixLen)
      digits    <- Gen.listOfN(digitLen, Gen.numChar).map(_.mkString)
    } yield prefix + digits

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validStrings
    )

    "not bind strings longer than maxLength" in {
      val longValue = "AB" + List.fill(maxLength - 1)('1').mkString

      val result = form.bind(Map(fieldName -> longValue))
      result.errors must contain(
        FormError(fieldName, lengthKey, Seq(maxLength))
      )
    }

    "not bind strings with invalid characters" in {
      val invalidValue = "ABC123💥"

      val result = form.bind(Map(fieldName -> invalidValue))

      result.errors.map(_.message) must contain(invalidKey)
    }

    "not bind a single character" in {
      val result = form.bind(Map(fieldName -> "A"))
      result.errors must contain(FormError(fieldName, minLengthKey, Seq(2)))
    }

    "not bind a value that does not start with 1 to 3 letters followed by digits" in {
      val invalidFormats = Seq("123456", "ABCD1234", "1AB234")
      invalidFormats.foreach { v =>
        val result = form.bind(Map(fieldName -> v))
        result.errors.map(_.message) must contain(invalidFormatKey)
      }
    }

    "bind valid title number formats" in {
      val validFormats = Seq("AB123456", "TGL312172", "A1", "ABC1234567890")
      validFormats.foreach { v =>
        val result = form.bind(Map(fieldName -> v))
        result.errors.map(_.message) must not contain invalidFormatKey
      }
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
