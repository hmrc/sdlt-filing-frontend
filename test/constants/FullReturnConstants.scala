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
    isReturnUser = Some("YES"),
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
    IRMarkGenerated = Some("YES"),
    landCertForEachProp = Some("false"),
    returnResourceRef = Some("RRF-2024-001"),
    declaration = Some("YES"),
    status = Some("SUBMITTED")
  )

  val completePurchaser1: Purchaser = Purchaser(
    purchaserID = Some("PUR001"),
    returnID = Some("RET123456789"),
    isCompany = Some("no"),
    isTrustee = Some("no"),
    isConnectedToVendor = Some("no"),
    isRepresentedByAgent = Some("yes"),
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
    lMigrated = Some("no"),
    createDate = Some("2024-10-01"),
    lastUpdateDate = Some("2024-10-15 11:00:00"),
    isUkCompany = Some("no"),
    hasNino = Some("yes"),
    dateOfBirth = Some("15/05/1985"),
    registrationNumber = None,
    placeOfRegistration = None
  )

  val completePurchaser2: Purchaser = Purchaser(
    purchaserID = Some("PUR002"),
    returnID = Some("RET123456789"),
    isCompany = Some("no"),
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
    lastUpdateDate = Some("2024-10-15 11:00:00"),
    isUkCompany = Some("no"),
    hasNino = Some("yes"),
    dateOfBirth = Some("22/08/1987"),
    registrationNumber = None,
    placeOfRegistration = None
  )

  val completePurchaser3: Purchaser = Purchaser(
    purchaserID = Some("PUR002"),
    returnID = Some("RET123456789"),
    isCompany = Some("yes"),
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
    lastUpdateDate = Some("2024-10-15 11:00:00"),
    isUkCompany = Some("no"),
    hasNino = Some("yes"),
    dateOfBirth = Some("22/08/1987"),
    registrationNumber = None,
    placeOfRegistration = None
  )

  val completeCompanyDetails: CompanyDetails = CompanyDetails(
    companyDetailsID = Some("CD001"),
    returnID = Some("RET123456789"),
    purchaserID = Some("PUR001"),
    UTR = Some("1234567890"),
    VATReference = Some("GB123456789"),
    companyTypeBank = Some("no"),
    companyTypeBuilder = Some("no"),
    companyTypeBuildsoc = Some("no"),
    companyTypeCentgov = Some("no"),
    companyTypeIndividual = Some("yes"),
    companyTypeInsurance = Some("no"),
    companyTypeLocalauth = Some("no"),
    companyTypeOthercharity = Some("no"),
    companyTypeOthercompany = Some("no"),
    companyTypeOtherfinancial = Some("no"),
    companyTypePartnership = Some("no"),
    companyTypeProperty = Some("no"),
    companyTypePubliccorp = Some("no"),
    companyTypeSoletrader = Some("no"),
    companyTypePensionfund = Some("no")
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
    isRepresentedByAgent = Some("yes"),
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
    isRepresentedByAgent = Some("no"),
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
    isRepresentedByAgent = Some("yes"),
    vendorResourceRef = Some("VEN-REF-003"),
    nextVendorID = None
  )

  val completeVendorWithNextId: Vendor = Vendor(
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
    isRepresentedByAgent = Some("yes"),
    vendorResourceRef = Some("VEN-REF-001"),
    nextVendorID = Some("VEN002")
  )

  val completeLand: Land = Land(
    landID = Some("LND001"),
    returnID = Some("RET123456789"),
    propertyType = Some("01"),
    interestCreatedTransferred = Some("FGS"),
    houseNumber = Some("123"),
    address1 = Some("Baker Street"),
    address2 = Some("Marylebone"),
    address3 = Some("London"),
    address4 = None,
    postcode = Some("NW1 6XE"),
    landArea = Some("250.5"),
    areaUnit = Some("SquareMetres"),
    localAuthorityNumber = Some("5900"),
    mineralRights = Some("no"),
    NLPGUPRN = Some("10012345678"),
    willSendPlanByPost = Some("no"),
    titleNumber = Some("TGL123456"),
    landResourceRef = Some("LND-REF-001"),
    nextLandID = None,
    DARPostcode = Some("NW1 6XE")
  )

  val completeLandAdditional: Land = Land(
    landID = Some("LND001"),
    returnID = Some("RET123456789"),
    propertyType = Some("04"),
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
    mineralRights = Some("no"),
    NLPGUPRN = Some("10012345678"),
    willSendPlanByPost = Some("no"),
    titleNumber = Some("TGL123456"),
    landResourceRef = Some("LND-REF-001"),
    nextLandID = None,
    DARPostcode = Some("NW1 6XE")
  )

  val completeLandNonResidential: Land = Land(
    landID = Some("LND001"),
    returnID = Some("RET123456789"),
    propertyType = Some("02"),
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
    mineralRights = Some("no"),
    NLPGUPRN = Some("10012345678"),
    willSendPlanByPost = Some("no"),
    titleNumber = Some("TGL123456"),
    landResourceRef = Some("LND-REF-001"),
    nextLandID = None,
    DARPostcode = Some("NW1 6XE")
  )

  val completeTransaction: Transaction = Transaction(
    transactionID = Some("TXN001"),
    returnID = Some("RET123456789"),
    claimingRelief = Some("no"),
    reliefAmount = None,
    reliefReason = None,
    reliefSchemeNumber = None,
    isLinked = Some("no"),
    totalConsiderationLinked = None,
    totalConsideration = Some("500000.00"),
    considerationBuild = Some("yes"),
    considerationCash = Some("no"),
    considerationContingent = Some("no"),
    considerationDebt = Some("no"),
    considerationEmploy = Some("no"),
    considerationOther = Some("yes"),
    considerationLand = Some("no"),
    considerationServices = Some("no"),
    considerationSharesQTD = Some("no"),
    considerationSharesUNQTD = Some("no"),
    considerationVAT = None,
    includesChattel = Some("no"),
    includesGoodwill = Some("no"),
    includesOther = Some("no"),
    includesStock = Some("no"),
    usedAsFactory = Some("no"),
    usedAsHotel = Some("no"),
    usedAsIndustrial = Some("yes"),
    usedAsOffice = Some("no"),
    usedAsOther = Some("no"),
    usedAsShop = Some("no"),
    usedAsWarehouse = Some("no"),
    contractDate = Some("15/09/2024"),
    isDependantOnFutureEvent = Some("no"),
    transactionDescription = Some("L"),
    newTransactionDescription = None,
    effectiveDate = Some("01/10/2024"),
    isLandExchanged = Some("no"),
    exchangedLandHouseNumber = None,
    exchangedLandAddress1 = None,
    exchangedLandAddress2 = None,
    exchangedLandAddress3 = None,
    exchangedLandAddress4 = None,
    exchangedLandPostcode = None,
    agreedToDeferPayment = Some("no"),
    postTransRulingApplied = Some("no"),
    isPursuantToPreviousOption = Some("no"),
    restrictionsAffectInterest = Some("no"),
    restrictionDetails = None,
    postTransRulingFollowed = Some("no"),
    isPartOfSaleOfBusiness = Some("no"),
    totalConsiderationBusiness = Some("1000.00")
  )

  val completeReturnAgent: ReturnAgent = ReturnAgent(
    returnAgentID = Some("RA001"),
    returnID = Some("RET123456789"),
    agentType = Some("PURCHASER"),
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
    isAuthorised = Some("yes")
  )

  val completeReturnAgentVendor: ReturnAgent = completeReturnAgent.copy(
    agentType = Some("VENDOR"),
    name = Some("Smith & Partners LLP"),
    address1 = Some("Fleet Street")
  )

  val completeAgent: Seq[Agent] = Seq(Agent(
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
  ))

  val completeLease: Lease = Lease(
    leaseID = Some("LSE001"),
    returnID = Some("RET123456789"),
    isAnnualRentOver1000 = Some("yes"),
    breakClauseType = Some("TENANT"),
    breakClauseDate = Some("2029-10-01"),
    leaseContReservedRent = Some("24000.00"),
    contractEndDate = Some("30/09/2034"),
    contractStartDate = Some("01/10/2024"),
    firstReviewDate = Some("2029-10-01"),
    leaseType = Some("R"),
    marketRent = Some("24000.00"),
    netPresentValue = Some("95000.00"),
    optionToRenew = Some("yes"),
    totalPremiumPayable = Some("50000.00"),
    rentChargeDate = Some("01/10/2024"),
    rentFreePeriod = Some("3 months"),
    reviewClauseType = Some("RPI"),
    rentReviewFrequency = Some("YEARLY"),
    serviceCharge = Some("2000.00"),
    serviceChargeFrequency = Some("ANNUAL"),
    startingRent = Some("24000.00"),
    startingRentEndDate = Some("30/09/2025"),
    laterRentKnown = Some("yes"),
    termsSurrendered = Some("no"),
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
    unasertainableRent = Some("no"),
    VATAmount = Some("10000.00")
  )

  val incompleteLease: Lease = Lease(
    leaseID = Some("LSE001"),
    returnID = Some("RET123456789"),
  )

  val completeTaxCalculation: TaxCalculation = TaxCalculation(
    taxCalculationID = Some("TC001"),
    returnID = Some("RET123456789"),
    amountPaid = Some("15000.00"),
    includesPenalty = Some("yes"),
    taxDue = Some("15000.00"),
    taxDuePremium = Some("2000.00"),
    taxDueNPV = Some("1897.00"),
    calcPenaltyDue = Some("0.00"),
    calcTaxDue = Some("15000.00"),
    calcTaxRate1 = Some("3.0"),
    calcTaxRate2 = Some("5.0"),
    calcTotalTaxPenaltyDue = Some("15000.00"),
    calcTotalNPVTax = None,
    calcTotalPremiumTax = Some("15000.00"),
    honestyDeclaration = Some("yes")
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
    submittedDate = Some("2024-10-15T10:30:00Z"),
    email = Some("john.smith@email.com"),
    submissionRequestDate = Some("2024-10-15T10:15:00Z"),
    irmarkSent = Some("IRMARK-SENT-001")
  )

  val submissionNoStatus: Submission = Submission(
    submissionID = Some("SUB001"),
    returnID = Some("RET123456789"),
    storn = Some("STORN123456"),
    submissionStatus = None,
    govtalkMessageClass = Some("HMRC-STAMP-SDLT"),
    UTRN = Some("UTRN123456789012"),
    irmarkReceived = None,
    submissionReceipt = None,
    govtalkErrorCode = None,
    govtalkErrorType = None,
    govtalkErrorMessage = None,
    numPolls = Some("3"),
    createDate = Some("2024-10-15T10:30:00"),
    lastUpdateDate = Some("2024-10-15T11:00:00"),
    acceptedDate = None,
    submittedDate = None,
    email = None,
    submissionRequestDate = None,
    irmarkSent = None
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
    isNonUkResidents = Some("yes"),
    isCloseCompany = Some("no"),
    isCrownRelief = Some("no")
  )

  val completeFullReturn: FullReturn = FullReturn(
    stornId = "STORN123456",
    returnResourceRef = "RRF-2024-001",
    sdltOrganisation = Some(completeSdltOrganisation),
    returnInfo = Some(completeReturnInfo),
    purchaser = Some(Seq(completePurchaser1, completePurchaser2)),
    companyDetails = Some(completeCompanyDetails),
    vendor = Some(Seq(completeVendor)),
    land = Some(Seq(completeLand)),
    transaction = Some(completeTransaction),
    returnAgent = Some(Seq(completeReturnAgent, completeReturnAgentVendor)),
    agent = Some(completeAgent),
    lease = Some(completeLease),
    taxCalculation = Some(completeTaxCalculation),
    submission = Some(completeSubmission),
    submissionErrorDetails = Some(completeSubmissionErrorDetails),
    residency = Some(completeResidency)
  )

  val completeFullReturnMultipleVendors: FullReturn = FullReturn(
    stornId = "STORN123456",
    returnResourceRef = "RRF-2024-001",
    sdltOrganisation = Some(completeSdltOrganisation),
    returnInfo = Some(completeReturnInfo),
    purchaser = Some(Seq(completePurchaser1, completePurchaser2)),
    companyDetails = Some(completeCompanyDetails),
    vendor = Some(Seq(completeVendor, completeVendor2, completeVendor3)),
    land = Some(Seq(completeLand)),
    transaction = Some(completeTransaction),
    returnAgent = Some(Seq(completeReturnAgent, completeReturnAgentVendor)),
    agent = Some(completeAgent),
    lease = Some(completeLease),
    taxCalculation = Some(completeTaxCalculation),
    submission = Some(completeSubmission),
    submissionErrorDetails = Some(completeSubmissionErrorDetails),
    residency = Some(completeResidency)
  )

  val incompleteFullReturn: FullReturn = FullReturn(
    stornId = "STORN123456",
    returnResourceRef = "RRF-2024-001",
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
  val emptyFullReturn: FullReturn = FullReturn(
    stornId = "STORN123456",
    returnResourceRef = "RRF-2024-001"
  )

  val minimalFullReturn: FullReturn = FullReturn(
    stornId = "STORN123456",
    returnResourceRef = "RRF-2024-001",
    sdltOrganisation = Some(completeSdltOrganisation),
    returnInfo = Some(completeReturnInfo)
  )
}
