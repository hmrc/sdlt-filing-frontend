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

package controllers.purchaser

import base.SpecBase
import connectors.StampDutyLandTaxConnector
import models.purchaser.{ConfirmNameOfThePurchaser, CreatePurchaserReturn}
import models.{FullReturn, Purchaser, ReturnInfo, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.prop.TableDrivenPropertyChecks.*
import org.scalatestplus.mockito.MockitoSugar
import pages.purchaser.ConfirmNameOfThePurchaserPage
import play.api.inject.bind
import play.api.libs.json.{JsNull, Json}
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.purchaser.{PurchaserCreateOrUpdateService, PurchaserRequestService}
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.govuk.SummaryListFluency

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

class PurchaserCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency with MockitoSugar with BeforeAndAfterEach {

  private val mockSessionRepository = mock[SessionRepository]
  private val mockBackendConnector = mock[StampDutyLandTaxConnector]
  private val mockPurchaserRequestService = mock[PurchaserRequestService]
  private val mockPurchaserCreateOrUpdateService = mock[PurchaserCreateOrUpdateService]

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val request: FakeRequest[_] = FakeRequest()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global


  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository)
  }

  "Check Your Answers Controller" - {

    "onPageLoad" - {

      "must redirect to ReturnTaskList when the UserAnswers data is empty" in {

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must redirect to ReturnTaskList when data is available but session is not" in {
        val userAnswers = UserAnswers(
          id = "12345",
          returnId = None,
          storn = "TESTSTORN",
          data = Json.obj(
            "whoIsThePurchaser" -> "Individual",
            "purchaserOrCompanyName" -> "John Doe",
            "purchaserAddress" -> Json.obj(
              "houseNumber" -> JsNull,
              "line1" -> "Test Street",
              "line2" -> JsNull,
              "line3" -> JsNull,
              "line4" -> JsNull,
              "line5" -> JsNull,
              "postcode" -> JsNull,
              "country" -> JsNull,
              "addressValidated" -> false
            ),
          ),
          lastUpdated = Instant.now
        )

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must return OK and the correct view when UserAnswers contains valid data" in {


        val purchaserCases = Table(
          ("whoIsThePurchaser", "purchaserOrCompanyName"),
          ("Individual", "John Doe"),
          ("Business", "ACME Ltd")
        )

        forAll(purchaserCases) { (purchaser, name) => {
          val baseData = Json.obj(
            "whoIsTheVendor" -> purchaser,
            "purchaserOrCompanyName" -> name,
            "purchaserAddress" -> Json.obj(
              "houseNumber" -> JsNull,
              "line1" -> "Test Street",
              "line2" -> JsNull,
              "line3" -> JsNull,
              "line4" -> JsNull,
              "line5" -> JsNull,
              "postcode" -> JsNull,
              "country" -> JsNull,
              "addressValidated" -> false
            )
          )
          val userAnswers = UserAnswers(
            id = "12345",
            returnId = Some("AB2346"),
            storn = "TESTSTORN",
            data = baseData,
            lastUpdated = Instant.now
          )

          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

          running(application) {
            val request = FakeRequest(GET, controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url)

            val result = route(application, request).value

            status(result) mustEqual OK
            contentAsString(result) must include("Check your answers")
          }
        }
        }
      }

      "must redirect to Journey Recovery when no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must return OK and the correct view when UserAnswers without mainPurchaserid for Individual" in {

        val testReturnId = "123456"
        //    val fullReturn: FullReturn = FullReturnConstants.completeFullReturn
        val returnInfo = ReturnInfo(
          returnID = Some("RET123456789"),
          storn = Some("STORN123456"),
          // mainPurchaserID = Some("PUR001"),
        )
        val fullRetur = FullReturn(
          stornId = "STORN123456",
          returnResourceRef = "RRF-2024-001",
          returnInfo = Some(returnInfo),
        )
        val userAnswers = {
          UserAnswers("id", storn = "TESTSTORN",
            Some(testReturnId), Some(fullRetur),
            data = Json.obj(
              "whoIsThePurchaser" -> "Individual",
              "purchaserOrCompanyName" -> "John Doe",
              "purchaserAddress" -> Json.obj(
                "houseNumber" -> JsNull,
                "line1" -> "Test Street",
                "line2" -> JsNull,
                "line3" -> JsNull,
                "line4" -> JsNull,
                "line5" -> JsNull,
                "postcode" -> JsNull,
                "country" -> JsNull,
                "addressValidated" -> false
              ),
            ),
          )
        }

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must include("Select answer")
        }
      }

      "must return OK and the correct view when UserAnswers with mainPurchaserid and ConfirmNameOfThePurchaser for Individual" in {

        val testReturnId = "123456"
        ///   val fullReturn: FullReturn = FullReturnConstants.completeFullReturn
        val returnInfo = ReturnInfo(
          returnID = Some("RET123456789"),
          storn = Some("STORN123456"),
          mainPurchaserID = Some("PUR001"),
        )
        val fullRetur = FullReturn(
          stornId = "STORN123456",
          returnResourceRef = "RRF-2024-001",
          returnInfo = Some(returnInfo),
        )
        val userAnswers = {
          UserAnswers("id", storn = "TESTSTORN",
            Some(testReturnId), Some(fullRetur),
            data = Json.obj(
              "whoIsThePurchaser" -> "Individual",
              "purchaserOrCompanyName" -> "John Doe",
              "nameOfPurchaser" -> Json.obj("name" -> "test"),
              "addPurchaserPhoneNumber" -> true,
              "enterPurchaserPhoneNumber" -> "+447466648072",
              "purchaserAddress" -> Json.obj(
                "houseNumber" -> JsNull,
                "line1" -> "Test Street",
                "line2" -> JsNull,
                "line3" -> JsNull,
                "line4" -> JsNull,
                "line5" -> JsNull,
                "postcode" -> JsNull,
                "country" -> JsNull,
                "addressValidated" -> false,
                "ConfirmNameOfThePurchaser" -> "ConfirmNameOfThePurchaser.Yes",
              ),
            ),
          )
        }

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must not include ("Select ActingAsTrustee")
        }
      }

      "must return OK and the correct view when UserAnswers has the mainPurchaserid for Company" in {

        val testReturnId = "123456"
        //    val fullReturn: FullReturn = FullReturnConstants.completeFullReturn
        val returnInfo = ReturnInfo(
          returnID = Some("RET123456789"),
          storn = Some("STORN123456"),
          mainPurchaserID = Some("PUR001"),
        )
        val fullRetur = FullReturn(
          stornId = "STORN123456",
          returnResourceRef = "RRF-2024-001",
          returnInfo = Some(returnInfo),
        )
        val userAnswers = {
          UserAnswers("id", storn = "TESTSTORN",
            Some(testReturnId), Some(fullRetur),
            data = Json.obj(
              "whoIsThePurchaser" -> "Company",
              "purchaserOrCompanyName" -> "UK Ltd",
              "purchaserAddress" -> Json.obj(
                "houseNumber" -> JsNull,
                "line1" -> "Test Street",
                "line2" -> JsNull,
                "line3" -> JsNull,
                "line4" -> JsNull,
                "line5" -> JsNull,
                "postcode" -> JsNull,
                "country" -> JsNull,
                "addressValidated" -> false
              ),
            ),
          )
        }

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must not include ("Select ActingAsTrustee")
        }
      }

      "must return OK and the correct view when UserAnswers without mainPurchaserid for Company" in {

        val testReturnId = "123456"
        //    val fullReturn: FullReturn = FullReturnConstants.completeFullReturn
        val returnInfo = ReturnInfo(
          returnID = Some("RET123456789"),
          storn = Some("STORN123456"),
          // mainPurchaserID = Some("PUR001"),
        )
        val fullRetur = FullReturn(
          stornId = "STORN123456",
          returnResourceRef = "RRF-2024-001",
          returnInfo = Some(returnInfo),
        )
        val userAnswers = {
          UserAnswers("id", storn = "TESTSTORN",
            Some(testReturnId), Some(fullRetur),
            data = Json.obj(
              "whoIsThePurchaser" -> "Company",
              "purchaserOrCompanyName" -> "John Doe",
              "purchaserAddress" -> Json.obj(
                "houseNumber" -> JsNull,
                "line1" -> "Test Street",
                "line2" -> JsNull,
                "line3" -> JsNull,
                "line4" -> JsNull,
                "line5" -> JsNull,
                "postcode" -> JsNull,
                "country" -> JsNull,
                "addressValidated" -> false
              ),
            ),
          )
        }

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must include("Select answer")
        }
      }

      "must return OK and the correct view when UserAnswers with mainPurchaserid and ConfirmNameOfThePurchaser for Company" in {

        val testReturnId = "123456"
        ///   val fullReturn: FullReturn = FullReturnConstants.completeFullReturn
        val returnInfo = ReturnInfo(
          returnID = Some("RET123456789"),
          storn = Some("STORN123456"),
          mainPurchaserID = Some("PUR001"),
        )
        val fullRetur = FullReturn(
          stornId = "STORN123456",
          returnResourceRef = "RRF-2024-001",
          returnInfo = Some(returnInfo),
        )
        val userAnswers = {
          UserAnswers("id", storn = "TESTSTORN",
            Some(testReturnId), Some(fullRetur),
            data = Json.obj(
              "whoIsThePurchaser" -> "Company",
              "purchaserOrCompanyName" -> "John Doe",
              "nameOfPurchaser" -> Json.obj("name" -> "test"),
              "addPurchaserPhoneNumber" -> true,
              "enterPurchaserPhoneNumber" -> "+447466648072",
              "purchaserAddress" -> Json.obj(
                "houseNumber" -> JsNull,
                "line1" -> "Test Street",
                "line2" -> JsNull,
                "line3" -> JsNull,
                "line4" -> JsNull,
                "line5" -> JsNull,
                "postcode" -> JsNull,
                "country" -> JsNull,
                "addressValidated" -> true,
              ),
              "ConfirmNameOfThePurchaser" -> "ConfirmNameOfThePurchaser.Yes",
            ),
          )
        }

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must not include ("Select ActingAsTrustee")
          //  redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url

        }
      }

      "must return OK and the correct view when UserAnswers with mainPurchaserid and ConfirmNameOfThePurchaser for Company and ConfirmationName as Yes" in {

        val testReturnId = "123456"
        ///   val fullReturn: FullReturn = FullReturnConstants.completeFullReturn
        val returnInfo = ReturnInfo(
          returnID = Some("RET123456789"),
          storn = Some("STORN123456"),
          mainPurchaserID = Some("PUR001"),
        )
        val fullRetur = FullReturn(
          stornId = "STORN123456",
          returnResourceRef = "RRF-2024-001",
          returnInfo = Some(returnInfo),
        )
        val userAnswers = {
          UserAnswers("id", storn = "TESTSTORN",
            Some(testReturnId), Some(fullRetur),
            data = Json.obj(
              "whoIsThePurchaser" -> "Company",
              "purchaserOrCompanyName" -> "John Doe",
              "nameOfPurchaser" -> Json.obj("name" -> "test"),
              "addPurchaserPhoneNumber" -> true,
              "enterPurchaserPhoneNumber" -> "+447466648072",
              "purchaserAddress" -> Json.obj(
                "houseNumber" -> JsNull,
                "line1" -> "Test Street",
                "line2" -> JsNull,
                "line3" -> JsNull,
                "line4" -> JsNull,
                "line5" -> JsNull,
                "postcode" -> JsNull,
                "country" -> JsNull,
                "addressValidated" -> true,
              ),
              "ConfirmNameOfThePurchaser" -> "ConfirmNameOfThePurchaser.Yes",
            ),
          )
        }

        val testuserAnswers = userAnswers.set(ConfirmNameOfThePurchaserPage, ConfirmNameOfThePurchaser.Yes).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(testuserAnswers)))

        val application = applicationBuilder(userAnswers = Some(testuserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must include("Check your answers")
        }
      }

      "must return OK and the correct view when UserAnswers with mainPurchaserid and ConfirmNameOfThePurchaser for Company and ConfirmationName as NO" in {

        val testReturnId = "123456"
        ///   val fullReturn: FullReturn = FullReturnConstants.completeFullReturn
        val returnInfo = ReturnInfo(
          returnID = Some("RET123456789"),
          storn = Some("STORN123456"),
          mainPurchaserID = Some("PUR001"),
        )
        val fullRetur = FullReturn(
          stornId = "STORN123456",
          returnResourceRef = "RRF-2024-001",
          returnInfo = Some(returnInfo),
        )
        val userAnswers = {
          UserAnswers("id", storn = "TESTSTORN",
            Some(testReturnId), Some(fullRetur),
            data = Json.obj(
              "whoIsThePurchaser" -> "Company",
              "purchaserOrCompanyName" -> "John Doe",
              "nameOfPurchaser" -> Json.obj("name" -> "test"),
              "addPurchaserPhoneNumber" -> true,
              "enterPurchaserPhoneNumber" -> "+447466648072",
              "purchaserAddress" -> Json.obj(
                "houseNumber" -> JsNull,
                "line1" -> "Test Street",
                "line2" -> JsNull,
                "line3" -> JsNull,
                "line4" -> JsNull,
                "line5" -> JsNull,
                "postcode" -> JsNull,
                "country" -> JsNull,
                "addressValidated" -> true,
              ),
            ),
          )
        }

        val testuserAnswers = userAnswers.set(ConfirmNameOfThePurchaserPage, ConfirmNameOfThePurchaser.No).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(testuserAnswers)))

        val application = applicationBuilder(userAnswers = Some(testuserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must include("Check your answers")
        }
      }
    }

    "onSubmit" - {

      "must redirect to PurchaserOverview when all required data is present and valid" in {

        val userAnswers = UserAnswers(
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

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        when(mockBackendConnector.createPurchaser(any())(any(), any())).thenReturn(Future.successful(CreatePurchaserReturn("PUR-REF-001", "PUR001")))

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        when(mockPurchaserCreateOrUpdateService.result(any(), any(), any(),
          any())(any(), any(), any())).thenReturn(Future.successful(Redirect(controllers.purchaser.routes.PurchaserOverviewController.onPageLoad())))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .overrides(bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector))
          .overrides(bind[PurchaserRequestService].toInstance(mockPurchaserRequestService))
          .overrides(bind[PurchaserCreateOrUpdateService].toInstance(mockPurchaserCreateOrUpdateService))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.purchaser.routes.PurchaserCheckYourAnswersController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          controllers.purchaser.routes.PurchaserOverviewController.onPageLoad().url.contains(redirectLocation(result).value) mustEqual true
        }
      }

      "must redirect back to JourneyRecoveryController when required data is missing or invalid" in {

        val incompleteData = Json.obj(
          "whoIsThePurchaser" -> "Individual"
          // Missing other required fields
        )

        val userAnswers = UserAnswers(
          id = userAnswersId,
          storn = "TESTSTORN",
          data = incompleteData
        )

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.purchaser.routes.PurchaserCheckYourAnswersController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect back to PurchaserCheckYourAnswersController on JSError" in {

        val incompleteData = Json.obj(
          "whoIsThePurchaser" -> "Individual"
          // Missing other required fields
        )

        val userAnswers = UserAnswers(
          id = userAnswersId,
          storn = "TESTSTORN",
          returnId = Some("12313"),
          data = incompleteData
        )

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.purchaser.routes.PurchaserCheckYourAnswersController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaser.routes.PurchaserCheckYourAnswersController.onSubmit().url
        }
      }

      "must redirect to JourneyRecoveryController when no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(POST, controllers.purchaser.routes.PurchaserCheckYourAnswersController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
