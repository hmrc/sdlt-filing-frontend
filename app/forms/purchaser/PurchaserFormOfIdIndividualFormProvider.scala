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
import models.purchaser.PurchaserFormOfIdIndividual
import play.api.data.Form
import play.api.data.Forms.*

import javax.inject.Inject

class PurchaserFormOfIdIndividualFormProvider @Inject() extends Mappings {

  private val idNumberOrReferenceMaxLength = 14
  private val countryIssuedMaxLength = 28

  private val formIdNumberOrReferenceRegex = "[A-Za-z0-9 ~!@%&'()*+,\\-./:=?\\[\\]^_{}\\;]*"
  private val formCountryIssuedRegex = "[A-Za-z0-9 ~!@%&'()*+,\\-./:=?\\[\\]^_{}\\;]*"

   def apply(): Form[PurchaserFormOfIdIndividual] = Form(
     mapping(
      "idNumberOrReference" -> text("purchaser.formOfIdIndividual.error.idNumberOrReference.required")
        .verifying(maxLength(idNumberOrReferenceMaxLength, "purchaser.formOfIdIndividual.error.idNumberOrReference.length"))
        .verifying(regexp(formIdNumberOrReferenceRegex, "purchaser.formOfIdIndividual.error.idNumberOrReference.invalid")),

       "countryIssued" -> text("purchaser.formOfIdIndividual.error.countryIssued.required")
         .verifying(maxLength(countryIssuedMaxLength, "purchaser.formOfIdIndividual.error.countryIssued.length"))
         .verifying(regexp(formCountryIssuedRegex, "purchaser.formOfIdIndividual.error.countryIssued.invalid"))
    )(PurchaserFormOfIdIndividual.apply)(o => Some(Tuple.fromProductTyped(o))))
 }
