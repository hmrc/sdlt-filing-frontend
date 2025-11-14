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
import models.requests.DataRequest

class PurchaserSurnameOrCompanyNameFormProvider @Inject() extends Mappings {

  private val formRegex = "[A-Za-z0-9 ~!@%&'()*+,\\-./:=?\\[\\]^_{}\\;]*"

  def apply(individualOrBusiness:String): Form[String] =
    Form(
      "purchaserSurnameOrCompanyName" -> text(errorKey(individualOrBusiness))
          .verifying(maxLength(56, maxLengthErrorKey(individualOrBusiness)))
          .verifying(regexp(formRegex, regexErrorKey(individualOrBusiness)))

  )

  private def errorKey(choice: String): String = choice match {
    case "Individual" => "purchaser.name.form.no.input.error.individual"
    case "Business" => "purchaser.name.form.no.input.error.business"
    case _ => "p.name.form.no.input.error"
  }


  private def maxLengthErrorKey(choice: String): String = choice match {
    case "Individual" => "purchaser.name.form.maxLength.error.individual"
    case "Business" => "purchaser.name.form.maxLength.error.business"
    case _ => "purchaser.name.form.maxLength.error"
  }


  private def regexErrorKey(choice: String): String = choice match {
    case "Individual" => "purchaser.name.form.regex.error.individual"
    case "Business" => "purchaser.name.form.regex.error.business"
    case _ => "purchaser.name.form.regex.error"
  }


}
