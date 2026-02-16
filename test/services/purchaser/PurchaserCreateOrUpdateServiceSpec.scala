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
import connectors.StampDutyLandTaxConnector
import constants.FullReturnConstants
import models.purchaser.*
import models.{CompanyDetails, FullReturn, Purchaser, ReturnInfo, ReturnVersionUpdateReturn, UserAnswers, Vendor}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatestplus.mockito.*
import play.api.libs.json.{JsNull, JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}

import java.time.{Instant, LocalDate}
import scala.concurrent.{ExecutionContext, Future}

class PurchaserCreateOrUpdateServiceSpec extends SpecBase with MockitoSugar {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit val request: FakeRequest[_] = FakeRequest()

  val testReturnId = "test-return-id"
  val testStorn = "test-storn"
  val testPurchaserId = "PUR002"
  val testIndividualId = "PUR123"
  val testmainPurchaserID = "PUR001"
  val testPurchaserCompanyDetailsId = "COMPDET001"
  val testPurchaserResourceRef = "purchaser-ref-123"
  val testNextPurchaserId = "next-purchaser-456"
  val testCreatePurchaserReturn = CreatePurchaserReturn(
    purchaserResourceRef = testPurchaserResourceRef,
    purchaserId = testPurchaserId
  )
  val testCreateCompanyDetailsReturn = CreateCompanyDetailsReturn(
    companyDetailsId = testPurchaserCompanyDetailsId
  )
  val testUpdateCompanyDetailsReturn = UpdateCompanyDetailsReturn(updated = true)
  val testUpdatePurchaserReturn = UpdatePurchaserReturn(updated = true)

  private val userAnswersMainPurchaserCompany = UserAnswers(
    id = "test-session-id",
    storn = "test-storn",
    returnId = Some("12345"),
    fullReturn = None,
    data = Json.obj(
      "purchaserCurrent" -> Json.obj(
        "purchaserAndCompanyId" -> Json.obj(
          "purchaserID" -> "PUR001",
          "companyDetailsID" -> "COMPDET001",
        ),
        "ConfirmNameOfThePurchaser" -> "yes",
        "whoIsMakingThePurchase" -> "Company",
        "nameOfPurchaser" -> Json.obj(
          "forename1" -> JsNull,
          "forename2" -> JsNull,
          "name" -> "Company",
        ),
        "purchaserAddress" -> Json.obj(
          "houseNumber" -> JsNull,
          "line1" -> "Street 1",
          "line2" -> "Street 2",
          "line3" -> "Street 3",
          "line4" -> "Street 4",
          "line5" -> "Street 5",
          "postcode" -> "CR7 8LU",
          "country" -> Json.obj(
            "code" -> "GB",
            "name" -> "UK"
          ),
          "addressValidated" -> true
        ),
        "addPurchaserPhoneNumber" -> true,
        "enterPurchaserPhoneNumber" -> "+447874363636",
        "doesPurchaserHaveNI" -> JsNull,
        "nationalInsuranceNumber" -> JsNull,
        "purchaserFormOfIdIndividual" -> JsNull,
        "purchaserDateOfBirth" -> JsNull,
        "purchaserConfirmIdentity" -> JsNull,
        "registrationNumber" -> "VAT123",
        "purchaserUTRPage" -> "UTR1234",
        "purchaserFormOfIdCompany" -> JsNull,
        "purchaserTypeOfCompany" -> Json.obj(
          "bank" -> "YES",
          "buildingAssociation" -> "NO",
          "centralGovernment" -> "NO",
          "individualOther" -> "NO",
          "insuranceAssurance" -> "NO",
          "localAuthority" -> "NO",
          "partnership" -> "NO",
          "propertyCompany" -> "NO",
          "publicCorporation" -> "NO",
          "otherCompany" -> "NO",
          "otherFinancialInstitute" -> "NO",
          "otherIncludingCharity" -> "NO",
          "superannuationOrPensionFund" -> "NO",
          "unincorporatedBuilder" -> "NO",
          "unincorporatedSoleTrader" -> "NO"
        ),
        "isPurchaserActingAsTrustee" -> "yes",
        "purchaserAndVendorConnected" -> "yes",
      )),
    lastUpdated = Instant.now)

  private val userAnswersPurchaserCompany = UserAnswers(
    id = "test-session-id",
    storn = "test-storn",
    returnId = Some("12345"),
    fullReturn = None,
    data = Json.obj(
      "purchaserCurrent" -> Json.obj(
        "purchaserAndCompanyId" -> Json.obj(
          "purchaserID" -> "PUR002",
          "companyDetailsID" -> "COMPDET001",
        ),
        "ConfirmNameOfThePurchaser" -> "yes",
        "whoIsMakingThePurchase" -> "Company",
        "nameOfPurchaser" -> Json.obj(
          "forename1" -> JsNull,
          "forename2" -> JsNull,
          "name" -> "Company",
        ),
        "purchaserAddress" -> Json.obj(
          "houseNumber" -> JsNull,
          "line1" -> "Street 1",
          "line2" -> "Street 2",
          "line3" -> "Street 3",
          "line4" -> "Street 4",
          "line5" -> "Street 5",
          "postcode" -> "CR7 8LU",
          "country" -> Json.obj(
            "code" -> "GB",
            "name" -> "UK"
          ),
          "addressValidated" -> true
        ),
        "addPurchaserPhoneNumber" -> true,
        "enterPurchaserPhoneNumber" -> "+447874363636",
        "doesPurchaserHaveNI" -> JsNull,
        "nationalInsuranceNumber" -> JsNull,
        "purchaserFormOfIdIndividual" -> JsNull,
        "purchaserDateOfBirth" -> JsNull,
        "purchaserConfirmIdentity" -> JsNull,
        "registrationNumber" -> "VAT123",
        "purchaserUTRPage" -> "UTR1234",
        "purchaserFormOfIdCompany" -> JsNull,
        "purchaserTypeOfCompany" -> Json.obj(
          "bank" -> "YES",
          "buildingAssociation" -> "NO",
          "centralGovernment" -> "NO",
          "individualOther" -> "NO",
          "insuranceAssurance" -> "NO",
          "localAuthority" -> "NO",
          "partnership" -> "NO",
          "propertyCompany" -> "NO",
          "publicCorporation" -> "NO",
          "otherCompany" -> "NO",
          "otherFinancialInstitute" -> "NO",
          "otherIncludingCharity" -> "NO",
          "superannuationOrPensionFund" -> "NO",
          "unincorporatedBuilder" -> "NO",
          "unincorporatedSoleTrader" -> "NO"
        ),
        "isPurchaserActingAsTrustee" -> "yes",
        "purchaserAndVendorConnected" -> "yes",
      )),
    lastUpdated = Instant.now)

  private val userAnswersMainPurchaserIndividual = UserAnswers(
    id = "test-session-id",
    storn = "test-storn",
    returnId = Some("12345"),
    fullReturn = None,
    data = Json.obj(
      "purchaserCurrent" -> Json.obj(
        "purchaserAndCompanyId" -> Json.obj(
          "purchaserID" -> "PUR001",
          "companyDetailsID" -> "COMPDET001",
        ),
        "ConfirmNameOfThePurchaser" -> "yes",
        "whoIsMakingThePurchase" -> "Individual",
        "nameOfPurchaser" -> Json.obj(
          "forename1" -> JsNull,
          "forename2" -> JsNull,
          "name" -> "Company",
        ),
        "purchaserAddress" -> Json.obj(
          "houseNumber" -> JsNull,
          "line1" -> "Street 1",
          "line2" -> "Street 2",
          "line3" -> "Street 3",
          "line4" -> "Street 4",
          "line5" -> "Street 5",
          "postcode" -> "CR7 8LU",
          "country" -> Json.obj(
            "code" -> "GB",
            "name" -> "UK"
          ),
          "addressValidated" -> true
        ),
        "addPurchaserPhoneNumber" -> true,
        "enterPurchaserPhoneNumber" -> "+447874363636",
        "doesPurchaserHaveNI" -> JsNull,
        "nationalInsuranceNumber" -> JsNull,
        "purchaserFormOfIdIndividual" -> JsNull,
        "purchaserDateOfBirth" -> JsNull,
        "purchaserConfirmIdentity" -> JsNull,
        "registrationNumber" -> "VAT123",
        "purchaserUTRPage" -> "UTR1234",
        "purchaserFormOfIdCompany" -> JsNull,
        "purchaserTypeOfCompany" -> Json.obj(
          "bank" -> "YES",
          "buildingAssociation" -> "NO",
          "centralGovernment" -> "NO",
          "individualOther" -> "NO",
          "insuranceAssurance" -> "NO",
          "localAuthority" -> "NO",
          "partnership" -> "NO",
          "propertyCompany" -> "NO",
          "publicCorporation" -> "NO",
          "otherCompany" -> "NO",
          "otherFinancialInstitute" -> "NO",
          "otherIncludingCharity" -> "NO",
          "superannuationOrPensionFund" -> "NO",
          "unincorporatedBuilder" -> "NO",
          "unincorporatedSoleTrader" -> "NO"
        ),
        "isPurchaserActingAsTrustee" -> "yes",
        "purchaserAndVendorConnected" -> "yes",
      )),
    lastUpdated = Instant.now)
  private val userAnswersPurchaserIndividual = UserAnswers(
    id = "test-session-id",
    storn = "test-storn",
    returnId = Some("12345"),
    fullReturn = None,
    data = Json.obj(
      "purchaserCurrent" -> Json.obj(
        "purchaserAndCompanyId" -> Json.obj(
          "purchaserID" -> "PUR002",
          "companyDetailsID" -> "COMPDET001",
        ),
        "ConfirmNameOfThePurchaser" -> "yes",
        "whoIsMakingThePurchase" -> "Individual",
        "nameOfPurchaser" -> Json.obj(
          "forename1" -> JsNull,
          "forename2" -> JsNull,
          "name" -> "Company",
        ),
        "purchaserAddress" -> Json.obj(
          "houseNumber" -> JsNull,
          "line1" -> "Street 1",
          "line2" -> "Street 2",
          "line3" -> "Street 3",
          "line4" -> "Street 4",
          "line5" -> "Street 5",
          "postcode" -> "CR7 8LU",
          "country" -> Json.obj(
            "code" -> "GB",
            "name" -> "UK"
          ),
          "addressValidated" -> true
        ),
        "addPurchaserPhoneNumber" -> true,
        "enterPurchaserPhoneNumber" -> "+447874363636",
        "doesPurchaserHaveNI" -> JsNull,
        "nationalInsuranceNumber" -> JsNull,
        "purchaserFormOfIdIndividual" -> JsNull,
        "purchaserDateOfBirth" -> JsNull,
        "purchaserConfirmIdentity" -> JsNull,
        "registrationNumber" -> "VAT123",
        "purchaserUTRPage" -> "UTR1234",
        "purchaserFormOfIdCompany" -> JsNull,
        "purchaserTypeOfCompany" -> Json.obj(
          "bank" -> "YES",
          "buildingAssociation" -> "NO",
          "centralGovernment" -> "NO",
          "individualOther" -> "NO",
          "insuranceAssurance" -> "NO",
          "localAuthority" -> "NO",
          "partnership" -> "NO",
          "propertyCompany" -> "NO",
          "publicCorporation" -> "NO",
          "otherCompany" -> "NO",
          "otherFinancialInstitute" -> "NO",
          "otherIncludingCharity" -> "NO",
          "superannuationOrPensionFund" -> "NO",
          "unincorporatedBuilder" -> "NO",
          "unincorporatedSoleTrader" -> "NO"
        ),
        "isPurchaserActingAsTrustee" -> "yes",
        "purchaserAndVendorConnected" -> "yes",
      )),
    lastUpdated = Instant.now)

  private def createMainPurchaserCompanyUserAnswers(
                                                     returnId: Option[String] = Some(testReturnId),
                                                     storn: String = testStorn,
                                                     fullReturn: Option[FullReturn] = None
                                                   ): UserAnswers = {
    userAnswersMainPurchaserCompany.copy(
      returnId = returnId,
      storn = storn,
      fullReturn = fullReturn
    )
  }

  private def createPurchaserCompanyUserAnswers(
                                                 returnId: Option[String] = Some(testReturnId),
                                                 storn: String = testStorn,
                                                 fullReturn: Option[FullReturn] = None
                                               ): UserAnswers = {
    userAnswersPurchaserCompany.copy(
      returnId = returnId,
      storn = storn,
      fullReturn = fullReturn
    )
  }

  private def createMainPurchaserIndividualUserAnswers(
                                                        returnId: Option[String] = Some(testReturnId),
                                                        storn: String = testStorn,
                                                        fullReturn: Option[FullReturn] = None
                                                      ): UserAnswers = {
    userAnswersMainPurchaserIndividual.copy(
      returnId = returnId,
      storn = storn,
      fullReturn = fullReturn
    )
  }

  private def createPurchaserIndividualUserAnswers(
                                                    returnId: Option[String] = Some(testReturnId),
                                                    storn: String = testStorn,
                                                    fullReturn: Option[FullReturn] = None
                                                  ): UserAnswers = {
    userAnswersPurchaserIndividual.copy(
      returnId = returnId,
      storn = storn,
      fullReturn = fullReturn,
    )
  }

//  private def createPurchaserJsonData(
//                                       purchaserId: String,
//                                       isCompany: String = "Individual"
//                                     ): JsObject = {
//    Json.obj(
//      "purchaserCurrent" -> Json.obj(
//        "purchaserAndCompanyId" -> Json.obj(
//          "purchaserID" -> purchaserId,
//          "companyDetailsID" -> "COMPDET001",
//        ),
//        "ConfirmNameOfThePurchaser" -> "yes",
//        "whoIsMakingThePurchase" -> isCompany,
//        "nameOfPurchaser" -> Json.obj(
//          "forename1" -> JsNull,
//          "forename2" -> JsNull,
//          "name" -> "Company",
//        ),
//        "purchaserAddress" -> Json.obj(
//          "houseNumber" -> JsNull,
//          "line1" -> "Street 1",
//          "line2" -> "Street 2",
//          "line3" -> "Street 3",
//          "line4" -> "Street 4",
//          "line5" -> "Street 5",
//          "postcode" -> "CR7 8LU",
//          "country" -> Json.obj(
//            "code" -> "GB",
//            "name" -> "UK"
//          ),
//          "addressValidated" -> true
//        ),
//        "addPurchaserPhoneNumber" -> true,
//        "enterPurchaserPhoneNumber" -> "+447874363636",
//        "doesPurchaserHaveNI" -> JsNull,
//        "nationalInsuranceNumber" -> JsNull,
//        "purchaserFormOfIdIndividual" -> JsNull,
//        "purchaserDateOfBirth" -> JsNull,
//        "purchaserConfirmIdentity" -> JsNull,
//        "registrationNumber" -> "VAT123",
//        "purchaserUTRPage" -> "UTR1234",
//        "purchaserFormOfIdCompany" -> JsNull,
//        "purchaserTypeOfCompany" -> Json.obj(
//          "bank" -> "YES",
//          "buildingAssociation" -> "NO",
//          "centralGovernment" -> "NO",
//          "individualOther" -> "NO",
//          "insuranceAssurance" -> "NO",
//          "localAuthority" -> "NO",
//          "partnership" -> "NO",
//          "propertyCompany" -> "NO",
//          "publicCorporation" -> "NO",
//          "otherCompany" -> "NO",
//          "otherFinancialInstitute" -> "NO",
//          "otherIncludingCharity" -> "NO",
//          "superannuationOrPensionFund" -> "NO",
//          "unincorporatedBuilder" -> "NO",
//          "unincorporatedSoleTrader" -> "NO"
//        ),
//        "isPurchaserActingAsTrustee" -> "yes",
//        "purchaserAndVendorConnected" -> "yes",
//      )
//    )
//  }

  private val purchaserName = NameOfPurchaser(forename1 = Some("Name1"), forename2 = Some("Name2"), name = "Samsung")

  private def createPurchaser(
                               purchaserID: Option[String],
                               purchaserResourceRef: Option[String],
                               nextPurchaserID: Option[String],
                               isCompany: Option[String]
                             ): Purchaser = {
    Purchaser(
      purchaserID = purchaserID,
      returnID = Some("12345"),
      isCompany = isCompany,
      isTrustee = Some("YES"),
      isConnectedToVendor = Some("YES"),
      isRepresentedByAgent = Some("NO"),
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
      purchaserResourceRef = purchaserResourceRef,
      nextPurchaserID = nextPurchaserID,
      lMigrated = None,
      createDate = None,
      lastUpdateDate = None,
      isUkCompany = None,
      hasNino = None,
      dateOfBirth = None,
      registrationNumber = Some("VAT123"),
      placeOfRegistration = None
    )
  }

  private def createCompanyDetails(purchaserID: String): CompanyDetails =
    CompanyDetails(
      companyDetailsID = Some("COMPDET001"),
      returnID = Some("12345"),
      purchaserID = Some(purchaserID),
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

  private def createSessionData(
                                 purchaserAndCompanyId: Option[PurchaserAndCompanyId] = None,
                                 isCompany: String
                               ): PurchaserSessionQuestions = {
    PurchaserSessionQuestions(
      PurchaserCurrent(
        purchaserAndCompanyId = purchaserAndCompanyId,
        ConfirmNameOfThePurchaser = Some(ConfirmNameOfThePurchaser.No),
        whoIsMakingThePurchase = if (isCompany == "Company") "YES" else "NO",
        nameOfPurchaser = purchaserName,
        purchaserAddress = PurchaserSessionAddress(
          houseNumber = Some("1"),
          line1 = Some("Street 1"),
          line2 = Some("Street 2"),
          line3 = Some("Street 3"),
          line4 = Some("Street 4"),
          line5 = Some("Street 5"),
          postcode = Some("CR7 8LU"),
          country = Some(PurchaserSessionCountry(
            code = Some("GB"),
            name = Some("UK")
          )),
          addressValidated = Some(true)),
        addPurchaserPhoneNumber = Some(true),
        enterPurchaserPhoneNumber = Some("+447874363636"),
        doesPurchaserHaveNI = Some(DoesPurchaserHaveNI.Yes),
        nationalInsuranceNumber = Some("AA123456A"),
        purchaserFormOfIdIndividual = Some(PurchaserFormOfIdIndividual(idNumberOrReference = "ref", countryIssued = "country")),
        purchaserDateOfBirth = Some(LocalDate.of(2000,2,2)),
        purchaserConfirmIdentity = Some(PurchaserConfirmIdentity.VatRegistrationNumber),
        registrationNumber = Some("VAT1234"),
        purchaserUTRPage = Some("UTR1234"),
        purchaserFormOfIdCompany  = Some(CompanyFormOfId(referenceId = "ID12345", countryIssued = "country")),
        purchaserTypeOfCompany  = Some(PurchaserTypeOfCompanyAnswers(bank = "YES",
          buildingAssociation = "YES",
          centralGovernment = "NO",
          individualOther = "NO",
          insuranceAssurance = "NO",
          localAuthority = "NO",
          partnership = "NO",
          propertyCompany = "NO",
          publicCorporation = "NO",
          otherCompany = "NO",
          otherFinancialInstitute = "NO",
          otherIncludingCharity = "NO",
          superannuationOrPensionFund = "NO",
          unincorporatedBuilder = "NO",
          unincorporatedSoleTrader = "NO")),
        isPurchaserActingAsTrustee = Some("yes"),
        purchaserAndVendorConnected = Some("yes")
      ))
  }

  private def createFullReturnInfo(mainPurchaserID: Option[String]): ReturnInfo =
    ReturnInfo(
      returnID = Some("RET123456789"),
      storn = Some("STORN123456"),
      purchaserCounter = Some("2"),
      vendorCounter = Some("1"),
      landCounter = Some("1"),
      purgeDate = Some("2026-12-31"),
      version = Some("1.0"),
      mainPurchaserID = mainPurchaserID,
      mainVendorID = Some("VEN001"),
      mainLandID = Some("LND001"),
      IRMarkGenerated = Some("true"),
      landCertForEachProp = Some("false"),
      returnResourceRef = Some("RRF-2024-001"),
      declaration = Some("true"),
      status = Some("SUBMITTED")
    )

  private def createFullReturn(
                                vendors: Seq[Vendor] = Seq.empty,
                                purchasers: Seq[Purchaser] = Seq.empty,
                                companyDetails: Option[CompanyDetails] = None,
                                returnInfo: Option[ReturnInfo] = None
                              ): FullReturn = {
    FullReturnConstants.completeFullReturn.copy(
      vendor = Some(vendors),
      purchaser = Some(purchasers),
      companyDetails = companyDetails,
      returnInfo = returnInfo
    )
  }

  "PurchaserCreateOrUpdateService" - {
    ".callCreatePurchaser" - {
      "when purchaser type is Individual" - {
        "must successfully create Purchaser" in {
          val mockBackendConnector = mock[StampDutyLandTaxConnector]
          val mockPurchaserRequestService = mock[PurchaserRequestService]
          val mockPurchaserService = mock[PurchaserService]
          val service = new PurchaserCreateOrUpdateService()

          val userAnswers = createMainPurchaserIndividualUserAnswers(
            fullReturn = Some(createFullReturn(
              returnInfo = Some(createFullReturnInfo(Some(testmainPurchaserID)))
            ))
          )
          val sessionData = createSessionData(purchaserAndCompanyId = None, isCompany = "Individual")

          when(mockBackendConnector.createPurchaser(any())(any(), any()))
            .thenReturn(Future.successful(testCreatePurchaserReturn))
          when(mockPurchaserService.createPurchaserName(any())).thenReturn(Some(purchaserName))

          val result = service.callCreatePurchaser(mockBackendConnector, mockPurchaserRequestService, mockPurchaserService, userAnswers, sessionData).futureValue

          status(Future.successful(result)) mustEqual SEE_OTHER
          redirectLocation(Future.successful(result)).value mustEqual
            controllers.purchaser.routes.PurchaserOverviewController.onPageLoad().url
          verify(mockBackendConnector, times(1)).createPurchaser(any())(any(), any())
          flash(Future.successful(result)).get("purchaserCreated") mustBe Some("Name1 Name2 Samsung")
        }
      }

      "when purchaser type is Company" - {
        "must successfully create Purchaser" in {
          val mockBackendConnector = mock[StampDutyLandTaxConnector]
          val mockPurchaserRequestService = mock[PurchaserRequestService]
          val mockPurchaserService = mock[PurchaserService]
          val service = new PurchaserCreateOrUpdateService()

          val userAnswers = createPurchaserCompanyUserAnswers(fullReturn = Some(createFullReturn(returnInfo = Some(createFullReturnInfo(Some("differentMain"))))))
          val sessionData = createSessionData(purchaserAndCompanyId = None, isCompany = "Company")

          when(mockBackendConnector.createPurchaser(any())(any(), any()))
            .thenReturn(Future.successful(testCreatePurchaserReturn))
          when(mockBackendConnector.createCompanyDetails(any())(any(), any()))
            .thenReturn(Future.successful(testCreateCompanyDetailsReturn))
          when(mockPurchaserService.createPurchaserName(any())).thenReturn(Some(purchaserName))

          val result = service.callCreatePurchaser(mockBackendConnector, mockPurchaserRequestService, mockPurchaserService, userAnswers, sessionData).futureValue

          status(Future.successful(result)) mustEqual SEE_OTHER
          redirectLocation(Future.successful(result)).value mustEqual
            controllers.purchaser.routes.PurchaserOverviewController.onPageLoad().url
          verify(mockBackendConnector, times(1)).createPurchaser(any())(any(), any())
        }

        "must successfully create Company Details if main purchaser" in {
          val mockBackendConnector = mock[StampDutyLandTaxConnector]
          val mockPurchaserRequestService = mock[PurchaserRequestService]
          val mockPurchaserService = mock[PurchaserService]
          val service = new PurchaserCreateOrUpdateService()

          val userAnswers = createMainPurchaserCompanyUserAnswers(fullReturn = Some(createFullReturn(returnInfo = None)))
          val sessionData = createSessionData(purchaserAndCompanyId = None, isCompany = "Company")

          when(mockBackendConnector.createPurchaser(any())(any(), any()))
            .thenReturn(Future.successful(testCreatePurchaserReturn))
          when(mockBackendConnector.createCompanyDetails(any())(any(), any()))
            .thenReturn(Future.successful(testCreateCompanyDetailsReturn))
          when(mockPurchaserService.createPurchaserName(any())).thenReturn(Some(purchaserName))

          val result = service.callCreatePurchaser(mockBackendConnector, mockPurchaserRequestService, mockPurchaserService, userAnswers, sessionData).futureValue

          status(Future.successful(result)) mustEqual SEE_OTHER
          redirectLocation(Future.successful(result)).value mustEqual
            controllers.purchaser.routes.PurchaserOverviewController.onPageLoad().url
          verify(mockBackendConnector, times(1)).createPurchaser(any())(any(), any())
          verify(mockBackendConnector, times(1)).createCompanyDetails(any())(any(), any())
        }
      }

      "must fail when returnId is missing" in {
        val mockBackendConnector = mock[StampDutyLandTaxConnector]
        val mockPurchaserRequestService = mock[PurchaserRequestService]
        val mockPurchaserService = mock[PurchaserService]
          val service = new PurchaserCreateOrUpdateService()

        val userAnswers = createPurchaserCompanyUserAnswers(returnId = None)
        val sessionData = createSessionData(purchaserAndCompanyId = None, isCompany = "Company")

        whenReady(service.callCreatePurchaser(mockBackendConnector, mockPurchaserRequestService, mockPurchaserService, userAnswers, sessionData).failed) { exception =>
          exception mustBe an[NotFoundException]
          exception.getMessage mustBe "Return ID is required"
        }
      }
    }

    ".callUpdatePurchaser" - {
      "when the purchaser is not main purchaser" - {
        "when purchaser type is Individual" - {
          "must successfully update purchaser when version update succeeds and returns new version" in {
            val mockBackendConnector = mock[StampDutyLandTaxConnector]
            val mockPurchaserRequestService = mock[PurchaserRequestService]
            val mockPurchaserService = mock[PurchaserService]
            val service = new PurchaserCreateOrUpdateService()

            val userAnswers = createPurchaserIndividualUserAnswers(
              fullReturn = Some(createFullReturn(
                returnInfo = Some(createFullReturnInfo(Some(testmainPurchaserID))),
                purchasers = Seq(
                  createPurchaser(
                    purchaserID = Some(testPurchaserId),
                    isCompany = Some("NO"),
                    purchaserResourceRef = Some(testPurchaserResourceRef),
                    nextPurchaserID = Some(testNextPurchaserId)
                  )
                )
              ))
            )

            val sessionData = createSessionData(purchaserAndCompanyId = Some(PurchaserAndCompanyId(testPurchaserId, companyDetailsID = None)), isCompany = "Individual")

            val returnVersionResponse = ReturnVersionUpdateReturn(
              newVersion = Some(2)
            )

            when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
              .thenReturn(Future.successful(returnVersionResponse))
            when(mockBackendConnector.updatePurchaser(any())(any(), any()))
              .thenReturn(Future.successful(testUpdatePurchaserReturn))
            when(mockPurchaserService.createPurchaserName(any())).thenReturn(Some(purchaserName))

            val result = service.callUpdatePurchaser(mockBackendConnector, mockPurchaserRequestService, mockPurchaserService, userAnswers, sessionData).futureValue

            status(Future.successful(result)) mustEqual SEE_OTHER
            redirectLocation(Future.successful(result)).value mustEqual
              controllers.purchaser.routes.PurchaserOverviewController.onPageLoad().url

            verify(mockBackendConnector, times(1)).updateReturnVersion(any())(any(), any())
            verify(mockBackendConnector, times(1)).updatePurchaser(any())(any(), any())
            flash(Future.successful(result)).get("purchaserUpdated") mustBe Some("Name1 Name2 Samsung")
          }
        }

        "when purchaser type is Company" - {
          "must successfully update purchaser when version update succeeds and returns new version" in {
            val mockBackendConnector = mock[StampDutyLandTaxConnector]
            val mockPurchaserRequestService = mock[PurchaserRequestService]
            val mockPurchaserService = mock[PurchaserService]
            val service = new PurchaserCreateOrUpdateService()

            val userAnswers = createPurchaserCompanyUserAnswers(
              fullReturn = Some(createFullReturn(
                returnInfo = Some(createFullReturnInfo(Some(testmainPurchaserID))),
                purchasers = Seq(
                  createPurchaser(
                    purchaserID = Some(testPurchaserId),
                    isCompany = Some("YES"),
                    purchaserResourceRef = Some(testPurchaserResourceRef),
                    nextPurchaserID = Some(testNextPurchaserId)
                  )
                )
              ))
            )

            val sessionData = createSessionData(purchaserAndCompanyId = Some(PurchaserAndCompanyId(testPurchaserId, companyDetailsID = None)), isCompany = "Company")

            val returnVersionResponse = ReturnVersionUpdateReturn(
              newVersion = Some(2)
            )

            when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
              .thenReturn(Future.successful(returnVersionResponse))
            when(mockBackendConnector.updatePurchaser(any())(any(), any()))
              .thenReturn(Future.successful(testUpdatePurchaserReturn))
            when(mockPurchaserService.createPurchaserName(any())).thenReturn(Some(purchaserName))

            val result = service.callUpdatePurchaser(mockBackendConnector, mockPurchaserRequestService, mockPurchaserService, userAnswers, sessionData).futureValue

            status(Future.successful(result)) mustEqual SEE_OTHER
            redirectLocation(Future.successful(result)).value mustEqual
              controllers.purchaser.routes.PurchaserOverviewController.onPageLoad().url

            verify(mockBackendConnector, times(1)).updateReturnVersion(any())(any(), any())
            verify(mockBackendConnector, times(1)).updatePurchaser(any())(any(), any())
          }
        }
      }

      "when the purchaser is a main purchaser" - {
        "when purchaser type is Individual" - {
          "must successfully update purchaser when version update succeeds and returns new version" in {
            val mockBackendConnector = mock[StampDutyLandTaxConnector]
            val mockPurchaserRequestService = mock[PurchaserRequestService]
            val mockPurchaserService = mock[PurchaserService]
            val service = new PurchaserCreateOrUpdateService()

            val userAnswers = createMainPurchaserIndividualUserAnswers(
              fullReturn = Some(createFullReturn(
                returnInfo = Some(createFullReturnInfo(Some(testmainPurchaserID))),
                purchasers = Seq(
                  createPurchaser(
                    purchaserID = Some(testmainPurchaserID),
                    isCompany = Some("NO"),
                    purchaserResourceRef = Some(testPurchaserResourceRef),
                    nextPurchaserID = Some(testNextPurchaserId)
                  )
                )
              ))
            )

            val sessionData = createSessionData(purchaserAndCompanyId = Some(PurchaserAndCompanyId(testmainPurchaserID, companyDetailsID = None)), isCompany = "Individual")

            val returnVersionResponse = ReturnVersionUpdateReturn(
              newVersion = Some(2)
            )

            when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
              .thenReturn(Future.successful(returnVersionResponse))
            when(mockBackendConnector.updatePurchaser(any())(any(), any()))
              .thenReturn(Future.successful(testUpdatePurchaserReturn))
            when(mockPurchaserService.createPurchaserName(any())).thenReturn(Some(purchaserName))

            val result = service.callUpdatePurchaser(mockBackendConnector, mockPurchaserRequestService, mockPurchaserService, userAnswers, sessionData).futureValue

            status(Future.successful(result)) mustEqual SEE_OTHER
            redirectLocation(Future.successful(result)).value mustEqual
              controllers.purchaser.routes.PurchaserOverviewController.onPageLoad().url

            verify(mockBackendConnector, times(1)).updateReturnVersion(any())(any(), any())
            verify(mockBackendConnector, times(1)).updatePurchaser(any())(any(), any())
          }
        }

        "when purchaser type is Company" - {
          "when company details do not exist" - {
            "must successfully update purchaser when version update succeeds, return new version and create Company Details" in {
              val mockBackendConnector = mock[StampDutyLandTaxConnector]
              val mockPurchaserRequestService = mock[PurchaserRequestService]
              val mockPurchaserService = mock[PurchaserService]
              val service = new PurchaserCreateOrUpdateService()

              val userAnswers = createMainPurchaserCompanyUserAnswers(
                fullReturn = Some(createFullReturn(
                  returnInfo = Some(createFullReturnInfo(Some(testmainPurchaserID))),
                  purchasers = Seq(
                    createPurchaser(
                      purchaserID = Some(testmainPurchaserID),
                      isCompany = Some("YES"),
                      purchaserResourceRef = Some(testPurchaserResourceRef),
                      nextPurchaserID = Some(testNextPurchaserId)
                    )
                  ),
                  companyDetails = None
                ))
              )

              val sessionData = createSessionData(purchaserAndCompanyId = Some(PurchaserAndCompanyId(testmainPurchaserID, companyDetailsID = None)), isCompany = "Company")

              val returnVersionResponse = ReturnVersionUpdateReturn(
                newVersion = Some(2)
              )

              when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
                .thenReturn(Future.successful(returnVersionResponse))
              when(mockBackendConnector.updatePurchaser(any())(any(), any()))
                .thenReturn(Future.successful(testUpdatePurchaserReturn))
              when(mockBackendConnector.createCompanyDetails(any())(any(), any()))
                .thenReturn(Future.successful(testCreateCompanyDetailsReturn))
              when(mockPurchaserService.createPurchaserName(any())).thenReturn(Some(purchaserName))

              val result = service.callUpdatePurchaser(mockBackendConnector, mockPurchaserRequestService, mockPurchaserService, userAnswers, sessionData).futureValue

              status(Future.successful(result)) mustEqual SEE_OTHER
              redirectLocation(Future.successful(result)).value mustEqual
                controllers.purchaser.routes.PurchaserOverviewController.onPageLoad().url

              verify(mockBackendConnector, times(1)).updateReturnVersion(any())(any(), any())
              verify(mockBackendConnector, times(1)).updatePurchaser(any())(any(), any())
              verify(mockBackendConnector, times(1)).createCompanyDetails(any())(any(), any())
            }
          }

          "when company details exist" - {
            "must successfully update purchaser when version update succeeds, return new version and update Company Details" in {
              val mockBackendConnector = mock[StampDutyLandTaxConnector]
              val mockPurchaserRequestService = mock[PurchaserRequestService]
              val mockPurchaserService = mock[PurchaserService]
              val service = new PurchaserCreateOrUpdateService()

              val userAnswers = createMainPurchaserCompanyUserAnswers(
                fullReturn = Some(createFullReturn(
                  returnInfo = Some(createFullReturnInfo(Some(testmainPurchaserID))),
                  purchasers = Seq(
                    createPurchaser(
                      purchaserID = Some(testmainPurchaserID),
                      isCompany = Some("YES"),
                      purchaserResourceRef = Some(testPurchaserResourceRef),
                      nextPurchaserID = Some(testNextPurchaserId)
                    )
                  ),
                  companyDetails = Some(createCompanyDetails(testmainPurchaserID))
                ))
              )

              val sessionData = createSessionData(purchaserAndCompanyId = Some(PurchaserAndCompanyId(testmainPurchaserID, companyDetailsID = None)), isCompany = "Company")

              val returnVersionResponse = ReturnVersionUpdateReturn(
                newVersion = Some(2)
              )

              when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
                .thenReturn(Future.successful(returnVersionResponse))
              when(mockBackendConnector.updatePurchaser(any())(any(), any()))
                .thenReturn(Future.successful(testUpdatePurchaserReturn))
              when(mockBackendConnector.updateCompanyDetails(any())(any(), any()))
                .thenReturn(Future.successful(testUpdateCompanyDetailsReturn))
              when(mockPurchaserService.createPurchaserName(any())).thenReturn(Some(purchaserName))

              val result = service.callUpdatePurchaser(mockBackendConnector, mockPurchaserRequestService, mockPurchaserService, userAnswers, sessionData).futureValue

              status(Future.successful(result)) mustEqual SEE_OTHER
              redirectLocation(Future.successful(result)).value mustEqual
                controllers.purchaser.routes.PurchaserOverviewController.onPageLoad().url

              verify(mockBackendConnector, times(1)).updateReturnVersion(any())(any(), any())
              verify(mockBackendConnector, times(1)).updatePurchaser(any())(any(), any())
              verify(mockBackendConnector, times(1)).updateCompanyDetails(any())(any(), any())
            }
          }
        }
      }

      "must fail when version update does not return a new version" in {
        val mockBackendConnector = mock[StampDutyLandTaxConnector]
        val mockPurchaserRequestService = mock[PurchaserRequestService]
        val mockPurchaserService = mock[PurchaserService]
          val service = new PurchaserCreateOrUpdateService()

        val purchaser = createPurchaser(
          purchaserID = Some(testPurchaserId),
          isCompany = Some("YES"),
          purchaserResourceRef = Some(testPurchaserResourceRef),
          nextPurchaserID = Some(testNextPurchaserId)
        )
        val fullReturn = createFullReturn(purchasers = Seq(purchaser), returnInfo = Some(createFullReturnInfo(Some(testmainPurchaserID))))
        val userAnswers = createPurchaserCompanyUserAnswers(fullReturn = Some(fullReturn))
        val sessionData = createSessionData(purchaserAndCompanyId = Some(PurchaserAndCompanyId(testPurchaserId, companyDetailsID = None)), isCompany = "Company")

        val returnVersionResponse = ReturnVersionUpdateReturn(
          newVersion = None
        )

        when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
          .thenReturn(Future.successful(returnVersionResponse))

        whenReady(service.callUpdatePurchaser(mockBackendConnector, mockPurchaserRequestService, mockPurchaserService, userAnswers, sessionData).failed) { exception =>
          exception mustBe an[IllegalStateException]
          exception.getMessage mustBe "Return version update did not produce a new version"
        }
      }


      "must call updateReturnVersion exactly once" in {
        val mockBackendConnector = mock[StampDutyLandTaxConnector]
        val mockPurchaserRequestService = mock[PurchaserRequestService]
        val mockPurchaserService = mock[PurchaserService]
          val service = new PurchaserCreateOrUpdateService()

        val purchaser = createPurchaser(
          purchaserID = Some(testPurchaserId),
          isCompany = Some("YES"),
          purchaserResourceRef = Some(testPurchaserResourceRef),
          nextPurchaserID = Some(testNextPurchaserId)
        )
        val fullReturn = createFullReturn(purchasers = Seq(purchaser), returnInfo = Some(createFullReturnInfo(Some(testmainPurchaserID))))
        val userAnswers = createPurchaserCompanyUserAnswers(fullReturn = Some(fullReturn))
        val sessionData = createSessionData(purchaserAndCompanyId = Some(PurchaserAndCompanyId(testPurchaserId, companyDetailsID = None)), isCompany = "Company")

        val returnVersionResponse = ReturnVersionUpdateReturn(
          newVersion = Some(2)
        )

        when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
          .thenReturn(Future.successful(returnVersionResponse))
        when(mockBackendConnector.updatePurchaser(any())(any(), any()))
          .thenReturn(Future.successful(testUpdatePurchaserReturn))
        when(mockPurchaserService.createPurchaserName(any())).thenReturn(Some(purchaserName))

        service.result(userAnswers, sessionData, mockBackendConnector, mockPurchaserRequestService, mockPurchaserService).futureValue

        verify(mockBackendConnector, times(1)).updateReturnVersion(any())(any(), any())
        verify(mockBackendConnector, times(1)).updatePurchaser(any())(any(), any())
      }

      "must fail when purchaser is not found in full return" in {
        val mockBackendConnector = mock[StampDutyLandTaxConnector]
        val mockPurchaserRequestService = mock[PurchaserRequestService]
        val mockPurchaserService = mock[PurchaserService]
          val service = new PurchaserCreateOrUpdateService()

        val fullReturn = createFullReturn(
          purchasers = Seq.empty,
          returnInfo = Some(createFullReturnInfo(Some(testmainPurchaserID))))
        val userAnswers = createPurchaserCompanyUserAnswers(fullReturn = Some(fullReturn))
        val sessionData = createSessionData(purchaserAndCompanyId = Some(PurchaserAndCompanyId(testPurchaserId, companyDetailsID = None)), isCompany = "Company")

        val returnVersionResponse = ReturnVersionUpdateReturn(
          newVersion = Some(2)
        )

        when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
          .thenReturn(Future.successful(returnVersionResponse))

        whenReady(service.callUpdatePurchaser(mockBackendConnector, mockPurchaserRequestService, mockPurchaserService, userAnswers, sessionData).failed) { exception =>
          exception mustBe an[NoSuchElementException]
          exception.getMessage must include("Purchaser mandatory Resources not found")
        }
      }

      "must fail when purchaser resource ref is not found" in {
        val mockBackendConnector = mock[StampDutyLandTaxConnector]
        val mockPurchaserRequestService = mock[PurchaserRequestService]
        val mockPurchaserService = mock[PurchaserService]
          val service = new PurchaserCreateOrUpdateService()

        val fullReturn = createFullReturn(
          purchasers = Seq(
            createPurchaser(
              Some(testPurchaserId),
              purchaserResourceRef = None,
              nextPurchaserID = Some(testNextPurchaserId),
              isCompany = Some("YES"))),
          returnInfo = Some(createFullReturnInfo(Some(testmainPurchaserID))))
        val userAnswers = createPurchaserCompanyUserAnswers(fullReturn = Some(fullReturn))
        val sessionData = createSessionData(purchaserAndCompanyId = Some(PurchaserAndCompanyId(testPurchaserId, companyDetailsID = None)), isCompany = "Company")

        val returnVersionResponse = ReturnVersionUpdateReturn(
          newVersion = Some(2)
        )

        when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
          .thenReturn(Future.successful(returnVersionResponse))

        whenReady(service.callUpdatePurchaser(mockBackendConnector, mockPurchaserRequestService, mockPurchaserService, userAnswers, sessionData).failed) { exception =>
          exception mustBe an[NoSuchElementException]
          exception.getMessage must include("Purchaser mandatory Resources not found")
        }
      }

      "must propagate backend connector updateReturnVersion failures" in {
        val mockBackendConnector = mock[StampDutyLandTaxConnector]
        val mockPurchaserRequestService = mock[PurchaserRequestService]
        val mockPurchaserService = mock[PurchaserService]
        val service = new PurchaserCreateOrUpdateService()

        val fullReturn = createFullReturn(
          purchasers = Seq(
            createPurchaser(
              Some(testPurchaserId),
              purchaserResourceRef = Some(testPurchaserResourceRef),
              nextPurchaserID = Some(testNextPurchaserId),
              isCompany = Some("YES"))),
          returnInfo = Some(createFullReturnInfo(Some(testmainPurchaserID))))
        val userAnswers = createPurchaserCompanyUserAnswers(fullReturn = Some(fullReturn))
        val sessionData = createSessionData(purchaserAndCompanyId = Some(PurchaserAndCompanyId(testPurchaserId, companyDetailsID = None)), isCompany = "Company")

        when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
          .thenReturn(Future.failed(new RuntimeException("Backend failure")))

        whenReady(service.callUpdatePurchaser(mockBackendConnector, mockPurchaserRequestService, mockPurchaserService, userAnswers, sessionData).failed) { exception =>
          exception mustBe an[RuntimeException]
          exception.getMessage mustBe "Backend failure"
        }
      }

      "must propagate backend connector updatePurchaser failures" in {
        val mockBackendConnector = mock[StampDutyLandTaxConnector]
        val mockPurchaserRequestService = mock[PurchaserRequestService]
        val mockPurchaserService = mock[PurchaserService]
        val service = new PurchaserCreateOrUpdateService()

        val fullReturn = createFullReturn(
          purchasers = Seq(
            createPurchaser(
              Some(testPurchaserId),
              purchaserResourceRef = Some(testPurchaserResourceRef),
              nextPurchaserID = Some(testNextPurchaserId),
              isCompany = Some("YES"))),
          returnInfo = Some(createFullReturnInfo(Some(testmainPurchaserID))))
        val userAnswers = createPurchaserCompanyUserAnswers(fullReturn = Some(fullReturn))
        val sessionData = createSessionData(purchaserAndCompanyId = Some(PurchaserAndCompanyId(testPurchaserId, companyDetailsID = None)), isCompany = "Company")

        val returnVersionResponse = ReturnVersionUpdateReturn(
          newVersion = Some(2)
        )

        when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
          .thenReturn(Future.successful(returnVersionResponse))

        when(mockBackendConnector.updatePurchaser(any())(any(), any()))
          .thenReturn(Future.failed(new RuntimeException("Update purchaser failed")))

        whenReady(service.callUpdatePurchaser(mockBackendConnector, mockPurchaserRequestService, mockPurchaserService, userAnswers, sessionData).failed) { exception =>
          exception mustBe an[RuntimeException]
          exception.getMessage mustBe "Update purchaser failed"
        }
      }

      "must pass HeaderCarrier to connector" in {
        val mockBackendConnector = mock[StampDutyLandTaxConnector]
        val mockPurchaserRequestService = mock[PurchaserRequestService]
        val mockPurchaserService = mock[PurchaserService]
        val service = new PurchaserCreateOrUpdateService()
        val testHc = HeaderCarrier(sessionId = Some(uk.gov.hmrc.http.SessionId("test-session")))

        val userAnswers = createPurchaserCompanyUserAnswers(
          fullReturn = Some(createFullReturn(
            returnInfo = Some(createFullReturnInfo(Some(testmainPurchaserID))),
            purchasers = Seq(
              createPurchaser(
                purchaserID = Some(testPurchaserId),
                isCompany = Some("YES"),
                purchaserResourceRef = Some(testPurchaserResourceRef),
                nextPurchaserID = Some(testNextPurchaserId)
              )
            )
          ))
        )

        val sessionData = createSessionData(purchaserAndCompanyId = Some(PurchaserAndCompanyId(testPurchaserId, companyDetailsID = None)), isCompany = "Company")

        val returnVersionResponse = ReturnVersionUpdateReturn(
          newVersion = Some(2)
        )

        when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
          .thenReturn(Future.successful(returnVersionResponse))
        when(mockBackendConnector.updatePurchaser(any())(any(), any()))
          .thenReturn(Future.successful(testUpdatePurchaserReturn))
        when(mockPurchaserService.createPurchaserName(any())).thenReturn(Some(purchaserName))

        service.result(userAnswers, sessionData, mockBackendConnector, mockPurchaserRequestService, mockPurchaserService)(ec, testHc, request).futureValue

        verify(mockBackendConnector, times(1)).updateReturnVersion(any())(eqTo(testHc), any())
        verify(mockBackendConnector, times(1)).updatePurchaser(any())(eqTo(testHc), any())
      }
    }

    ".result" - {
      "when updating an existing Purchaser" - {
        "must call callUpdatePurchaser" in {
          val mockBackendConnector = mock[StampDutyLandTaxConnector]
          val mockPurchaserRequestService = mock[PurchaserRequestService]
          val mockPurchaserService = mock[PurchaserService]
          val service = new PurchaserCreateOrUpdateService()

          val userAnswers = createPurchaserCompanyUserAnswers(
            fullReturn = Some(createFullReturn(
              returnInfo = Some(createFullReturnInfo(Some(testmainPurchaserID))),
              purchasers = Seq(
                createPurchaser(
                  purchaserID = Some(testPurchaserId),
                  isCompany = Some("YES"),
                  purchaserResourceRef = Some(testPurchaserResourceRef),
                  nextPurchaserID = Some(testNextPurchaserId)
                )
              )
            ))
          )

          val sessionData = createSessionData(purchaserAndCompanyId = Some(PurchaserAndCompanyId(testPurchaserId, companyDetailsID = None)), isCompany = "Company")

          val returnVersionResponse = ReturnVersionUpdateReturn(
            newVersion = Some(2)
          )

          when(mockBackendConnector.updateReturnVersion(any())(any(), any()))
            .thenReturn(Future.successful(returnVersionResponse))
          when(mockBackendConnector.updatePurchaser(any())(any(), any()))
            .thenReturn(Future.successful(testUpdatePurchaserReturn))
          when(mockPurchaserService.createPurchaserName(any())).thenReturn(Some(purchaserName))

          val result = service.result(userAnswers, sessionData, mockBackendConnector, mockPurchaserRequestService, mockPurchaserService).futureValue

          status(Future.successful(result)) mustEqual SEE_OTHER
          redirectLocation(Future.successful(result)).value mustEqual
            controllers.purchaser.routes.PurchaserOverviewController.onPageLoad().url

          verify(mockBackendConnector, times(1)).updateReturnVersion(any())(any(), any())
          verify(mockBackendConnector, times(1)).updatePurchaser(any())(any(), any())
        }
      }

      "when creating a Purchaser" - {
        "must call callCreatePurchaser" in {
          val mockBackendConnector = mock[StampDutyLandTaxConnector]
          val mockPurchaserRequestService = mock[PurchaserRequestService]
          val mockPurchaserService = mock[PurchaserService]
          val service = new PurchaserCreateOrUpdateService()

          val userAnswers = createPurchaserCompanyUserAnswers(fullReturn = Some(createFullReturn(returnInfo = Some(createFullReturnInfo(Some("differentMain"))))))
          val sessionData = createSessionData(purchaserAndCompanyId = None, isCompany = "Company")

          when(mockBackendConnector.createPurchaser(any())(any(), any()))
            .thenReturn(Future.successful(testCreatePurchaserReturn))
          when(mockBackendConnector.createCompanyDetails(any())(any(), any()))
            .thenReturn(Future.successful(testCreateCompanyDetailsReturn))
          when(mockPurchaserService.createPurchaserName(any())).thenReturn(Some(purchaserName))

          val result = service.result(userAnswers, sessionData, mockBackendConnector, mockPurchaserRequestService, mockPurchaserService).futureValue

          status(Future.successful(result)) mustEqual SEE_OTHER
          redirectLocation(Future.successful(result)).value mustEqual
            controllers.purchaser.routes.PurchaserOverviewController.onPageLoad().url
          verify(mockBackendConnector, times(1)).createPurchaser(any())(any(), any())
        }

        "must skip creation when errorCalc is false (99 or more entities)" in {
          val mockBackendConnector = mock[StampDutyLandTaxConnector]
          val mockPurchaserRequestService = mock[PurchaserRequestService]
          val mockPurchaserService = mock[PurchaserService]
          val service = new PurchaserCreateOrUpdateService()

          val purchasers = (1 to 99).map(i => createPurchaser(
            purchaserID =  Some(s"purchaser-$i"),
            isCompany = Some("YES"),
            purchaserResourceRef = Some(testPurchaserResourceRef),
            nextPurchaserID = Some(testNextPurchaserId)
          ))

          val fullReturn = createFullReturn(
            returnInfo = Some(createFullReturnInfo(Some(testmainPurchaserID))),
            purchasers = purchasers
          )

          val userAnswers = createMainPurchaserIndividualUserAnswers(
            fullReturn = Some(fullReturn)
          )

          val sessionData = createSessionData(purchaserAndCompanyId = None, isCompany = "Company")

          val result = service.result(userAnswers, sessionData, mockBackendConnector, mockPurchaserRequestService, mockPurchaserService).futureValue

          status(Future.successful(result)) mustEqual SEE_OTHER
          redirectLocation(Future.successful(result)).value mustEqual
            controllers.purchaser.routes.PurchaserOverviewController.onPageLoad().url
          verify(mockBackendConnector, never).createPurchaser(any())(any(), any())
        }

        "must skip creation when errorCalc is false (99 or more vendors and purchasers combined)" in {
          val mockBackendConnector = mock[StampDutyLandTaxConnector]
          val mockPurchaserRequestService = mock[PurchaserRequestService]
          val mockPurchaserService = mock[PurchaserService]
          val service = new PurchaserCreateOrUpdateService()

          val vendors = (1 to 50).map(i => mock[models.Vendor])
          val purchasers = (1 to 49).map(i => createPurchaser(
            purchaserID =  Some(s"purchaser-$i"),
            isCompany = Some("YES"),
            purchaserResourceRef = Some(testPurchaserResourceRef),
            nextPurchaserID = Some(testNextPurchaserId)
          ))
          val fullReturn = createFullReturn(vendors = vendors, purchasers = purchasers)

          val userAnswers = createMainPurchaserIndividualUserAnswers(
            fullReturn = Some(fullReturn)
          )

          val sessionData = createSessionData(purchaserAndCompanyId = None, isCompany = "Company")

          val result = service.result(userAnswers, sessionData, mockBackendConnector, mockPurchaserRequestService, mockPurchaserService).futureValue

          status(Future.successful(result)) mustEqual SEE_OTHER
          redirectLocation(Future.successful(result)).value mustEqual
            controllers.purchaser.routes.PurchaserOverviewController.onPageLoad().url
          verify(mockBackendConnector, never).createPurchaser(any())(any(), any())
        }

        "must calculate errorCalc correctly with vendors and purchasers combined (98 total)" in {
          val mockBackendConnector = mock[StampDutyLandTaxConnector]
          val mockPurchaserRequestService = mock[PurchaserRequestService]
          val mockPurchaserService = mock[PurchaserService]
          val service = new PurchaserCreateOrUpdateService()

          val vendors = (1 to 50).map(i => mock[models.Vendor])
          val purchasers = (1 to 48).map(i => createPurchaser(
            purchaserID =  Some(s"purchaser-$i"),
            isCompany = Some("YES"),
            purchaserResourceRef = Some(testPurchaserResourceRef),
            nextPurchaserID = Some(testNextPurchaserId)
          ))
          val fullReturn = createFullReturn(vendors = vendors, purchasers = purchasers)

          val userAnswers = createMainPurchaserIndividualUserAnswers(
            fullReturn = Some(fullReturn)
          )

          val sessionData = createSessionData(purchaserAndCompanyId = None, isCompany = "Company")

          when(mockBackendConnector.createPurchaser(any())(any(), any()))
            .thenReturn(Future.successful(testCreatePurchaserReturn))
          when(mockPurchaserService.createPurchaserName(any())).thenReturn(Some(purchaserName))

          val result = service.result(userAnswers, sessionData, mockBackendConnector, mockPurchaserRequestService, mockPurchaserService).futureValue

          status(Future.successful(result)) mustEqual SEE_OTHER
          redirectLocation(Future.successful(result)).value mustEqual
            controllers.purchaser.routes.PurchaserOverviewController.onPageLoad().url
          verify(mockBackendConnector, times(1)).createPurchaser(any())(any(), any())
        }
      }

      "when handling errors" - {
        "must redirect to CYA when returnId is missing" in {
          val mockBackendConnector = mock[StampDutyLandTaxConnector]
          val mockPurchaserRequestService = mock[PurchaserRequestService]
          val mockPurchaserService = mock[PurchaserService]
          val service = new PurchaserCreateOrUpdateService()

          val userAnswers = createPurchaserCompanyUserAnswers(returnId = None)
          val sessionData = createSessionData(purchaserAndCompanyId = None, isCompany = "Company")

          val result = service.result(userAnswers, sessionData, mockBackendConnector, mockPurchaserRequestService, mockPurchaserService).futureValue

          status(Future.successful(result)) mustEqual SEE_OTHER
          redirectLocation(Future.successful(result)).value mustEqual
            controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url
        }
      }
    }
  }

}