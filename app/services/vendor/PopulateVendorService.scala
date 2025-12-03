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

import models.UserAnswers
import models._
import models.vendor._
import models.address._
import pages.vendor._
import scala.util.Try

class PopulateVendorService {

  def populateVendorInSession(
                               vendor: Vendor,
                               id: String,
                               userAnswers: UserAnswers): Try[UserAnswers] = {

    (vendor.address1, vendor.name, vendor.vendorID) match {
      case (Some(line1), Some(name), Some(vendorId)) =>
        val vendorName = VendorName(
          forename1 = vendor.forename1,
          forename2 = vendor.forename2,
          name = name
        )

        val address = Address(
          line1 = line1,
          line2 = vendor.address2,
          line3 = vendor.address3,
          line4 = vendor.address4,
          postcode = vendor.postcode
        )

        val whoIsTheVen = if (vendorName.forename1.isDefined || vendorName.forename2.isDefined) whoIsTheVendor.Individual else whoIsTheVendor.Company

        for {
          whoIsTheVendorPage <- userAnswers.set(WhoIsTheVendorPage, whoIsTheVen)
          withName <- whoIsTheVendorPage.set(VendorOrCompanyNamePage, vendorName)
          withAddress <- withName.set(VendorAddressPage, address)
          finalAnswers <- withAddress.set(VendorOverviewVendorIdPage, vendorId)
        } yield finalAnswers

      case _ =>
        Try(throw new IllegalStateException(s"Vendor ${vendor.vendorID} is missing required address line 1"))
    }
  }
  
  
}
