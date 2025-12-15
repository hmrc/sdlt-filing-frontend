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
import models.*
import models.vendor.*
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar

class VendorRequestServiceSpec extends SpecBase with MockitoSugar {

  val service = new VendorRequestService()


  "VendorRequestService" - {

    "must convertToVendorRequest and return createVendorRequest" in {
      val testStornId = "STORN123456"
      val returnResourceRef = "RRF-2024-001"
      val mockVendor = mock[Vendor]

      when(mockVendor.title).thenReturn(Some("Mr"))
      when(mockVendor.forename1).thenReturn(Some("John"))
      when(mockVendor.forename2).thenReturn(Some("Peter"))
      when(mockVendor.name).thenReturn(Some("Doe"))
      when(mockVendor.houseNumber).thenReturn(Some("12"))
      when(mockVendor.address1).thenReturn(Some("Kyn Ave"))
      when(mockVendor.address2).thenReturn(Some("Surrey"))
      when(mockVendor.address3).thenReturn(Some("London"))
      when(mockVendor.address4).thenReturn(Some("Croydon"))
      when(mockVendor.postcode).thenReturn(Some("CR7 8LU"))
      when(mockVendor.isRepresentedByAgent).thenReturn(Some("no"))

      val result = service.convertToVendorRequest(mockVendor, testStornId, returnResourceRef)

      result mustBe a[CreateVendorRequest]
      result.stornId mustBe testStornId
      result.returnResourceRef mustBe returnResourceRef
      result.title mustBe Some("Mr")
      result.forename1 mustBe Some("John")
      result.forename2 mustBe Some("Peter")
      result.name mustBe "Doe"
      result.houseNumber mustBe Some("12")
      result.addressLine1 mustBe "Kyn Ave"
      result.addressLine2 mustBe Some("Surrey")
      result.addressLine3 mustBe Some("London")
      result.addressLine4 mustBe Some("Croydon")
      result.postcode mustBe Some("CR7 8LU")
      result.isRepresentedByAgent mustBe "no"
    }

    "must convertToUpdateVendorRequest and return UpdateVendorRequest" in {
      val testStornId = "STORN123456"
      val returnResourceRef = "RRF-2024-001"
      val mockVendor = mock[Vendor]

      when(mockVendor.title).thenReturn(Some("Mr"))
      when(mockVendor.forename1).thenReturn(Some("John"))
      when(mockVendor.forename2).thenReturn(Some("Peter"))
      when(mockVendor.name).thenReturn(Some("Doe"))
      when(mockVendor.houseNumber).thenReturn(Some("12"))
      when(mockVendor.address1).thenReturn(Some("Kyn Ave"))
      when(mockVendor.address2).thenReturn(Some("Surrey"))
      when(mockVendor.address3).thenReturn(Some("London"))
      when(mockVendor.address4).thenReturn(Some("Croydon"))
      when(mockVendor.postcode).thenReturn(Some("CR7 8LU"))
      when(mockVendor.isRepresentedByAgent).thenReturn(Some("no"))
      when(mockVendor.vendorResourceRef).thenReturn(Some("VRF-001"))
      when(mockVendor.nextVendorID).thenReturn(Some("VID-001"))

      val result = service.convertToUpdateVendorRequest(vendor = mockVendor, stornId = testStornId,
        returnResourceRef = returnResourceRef, vendorResourceRef = "VRF-001", nextVendorId = Some("VID-001"))

      result mustBe a[UpdateVendorRequest]
      result.stornId mustBe testStornId
      result.returnResourceRef mustBe returnResourceRef
      result.title mustBe Some("Mr")
      result.forename1 mustBe Some("John")
      result.forename2 mustBe Some("Peter")
      result.name mustBe "Doe"
      result.houseNumber mustBe Some("12")
      result.addressLine1 mustBe "Kyn Ave"
      result.addressLine2 mustBe Some("Surrey")
      result.addressLine3 mustBe Some("London")
      result.addressLine4 mustBe Some("Croydon")
      result.postcode mustBe Some("CR7 8LU")
      result.isRepresentedByAgent mustBe "no"
      result.vendorResourceRef mustBe "VRF-001"
      result.nextVendorId mustBe Some("VID-001")
    }

  }

}