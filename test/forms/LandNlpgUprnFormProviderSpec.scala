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

package forms

import forms.behaviours.StringFieldBehaviours
import forms.land.LandNlpgUprnFormProvider
import play.api.data.FormError
import org.scalacheck.Gen

class LandNlpgUprnFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "land.nlpgUprn.error.required"
  val lengthKey = "land.nlpgUprn.error.length"
  val formatKey = "land.nlpgUprn.error.format"
  val maxLength = 14

  val form = new LandNlpgUprnFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(
        "123456",
        "NI123456",
        "ni987654",
        "1",
        "NI1"
      )
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must not bind strings that do not match the NLPG UPRN format" in {
      val invalidValues = Seq(
        "ABC123",
        "NI",
        "ni",
        "NI123ABC",
        "123 456",
        "NI-123"
      )

      invalidValues.foreach { value =>
        val result = form.bind(Map(fieldName -> value))
        result.errors must contain(
          FormError(fieldName, formatKey, Seq("(NI|ni)?[0-9]+"))
        )
      }
    }
  }
}
