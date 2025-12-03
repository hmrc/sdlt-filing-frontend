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

package controllers.vendor

import base.SpecBase
import connectors.StampDutyLandTaxConnector
import constants.FullReturnConstants.completeFullReturn
import models.vendor.{CreateVendorRequest, CreateVendorReturn, VendorCurrent, VendorName, VendorSessionAddress, VendorSessionCountry, VendorSessionQuestions}
import models.{CreateReturnResult, ReturnInfo, UserAnswers, Vendor}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.libs.json.{JsNull, JsObject, Json}
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.vendor.{VendorCreateOrUpdateService, VendorRequestService}
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.govuk.SummaryListFluency
import views.html.vendor.VendorCheckYourAnswersView

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class VendorCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency with MockitoSugar with BeforeAndAfterEach {

  private val mockSessionRepository = mock[SessionRepository]
  private val mockBackendConnector = mock[StampDutyLandTaxConnector]
  private val mockVendorRequestService = mock[VendorRequestService]
  private val mockVendorCreateOrUpdateService = mock[VendorCreateOrUpdateService]

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val request: FakeRequest[_] = FakeRequest()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository)
  }

  "Check Your Answers Controller" - {

    "onPageLoad" - {

      "must redirect to BeforeStartReturnController when the UserAnswers data is empty" in {

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.preliminary.routes.BeforeStartReturnController.onPageLoad().url
        }
      }

      "must redirect to ReturnTaskList when data is available but session is not" in {
        val userAnswers = UserAnswers(
          id = "12345",
          returnId = None,
          storn = "TESTSTORN",
          data = Json.obj(
            "whoIsTheVendor" -> "Individual",
            "vendorOrCompanyName" -> "John Doe",
            "vendorAddress" -> Json.obj(
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
          val request = FakeRequest(GET, controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must return OK and the correct view when UserAnswers contains valid data" in {

        val userAnswers = UserAnswers(
          id = "12345",
          returnId = None,
          storn = "TESTSTORN",
          data = Json.obj(
            "whoIsTheVendor" -> "Individual",
            "vendorOrCompanyName" -> "John Doe",
            "vendorAddress" -> Json.obj(
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

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[VendorCheckYourAnswersView]

          status(result) mustEqual OK
          contentAsString(result) must include("Check your answers")
        }
      }

      "must redirect to Journey Recovery when no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value
          
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "onSubmit" - {
      "must redirect to VendorOverview when all required data is present and valid" in {
        
        val fullReturn = completeFullReturn
        
        val createVendorRequest = CreateVendorRequest(
          stornId = "12345",
          returnResourceRef = "RRF-2024-001",
          name = "Samsung",
          addressLine1 = "Street 1",
          isRepresentedByAgent = "Yes"
        )

        val userAnswers = UserAnswers(
          id = "12345",
          storn = "TESTSTORN",
          returnId = Some("12313"),
          fullReturn = Some(fullReturn),
          data = Json.obj(
            "vendorCurrent" -> Json.obj(
              "vendorID" -> "VEN001",
              "whoIsTheVendor" -> "Individual",
              "vendorOrCompanyName" -> Json.obj(
                "forename1" -> "Jane",
                "forename2" -> "Elizabeth",
                "name" -> "Johnson",
              ),
              "vendorAddress" -> Json.obj(
                "houseNumber" -> "15",
                "line1" -> "Park Lane",
                "line2" -> "Mayfair",
                "line3" -> "London",
                "line4" -> JsNull,
                "line5" -> JsNull,
                "postcode" ->  "W1K 1LB",
                "country" -> Json.obj(
                  "code" -> "GB",
                  "name" -> "UK"
                ),
                "addressValidated" -> true
              ),
              "representedByAnAgent" -> "true"
            )),
          lastUpdated = Instant.now
        )

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        when(mockBackendConnector.createVendor(any())(any(), any())).thenReturn(Future.successful(CreateVendorReturn("VEN-REF-001","VEN001")))

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        when(mockVendorRequestService.convertToVendorRequest(any(), any(), any())).thenReturn(createVendorRequest)

        when(mockVendorCreateOrUpdateService.result(any(), any(), any(),
          any())(any(),any(),any())).thenReturn(Future.successful(Redirect(controllers.vendor.routes.VendorOverviewController.onPageLoad())))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .overrides(bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector))
          .overrides(bind[VendorRequestService].toInstance(mockVendorRequestService))
          .overrides(bind[VendorCreateOrUpdateService].toInstance(mockVendorCreateOrUpdateService))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.vendor.routes.VendorCheckYourAnswersController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.vendor.routes.VendorOverviewController.onPageLoad().url
        }
      }

      "must redirect back to JourneyRecoveryController when required data is missing or invalid" in {

        val incompleteData = Json.obj(
          "whoIsTheVendor" -> "Individual"
          // Missing other required fields
        )

        val userAnswers = UserAnswers(
          id = userAnswersId,
          storn = "TESTSTORN",
          data = incompleteData
        )

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        when(mockBackendConnector.createReturn(any())(any(), any())).thenReturn(Future.successful(CreateReturnResult("12345")))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .overrides(bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.vendor.routes.VendorCheckYourAnswersController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect back to VendorCheckYourAnswersController on JSError" in {

        val incompleteData = Json.obj(
          "whoIsTheVendor" -> "Individual"
          // Missing other required fields
        )

        val userAnswers = UserAnswers(
          id = userAnswersId,
          storn = "TESTSTORN",
          returnId = Some("12313"),
          data = incompleteData
        )

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        when(mockBackendConnector.createReturn(any())(any(), any())).thenReturn(Future.successful(CreateReturnResult("12345")))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .overrides(bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.vendor.routes.VendorCheckYourAnswersController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad().url
        }
      }

      "must redirect to JourneyRecoveryController when no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(POST, controllers.vendor.routes.VendorCheckYourAnswersController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

    }
  }
}
