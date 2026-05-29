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

package forms.vendor

import forms.mappings.Mappings
import models.vendor.VendorName
import play.api.data.Form
import play.api.data.Forms.*

import javax.inject.Inject

class VendorOrCompanyNameFormProvider @Inject() extends Mappings {

  private val formRegex = "[A-Za-z0-9 ~!@%&'()*+,\\-./:=?\\[\\]^_{}\\;]*"

  def apply(vendorOrCompany:String): Form[VendorName] =
    Form(
      mapping(
        "forename1" -> optionalText()
          .verifying(optionalMaxLength(14, s"vendor.individual.firstName.error.length"))
          .verifying(optionalRegexp(formRegex, s"vendor.individual.firstName.regex.error")),
        "forename2" -> optionalText()
          .verifying(optionalMaxLength(14, s"vendor.individual.middleName.error.length"))
          .verifying(optionalRegexp(formRegex, s"vendor.individual.middleName.regex.error")),
        "name" -> text(s"vendor.$vendorOrCompany.name.error.required")
          .verifying(maxLength(56, s"vendor.$vendorOrCompany.name.error.length"))
          .verifying(regexp(formRegex, s"vendor.$vendorOrCompany.name.regex.error"))
      )(VendorName.apply)(o => Some(Tuple.fromProductTyped(o)))
    )
}