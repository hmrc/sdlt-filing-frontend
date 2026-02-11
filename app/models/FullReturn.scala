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

package models

import models.vendor.VendorSessionQuestions
import models.purchaser.PurchaserSessionQuestions
import org.slf4j.Logger
import play.api.libs.json.{Json, OFormat}

import scala.concurrent.Future


case class SdltOrganisation(
                             isReturnUser: Option[String] = None,
                             doNotDisplayWelcomePage: Option[String] = None,
                             storn: Option[String] = None,
                             version: Option[String] = None
                           )

object SdltOrganisation {
  implicit val format: OFormat[SdltOrganisation] = Json.format[SdltOrganisation]
}

case class ReturnInfo(
                   returnID: Option[String] = None,
                   storn: Option[String] = None,
                   purchaserCounter: Option[String] = None,
                   vendorCounter: Option[String] = None,
                   landCounter: Option[String] = None,
                   purgeDate: Option[String] = None,
                   version: Option[String] = None,
                   mainPurchaserID: Option[String] = None,
                   mainVendorID: Option[String] = None,
                   mainLandID: Option[String] = None,
                   IRMarkGenerated: Option[String] = None,
                   landCertForEachProp: Option[String] = None,
                   returnResourceRef: Option[String] = None,
                   declaration: Option[String] = None,
                   status: Option[String] = None
                 )

object ReturnInfo {
  implicit val format: OFormat[ReturnInfo] = Json.format[ReturnInfo]
}

case class Purchaser(
                      purchaserID: Option[String] = None,
                      returnID: Option[String] = None,
                      isCompany: Option[String] = None,
                      isTrustee: Option[String] = None,
                      isConnectedToVendor: Option[String] = None,
                      isRepresentedByAgent: Option[String] = None,
                      title: Option[String] = None,
                      surname: Option[String] = None,
                      forename1: Option[String] = None,
                      forename2: Option[String] = None,
                      companyName: Option[String] = None,
                      houseNumber: Option[String] = None,
                      address1: Option[String] = None,
                      address2: Option[String] = None,
                      address3: Option[String] = None,
                      address4: Option[String] = None,
                      postcode: Option[String] = None,
                      phone: Option[String] = None,
                      nino: Option[String] = None,
                      purchaserResourceRef: Option[String] = None,
                      nextPurchaserID: Option[String] = None,
                      lMigrated: Option[String] = None, // Used in backend
                      createDate: Option[String] = None, // Used in backend
                      lastUpdateDate: Option[String] = None, // Used in backend
                      isUkCompany: Option[String] = None,
                      hasNino: Option[String] = None,
                      dateOfBirth: Option[String] = None,
                      registrationNumber: Option[String] = None,
                      placeOfRegistration: Option[String] = None
                    )

object Purchaser {
  implicit val format: OFormat[Purchaser] = Json.format[Purchaser]

  def from(userAnswers: Option[UserAnswers], logger: Logger): Future[Purchaser] = {
    val purchaserSessionQuestions: PurchaserSessionQuestions = userAnswers.get.data.as[PurchaserSessionQuestions]

    val existingPurchaser = for {
      answers <- userAnswers
      fullReturn <- answers.fullReturn
      purchasers <- fullReturn.purchaser
      purchaserId <- purchaserSessionQuestions.purchaserCurrent.purchaserAndCompanyId.map(_.purchaserID)
      existing <- purchasers.find(_.purchaserID.contains(purchaserId))
    } yield existing


    logger.info(s"[Purchaser][from] existing purchaser found: \n $existingPurchaser")


    Future.successful(
        Purchaser(
          purchaserID = purchaserSessionQuestions.purchaserCurrent.purchaserAndCompanyId.map(_.purchaserID),
          returnID = userAnswers.flatMap(_.returnId),
          isCompany = if (purchaserSessionQuestions.purchaserCurrent.whoIsMakingThePurchase == "Company") Some("YES") else Some("NO"),
          isTrustee =  purchaserSessionQuestions.purchaserCurrent.isPurchaserActingAsTrustee.map(_.toUpperCase),
          isConnectedToVendor = purchaserSessionQuestions.purchaserCurrent.purchaserAndVendorConnected.map(_.toUpperCase),
          isRepresentedByAgent = Some("NO"),
          title = None,
          surname = if (purchaserSessionQuestions.purchaserCurrent.whoIsMakingThePurchase == "Individual") {
            Some(purchaserSessionQuestions.purchaserCurrent.nameOfPurchaser.name)
          } else {
            None
          },
          forename1 = purchaserSessionQuestions.purchaserCurrent.nameOfPurchaser.forename1,
          forename2 = purchaserSessionQuestions.purchaserCurrent.nameOfPurchaser.forename2,
          companyName =  if (purchaserSessionQuestions.purchaserCurrent.whoIsMakingThePurchase == "Company") {
            Some(purchaserSessionQuestions.purchaserCurrent.nameOfPurchaser.name)
          } else {
            None
          },
          houseNumber  = purchaserSessionQuestions.purchaserCurrent.purchaserAddress.houseNumber,
          address1 = purchaserSessionQuestions.purchaserCurrent.purchaserAddress.line1,
          address2 = purchaserSessionQuestions.purchaserCurrent.purchaserAddress.line2,
          address3 = purchaserSessionQuestions.purchaserCurrent.purchaserAddress.line3,
          address4 = purchaserSessionQuestions.purchaserCurrent.purchaserAddress.line4,
          postcode  = purchaserSessionQuestions.purchaserCurrent.purchaserAddress.postcode,
          phone  = purchaserSessionQuestions.purchaserCurrent.enterPurchaserPhoneNumber,
          nino = purchaserSessionQuestions.purchaserCurrent.nationalInsuranceNumber,
          purchaserResourceRef  = existingPurchaser.flatMap(_.purchaserResourceRef),
          nextPurchaserID = existingPurchaser.flatMap(_.nextPurchaserID),
          lMigrated  = existingPurchaser.flatMap(_.lMigrated),
          createDate  = existingPurchaser.flatMap(_.createDate),
          lastUpdateDate = existingPurchaser.flatMap(_.lastUpdateDate),
          isUkCompany = existingPurchaser.flatMap(_.isUkCompany),
          hasNino  = purchaserSessionQuestions.purchaserCurrent.doesPurchaserHaveNI.map(_.toString.toLowerCase),
          dateOfBirth  = purchaserSessionQuestions.purchaserCurrent.purchaserDateOfBirth.map(_.toString),
          registrationNumber = if (purchaserSessionQuestions.purchaserCurrent.whoIsMakingThePurchase == "Individual") {
            purchaserSessionQuestions.purchaserCurrent.purchaserFormOfIdIndividual.map(_.idNumberOrReference)
          } else {
            purchaserSessionQuestions.purchaserCurrent.purchaserFormOfIdCompany.map(_.referenceId)
          },
          placeOfRegistration = if (purchaserSessionQuestions.purchaserCurrent.whoIsMakingThePurchase == "Individual") {
            purchaserSessionQuestions.purchaserCurrent.purchaserFormOfIdIndividual.map(_.countryIssued)
          } else {
            purchaserSessionQuestions.purchaserCurrent.purchaserFormOfIdCompany.map(_.countryIssued)
          }
    ) )
  }
}

case class CompanyDetails(
                           companyDetailsID: Option[String] = None,
                           returnID: Option[String] = None,
                           purchaserID: Option[String] = None,
                           UTR: Option[String] = None,
                           VATReference: Option[String] = None,
                           companyTypeBank: Option[String] = None,
                           companyTypeBuilder: Option[String] = None,
                           companyTypeBuildsoc: Option[String] = None,
                           companyTypeCentgov: Option[String] = None,
                           companyTypeIndividual: Option[String] = None,
                           companyTypeInsurance: Option[String] = None,
                           companyTypeLocalauth: Option[String] = None,
                           companyTypeOthercharity: Option[String] = None,
                           companyTypeOthercompany: Option[String] = None,
                           companyTypeOtherfinancial: Option[String] = None,
                           companyTypePartnership: Option[String] = None,
                           companyTypeProperty: Option[String] = None,
                           companyTypePubliccorp: Option[String] = None,
                           companyTypeSoletrader: Option[String] = None,
                           companyTypePensionfund: Option[String] = None
                         )

object CompanyDetails {
  implicit val format: OFormat[CompanyDetails] = Json.format[CompanyDetails]

  def from(userAnswers: UserAnswers): Future[CompanyDetails] = {
    val companyDetailsSessionQuestions: PurchaserSessionQuestions = userAnswers.data.as[PurchaserSessionQuestions]
    Future.successful(
      CompanyDetails(
        companyDetailsID = companyDetailsSessionQuestions.purchaserCurrent.purchaserAndCompanyId.flatMap(_.companyDetailsID),
        returnID = userAnswers.returnId,
        purchaserID = companyDetailsSessionQuestions.purchaserCurrent.purchaserAndCompanyId.map(_.purchaserID),
        UTR  = companyDetailsSessionQuestions.purchaserCurrent.purchaserUTRPage,
        VATReference  = companyDetailsSessionQuestions.purchaserCurrent.registrationNumber,
        companyTypeBank = companyDetailsSessionQuestions.purchaserCurrent.purchaserTypeOfCompany.map(_.bank),
        companyTypeBuilder = companyDetailsSessionQuestions.purchaserCurrent.purchaserTypeOfCompany.map(_.unincorporatedBuilder),
        companyTypeBuildsoc = companyDetailsSessionQuestions.purchaserCurrent.purchaserTypeOfCompany.map(_.buildingAssociation),
        companyTypeCentgov = companyDetailsSessionQuestions.purchaserCurrent.purchaserTypeOfCompany.map(_.centralGovernment),
        companyTypeIndividual = companyDetailsSessionQuestions.purchaserCurrent.purchaserTypeOfCompany.map(_.individualOther),
        companyTypeInsurance = companyDetailsSessionQuestions.purchaserCurrent.purchaserTypeOfCompany.map(_.insuranceAssurance),
        companyTypeLocalauth = companyDetailsSessionQuestions.purchaserCurrent.purchaserTypeOfCompany.map(_.localAuthority),
        companyTypeOthercharity  = companyDetailsSessionQuestions.purchaserCurrent.purchaserTypeOfCompany.map(_.otherIncludingCharity),
        companyTypeOthercompany = companyDetailsSessionQuestions.purchaserCurrent.purchaserTypeOfCompany.map(_.otherCompany),
        companyTypeOtherfinancial = companyDetailsSessionQuestions.purchaserCurrent.purchaserTypeOfCompany.map(_.otherFinancialInstitute),
        companyTypePartnership  = companyDetailsSessionQuestions.purchaserCurrent.purchaserTypeOfCompany.map(_.partnership),
        companyTypeProperty = companyDetailsSessionQuestions.purchaserCurrent.purchaserTypeOfCompany.map(_.propertyCompany),
        companyTypePubliccorp = companyDetailsSessionQuestions.purchaserCurrent.purchaserTypeOfCompany.map(_.publicCorporation),
        companyTypeSoletrader = companyDetailsSessionQuestions.purchaserCurrent.purchaserTypeOfCompany.map(_.unincorporatedSoleTrader),
        companyTypePensionfund = companyDetailsSessionQuestions.purchaserCurrent.purchaserTypeOfCompany.map(_.superannuationOrPensionFund)
    ))
  }
}

case class Vendor(
                   vendorID: Option[String] = None,
                   returnID: Option[String] = None,
                   title: Option[String] = None,
                   forename1: Option[String] = None,
                   forename2: Option[String] = None,
                   name: Option[String] = None,
                   houseNumber: Option[String] = None,
                   address1: Option[String] = None,
                   address2: Option[String] = None,
                   address3: Option[String] = None,
                   address4: Option[String] = None,
                   postcode: Option[String] = None,
                   isRepresentedByAgent: Option[String] = None,
                   vendorResourceRef: Option[String] = None,
                   nextVendorID: Option[String] = None
                 )


object Vendor {
  implicit val format: OFormat[Vendor] = Json.format[Vendor]

  def from(userAnswers: Option[UserAnswers]): Future[Vendor] = {
    val vendorSessionQuestions: VendorSessionQuestions = userAnswers.get.data.as[VendorSessionQuestions]
    Future.successful(Vendor(
      vendorID = vendorSessionQuestions.vendorCurrent.vendorID,
      forename1 = vendorSessionQuestions.vendorCurrent.vendorOrCompanyName.forename1,
      forename2 = vendorSessionQuestions.vendorCurrent.vendorOrCompanyName.forename2,
      name = Some(vendorSessionQuestions.vendorCurrent.vendorOrCompanyName.name),
      houseNumber = vendorSessionQuestions.vendorCurrent.vendorAddress.houseNumber,
      address1 = vendorSessionQuestions.vendorCurrent.vendorAddress.line1,
      address2 = vendorSessionQuestions.vendorCurrent.vendorAddress.line2,
      address3 = vendorSessionQuestions.vendorCurrent.vendorAddress.line3,
      address4 = vendorSessionQuestions.vendorCurrent.vendorAddress.line4,
      postcode = vendorSessionQuestions.vendorCurrent.vendorAddress.postcode,
      isRepresentedByAgent = Some("NO")
    )
    )
  }
  
}

case class Land(
                 landID: Option[String] = None,
                 returnID: Option[String] = None, // Used in backend
                 propertyType: Option[String] = None,
                 interestCreatedTransferred: Option[String] = None,
                 houseNumber: Option[String] = None,
                 address1: Option[String] = None,
                 address2: Option[String] = None,
                 address3: Option[String] = None,
                 address4: Option[String] = None,
                 postcode: Option[String] = None,
                 landArea: Option[String] = None,
                 areaUnit: Option[String] = None,
                 localAuthorityNumber: Option[String] = None,
                 mineralRights: Option[String] = None,
                 NLPGUPRN: Option[String] = None,
                 willSendPlanByPost: Option[String] = None,
                 titleNumber: Option[String] = None,
                 landResourceRef: Option[String] = None,
                 nextLandID: Option[String] = None,
                 DARPostcode: Option[String] = None
               )

object Land {
  implicit val format: OFormat[Land] = Json.format[Land]
}

case class Transaction(
                        transactionID: Option[String] = None,
                        returnID: Option[String] = None,
                        claimingRelief: Option[String] = None,
                        reliefAmount: Option[BigDecimal] = None,
                        reliefReason: Option[String] = None,
                        reliefSchemeNumber: Option[String] = None,
                        isLinked: Option[String] = None,
                        totalConsiderationLinked: Option[BigDecimal] = None,
                        totalConsideration: Option[BigDecimal] = None,
                        considerationBuild: Option[BigDecimal] = None,
                        considerationCash: Option[BigDecimal] = None,
                        considerationContingent: Option[BigDecimal] = None,
                        considerationDebt: Option[BigDecimal] = None,
                        considerationEmploy: Option[BigDecimal] = None,
                        considerationOther: Option[BigDecimal] = None,
                        considerationLand: Option[BigDecimal] = None,
                        considerationServices: Option[BigDecimal] = None,
                        considerationSharesQTD: Option[BigDecimal] = None,
                        considerationSharesUNQTD: Option[BigDecimal] = None,
                        considerationVAT: Option[BigDecimal] = None,
                        includesChattel: Option[String] = None,
                        includesGoodwill: Option[String] = None,
                        includesOther: Option[String] = None,
                        includesStock: Option[String] = None,
                        usedAsFactory: Option[String] = None,
                        usedAsHotel: Option[String] = None,
                        usedAsIndustrial: Option[String] = None,
                        usedAsOffice: Option[String] = None,
                        usedAsOther: Option[String] = None,
                        usedAsShop: Option[String] = None,
                        usedAsWarehouse: Option[String] = None,
                        contractDate: Option[String] = None,
                        isDependantOnFutureEvent: Option[String] = None,
                        transactionDescription: Option[String] = None,
                        newTransactionDescription: Option[String] = None,
                        effectiveDate: Option[String] = None,
                        isLandExchanged: Option[String] = None,
                        exchangedLandHouseNumber: Option[String] = None,
                        exchangedLandAddress1: Option[String] = None,
                        exchangedLandAddress2: Option[String] = None,
                        exchangedLandAddress3: Option[String] = None,
                        exchangedLandAddress4: Option[String] = None,
                        exchangedLandPostcode: Option[String] = None,
                        agreedToDeferPayment: Option[String] = None,
                        postTransRulingApplied: Option[String] = None,
                        isPursuantToPreviousOption: Option[String] = None,
                        restrictionsAffectInterest: Option[String] = None,
                        restrictionDetails: Option[String] = None,
                        postTransRulingFollowed: Option[String] = None,
                        isPartOfSaleOfBusiness: Option[String] = None,
                        totalConsiderationBusiness: Option[BigDecimal] = None
                      )

object Transaction {
  implicit val format: OFormat[Transaction] = Json.format[Transaction]
}

case class ReturnAgent(
                        returnAgentID: Option[String] = None,
                        returnID: Option[String] = None,
                        agentType: Option[String] = None,
                        name: Option[String] = None,
                        houseNumber: Option[String] = None,
                        address1: Option[String] = None,
                        address2: Option[String] = None,
                        address3: Option[String] = None,
                        address4: Option[String] = None,
                        postcode: Option[String] = None,
                        phone: Option[String] = None,
                        email: Option[String] = None,
                        DXAddress: Option[String] = None,
                        reference: Option[String] = None,
                        isAuthorised: Option[String] = None
                      )

object ReturnAgent {
  implicit val format: OFormat[ReturnAgent] = Json.format[ReturnAgent]
}

case class Agent(
                  agentId: Option[String] = None,
                  storn: Option[String] = None,
                  name: Option[String] = None,
                  houseNumber: Option[String] = None,
                  address1: Option[String] = None,
                  address2: Option[String] = None,
                  address3: Option[String] = None,
                  address4: Option[String] = None,
                  postcode: Option[String] = None,
                  phone: Option[String] = None,
                  email: Option[String] = None,
                  dxAddress: Option[String] = None,
                  agentResourceReference: Option[String] = None
                )

object Agent {
  implicit val format: OFormat[Agent] = Json.format[Agent]
}

case class Lease(
                  leaseID: Option[String] = None,
                  returnID: Option[String] = None,
                  isAnnualRentOver1000: Option[String] = None,
                  breakClauseType: Option[String] = None,
                  breakClauseDate: Option[String] = None,
                  leaseContReservedRent: Option[String] = None,
                  contractEndDate: Option[String] = None,
                  contractStartDate: Option[String] = None,
                  firstReviewDate: Option[String] = None,
                  leaseType: Option[String] = None,
                  marketRent: Option[String] = None,
                  netPresentValue: Option[String] = None,
                  optionToRenew: Option[String] = None,
                  totalPremiumPayable: Option[String] = None,
                  rentChargeDate: Option[String] = None,
                  rentFreePeriod: Option[String] = None,
                  reviewClauseType: Option[String] = None,
                  rentReviewFrequency: Option[String] = None,
                  serviceCharge: Option[String] = None,
                  serviceChargeFrequency: Option[String] = None,
                  startingRent: Option[String] = None,
                  startingRentEndDate: Option[String] = None,
                  laterRentKnown: Option[String] = None,
                  termsSurrendered: Option[String] = None,
                  considToLndlrdBuild: Option[String] = None,
                  considToLndlrdContin: Option[String] = None,
                  considToLndlrdDebt: Option[String] = None,
                  considToLndlrdEmploy: Option[String] = None,
                  considToLndlrdOther: Option[String] = None,
                  considToLndlrdLand: Option[String] = None,
                  considToLndlrdServices: Option[String] = None,
                  considToLndlrdSharedQTD: Option[String] = None,
                  considToLndlrdSharedUNQTD: Option[String] = None,
                  considToTenantBuild: Option[String] = None,
                  considToTenantContin: Option[String] = None,
                  considToTenantEmploy: Option[String] = None,
                  considToTenantOther: Option[String] = None,
                  considToTenantLand: Option[String] = None,
                  considToTenantServices: Option[String] = None,
                  considToTenantSharesQTD: Option[String] = None,
                  considToTenantSharesUNQTD: Option[String] = None,
                  turnoverRent: Option[String] = None,
                  unasertainableRent: Option[String] = None,
                  VATAmount: Option[String] = None
                )

object Lease {
  implicit val format: OFormat[Lease] = Json.format[Lease]
}

case class TaxCalculation(
                           taxCalculationID: Option[String] = None,
                           returnID: Option[String] = None, // Used in backend
                           amountPaid: Option[String] = None,
                           includesPenalty: Option[String] = None,
                           taxDue: Option[String] = None,
                           taxDuePremium: Option[String] = None,
                           taxDueNPV: Option[String] = None,
                           calcPenaltyDue: Option[String] = None,
                           calcTaxDue: Option[String] = None,
                           calcTaxRate1: Option[String] = None,
                           calcTaxRate2: Option[String] = None,
                           calcTotalTaxPenaltyDue: Option[String] = None,
                           calcTotalNPVTax: Option[String] = None,
                           calcTotalPremiumTax: Option[String] = None,
                           honestyDeclaration: Option[String] = None
                         )

object TaxCalculation {
  implicit val format: OFormat[TaxCalculation] = Json.format[TaxCalculation]
}

case class Submission(
                       submissionID: Option[String] = None,
                       returnID: Option[String] = None,
                       storn: Option[String] = None,
                       submissionStatus: Option[String] = None, // Used in backend
                       govtalkMessageClass: Option[String] = None, // Used in backend
                       UTRN: Option[String] = None,
                       irmarkReceived: Option[String] = None,
                       submissionReceipt: Option[String] = None, // Used in backend
                       govtalkErrorCode: Option[String] = None,
                       govtalkErrorType: Option[String] = None,
                       govtalkErrorMessage: Option[String] = None,
                       numPolls: Option[String] = None, // Used in backend
                       createDate: Option[String] = None,
                       lastUpdateDate: Option[String] = None,
                       acceptedDate: Option[String] = None,
                       submittedDate: Option[String] = None, // Used in backend
                       email: Option[String] = None,
                       submissionRequestDate: Option[String] = None,
                       irmarkSent: Option[String] = None
                     )

object Submission {
  implicit val format: OFormat[Submission] = Json.format[Submission]
}

case class SubmissionErrorDetails(
                                   errorDetailID: Option[String] = None,
                                   returnID: Option[String] = None,
                                   position: Option[String] = None,
                                   errorMessage: Option[String] = None,
                                   storn: Option[String] = None,
                                   submissionID: Option[String] = None // Used in backend
                                 )

object SubmissionErrorDetails {
  implicit val format: OFormat[SubmissionErrorDetails] = Json.format[SubmissionErrorDetails]
}

case class Residency(
                      residencyID: Option[String] = None,
                      isNonUkResidents: Option[String] = None,
                      isCloseCompany: Option[String] = None,
                      isCrownRelief: Option[String] = None
                    )

object Residency {
  implicit val format: OFormat[Residency] = Json.format[Residency]
}


case class FullReturn (
                  stornId: String,
                  returnResourceRef: String,
                  sdltOrganisation: Option[SdltOrganisation] = None,
                  returnInfo: Option[ReturnInfo] = None,
                  purchaser: Option[Seq[Purchaser]] = None,
                  companyDetails: Option[CompanyDetails] = None,
                  vendor: Option[Seq[Vendor]] = None,
                  land: Option[Seq[Land]] = None,
                  transaction: Option[Transaction] = None,
                  returnAgent: Option[Seq[ReturnAgent]] = None,
                  agent: Option[Seq[Agent]] = None,
                  lease: Option[Lease] = None,
                  taxCalculation: Option[TaxCalculation] = None,
                  submission: Option[Submission] = None,
                  submissionErrorDetails: Option[SubmissionErrorDetails] = None,
                  residency: Option[Residency] = None
                )

object FullReturn {
  implicit val format: OFormat[FullReturn] = Json.format[FullReturn]
}

