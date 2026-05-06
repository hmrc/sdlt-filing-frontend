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

package forms.taxCalculation

import forms.mappings.Mappings
import play.api.data.Form

import javax.inject.Inject

class TotalAmountToPayFormProvider @Inject() extends Mappings {

  private val totalAmountToPayRegex = "^[0-9,]*(\\.[0-9]+)?$\t^[0-9,]+[.]{0,1}[0]{0,2}"
  private val maximumLength :Int = 16

  def apply(): Form[String] =
    Form(
      "value" -> text("taxCalculation.totalAmountDue.totalAmountToPay.heading2")
        .verifying(regexp(totalAmountToPayRegex, "taxCalculation.totalAmountDue.totalAmountToPay.error.invalid"))
        .verifying(maxLength(maximumLength, "taxCalculation.totalAmountDue.totalAmountToPay.error.length"))
    )

}

