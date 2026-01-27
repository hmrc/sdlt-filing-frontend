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

package services.purchaser

import models.{CompanyDetails, Purchaser}
import models.purchaser.{CreateCompanyDetailsRequest, CreatePurchaserRequest, UpdateCompanyDetailsRequest, UpdatePurchaserRequest}

class PurchaserRequestService {

  def convertToCreatePurchaserRequest(purchaser: Purchaser,
                                      stornId: String,
                                      returnResourceRef: String): CreatePurchaserRequest = {
    CreatePurchaserRequest(
      stornId = stornId,
      returnResourceRef = returnResourceRef,
      isCompany = purchaser.isCompany.getOrElse(""),
      isTrustee = purchaser.isTrustee.getOrElse(""),
      isConnectedToVendor = purchaser.isConnectedToVendor.getOrElse(""),
      isRepresentedByAgent = purchaser.isRepresentedByAgent.getOrElse(""),
      title = purchaser.title,
      surname = purchaser.surname,
      forename1 = purchaser.forename1,
      forename2 = purchaser.forename2,
      companyName = purchaser.companyName,
      houseNumber = purchaser.houseNumber,
      address1 = purchaser.address1.getOrElse(""),
      address2 = purchaser.address2,
      address3 = purchaser.address3,
      address4 = purchaser.address4,
      postcode = purchaser.postcode,
      phone = purchaser.phone,
      nino = purchaser.nino,
      isUkCompany = purchaser.isUkCompany,
      hasNino = purchaser.hasNino,
      dateOfBirth = purchaser.dateOfBirth,
      registrationNumber = purchaser.registrationNumber,
      placeOfRegistration = purchaser.placeOfRegistration
    )
  }

  def convertToUpdatePurchaserRequest(purchaser: Purchaser,
                                      stornId: String,
                                      returnResourceRef: String,
                                      purchaserResourceRef: String,
                                      nextPurchaserId: Option[String]): UpdatePurchaserRequest = {
    UpdatePurchaserRequest(
      stornId = stornId,
      returnResourceRef = returnResourceRef,
      purchaserResourceRef = purchaserResourceRef,
      isCompany = purchaser.isCompany,
      isTrustee = purchaser.isTrustee,
      isConnectedToVendor = purchaser.isConnectedToVendor,
      isRepresentedByAgent = purchaser.isRepresentedByAgent,
      title = purchaser.title,
      surname = purchaser.surname,
      forename1 = purchaser.forename1,
      forename2 = purchaser.forename2,
      companyName = purchaser.companyName,
      houseNumber = purchaser.houseNumber,
      address1 = purchaser.address1,
      address2 = purchaser.address2,
      address3 = purchaser.address3,
      address4 = purchaser.address4,
      postcode = purchaser.postcode,
      phone = purchaser.phone,
      nino = purchaser.nino,
      nextPurchaserId = purchaser.nextPurchaserID,
      isUkCompany = purchaser.isUkCompany,
      hasNino = purchaser.hasNino,
      dateOfBirth = purchaser.dateOfBirth,
      registrationNumber = purchaser.registrationNumber,
      placeOfRegistration = purchaser.placeOfRegistration)
  }

  def convertToCreateCompanyDetailsRequest(companyDetails: CompanyDetails,
                                           stornId: String,
                                           returnResourceRef: String,
                                           purchaserResourceRef: String): CreateCompanyDetailsRequest = {
    CreateCompanyDetailsRequest(
      stornId = stornId,
      returnResourceRef = returnResourceRef,
      purchaserResourceRef = purchaserResourceRef,
      utr = companyDetails.UTR,
      vatReference = companyDetails.VATReference,
      compTypeBank = companyDetails.companyTypeBank,
      compTypeBuilder = companyDetails.companyTypeBuilder,
      compTypeBuildsoc = companyDetails.companyTypeBuildsoc,
      compTypeCentgov = companyDetails.companyTypeCentgov,
      compTypeIndividual = companyDetails.companyTypeIndividual,
      compTypeInsurance = companyDetails.companyTypeInsurance,
      compTypeLocalauth = companyDetails.companyTypeLocalauth,
      compTypeOcharity = companyDetails.companyTypeOthercharity,
      compTypeOcompany = companyDetails.companyTypeOthercompany,
      compTypeOfinancial = companyDetails.companyTypeOtherfinancial,
      compTypePartship = companyDetails.companyTypePartnership,
      compTypeProperty = companyDetails.companyTypeProperty,
      compTypePubliccorp = companyDetails.companyTypePubliccorp,
      compTypeSoletrader = companyDetails.companyTypeSoletrader,
      compTypePenfund = companyDetails.companyTypePensionfund
    )
    
  }

  def convertToUpdateCompanyDetailsRequest(companyDetails: CompanyDetails,
                                           stornId: String,
                                           returnResourceRef: String,
                                           purchaserResourceRef: String): UpdateCompanyDetailsRequest = {
    UpdateCompanyDetailsRequest(
      stornId = stornId,
      returnResourceRef = returnResourceRef,
      purchaserResourceRef = purchaserResourceRef,
      utr = companyDetails.UTR,
      vatReference = companyDetails.VATReference,
      compTypeBank = companyDetails.companyTypeBank,
      compTypeBuilder = companyDetails.companyTypeBuilder,
      compTypeBuildsoc = companyDetails.companyTypeBuildsoc,
      compTypeCentgov = companyDetails.companyTypeCentgov,
      compTypeIndividual = companyDetails.companyTypeIndividual,
      compTypeInsurance = companyDetails.companyTypeInsurance,
      compTypeLocalauth = companyDetails.companyTypeLocalauth,
      compTypeOcharity = companyDetails.companyTypeOthercharity,
      compTypeOcompany = companyDetails.companyTypeOthercompany,
      compTypeOfinancial = companyDetails.companyTypeOtherfinancial,
      compTypePartship = companyDetails.companyTypePartnership,
      compTypeProperty = companyDetails.companyTypeProperty,
      compTypePubliccorp = companyDetails.companyTypePubliccorp,
      compTypeSoletrader = companyDetails.companyTypeSoletrader,
      compTypePenfund = companyDetails.companyTypePensionfund
    )
  }
}
