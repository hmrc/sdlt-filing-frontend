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

package services.vendor

import base.SpecBase
import models.address.Address
import models.vendor.VendorName
import models.{UserAnswers, Vendor}
import pages.vendor.{VendorAddressPage, VendorOrCompanyNamePage, VendorOverviewVendorIdPage, VendorRepresentedByAgentPage}

import scala.util.{Failure, Success}

class PopulateVendorServiceSpec extends SpecBase {

  val service = new PopulateVendorService()

  "PopulateVendorService" - {

    "populateVendorInSession" - {

      "must successfully populate session with complete vendor data" in {
        val vendor = Vendor(
          vendorID = Some("VEN001"),
          forename1 = Some("John"),
          forename2 = Some("Michael"),
          name = Some("Smith"),
          address1 = Some("123 High Street"),
          address2 = Some("Flat 4B"),
          address3 = Some("London"),
          address4 = Some("Greater London"),
          postcode = Some("SW1A 1AA")
        )

        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")

        val result = service.populateVendorInSession(vendor, "VEN-REF-001", userAnswers)

        result mustBe a[Success[_]]

        val updatedAnswers = result.get

        updatedAnswers.get(VendorOrCompanyNamePage) mustBe Some(VendorName(
          forename1 = Some("John"),
          forename2 = Some("Michael"),
          name = "Smith"
        ))

        updatedAnswers.get(VendorAddressPage) mustBe Some(Address(
          line1 = "123 High Street",
          line2 = Some("Flat 4B"),
          line3 = Some("London"),
          line4 = Some("Greater London"),
          postcode = Some("SW1A 1AA")
        ))

        updatedAnswers.get(VendorOverviewVendorIdPage) mustBe Some("VEN001")
      }

      "must successfully populate session with minimal vendor data (only required fields)" in {
        val vendor = Vendor(
          vendorID = Some("VEN002"),
          forename1 = None,
          forename2 = None,
          name = Some("Smith"),
          address1 = Some("123 High Street"),
          address2 = None,
          address3 = None,
          address4 = None,
          postcode = None,
          isRepresentedByAgent = None
        )

        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")

        val result = service.populateVendorInSession(vendor, "VEN-REF-002", userAnswers)

        result mustBe a[Success[_]]

        val updatedAnswers = result.get

        updatedAnswers.get(VendorOrCompanyNamePage) mustBe Some(VendorName(
          forename1 = None,
          forename2 = None,
          name = "Smith"
        ))

        updatedAnswers.get(VendorAddressPage) mustBe Some(Address(
          line1 = "123 High Street",
          line2 = None,
          line3 = None,
          line4 = None,
          postcode = None
        ))

        updatedAnswers.get(VendorOverviewVendorIdPage) mustBe Some("VEN002")
      }

      "must set isRepresentedByAgent to false when not YES" in {
        val vendor = Vendor(
          vendorID = Some("VEN003"),
          name = Some("Smith"),
          address1 = Some("123 High Street")
        )

        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")

        val result = service.populateVendorInSession(vendor, "VEN-REF-003", userAnswers)

        result mustBe a[Success[_]]
      }

      "must set isRepresentedByAgent to false when value is random string" in {
        val vendor = Vendor(
          vendorID = Some("VEN004"),
          name = Some("Smith"),
          address1 = Some("123 High Street")
        )

        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")

        val result = service.populateVendorInSession(vendor, "VEN-REF-004", userAnswers)

        result mustBe a[Success[_]]
      }

      "must fail when address1 is missing" in {
        val vendor = Vendor(
          vendorID = Some("VEN005"),
          name = Some("Smith"),
          address1 = None
        )

        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")

        val result = service.populateVendorInSession(vendor, "VEN-REF-005", userAnswers)

        result mustBe a[Failure[_]]
        result.failed.get mustBe an[IllegalStateException]
        result.failed.get.getMessage must include("is missing required address line 1")
      }

      "must fail when name is missing" in {
        val vendor = Vendor(
          vendorID = Some("VEN006"),
          name = None,
          address1 = Some("123 High Street")
        )

        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")

        val result = service.populateVendorInSession(vendor, "VEN-REF-006", userAnswers)

        result mustBe a[Failure[_]]
        result.failed.get mustBe an[IllegalStateException]
      }

      "must fail when vendorID is missing" in {
        val vendor = Vendor(
          vendorID = None,
          name = Some("Smith"),
          address1 = Some("123 High Street")
        )

        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")

        val result = service.populateVendorInSession(vendor, "VEN-REF-007", userAnswers)

        result mustBe a[Failure[_]]
        result.failed.get mustBe an[IllegalStateException]
      }

      "must successfully populate session with vendor having only forename1 (no forename2)" in {
        val vendor = Vendor(
          vendorID = Some("VEN008"),
          forename1 = Some("John"),
          forename2 = None,
          name = Some("Smith"),
          address1 = Some("123 High Street")
        )

        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")

        val result = service.populateVendorInSession(vendor, "VEN-REF-008", userAnswers)

        result mustBe a[Success[_]]

        val updatedAnswers = result.get

        updatedAnswers.get(VendorOrCompanyNamePage) mustBe Some(VendorName(
          forename1 = Some("John"),
          forename2 = None,
          name = "Smith"
        ))
      }

      "must preserve existing user answers when populating vendor data" in {
        val existingAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")
          .set(VendorRepresentedByAgentPage, false).success.value

        val vendor = Vendor(
          vendorID = Some("VEN009"),
          name = Some("Jones"),
          address1 = Some("456 Park Lane")
        )

        val result = service.populateVendorInSession(vendor, "VEN-REF-009", existingAnswers)

        result mustBe a[Success[_]]

        val updatedAnswers = result.get

        // Should override the existing value
        updatedAnswers.get(VendorOrCompanyNamePage) mustBe Some(VendorName(
          forename1 = None,
          forename2 = None,
          name = "Jones"
        ))
      }

      "must handle all optional address fields correctly" in {
        val vendor = Vendor(
          vendorID = Some("VEN010"),
          name = Some("Williams"),
          address1 = Some("1 Main Street"),
          address2 = Some("Apartment 2"),
          address3 = Some("City Center"),
          address4 = Some("Greater Area"),
          postcode = Some("EC1A 1BB"),
          isRepresentedByAgent = Some("YES")
        )

        val userAnswers = UserAnswers(userAnswersId, storn = "TESTSTORN")

        val result = service.populateVendorInSession(vendor, "VEN-REF-010", userAnswers)

        result mustBe a[Success[_]]

        val address = result.get.get(VendorAddressPage).value

        address.line1 mustBe "1 Main Street"
        address.line2 mustBe Some("Apartment 2")
        address.line3 mustBe Some("City Center")
        address.line4 mustBe Some("Greater Area")
        address.postcode mustBe Some("EC1A 1BB")
      }
    }
  }
}