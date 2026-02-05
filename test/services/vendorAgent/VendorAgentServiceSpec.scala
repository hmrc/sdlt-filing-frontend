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

import base.SpecBase
import models.address.Address
import models.vendorAgent.{VendorAgentsAddReference, VendorAgentsContactDetails}
import models.{FullReturn, ReturnAgent, UserAnswers}
import pages.vendorAgent.*

class VendorAgentServiceSpec extends SpecBase {

  private val service = new VendorAgentService()

  private def emptyFullReturn: FullReturn = FullReturn(
    returnResourceRef = "REF123",
    stornId = "TESTSTORN",
    vendor = None,
    purchaser = None,
    transaction = None,
    returnAgent = None
  )

  "VendorAgentService" - {

    "populateAssignedVendorAgentInSession" - {

      "must populate vendor agent pages when returnAgentID is present" in {

        val fullReturn = emptyFullReturn
        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

        val returnAgent = ReturnAgent(
          returnAgentID = Some("AGENT123"),
          agentType = Some("VENDOR"),
          name = Some("Assigned Vendor Agent"),
          houseNumber = None,
          address1 = Some("1 Assigned Street"),
          address2 = Some("Assigned Town"),
          address3 = Some("Assigned City"),
          address4 = None,
          postcode = Some("ZZ1 1ZZ"),
          phone = Some("07123456789"),
          email = Some("assigned@example.com"),
          reference = Some("ABF1241"),
          isAuthorised = Some("NO")
        )

        val expectedAddress = Address(
          line1 = "1 Assigned Street",
          line2 = Some("Assigned Town"),
          line3 = Some("Assigned City"),
          line4 = None,
          postcode = Some("ZZ1 1ZZ")
        )

        val expectedContactDetails = VendorAgentsContactDetails(
          phoneNumber = Some("07123456789"),
          emailAddress = Some("assigned@example.com")
        )

        val result =
          service.populateAssignedVendorAgentInSession(returnAgent, userAnswers)

        result.isSuccess mustBe true

        val updatedAnswers = result.get

        updatedAnswers.get(AgentNamePage) mustBe Some("Assigned Vendor Agent")
        updatedAnswers.get(VendorAgentAddressPage) mustBe Some(expectedAddress)
        updatedAnswers.get(VendorAgentsContactDetailsPage) mustBe Some(expectedContactDetails)
        updatedAnswers.get(VendorAgentsAddReferencePage) mustBe Some(VendorAgentsAddReference.Yes)
        updatedAnswers.get(VendorAgentsReferencePage) mustBe Some("ABF1241")
      }

      "must fail with IllegalStateException when returnAgentID is missing" in {

        val userAnswers = emptyUserAnswers

        val returnAgent = ReturnAgent(
          returnAgentID = None,
          agentType = Some("VENDOR"),
          name = Some("Broken Agent"),
          houseNumber = None,
          address1 = Some("Broken Street"),
          address2 = None,
          address3 = None,
          address4 = None,
          postcode = Some("XX1 1XX"),
          phone = None,
          email = None,
          reference = None
        )

        val result =
          service.populateAssignedVendorAgentInSession(returnAgent, userAnswers)

        result.isFailure mustBe true
        result.failed.get mustBe a[IllegalStateException]
      }
    }
  }
}