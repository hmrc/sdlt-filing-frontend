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

import javax.inject.Inject

class VendorAgentsContactDetailsFormProvider @Inject() extends Mappings {

  private val agentNumberMaxLength = 14
  private val agentEmailMaxLength = 36
  
  private val formNumberRegex = "[A-Za-z0-9 \\~\\!\\@\\%\\&\\'\\(\\)\\*\\+,\\-\\.\\/\\:\\=\\?\\[\\]\\^\\_\\{\\}\\;]*"
  private val formEmailRegex = "^[^@|<>\"'`]+@[^@|<>\"'`]+$"


  def apply(): Form[VendorAgentsContactDetails] = Form(
    mapping(
      "phoneNumber" -> optionalText()
        .verifying(optionalMaxLength(agentNumberMaxLength,"vendorAgent.vendorAgentsContactDetails.error.agentPhoneNumber.length"))
        .verifying(optionalRegexp(formNumberRegex, "vendorAgent.vendorAgentsContactDetails.error.agentPhoneNumber.invalid"))
      ,
      "emailAddress" -> optionalText()
        .verifying(optionalMaxLength(agentEmailMaxLength, "vendorAgent.vendorAgentsContactDetails.error.agentEmailAddress.length"))
        .verifying(optionalRegexp(formEmailRegex, "vendorAgent.vendorAgentsContactDetails.error.agentEmailAddress.invalid"))
  )(VendorAgentsContactDetails.apply)( x =>
    Some((x.phoneNumber, x.emailAddress))
    )
    .verifying(
      "vendorAgent.vendorAgentsContactDetails.error.oneRequired",
      details =>
        details.phoneNumber.exists(_.trim.nonEmpty) ||
          details.emailAddress.exists(_.trim.nonEmpty)
    )
  )
 }
