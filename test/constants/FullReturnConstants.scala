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

package constants

import models.*

object FullReturnConstants {

  val completeSdltOrganisation: SdltOrganisation = SdltOrganisation(
    isReturnUser = Some("true"),
    doNotDisplayWelcomePage = Some("false"),
    storn = Some("STORN123456"),
    version = Some("1.0")
  )

  val completeReturnInfo: ReturnInfo = ReturnInfo(
    returnID = Some("RET123456789"),
    storn = Some("STORN123456"),
    purchaserCounter = Some("2"),
    vendorCounter = Some("1"),
    landCounter = Some("1"),
    purgeDate = Some("2026-12-31"),
    version = Some("1.0"),
    mainPurchaserID = Some("PUR001"),
    mainVendorID = Some("VEN001"),
    mainLandID = Some("LND001"),
    IRMarkGenerated = Some("true"),
    landCertForEachProp = Some("false"),
    returnResourceRef = Some("RRF-2024-001"),
    declaration = Some("true"),
    status = Some("SUBMITTED")
  )

  val completePurchaser1: Purchaser = Purchaser(
    purchaserID = Some("PUR001"),
    returnID = Some("RET123456789"),
    isCompany = Some("false"),
    isTrustee = Some("false"),
    isConnectedToVendor = Some("false"),
    isRepresentedByAgent = Some("true"),
    title = Some("Mr"),
    surname = Some("Smith"),
    forename1 = Some("John"),
    forename2 = Some("David"),
    companyName = None,
    houseNumber = Some("42"),
    address1 = Some("High Street"),
    address2 = Some("Kensington"),
    address3 = Some("London"),
    address4 = None,
    postcode = Some("SW1A 1AA"),
    phone = Some("020 7946 0958"),
    nino = Some("AB123456C"),
    purchaserResourceRef = Some("PUR-REF-001"),
    nextPurchaserID = Some("PUR002"),
    lMigrated = Some("N"),
    createDate = Some("2024-10-01"),
    lastUpdateDate = Some("2024-10-15"),
    isUkCompany = Some("false"),
    hasNino = Some("true"),
    dateOfBirth = Some("1985-05-15"),
    registrationNumber = None,
    placeOfRegistration = None
  )

  val completePurchaser2: Purchaser = Purchaser(
    purchaserID = Some("PUR002"),
    returnID = Some("RET123456789"),
    isCompany = Some("false"),
    isTrustee = Some("false"),
    isConnectedToVendor = Some("false"),
    isRepresentedByAgent = Some("true"),
    title = Some("Mrs"),
    surname = Some("Smith"),
    forename1 = Some("Sarah"),
    forename2 = Some("Jane"),
    companyName = None,
    houseNumber = Some("42"),
    address1 = Some("High Street"),
    address2 = Some("Kensington"),
    address3 = Some("London"),
    address4 = None,
    postcode = Some("SW1A 1AA"),
    phone = Some("020 7946 0959"),
    nino = Some("CD987654Z"),
    purchaserResourceRef = Some("PUR-REF-002"),
    nextPurchaserID = None,
    lMigrated = Some("N"),
    createDate = Some("2024-10-01"),
    lastUpdateDate = Some("2024-10-15"),
    isUkCompany = Some("false"),
    hasNino = Some("true"),
    dateOfBirth = Some("1987-08-22"),
    registrationNumber = None,
    placeOfRegistration = None
  )

  val completeCompanyDetails: CompanyDetails = CompanyDetails(
    companyDetailsID = Some("CD001"),
    returnID = Some("RET123456789"),
    purchaserID = Some("PUR001"),
    UTR = Some("1234567890"),
    VATReference = Some("GB123456789"),
    companyTypeBank = Some("false"),
    companyTypeBuilder = Some("false"),
    companyTypeBuildsoc = Some("false"),
    companyTypeCentgov = Some("false"),
    companyTypeIndividual = Some("true"),
    companyTypeInsurance = Some("false"),
    companyTypeLocalauth = Some("false"),
    companyTypeOthercharity = Some("false"),
    companyTypeOthercompany = Some("false"),
    companyTypeOtherfinancial = Some("false"),
    companyTypePartnership = Some("false"),
    companyTypeProperty = Some("false"),
    companyTypePubliccorp = Some("false"),
    companyTypeSoletrader = Some("false"),
    companyTypePensionfund = Some("false")
  )

  val completeVendor: Vendor = Vendor(
    vendorID = Some("VEN001"),
    returnID = Some("RET123456789"),
    title = Some("Mrs"),
    forename1 = Some("Jane"),
    forename2 = Some("Elizabeth"),
    name = Some("Johnson"),
    houseNumber = Some("15"),
    address1 = Some("Park Lane"),
    address2 = Some("Mayfair"),
    address3 = Some("London"),
    address4 = None,
    postcode = Some("W1K 1LB"),
    isRepresentedByAgent = Some("true"),
    vendorResourceRef = Some("VEN-REF-001"),
    nextVendorID = None
  )

  val completeVendor2: Vendor = Vendor(
    vendorID = Some("VEN002"),
    returnID = Some("RET123456780"),
    title = Some("Mr"),
    forename1 = Some("John"),
    forename2 = Some("Randall"),
    name = Some("Tarley"),
    houseNumber = Some("11"),
    address1 = Some("Park Road"),
    address2 = Some("Paddington"),
    address3 = Some("London"),
    address4 = None,
    postcode = Some("W12 1BL"),
    isRepresentedByAgent = Some("false"),
    vendorResourceRef = Some("VEN-REF-002"),
    nextVendorID = None
  )

  val completeVendor3: Vendor = Vendor(
    vendorID = Some("VEN003"),
    returnID = Some("RET123456711"),
    title = Some("Mrs"),
    forename1 = Some("Elizabeth"),
    forename2 = Some("Jane"),
    name = Some("Thompson"),
    houseNumber = Some("9"),
    address1 = Some("Forest Lane"),
    address2 = Some("Southend"),
    address3 = Some("Essex"),
    address4 = None,
    postcode = Some("SS1 1LB"),
    isRepresentedByAgent = Some("true"),
    vendorResourceRef = Some("VEN-REF-003"),
    nextVendorID = None
  )

  val completeLand: Land = Land(
    landID = Some("LND001"),
    returnID = Some("RET123456789"),
    propertyType = Some("RESIDENTIAL"),
    interestCreatedTransferred = Some("FREEHOLD"),
    houseNumber = Some("123"),
    address1 = Some("Baker Street"),
    address2 = Some("Marylebone"),
    address3 = Some("London"),
    address4 = None,
    postcode = Some("NW1 6XE"),
    landArea = Some("250.5"),
    areaUnit = Some("SQMETRE"),
    localAuthorityNumber = Some("5900"),
    mineralRights = Some("false"),
    NLPGUPRN = Some("10012345678"),
    willSendPlanByPost = Some("false"),
    titleNumber = Some("TGL123456"),
    landResourceRef = Some("LND-REF-001"),
    nextLandID = None,
    DARPostcode = Some("NW1 6XE")
  )

  val completeTransaction: Transaction = Transaction(
    transactionID = Some("TXN001"),
    returnID = Some("RET123456789"),
    claimingRelief = Some("false"),
    reliefAmount = None,
    reliefReason = None,
    reliefSchemeNumber = None,
    isLinked = Some("false"),
    totalConsiderationLinked = None,
    totalConsideration = Some(BigDecimal("500000.00")),
    considerationBuild = None,
    considerationCash = Some(BigDecimal("500000.00")),
    considerationContingent = None,
    considerationDebt = None,
    considerationEmploy = None,
    considerationOther = None,
    considerationLand = None,
    considerationServices = None,
    considerationSharesQTD = None,
    considerationSharesUNQTD = None,
    considerationVAT = None,
    includesChattel = Some("false"),
    includesGoodwill = Some("false"),
    includesOther = Some("false"),
    includesStock = Some("false"),
    usedAsFactory = Some("false"),
    usedAsHotel = Some("false"),
    usedAsIndustrial = Some("false"),
    usedAsOffice = Some("false"),
    usedAsOther = Some("false"),
    usedAsShop = Some("false"),
    usedAsWarehouse = Some("false"),
    contractDate = Some("2024-09-15"),
    isDependantOnFutureEvent = Some("false"),
    transactionDescription = Some("CONVEYANCE"),
    newTransactionDescription = None,
    effectiveDate = Some("2024-10-01"),
    isLandExchanged = Some("false"),
    exchangedLandHouseNumber = None,
    exchangedLandAddress1 = None,
    exchangedLandAddress2 = None,
    exchangedLandAddress3 = None,
    exchangedLandAddress4 = None,
    exchangedLandPostcode = None,
    agreedToDeferPayment = Some("false"),
    postTransRulingApplied = Some("false"),
    isPursuantToPreviousOption = Some("false"),
    restrictionsAffectInterest = Some("false"),
    restrictionDetails = None,
    postTransRulingFollowed = Some("false"),
    isPartOfSaleOfBusiness = Some("false"),
    totalConsiderationBusiness = None
  )

  val completeReturnAgent: ReturnAgent = ReturnAgent(
    returnAgentID = Some("RA001"),
    returnID = Some("RET123456789"),
    agentType = Some("SOLICITOR"),
    name = Some("Smith & Partners LLP"),
    houseNumber = Some("100"),
    address1 = Some("Fleet Street"),
    address2 = Some("City of London"),
    address3 = Some("London"),
    address4 = None,
    postcode = Some("EC4A 2DQ"),
    phone = Some("020 7123 4567"),
    email = Some("info@smithpartners.co.uk"),
    DXAddress = Some("DX 123 London"),
    reference = Some("SP/2024/001"),
    isAuthorised = Some("true")
  )

  val completeAgent: Agent = Agent(
    agentId = Some("AGT001"),
    storn = Some("STORN123456"),
    name = Some("Smith & Partners LLP"),
    houseNumber = Some("100"),
    address1 = Some("Fleet Street"),
    address2 = Some("City of London"),
    address3 = Some("London"),
    address4 = None,
    postcode = Some("EC4A 2DQ"),
    phone = Some("020 7123 4567"),
    email = Some("info@smithpartners.co.uk"),
    dxAddress = Some("DX 123 London"),
    agentResourceReference = Some("AGT-REF-001")
  )

  val completeLease: Lease = Lease(
    leaseID = Some("LSE001"),
    returnID = Some("RET123456789"),
    isAnnualRentOver1000 = Some("true"),
    breakClauseType = Some("TENANT"),
    breakClauseDate = Some("2029-10-01"),
    leaseContReservedRent = Some("24000.00"),
    contractEndDate = Some("2034-09-30"),
    contractStartDate = Some("2024-10-01"),
    firstReviewDate = Some("2029-10-01"),
    leaseType = Some("NEW"),
    marketRent = Some("24000.00"),
    netPresentValue = Some("95000.00"),
    optionToRenew = Some("true"),
    totalPremiumPayable = Some("50000.00"),
    rentChargeDate = Some("2024-10-01"),
    rentFreePeriod = Some("3 months"),
    reviewClauseType = Some("RPI"),
    rentReviewFrequency = Some("YEARLY"),
    serviceCharge = Some("2000.00"),
    serviceChargeFrequency = Some("ANNUAL"),
    startingRent = Some("24000.00"),
    startingRentEndDate = Some("2025-09-30"),
    laterRentKnown = Some("true"),
    termsSurrendered = Some("false"),
    considToLndlrdBuild = None,
    considToLndlrdContin = None,
    considToLndlrdDebt = None,
    considToLndlrdEmploy = None,
    considToLndlrdOther = None,
    considToLndlrdLand = None,
    considToLndlrdServices = None,
    considToLndlrdSharedQTD = None,
    considToLndlrdSharedUNQTD = None,
    considToTenantBuild = None,
    considToTenantContin = None,
    considToTenantEmploy = None,
    considToTenantOther = None,
    considToTenantLand = None,
    considToTenantServices = None,
    considToTenantSharesQTD = None,
    considToTenantSharesUNQTD = None,
    turnoverRent = None,
    unasertainableRent = Some("false"),
    VATAmount = Some("10000.00")
  )

  val completeTaxCalculation: TaxCalculation = TaxCalculation(
    taxCalculationID = Some("TC001"),
    returnID = Some("RET123456789"),
    amountPaid = Some("15000.00"),
    includesPenalty = Some("false"),
    taxDue = Some("15000.00"),
    taxDuePremium = Some("15000.00"),
    taxDueNPV = None,
    calcPenaltyDue = Some("0.00"),
    calcTaxDue = Some("15000.00"),
    calcTaxRate1 = Some("3.0"),
    calcTaxRate2 = Some("5.0"),
    calcTotalTaxPenaltyDue = Some("15000.00"),
    calcTotalNPVTax = None,
    calcTotalPremiumTax = Some("15000.00"),
    honestyDeclaration = Some("true")
  )

  val completeSubmission: Submission = Submission(
    submissionID = Some("SUB001"),
    returnID = Some("RET123456789"),
    storn = Some("STORN123456"),
    submissionStatus = Some("ACCEPTED"),
    govtalkMessageClass = Some("HMRC-STAMP-SDLT"),
    UTRN = Some("UTRN123456789012"),
    irmarkReceived = Some("IRMARK-RCV-001"),
    submissionReceipt = Some("RECEIPT-001"),
    govtalkErrorCode = None,
    govtalkErrorType = None,
    govtalkErrorMessage = None,
    numPolls = Some("3"),
    createDate = Some("2024-10-15T10:30:00"),
    lastUpdateDate = Some("2024-10-15T11:00:00"),
    acceptedDate = Some("2024-10-15T11:00:00"),
    submittedDate = Some("2024-10-15T10:30:00"),
    email = Some("john.smith@email.com"),
    submissionRequestDate = Some("2024-10-15T10:15:00"),
    irmarkSent = Some("IRMARK-SENT-001")
  )

  val completeSubmissionErrorDetails: SubmissionErrorDetails = SubmissionErrorDetails(
    errorDetailID = None,
    returnID = Some("RET123456789"),
    position = None,
    errorMessage = None,
    storn = Some("STORN123456"),
    submissionID = Some("SUB001")
  )

  val completeResidency: Residency = Residency(
    residencyID = Some("RES001"),
    isNonUkResidents = Some("false"),
    isCloseCompany = Some("false"),
    isCrownRelief = Some("false")
  )

  val completeFullReturn: FullReturn = FullReturn(
    stornId = Some("STORN123456"),
    returnResourceRef = Some("RRF-2024-001"),
    sdltOrganisation = Some(completeSdltOrganisation),
    returnInfo = Some(completeReturnInfo),
    purchaser = Some(Seq(completePurchaser1, completePurchaser2)),
    companyDetails = Some(completeCompanyDetails),
    vendor = Some(Seq(completeVendor)),
    land = Some(Seq(completeLand)),
    transaction = Some(completeTransaction),
    returnAgent = Some(Seq(completeReturnAgent)),
    agent = Some(completeAgent),
    lease = Some(completeLease),
    taxCalculation = Some(completeTaxCalculation),
    submission = Some(completeSubmission),
    submissionErrorDetails = Some(completeSubmissionErrorDetails),
    residency = Some(completeResidency)
  )

  val completeFullReturnMultipleVendors: FullReturn = FullReturn(
    stornId = Some("STORN123456"),
    returnResourceRef = Some("RRF-2024-001"),
    sdltOrganisation = Some(completeSdltOrganisation),
    returnInfo = Some(completeReturnInfo),
    purchaser = Some(Seq(completePurchaser1, completePurchaser2)),
    companyDetails = Some(completeCompanyDetails),
    vendor = Some(Seq(completeVendor, completeVendor2, completeVendor3)),
    land = Some(Seq(completeLand)),
    transaction = Some(completeTransaction),
    returnAgent = Some(Seq(completeReturnAgent)),
    agent = Some(completeAgent),
    lease = Some(completeLease),
    taxCalculation = Some(completeTaxCalculation),
    submission = Some(completeSubmission),
    submissionErrorDetails = Some(completeSubmissionErrorDetails),
    residency = Some(completeResidency)
  )

  // Minimal/Incomplete version for testing
  val incompleteFullReturn: FullReturn = FullReturn(
    stornId = Some("STORN123456"),
    returnResourceRef = None,
    sdltOrganisation = None,
    returnInfo = None,
    purchaser = None,
    companyDetails = None,
    vendor = None,
    land = None,
    transaction = None,
    returnAgent = None,
    agent = None,
    lease = None,
    taxCalculation = None,
    submission = None,
    submissionErrorDetails = None,
    residency = None
  )

  // Empty version for testing
  val emptyFullReturn: FullReturn = FullReturn()

  // Version with just basic info
  val minimalFullReturn: FullReturn = FullReturn(
    stornId = Some("STORN123456"),
    returnResourceRef = Some("RRF-2024-001"),
    sdltOrganisation = Some(completeSdltOrganisation),
    returnInfo = Some(completeReturnInfo)
  )
}
