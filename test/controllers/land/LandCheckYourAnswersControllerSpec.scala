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

package controllers.land

import base.SpecBase
import connectors.StampDutyLandTaxConnector
import constants.FullReturnConstants.incompleteFullReturn
import models.land.{CreateLandReturn, LandTypeOfProperty, UpdateLandRequest, UpdateLandReturn}
import models.{Land, ReturnInfo, ReturnVersionUpdateRequest, ReturnVersionUpdateReturn, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.land.*
import play.api.inject.bind
import play.api.libs.json.{JsNull, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.govuk.SummaryListFluency

import scala.concurrent.{ExecutionContext, Future}

class LandCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency with MockitoSugar with BeforeAndAfterEach {

  private val mockSessionRepository = mock[SessionRepository]
  private val mockBackendConnector = mock[StampDutyLandTaxConnector]

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val request: FakeRequest[_] = FakeRequest()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository)
    reset(mockBackendConnector)
  }

  private val completeLandFullReturn = incompleteFullReturn.copy(
    returnInfo = Some(ReturnInfo(version = Some("1"))),
    land = Some(Seq(Land(
      landID = Some("LAND001"),
      landResourceRef = Some("LAND-REF-001"),
      propertyType = Some("NonResidential"),
      interestCreatedTransferred = Some("Transfer"),
      address1 = Some("1 Test Street"),
      address2 = Some("Test Town"),
      houseNumber = None,
      address3 = None,
      address4 = None,
      postcode = Some("AB1 2CD"),
      landArea = None,
      areaUnit = None,
      localAuthorityNumber = Some("1234"),
      mineralRights = None,
      NLPGUPRN = None,
      willSendPlanByPost = None,
      titleNumber = None,
      nextLandID = None
    )))
  )

  private def landCurrentData(landId: Option[String] = None) = Json.obj(
    "landCurrent" -> Json.obj(
      "landId"                           -> Json.toJson(landId),
      "propertyType"                     -> "NonResidential",
      "landInterestTransferredOrCreated" -> "Transfer",
      "landAddress" -> Json.obj(
        "houseNumber"      -> JsNull,
        "line1"            -> "1 Test Street",
        "line2"            -> "Test Town",
        "line3"            -> JsNull,
        "line4"            -> JsNull,
        "line5"            -> JsNull,
        "postcode"         -> "AB1 2CD",
        "country" -> Json.obj(
          "code" -> "GB",
          "name" -> "UK"
        ),
        "addressValidated" -> true
      ),
      "localAuthorityCode"              -> "1234",
      "landSendingPlanByPost"           -> false,
      "landMineralsOrMineralRights"     -> false,
      "agriculturalOrDevelopmentalLand" -> false
    )
  )

  "LandCheckYourAnswers Controller" - {

    "onPageLoad" - {

      "must redirect to LandBeforeYouStart when the UserAnswers data is empty" in {

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.land.routes.LandBeforeYouStartController.onPageLoad().url
        }
      }

      "must redirect to LandBeforeYouStart when data is available but session is not" in {

        val userAnswers = UserAnswers(
          id = "12345",
          returnId = None,
          storn = "TESTSTORN",
          data = landCurrentData()
        )

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.land.routes.LandBeforeYouStartController.onPageLoad().url
        }
      }

      "must return OK and the correct view when UserAnswers contains valid data" in {

        val userAnswers = UserAnswers(
          id = "12345",
          returnId = Some("AB2346"),
          storn = "TESTSTORN",
          data = landCurrentData()
        )

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Check your answers")
        }
      }

      "must return OK and show agricultural row when property type is NonResidential and agricultural is true" in {

        val userAnswers = UserAnswers(
          id = "12345",
          returnId = Some("AB2346"),
          storn = "TESTSTORN",
          data = landCurrentData()
        ).set(LandTypeOfPropertyPage, LandTypeOfProperty.NonResidential).success.value
          .set(AgriculturalOrDevelopmentalLandPage, true).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must return OK and show know area row when property type is NonResidential, agricultural is true and knowArea is true" in {

        val userAnswers = UserAnswers(
          id = "12345",
          returnId = Some("AB2346"),
          storn = "TESTSTORN",
          data = landCurrentData()
        ).set(LandTypeOfPropertyPage, LandTypeOfProperty.NonResidential).success.value
          .set(AgriculturalOrDevelopmentalLandPage, true).success.value
          .set(DoYouKnowTheAreaOfLandPage, true).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Do you know the area of the land?")
          contentAsString (result) must include("Area of land")
        }
      }

      "must return OK and show title number row when HM Registry is true" in {

        val userAnswers = UserAnswers(
          id = "12345",
          returnId = Some("AB2346"),
          storn = "TESTSTORN",
          data = landCurrentData()
        ).set(LandRegisteredHmRegistryPage, true).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Is the land or property registered with HM Land Registry?")
          contentAsString(result) must include("Title number")
        }
      }

      "must return OK and show NLPG UPRN row when add NLPG is true" in {

        val userAnswers = UserAnswers(
          id = "12345",
          returnId = Some("AB2346"),
          storn = "TESTSTORN",
          data = landCurrentData()
        ).set(LandAddNlpgUprnPage, true).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Do you have an NLPG UPRN?")
          contentAsString(result) must include("NLPG UPRN")
        }
      }

      "must return OK and show Mixed property type rows" in {

        val userAnswers = UserAnswers(
          id = "12345",
          returnId = Some("AB2346"),
          storn = "TESTSTORN",
          data = landCurrentData()
        ).set(LandTypeOfPropertyPage, LandTypeOfProperty.Mixed).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Does the transaction involve agricultural or developmental land?")
        }
      }

      "must redirect to Journey Recovery when no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "onSubmit" - {

      "must create land and redirect to LandOverview when all required data is present and no land ID present" in {

        val userAnswers = UserAnswers(
          id = "test-session-id",
          storn = "test-storn",
          returnId = Some("12345"),
          fullReturn = Some(completeLandFullReturn),
          data = landCurrentData()
        )

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))
        when(mockBackendConnector.createLand(any())(any(), any()))
          .thenReturn(Future.successful(CreateLandReturn(landResourceRef = "LAND-REF-001", landId = "LAND001")))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .overrides(bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.land.routes.LandCheckYourAnswersController.onSubmit().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.land.routes.LandOverviewController.onPageLoad().url
          verify(mockBackendConnector, times(1)).createLand(any())(any(), any())
        }
      }

      "must update land and redirect to LandOverview when all required data is present and land ID present" in {

        val userAnswers = UserAnswers(
          id = "test-session-id",
          storn = "test-storn",
          returnId = Some("12345"),
          fullReturn = Some(completeLandFullReturn),
          data = landCurrentData(Some("LAND001"))
        ).set(LandOverviewPage, "LAND001").success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))
        when(mockBackendConnector.updateReturnVersion(any[ReturnVersionUpdateRequest])(any(), any()))
          .thenReturn(Future.successful(ReturnVersionUpdateReturn(newVersion = Some(2))))
        when(mockBackendConnector.updateLand(any[UpdateLandRequest])(any(), any()))
          .thenReturn(Future.successful(UpdateLandReturn(updated = true)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .overrides(bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.land.routes.LandCheckYourAnswersController.onSubmit().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.land.routes.LandOverviewController.onPageLoad().url
          verify(mockBackendConnector, times(1)).updateReturnVersion(any())(any(), any())
          verify(mockBackendConnector, times(1)).updateLand(any())(any(), any())
        }
      }

      "must redirect back to LandCheckYourAnswers when update returns false" in {

        val userAnswers = UserAnswers(
          id = "test-session-id",
          storn = "test-storn",
          returnId = Some("12345"),
          fullReturn = Some(completeLandFullReturn),
          data = landCurrentData(Some("LAND001"))
        ).set(LandOverviewPage, "LAND001").success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))
        when(mockBackendConnector.updateReturnVersion(any[ReturnVersionUpdateRequest])(any(), any()))
          .thenReturn(Future.successful(ReturnVersionUpdateReturn(newVersion = Some(2))))
        when(mockBackendConnector.updateLand(any[UpdateLandRequest])(any(), any()))
          .thenReturn(Future.successful(UpdateLandReturn(updated = false)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .overrides(bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.land.routes.LandCheckYourAnswersController.onSubmit().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url
        }
      }

      "must redirect back to LandCheckYourAnswers when create returns empty landId" in {

        val userAnswers = UserAnswers(
          id = "test-session-id",
          storn = "test-storn",
          returnId = Some("12345"),
          fullReturn = Some(completeLandFullReturn),
          data = landCurrentData()
        )

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))
        when(mockBackendConnector.createLand(any())(any(), any()))
          .thenReturn(Future.successful(CreateLandReturn(landResourceRef = "LAND-REF-001", landId = "")))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .overrides(bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.land.routes.LandCheckYourAnswersController.onSubmit().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url
        }
      }

      "must redirect back to LandCheckYourAnswers when title number validation fails" in {

        val userAnswers = UserAnswers(
          id = "test-session-id",
          storn = "test-storn",
          returnId = Some("12345"),
          fullReturn = Some(completeLandFullReturn),
          data = landCurrentData()
        ).set(LandRegisteredHmRegistryPage, true).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.land.routes.LandCheckYourAnswersController.onSubmit().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url
        }
      }

      "must redirect back to LandCheckYourAnswers when NLPG UPRN validation fails" in {

        val userAnswers = UserAnswers(
          id = "test-session-id",
          storn = "test-storn",
          returnId = Some("12345"),
          fullReturn = Some(completeLandFullReturn),
          data = landCurrentData()
        ).set(LandAddNlpgUprnPage, true).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.land.routes.LandCheckYourAnswersController.onSubmit().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url
        }
      }

      "must redirect back to LandCheckYourAnswers when agricultural validation fails" in {

        val userAnswers = UserAnswers(
          id = "test-session-id",
          storn = "test-storn",
          returnId = Some("12345"),
          fullReturn = Some(completeLandFullReturn),
          data = landCurrentData()
        ).set(AgriculturalOrDevelopmentalLandPage, true).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.land.routes.LandCheckYourAnswersController.onSubmit().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url
        }
      }

      "must redirect back to LandCheckYourAnswers when area of land validation fails" in {

        val userAnswers = UserAnswers(
          id = "test-session-id",
          storn = "test-storn",
          returnId = Some("12345"),
          fullReturn = Some(completeLandFullReturn),
          data = landCurrentData()
        ).set(LandTypeOfPropertyPage, LandTypeOfProperty.NonResidential).success.value
          .set(AgriculturalOrDevelopmentalLandPage, true).success.value
          .set(DoYouKnowTheAreaOfLandPage, true).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.land.routes.LandCheckYourAnswersController.onSubmit().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url
        }
      }

      "must redirect back to LandCheckYourAnswers on JsError" in {

        val userAnswers = UserAnswers(
          id = "test-session-id",
          storn = "test-storn",
          returnId = Some("12345"),
          data = Json.obj("landCurrent" -> "invalid")
        )

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.land.routes.LandCheckYourAnswersController.onSubmit().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.land.routes.LandCheckYourAnswersController.onPageLoad().url
        }
      }

      "must redirect to JourneyRecovery when no session data is found on submit" in {

        val userAnswers = UserAnswers(
          id = "test-session-id",
          storn = "test-storn",
          returnId = Some("12345"),
          data = landCurrentData()
        )

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.land.routes.LandCheckYourAnswersController.onSubmit().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to JourneyRecovery when no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(POST, controllers.land.routes.LandCheckYourAnswersController.onSubmit().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}