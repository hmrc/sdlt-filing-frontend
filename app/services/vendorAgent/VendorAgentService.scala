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

package services.vendorAgent

import models.address.*
import models.vendorAgent.*
import models.{ReturnAgent, UserAnswers}
import pages.vendorAgent.*
import scala.util.Try

class VendorAgentService {

  def populateAssignedVendorAgentInSession(returnAgent: ReturnAgent, userAnswers: UserAnswers): Try[UserAnswers] = {

    returnAgent.returnAgentID match {
      case Some(agentId) =>

        val vendorAgentAddress = Address(
          line1 = returnAgent.address1.getOrElse(""),
          line2 = returnAgent.address2,
          line3 = returnAgent.address3,
          line4 = returnAgent.address4,
          postcode = returnAgent.postcode
        )

        val vendorAgentsContactDetails = VendorAgentsContactDetails(
          phoneNumber = returnAgent.phone,
          emailAddress = returnAgent.email
        )

        val hasContactDetails = returnAgent.phone.isDefined || returnAgent.email.isDefined
        val hasReference = returnAgent.reference.isDefined

        for {
          withId <- userAnswers.set(VendorAgentOverviewPage, agentId)
          withName <- returnAgent.name match {
            case Some(name) => withId.set(AgentNamePage, name)
            case None => Try(withId)
          }
          withAddress <- returnAgent.address1 match {
            case Some(_) => withName.set(VendorAgentAddressPage, vendorAgentAddress)
            case None => Try(withName)
          }
          withContact <- withAddress.set(AddVendorAgentContactDetailsPage, hasContactDetails)
          withDetails <- if (hasContactDetails) withContact.set(VendorAgentsContactDetailsPage, vendorAgentsContactDetails)
          else Try(withContact)
          withAddRef <- withDetails.set(VendorAgentsAddReferencePage, if (hasReference) VendorAgentsAddReference.Yes else VendorAgentsAddReference.No)
          finalAnswers <- returnAgent.reference match {
            case Some(ref) => withAddRef.set(VendorAgentsReferencePage, ref)
            case None => Try(withAddRef)
          }
        } yield finalAnswers

      case None =>
        Try(throw new IllegalStateException(s"ReturnAgent is missing a returnAgentID"))
    }
  }
  
}
