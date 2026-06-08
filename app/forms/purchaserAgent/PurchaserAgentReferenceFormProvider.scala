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

package forms.purchaserAgent

import forms.mappings.Mappings
import play.api.data.Form
import play.api.i18n.Messages

import javax.inject.Inject

class PurchaserAgentReferenceFormProvider @Inject() extends Mappings {

  private val formRegex = "[A-Za-z0-9 ~!@%&'()*+,\\-./:=?\\[\\]^_{};]*"

  def apply(agentName: String)(implicit messages: Messages): Form[String] =
    Form(
      "value" -> text(messages("purchaserAgent.reference.error.required", agentName))
        .verifying(maxLength(14, messages("purchaserAgent.reference.error.length", agentName)))
        .verifying(regexp(formRegex, messages("purchaserAgent.reference.error.invalid", agentName)))
    )
}
