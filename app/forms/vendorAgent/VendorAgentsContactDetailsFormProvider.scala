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

package forms.vendorAgent

import forms.mappings.Mappings
import models.vendorAgent.VendorAgentsContactDetails
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.i18n.Messages

import javax.inject.Inject

class VendorAgentsContactDetailsFormProvider @Inject() extends Mappings {

  private val agentEmailMaxLength = 36

  private val emailInvalidCharsRegex = "^[^|<>\"'`]+$"
  private val emailFormatRegex = "^[^@|<>\"'`]+@[^@|<>\"'`]+$"

  def apply(agentName: String)(implicit messages: Messages): Form[VendorAgentsContactDetails] = Form(
    mapping(
      "phoneNumber" -> optionalPhoneNumber(
        messages("vendorAgent.vendorAgentsContactDetails.error.agentPhoneNumber.length", agentName),
        messages("vendorAgent.vendorAgentsContactDetails.error.agentPhoneNumber.invalid", agentName)
      ),
      "emailAddress" -> optionalText()
        .verifying(firstError(
          optionalMaxLength(agentEmailMaxLength, messages("vendorAgent.vendorAgentsContactDetails.error.agentEmailAddress.maxlength", agentName)),
          optionalRegexp(emailInvalidCharsRegex, messages("vendorAgent.vendorAgentsContactDetails.error.agentEmailAddress.invalid", agentName)),
          optionalRegexp(emailFormatRegex, messages("vendorAgent.vendorAgentsContactDetails.error.agentEmailAddress.invalidFormat", agentName))
        ))
  )(VendorAgentsContactDetails.apply)( x =>
    Some((x.phoneNumber, x.emailAddress))
    )
    .verifying(
      messages("vendorAgent.vendorAgentsContactDetails.error.oneRequired",agentName),
      details =>
        details.phoneNumber.exists(_.trim.nonEmpty) ||
          details.emailAddress.exists(_.trim.nonEmpty)
    )
  )
 }
