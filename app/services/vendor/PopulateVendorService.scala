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

import models.*
import models.address.*
import models.vendor.*
import pages.vendor.*

import scala.util.Try

class PopulateVendorService {

  def populateVendorInSession(
                               vendor: Vendor,
                               id: String,
                               userAnswers: UserAnswers): Try[UserAnswers] = {

    vendor.vendorID match {
      case Some(vendorId) =>
        // Build only the answers we actually have. A vendor shown on the
        // incomplete overview is, by definition, missing name and/or address,
        // so anything not present is left unset for the user to fill in.
        val maybeName: Option[VendorName] = vendor.name.map { name =>
          VendorName(
            forename1 = vendor.forename1,
            forename2 = vendor.forename2,
            name = name
          )
        }

        val maybeAddress: Option[Address] = vendor.address1.map { line1 =>
          Address(
            line1 = line1,
            line2 = vendor.address2,
            line3 = vendor.address3,
            line4 = vendor.address4,
            postcode = vendor.postcode
          )
        }

        val whoIsTheVen =
          if (vendor.forename1.isDefined || vendor.forename2.isDefined) whoIsTheVendor.Individual
          else whoIsTheVendor.Company

        for {
          withWho      <- userAnswers.set(WhoIsTheVendorPage, whoIsTheVen)
          withName     <- maybeName.fold(Try(withWho))(name => withWho.set(VendorOrCompanyNamePage, name))
          withAddress  <- maybeAddress.fold(Try(withName))(address => withName.set(VendorAddressPage, address))
          finalAnswers <- withAddress.set(VendorOverviewVendorIdPage, vendorId)
        } yield finalAnswers

      case None =>
        Try(throw new IllegalStateException(s"Vendor ${vendor.vendorID} is missing a vendorID"))
    }
  }
}