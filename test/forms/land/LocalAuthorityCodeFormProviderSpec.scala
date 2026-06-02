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


  private val requiredKey            = "land.localAuthorityCode.error.required"
  private val lengthKey              = "land.localAuthorityCode.error.length"
  private val invalidCharsKey        = "land.localAuthorityCode.error.invalidChars"
  private val invalidFormatKey       = "land.localAuthorityCode.error.invalidFormat"
  private val invalidAreaKey         = "land.localAuthorityCode.error.invalidArea"
  private val invalidAreaPostcodeKey = "land.localAuthorityCode.error.invalidAreaPostcode"
  private val welshKey               = "land.localAuthorityCode.error.welsh"
  val maxLength = 4
  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  val effectiveTransactionDate: Option[LocalDate] = Option(LocalDate.parse("2015-04-01", formatter))
  val contractEffDate: Option[LocalDate] = Option(LocalDate.parse("2015-04-01", formatter))
  val postcode: Option[String] = Some("RG1 7NQ")

   val form = new LocalAuthorityCodeFormProvider()(effectiveTransactionDate,contractEffDate, postcode)

  ".value" - {

    val fieldName = "value"

    "must not bind strings longer than 4 characters" in {
      val result = form.bind(Map(fieldName -> "1234567"))
      result.errors.map(_.message) must contain(lengthKey)
    }

    "must not bind strings shorter than 4 characters" in {
      val result = form.bind(Map(fieldName -> "123"))
      result.errors.map(_.message) must contain(lengthKey)
    }


    "must not bind strings with mandatory value" in {
      val result = form.bind(Map(fieldName -> ""))
      result.errors.map(_.message) must contain(requiredKey)
    }

    "must not bind non-numeric characters" in {
      val result = form.bind(Map(fieldName -> "AB12"))
      result.errors.map(_.message) must contain(invalidCharsKey)
    }

    "must not bind a numeric code that is not in the approved list" in {
      val result = form.bind(Map(fieldName -> "1234"))
      result.errors.map(_.message) must contain(invalidFormatKey)
    }

    "must not bind a Scottish authority code when date rules disqualify it" in {
      val scotlandForm = new LocalAuthorityCodeFormProvider()(
        Some(LocalDate.parse("2020-01-01", formatter)),
        Some(LocalDate.parse("2020-01-01", formatter)),
        Some("RG1 7NQ")
      )
      val result = scotlandForm.bind(Map(fieldName -> "8999"))
      result.errors.map(_.message) must contain(invalidAreaKey)
    }

    "must not bind a valid code when the postcode is Scottish" in {
      val scottishPostcodeForm = new LocalAuthorityCodeFormProvider()(
        Some(LocalDate.parse("2020-01-01", formatter)),
        Some(LocalDate.parse("2020-01-01", formatter)),
        Some("EH1 1AB")
      )
      val result = scottishPostcodeForm.bind(Map(fieldName -> "1150"))
      result.errors.map(_.message) must contain(invalidAreaPostcodeKey)
    }

    "must not bind a Welsh authority code used after the Wales Act effective date" in {
      val welshForm = new LocalAuthorityCodeFormProvider()(
        Some(LocalDate.parse("2019-01-01", formatter)),
        Some(LocalDate.parse("2019-01-01", formatter)),
        Some("RG1 7NQ")
      )
      val result = welshForm.bind(Map(fieldName -> "6810"))
      result.errors.map(_.message) must contain(welshKey)
    }
  }

}
