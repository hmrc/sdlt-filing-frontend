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
import play.api.i18n.Messages

import javax.inject.Inject

class PurchaserFormOfIdIndividualFormProvider @Inject() extends Mappings {

  private val idNumberOrReferenceMaxLength = 14
  private val countryIssuedMaxLength = 28

  private val formIdNumberOrReferenceRegex = "[A-Za-z0-9 ~!@%&'()*+,\\-./:=?\\[\\]^_{}\\;]*"
  private val formCountryIssuedRegex = "[A-Za-z0-9 ~!@%&'()*+,\\-./:=?\\[\\]^_{}\\;]*"

   def apply(purchaserName: String)(implicit messages: Messages): Form[PurchaserFormOfIdIndividual] = Form(
     mapping(
      "idNumberOrReference" -> text(messages("purchaser.formOfIdIndividual.error.idNumberOrReference.required", purchaserName))
        .verifying(maxLength(idNumberOrReferenceMaxLength, messages("purchaser.formOfIdIndividual.error.idNumberOrReference.length", purchaserName)))
        .verifying(regexp(formIdNumberOrReferenceRegex, messages("purchaser.formOfIdIndividual.error.idNumberOrReference.invalid", purchaserName))),

       "countryIssued" -> text(messages("purchaser.formOfIdIndividual.error.countryIssued.required", purchaserName))
         .verifying(maxLength(countryIssuedMaxLength, messages("purchaser.formOfIdIndividual.error.countryIssued.length", purchaserName)))
         .verifying(regexp(formCountryIssuedRegex, messages("purchaser.formOfIdIndividual.error.countryIssued.invalid", purchaserName)))
    )(PurchaserFormOfIdIndividual.apply)(o => Some(Tuple.fromProductTyped(o))))
 }
