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

import base.SpecBase
import models.{CompanyDetails, Purchaser}
import models.purchaser.{CreateCompanyDetailsRequest, CreatePurchaserRequest, UpdateCompanyDetailsRequest, UpdatePurchaserRequest}
import org.scalatest.matchers.should.Matchers.shouldBe

class PurchaserRequestServiceSpec extends SpecBase{

  private val testStornId = "STORN123456"
  private val returnResourceRef = "RRF-2024-001"
  private val purchaserResourceRef = "PUR-REF-001"
  private val validPurchaserCompanyObject = Purchaser(
    purchaserID = Some("PUR123"),
    returnID = Some("12345"),
    isCompany = Some("YES"),
    isTrustee = Some("yes"),
    isConnectedToVendor = Some("yes"),
    isRepresentedByAgent = Some("yes"),
    title = None,
    surname = Some("Company"),
    forename1 = None,
    forename2 = None,
    companyName = Some("Company"),
    houseNumber = None,
    address1 = Some("Street 1"),
    address2 = Some("Street 2"),
    address3 = Some("Street 3"),
    address4 = Some("Street 4"),
    postcode = Some("CR7 8LU"),
    phone = Some("+447874363636"),
    nino = None,
    purchaserResourceRef = None,
    nextPurchaserID = None,
    lMigrated = None,
    createDate = None,
    lastUpdateDate = None,
    isUkCompany = None,
    hasNino = None,
    dateOfBirth = None,
    registrationNumber = Some("VAT123"),
    placeOfRegistration = None
  )
  private val validCompanyDetails = CompanyDetails(
    companyDetailsID = Some("COMPDET001"),
    returnID = Some("12345"),
    purchaserID = Some("PUR123"),
    UTR = Some("UTR1234"),
    VATReference = Some("VAT123"),
    companyTypeBank = Some("YES"),
    companyTypeBuilder = Some("NO"),
    companyTypeBuildsoc = Some("NO"),
    companyTypeCentgov = Some("NO"),
    companyTypeIndividual = Some("NO"),
    companyTypeInsurance = Some("NO"),
    companyTypeLocalauth = Some("NO"),
    companyTypeOthercharity = Some("NO"),
    companyTypeOthercompany = Some("NO"),
    companyTypeOtherfinancial = Some("NO"),
    companyTypePartnership = Some("NO"),
    companyTypeProperty = Some("NO"),
    companyTypePubliccorp = Some("NO"),
    companyTypeSoletrader = Some("NO"),
    companyTypePensionfund = Some("NO")
  )

  val service = new PurchaserRequestService()

  "PurchaserRequestService" - {
    ".convertToCreatePurchaserRequest" in {
      val expectedCreatePurchaserRequest = CreatePurchaserRequest(
        testStornId,
        returnResourceRef,
        isCompany = "YES",
        isTrustee = "yes",
        isConnectedToVendor = "yes",
        isRepresentedByAgent = "yes",
        title = None,
        surname = Some("Company"),
        forename1 = None,
        forename2 = None,
        companyName = Some("Company"),
        houseNumber = None,
        address1 = "Street 1",
        address2 = Some("Street 2"),
        address3 = Some("Street 3"),
        address4 = Some("Street 4"),
        postcode = Some("CR7 8LU"),
        phone = Some("+447874363636"),
        nino = None,
        isUkCompany = None,
        hasNino = None,
        dateOfBirth = None,
        registrationNumber = Some("VAT123"),
        placeOfRegistration = None
      )
      val result = service.convertToCreatePurchaserRequest(validPurchaserCompanyObject, testStornId, returnResourceRef)

      result shouldBe expectedCreatePurchaserRequest
    }

    ".convertToUpdatePurchaserRequest" in {
      val expectedUpdatePurchaserRequest = UpdatePurchaserRequest(
        testStornId,
        returnResourceRef,
        purchaserResourceRef = "PUR-REF-001",
        isCompany = Some("YES"),
        isTrustee =  Some("yes"),
        isConnectedToVendor =  Some("yes"),
        isRepresentedByAgent =  Some("yes"),
        title = None,
        surname = Some("Company"),
        forename1 = None,
        forename2 = None,
        companyName = Some("Company"),
        houseNumber = None,
        address1 =  Some("Street 1"),
        address2 = Some("Street 2"),
        address3 = Some("Street 3"),
        address4 = Some("Street 4"),
        postcode = Some("CR7 8LU"),
        phone = Some("+447874363636"),
        nino = None,
        isUkCompany = None,
        hasNino = None,
        dateOfBirth = None,
        registrationNumber = Some("VAT123"),
        placeOfRegistration = None
      )

      val result = service.convertToUpdatePurchaserRequest(
        validPurchaserCompanyObject,
        testStornId,
        returnResourceRef,
        purchaserResourceRef,
        None
      )

      result shouldBe expectedUpdatePurchaserRequest
    }

    ".convertToCreateCompanyDetailsRequest" in {
      val expectedCreateCompanyDetailsRequest = CreateCompanyDetailsRequest(
        testStornId,
        returnResourceRef,
        purchaserResourceRef,
        utr = Some("UTR1234"),
        vatReference = Some("VAT123"),
        compTypeBank = Some("YES"),
        compTypeBuilder = Some("NO"),
        compTypeBuildsoc = Some("NO"),
        compTypeCentgov = Some("NO"),
        compTypeIndividual = Some("NO"),
        compTypeInsurance = Some("NO"),
        compTypeLocalauth = Some("NO"),
        compTypeOcharity = Some("NO"),
        compTypeOcompany = Some("NO"),
        compTypeOfinancial = Some("NO"),
        compTypePartship = Some("NO"),
        compTypeProperty = Some("NO"),
        compTypePubliccorp = Some("NO"),
        compTypeSoletrader = Some("NO"),
        compTypePenfund = Some("NO")
      )

      val result = service.convertToCreateCompanyDetailsRequest(
        validCompanyDetails,
        testStornId,
        returnResourceRef,
        purchaserResourceRef
      )

      result shouldBe expectedCreateCompanyDetailsRequest
    }

    ".convertToUpdateCompanyDetailsRequest" - {
      val expectedUpdateCompanyDetailsRequest = UpdateCompanyDetailsRequest(
        testStornId,
        returnResourceRef,
        purchaserResourceRef,
        utr = Some("UTR1234"),
        vatReference = Some("VAT123"),
        compTypeBank = Some("YES"),
        compTypeBuilder = Some("NO"),
        compTypeBuildsoc = Some("NO"),
        compTypeCentgov = Some("NO"),
        compTypeIndividual = Some("NO"),
        compTypeInsurance = Some("NO"),
        compTypeLocalauth = Some("NO"),
        compTypeOcharity = Some("NO"),
        compTypeOcompany = Some("NO"),
        compTypeOfinancial = Some("NO"),
        compTypePartship = Some("NO"),
        compTypeProperty = Some("NO"),
        compTypePubliccorp = Some("NO"),
        compTypeSoletrader = Some("NO"),
        compTypePenfund = Some("NO")
      )

      val result = service.convertToUpdateCompanyDetailsRequest(
        validCompanyDetails,
        testStornId,
        returnResourceRef,
        purchaserResourceRef
      )

      result shouldBe expectedUpdateCompanyDetailsRequest
    }
  }

}
