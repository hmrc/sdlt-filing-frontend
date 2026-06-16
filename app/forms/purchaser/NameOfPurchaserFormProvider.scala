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

import forms.mappings.Mappings
import models.purchaser.NameOfPurchaser
import play.api.data.Form
import play.api.data.Forms.*

import javax.inject.Inject

class NameOfPurchaserFormProvider @Inject() extends Mappings {

  private val formRegex = "[A-Za-z0-9 ~!@%&'()*+,\\-./:=?\\[\\]^_{}\\;]*"

  def apply(purchaserOrCompany: String): Form[NameOfPurchaser] = {
    if (purchaserOrCompany == "Individual") {
      Form(
        mapping(
          "forename1" -> optionalText()
            .verifying(optionalMaxLength(14, "purchaser.individual.error.length.firstName"))
            .verifying(optionalRegexp(formRegex, "purchaser.name.form.regex.error.firstName")),
          "forename2" -> optionalText()
            .verifying(optionalMaxLength(14, "purchaser.individual.error.length.middleName"))
            .verifying(optionalRegexp(formRegex, "purchaser.name.form.regex.error.middleName")),
          "name" -> text("purchaser.individual.error.required")
            .verifying(maxLength(56, "purchaser.individual.error.length.lastName"))
            .verifying(regexp(formRegex, "purchaser.name.form.regex.error.lastName"))
        )(NameOfPurchaser.apply)(o => Some(Tuple.fromProductTyped(o)))
      )
    } else {
      Form(
        mapping(
          "forename1" -> ignored(Option.empty[String]),
          "forename2" -> ignored(Option.empty[String]),
          "name" -> text("purchaser.company.error.required")
            .verifying(maxLength(56, "purchaser.company.error.length.name"))
            .verifying(regexp(formRegex, "purchaser.name.form.regex.error.company"))
        )(NameOfPurchaser.apply)(o => Some(Tuple.fromProductTyped(o)))
      )
    }
  }
}