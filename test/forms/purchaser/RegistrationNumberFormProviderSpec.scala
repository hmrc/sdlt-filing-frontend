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
import org.scalacheck.Gen
import play.api.data.FormError

class RegistrationNumberFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "purchaser.registrationNumber.error.required"
  val lengthKey = "purchaser.registrationNumber.error.length"
  val exactLength = 9
  val invalidKey = "purchaser.registrationNumber.error.regex.error"

  val form = new RegistrationNumberFormProvider()()
  val fieldName = "registrationNumber"

  // Generator: exactly 9 numeric characters
  private val numericStringsOfExactLengthNine: Gen[String] =
    Gen.listOfN(exactLength, Gen.numChar).map(_.mkString)

  ".value" - {

    "bind successfully when pass correct registration number" in {
      val result = form.bind(Map(fieldName -> "438573857"))
      result.errors mustBe Nil
    }

    "bind successfully when pass incorrect registration number" in {
      val result = form.bind(Map(fieldName -> ""))
      result.errors.map(_.message) must contain only "purchaser.registrationNumber.error.required"
    }

    "bind successfully when leading zeros are present" in {
      val result = form.bind(Map(fieldName -> "438573857"))
      result.errors mustBe Nil
    }


    "fail with length error when fewer than exact length" in {
      val tooShortValues = Seq("1", "12", "123", "1234", "12345", "123456", "1234567", "12345678", "2345678")
      tooShortValues.foreach { v =>
        val result = form.bind(Map(fieldName -> v))
        result.errors must contain only FormError(fieldName, lengthKey, Seq(exactLength))
      }

    }
    "fail with regex error for non-numeric input (ignoring args)" in {
      val badValues = Seq("ABCDEFGHI", "12345ABCD", "12 345678", "1234-5678")
      badValues.foreach { v =>
        val result = form.bind(Map(fieldName -> v))
        result.errors.map(_.message) must contain only "purchaser.registrationNumber.error.regex.invalid"
      }
    }
  }
  }

