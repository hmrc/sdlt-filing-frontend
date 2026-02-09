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
import models.CompanyDetails
import models.purchaser.{CreateCompanyDetailsRequest, UpdateCompanyDetailsRequest}
import org.scalatest.matchers.should.Matchers.shouldBe

class PurchaserRequestServiceSpec extends SpecBase{

  private val testStornId = "STORN123456"
  private val returnResourceRef = "RRF-2024-001"
  private val purchaserResourceRef = "PUR-REF-001"
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
