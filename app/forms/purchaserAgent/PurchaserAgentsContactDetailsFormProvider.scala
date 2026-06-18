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
import models.purchaserAgent.PurchaserAgentsContactDetails
import play.api.data.Form
import play.api.data.Forms.*
import play.api.i18n.Messages

import javax.inject.Inject

class PurchaserAgentsContactDetailsFormProvider @Inject() extends Mappings {

  private val emailMaxLength = 36

  private val emailFormatRegex = "^[^@|<>\"'`]+@[^@|<>\"'`]+$"
  private val emailInvalidCharsRegex = "^[^|<>\"'`]+$"

  def apply(agentName: String)(implicit messages: Messages): Form[PurchaserAgentsContactDetails] = Form(
    mapping(
      "phoneNumber" -> optionalPhoneNumber(
        messages("purchaserAgent.contactDetails.error.agentPhoneNumber.length", agentName),
        messages("purchaserAgent.contactDetails.error.agentPhoneNumber.invalid", agentName)
      ),
      "emailAddress" -> optionalText()
        .verifying(firstError(
          optionalMaxLength(emailMaxLength, messages("purchaserAgent.contactDetails.error.agentEmailAddress.length", agentName)),
          optionalRegexp(emailInvalidCharsRegex, messages("purchaserAgent.contactDetails.error.agentEmailAddress.invalid", agentName)),
          optionalRegexp(emailFormatRegex, messages("purchaserAgent.contactDetails.error.agentEmailAddress.invalidFormat", agentName))
        ))
    )(PurchaserAgentsContactDetails.apply)(x =>
      Some((x.phoneNumber, x.emailAddress))
    )
      .verifying(
        messages("purchaserAgent.contactDetails.error.oneRequired", agentName),
        details =>
          details.phoneNumber.exists(_.trim.nonEmpty) ||
            details.emailAddress.exists(_.trim.nonEmpty)
      )
  )
}