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

package forms.preliminary

import forms.mappings.Mappings
import play.api.data.Form

import javax.inject.Inject

class PurchaserSurnameOrCompanyNameFormProvider @Inject() extends Mappings {

  private val formRegex = "[A-Za-z0-9 ~!@%&'()*+,\\-./:=?\\[\\]^_{}\\;]*"

  def apply(individualOrCompany: String): Form[String] =
    Form(
        "purchaserSurnameOrCompanyName" -> text(errorKey(individualOrCompany))
          .verifying(maxLength(56, maxLengthErrorKey(individualOrCompany)))
          .verifying(regexp(formRegex, regexErrorKey(individualOrCompany)))
      )

  private def errorKey(choice: String): String = choice match {
    case "Individual" => "prelim.purchaser.name.form.no.input.error.individual"
    case "Company" => "prelim.purchaser.name.form.no.input.error.company"
    case _ => "prelim.purchaser.name.form.no.input.error"
  }


  private def maxLengthErrorKey(choice: String): String = choice match {
    case "Individual" => "prelim.purchaser.name.form.maxLength.error.individual"
    case "Company" => "prelim.purchaser.name.form.maxLength.error.company"
    case _ => "prelim.purchaser.name.form.maxLength.error"
  }


  private def regexErrorKey(choice: String): String = choice match {
    case "Individual" => "prelim.purchaser.name.form.regex.error.individual"
    case "Company" => "prelim.purchaser.name.form.regex.error.company"
    case _ => "prelim.purchaser.name.form.regex.error"
  }
}
