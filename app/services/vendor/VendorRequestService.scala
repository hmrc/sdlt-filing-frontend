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

import models.Vendor
import models.vendor.{CreateVendorRequest, UpdateVendorRequest}

class VendorRequestService {

  def convertToVendorRequest(vendor: Vendor,
                             stornId: String,
                             returnResourceRef: String): CreateVendorRequest = {
    CreateVendorRequest(stornId = stornId,
      returnResourceRef = returnResourceRef,
      title = vendor.title,
      forename1 = vendor.forename1,
      forename2 = vendor.forename2,
      name = vendor.name.getOrElse(""),
      houseNumber = vendor.houseNumber,
      addressLine1 = vendor.address1.getOrElse(""),
      addressLine2 = vendor.address2,
      addressLine3 = vendor.address3,
      addressLine4 = vendor.address4,
      postcode = vendor.postcode,
      isRepresentedByAgent = vendor.isRepresentedByAgent.getOrElse(""))
  }

  def convertToUpdateVendorRequest(vendor: Vendor,
                                   stornId: String,
                                   returnResourceRef: String,
                                   vendorResourceRef: String,
                                   nextVendorId: Option[String]): UpdateVendorRequest = {
    UpdateVendorRequest(stornId = stornId,
      returnResourceRef = returnResourceRef, title = vendor.title,
      forename1 = vendor.forename1,
      forename2 = vendor.forename2,
      name = vendor.name.getOrElse(""),
      houseNumber = vendor.houseNumber,
      addressLine1 = vendor.address1.getOrElse(""),
      addressLine2 = vendor.address2,
      addressLine3 = vendor.address3,
      addressLine4 = vendor.address4,
      postcode = vendor.postcode,
      isRepresentedByAgent = vendor.isRepresentedByAgent.getOrElse(""),
      vendorResourceRef = vendorResourceRef,
      nextVendorId = nextVendorId)
  }
}
