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

import models.CompanyDetails
import models.purchaser.{CreateCompanyDetailsRequest, UpdateCompanyDetailsRequest}

class PurchaserRequestService {

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
