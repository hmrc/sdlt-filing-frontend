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

class LocalAuthorityCodeFormProviderSpec extends StringFieldBehaviours {

  private val requiredKey     = "land.localAuthorityCode.error.required"
  private val lengthKey       = "land.localAuthorityCode.error.length"
  private val invalidCharsKey = "land.localAuthorityCode.error.invalidChars"
  private val unrecognisedKey = "land.localAuthorityCode.constraint.invalid"

  val form = new LocalAuthorityCodeFormProvider()()

  ".value" - {

    val fieldName = "value"

    "must bind a valid English local authority code (0114)" in {
      val result = form.bind(Map(fieldName -> "0114"))
      result.errors mustBe empty
      result.value mustBe Some("0114")
    }

    "must bind a Welsh regular code (6810)" in {
      val result = form.bind(Map(fieldName -> "6810"))
      result.errors mustBe empty
      result.value mustBe Some("6810")
    }

    "must bind a Welsh special code (6996)" in {
      val result = form.bind(Map(fieldName -> "6996"))
      result.errors mustBe empty
      result.value mustBe Some("6996")
    }

    "must bind a Welsh special code (6999)" in {
      val result = form.bind(Map(fieldName -> "6999"))
      result.errors mustBe empty
      result.value mustBe Some("6999")
    }

    "must bind a dummy code (8998)" in {
      val result = form.bind(Map(fieldName -> "8998"))
      result.errors mustBe empty
      result.value mustBe Some("8998")
    }

    "must bind a dummy code (8999)" in {
      val result = form.bind(Map(fieldName -> "8999"))
      result.errors mustBe empty
      result.value mustBe Some("8999")
    }

    "must bind a Scottish code (9051)" in {
      val result = form.bind(Map(fieldName -> "9051"))
      result.errors mustBe empty
      result.value mustBe Some("9051")
    }

    "must bind a Scottish code (9079) on the upper edge of the listed range" in {
      val result = form.bind(Map(fieldName -> "9079"))
      result.errors mustBe empty
      result.value mustBe Some("9079")
    }
    
    "must not bind when value is empty" in {
      val result = form.bind(Map(fieldName -> ""))
      result.errors.map(_.message) must contain(requiredKey)
    }

    "must not bind when value is missing entirely" in {
      val result = form.bind(Map.empty[String, String])
      result.errors.map(_.message) must contain(requiredKey)
    }

    "must not bind strings longer than 4 characters" in {
      val result = form.bind(Map(fieldName -> "12345"))
      result.errors.map(_.message) must contain(lengthKey)
    }

    "must not bind strings much longer than 4 characters" in {
      val result = form.bind(Map(fieldName -> "1234567"))
      result.errors.map(_.message) must contain(lengthKey)
    }

    "must not bind strings shorter than 4 characters" in {
      val result = form.bind(Map(fieldName -> "123"))
      result.errors.map(_.message) must contain(lengthKey)
    }

    "must not bind a single character" in {
      val result = form.bind(Map(fieldName -> "1"))
      result.errors.map(_.message) must contain(lengthKey)
    }

    "must not bind purely alphabetic strings" in {
      val result = form.bind(Map(fieldName -> "ABCD"))
      result.errors.map(_.message) must contain(invalidCharsKey)
    }

    "must not bind mixed alphanumeric strings" in {
      val result = form.bind(Map(fieldName -> "AB12"))
      result.errors.map(_.message) must contain(invalidCharsKey)
    }

    "must not bind strings with whitespace" in {
      val result = form.bind(Map(fieldName -> "12 4"))
      result.errors.map(_.message) must contain(invalidCharsKey)
    }

    "must not bind strings with special characters" in {
      val result = form.bind(Map(fieldName -> "12-4"))
      result.errors.map(_.message) must contain(invalidCharsKey)
    }

    "must not bind a 4-digit numeric code that isn't on the allowlist (0001)" in {
      val result = form.bind(Map(fieldName -> "0001"))
      result.errors.map(_.message) must contain(unrecognisedKey)
    }

    "must not bind a 4-digit numeric code that isn't on the allowlist (9999)" in {
      val result = form.bind(Map(fieldName -> "9999"))
      result.errors.map(_.message) must contain(unrecognisedKey)
    }

    "must not bind a 4-digit numeric code that sits between listed ranges (5000)" in {
      val result = form.bind(Map(fieldName -> "5000"))
      result.errors.map(_.message) must contain(unrecognisedKey)
    }

    "must not bind a 4-digit numeric code just above the Scottish range (9080)" in {
      // 9079 is on the list (highest), 9080 is not.
      val result = form.bind(Map(fieldName -> "9080"))
      result.errors.map(_.message) must contain(unrecognisedKey)
    }

    "must not bind all-zeros" in {
      val result = form.bind(Map(fieldName -> "0000"))
      result.errors.map(_.message) must contain(unrecognisedKey)
    }
    
    "must report length error (not invalidChars) for a too-short non-numeric string" in {
      val result = form.bind(Map(fieldName -> "AB"))
      result.errors.map(_.message) must contain(lengthKey)
      result.errors.map(_.message) must not contain invalidCharsKey
    }

    "must report invalidChars (not unrecognised) for a 4-character non-numeric string" in {
      val result = form.bind(Map(fieldName -> "ABCD"))
      result.errors.map(_.message) must contain(invalidCharsKey)
      result.errors.map(_.message) must not contain unrecognisedKey
    }

    "must report unrecognised (not invalidChars) for a 4-digit code that's numeric but off the list" in {
      val result = form.bind(Map(fieldName -> "0001"))
      result.errors.map(_.message) must contain(unrecognisedKey)
      result.errors.map(_.message) must not contain invalidCharsKey
    }
  }
}