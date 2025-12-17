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
import models.purchaser.CompanyFormOfId
import play.api.data.Form
import play.api.data.Forms.*

import javax.inject.Inject

class CompanyFormOfIdFormProvider @Inject() extends Mappings {

  private val referenceIdMaxLength = 14
  private val countryIssuesMaxLength = 28

  private val referenceIdRegex = "[A-Za-z0-9 \\~\\!\\@\\%\\&\\'\\(\\)\\*\\+,\\-\\.\\/\\:\\=\\?\\[\\]\\^\\_\\{\\}\\;]*"
  private val countryIssuedRegex = "[A-Za-z0-9 ~!@%&'()*+,\\-./:=?\\[\\]^_{}\\;]*"

   def apply(): Form[CompanyFormOfId] = Form(
     mapping(
      "referenceId" -> text("purchaser.companyFormOfId.error.referenceId.required")
        .verifying(maxLength(referenceIdMaxLength, "purchaser.companyFormOfId.error.referenceId.length"))
        .verifying(regexp(referenceIdRegex, "purchaser.companyFormOfId.error.referenceId.invalid")),
       
      "countryIssued" -> text("purchaser.companyFormOfId.error.countryIssued.required")
        .verifying(maxLength(countryIssuesMaxLength, "purchaser.companyFormOfId.error.countryIssued.length"))
        .verifying(regexp(countryIssuedRegex, "purchaser.companyFormOfId.error.countryIssued.invalid"))
    )(CompanyFormOfId.apply)(x => Some((x.referenceId, x.countryIssued)))
   )
 }
