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


 import forms.behaviours. StringFieldBehaviours
 import java.time.LocalDate
 import java.time.format.DateTimeFormatter

class LocalAuthorityCodeFormProviderSpec extends StringFieldBehaviours {


  private val requiredKey = "land.localAuthorityCode.error.required"
  private val lengthKey = "land.localAuthorityCode.error.length"
  private val invalidKeyMessage = "land.localAuthorityCode.constraint.invalid"
  val maxLength = 4
  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  val effectiveTransactionDate: Option[LocalDate] = Option(LocalDate.parse("2015-04-01", formatter))
  val contractEffDate: Option[LocalDate] = Option(LocalDate.parse("2015-04-01", formatter))
  val postcode: Option[String] = Some("RG1 7NQ")

   val form = new LocalAuthorityCodeFormProvider()(effectiveTransactionDate,contractEffDate, postcode)

  ".value" - {

    val fieldName = "value"

    "must  bind strings with max Length " in {
      val result = form.bind(Map(fieldName -> "1234567"))
      result.errors.map(_.message) must contain(lengthKey)
    }


    "must not bind strings with mandatory value" in {
      val result = form.bind(Map(fieldName -> ""))
      result.errors.map(_.message) must contain(requiredKey)
    }

    "must not bind strings with invalid data" in {
      val result = form.bind(Map(fieldName -> "1234"))
      result.errors.map(_.message) must contain(invalidKeyMessage)
    }
  }

}
