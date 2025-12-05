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
import play.api.data.Form

import javax.inject.Inject

class PurchaserNationalInsuranceFormProvider @Inject() extends Mappings {

  private val nationalInsuranceNumberLength = 9
  private val formNumberRegex = "^(?!FY)?(?!GB)(?!NK)(?!TN)(?!ZZ)[A-CEGHJ-PR-TW-Z][A-CEGHJ-NPR-TW-Z][0-9]{6}[A-D]?"

  def apply(): Form[String] =
    Form(
      "nationalInsuranceNumber" -> text("purchaserNationalInsurance.error.required")
        .verifying(maxLength(nationalInsuranceNumberLength, "purchaserNationalInsurance.error.length"))
        .verifying(regexp(formNumberRegex, "purchaserNationalInsurance.error.invalid"))
    )
}
