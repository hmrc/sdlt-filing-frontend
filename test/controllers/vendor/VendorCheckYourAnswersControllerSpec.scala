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
import constants.FullReturnConstants.*
import models.vendor.{CreateVendorReturn, UpdateVendorRequest, UpdateVendorReturn}
import models.{CreateReturnResult, ReturnVersionUpdateRequest, ReturnVersionUpdateReturn, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.libs.json.{JsNull, JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.govuk.SummaryListFluency

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

class VendorCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency with MockitoSugar with BeforeAndAfterEach {

  private val mockSessionRepository = mock[SessionRepository]
  private val mockBackendConnector = mock[StampDutyLandTaxConnector]

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val request: FakeRequest[_] = FakeRequest()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository)
  }

  private def vendorCurrentData(vendorId: Option[String] = None) = Json.obj(
    "vendorCurrent" -> Json.obj(
      "vendorID" -> Json.toJson(vendorId),
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
        "postcode" -> "W1K 1LB",
        "country" -> Json.obj(
          "code" -> "GB",
          "name" -> "UK"
        ),
        "addressValidated" -> true
      ),
      "representedByAnAgent" -> "true"
    )
  )

  "Check Your Answers Controller" - {

    "onPageLoad" - {

      "must redirect to ReturnTaskList when the UserAnswers data is empty" in {

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad().url)

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
          data = vendorCurrentData()
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
          returnId = Some("AB2346"),
          storn = "TESTSTORN",
          data = vendorCurrentData()
        )

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

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
      "must create vendor and redirect to VendorOverview when all required data is present and valid" in {

        val userAnswers = UserAnswers(
          id = "12345",
          storn = "TESTSTORN",
          returnId = Some("12313"),
          fullReturn = Some(incompleteFullReturn),
          data = vendorCurrentData()
        )

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))
        val createVendorReturn = CreateVendorReturn("VEN-REF-001","VEN001")
        when(mockBackendConnector.createVendor(any())(any(), any()))
          .thenReturn(Future.successful(createVendorReturn))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .overrides(bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.vendor.routes.VendorCheckYourAnswersController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.vendor.routes.VendorOverviewController.onPageLoad().url
          verify(mockBackendConnector, times(1)).createVendor(any())(any(), any())
          flash(result).get("vendorCreated") mustBe Some("Jane Elizabeth Johnson")
        }
      }

      "must update vendor and redirect to VendorOverview when all required data is present and valid for an existing vendor" in {

        val fullReturn = completeFullReturn

        val userAnswers = UserAnswers(
          id = "12345",
          storn = "TESTSTORN",
          returnId = Some("12313"),
          fullReturn = Some(fullReturn),
          data = vendorCurrentData(Some("VEN001"))
        )

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val returnVersionResponse = ReturnVersionUpdateReturn(newVersion = Some(2))
        val updateVendorReturn = UpdateVendorReturn(true)
        when(mockBackendConnector.updateReturnVersion(any[ReturnVersionUpdateRequest])(any(), any()))
          .thenReturn(Future.successful(returnVersionResponse))
        when(mockBackendConnector.updateVendor(any[UpdateVendorRequest])(any(), any()))
          .thenReturn(Future.successful(updateVendorReturn))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .overrides(bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.vendor.routes.VendorCheckYourAnswersController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.vendor.routes.VendorOverviewController.onPageLoad().url
          verify(mockBackendConnector, times(1)).updateReturnVersion(any())(any(), any())
          verify(mockBackendConnector, times(1)).updateVendor(any())(any(), any())
          flash(result).get("vendorUpdated") mustBe Some("Jane Elizabeth Johnson")
        }
      }

      "must redirect to overview when vendor and purchaser count exceeds maximum" in {
        val vendors = (1 to 50).map(i => mock[models.Vendor])
        val purchasers = (1 to 49).map(i => mock[models.Purchaser])

        val userAnswers = UserAnswers(
          id = "12345",
          storn = "TESTSTORN",
          returnId = Some("12313"),
          fullReturn = Some(incompleteFullReturn.copy(
            vendor = Some(vendors),
            purchaser = Some(purchasers)
          )),
          data = vendorCurrentData(None)
        )

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.vendor.routes.VendorCheckYourAnswersController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.vendor.routes.VendorOverviewController.onPageLoad().url
        }
      }

      "must redirect back to JourneyRecoveryController when returnId is missing" in {

        val incompleteData = Json.obj(
          "whoIsTheVendor" -> "Individual"
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
