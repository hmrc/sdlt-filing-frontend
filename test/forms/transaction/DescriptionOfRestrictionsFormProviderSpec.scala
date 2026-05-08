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

package forms.transaction

import forms.behaviours.StringFieldBehaviours
import org.scalacheck.Gen
import play.api.data.FormError

class DescriptionOfRestrictionsFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "transaction.descriptionOfRestrictions.error.required"
  val lengthKey = "transaction.descriptionOfRestrictions.error.length"
  val invalidKey = "transaction.descriptionOfRestrictions.error.regex"

  val maxLength = 42

  val form = new DescriptionOfRestrictionsFormProvider()()

  private val allowedChars =
    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789 -`"

  private val validStrings: Gen[String] =
    Gen.choose(1, maxLength).flatMap { size =>
      Gen.listOfN(size, Gen.oneOf(allowedChars)).map(_.mkString)
    }

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validStrings
    )

    "not bind strings longer than maxLength" in {
      val longValue = List.fill(maxLength + 1)('A').mkString

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

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
