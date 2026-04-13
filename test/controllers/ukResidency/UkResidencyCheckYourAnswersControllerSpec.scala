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

package controllers.ukResidency

import base.SpecBase
import connectors.StampDutyLandTaxConnector
import constants.FullReturnConstants.{completeFullReturn, completeLandAdditional, completeLandNonResidential}
import models.UserAnswers
import models.ukResidency.{CreateResidencyReturn, UpdateResidencyReturn}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.ukResidency.{CloseCompanyPage, CrownEmploymentReliefPage, NonUkResidentPurchaserPage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.govuk.SummaryListFluency

import scala.concurrent.{ExecutionContext, Future}

class UkResidencyCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency with MockitoSugar with BeforeAndAfterEach {

  private val mockSessionRepository = mock[SessionRepository]
  private val mockBackendConnector  = mock[StampDutyLandTaxConnector]

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val request: FakeRequest[_] = FakeRequest()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository)
    reset(mockBackendConnector)
  }

  private def ukResidencyData(nonUkResident: Boolean = true, closeCompany: Boolean = false, crownEmployment: Boolean = false) =
    Json.obj(
      "ukResidencyCurrent" -> Json.obj(
        "nonUkResidentPurchaser" -> nonUkResident,
        "closeCompany"           -> closeCompany,
        "crownEmploymentRelief"  -> crownEmployment
      )
    )

  "UkResidencyCheckYourAnswers Controller" - {

    "onPageLoad" - {

      "must return OK and the correct view when session has data" in {
        val userAnswers = UserAnswers(id = userAnswersId, storn = "TESTSTORN", returnId = Some("12345"), fullReturn = Some(completeFullReturn), data = ukResidencyData())

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.ukResidency.routes.UkResidencyCheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Check your answers")
        }
      }

      "must pre-populate from fullReturn.residency and render CYA when session data is empty but residency exists" in {
        val userAnswers = emptyUserAnswers.copy(returnId = Some("12345"), fullReturn = Some(completeFullReturn))

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.ukResidency.routes.UkResidencyCheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Check your answers")
        }
      }

      "must redirect to UkResidencyBeforeYouStart when session data is empty and no residency exists in fullReturn" in {
        val userAnswers = emptyUserAnswers.copy(returnId = Some("12345"), fullReturn = Some(completeFullReturn.copy(residency = None)))

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.ukResidency.routes.UkResidencyCheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.ukResidency.routes.UkResidencyBeforeYouStartController.onPageLoad().url
        }
      }

      "must redirect to ReturnTaskList when returnId is not set" in {
        val userAnswers = UserAnswers(id = userAnswersId, storn = "TESTSTORN", fullReturn = Some(completeFullReturn), data = ukResidencyData())

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.ukResidency.routes.UkResidencyCheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must redirect to JourneyRecovery when no user answers exist" in {
        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, controllers.ukResidency.routes.UkResidencyCheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must return OK when property type is Additional" in {
        val userAnswers = UserAnswers(id = userAnswersId, storn = "TESTSTORN", returnId = Some("12345"), fullReturn = Some(completeFullReturn.copy(land = Some(Seq(completeLandAdditional)))), data = ukResidencyData())

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.ukResidency.routes.UkResidencyCheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Check your answers")
        }
      }

      "must redirect to ReturnTaskList when property type is not residential or additional" in {
        val userAnswers = UserAnswers(id = userAnswersId, storn = "TESTSTORN", data = ukResidencyData())

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.ukResidency.routes.UkResidencyCheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must redirect to ReturnTaskList when property type is NonResidential" in {
        val userAnswers = UserAnswers(id = userAnswersId, storn = "TESTSTORN", fullReturn = Some(completeFullReturn.copy(land = Some(Seq(completeLandNonResidential)))), data = ukResidencyData())

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.ukResidency.routes.UkResidencyCheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must include crown employment relief row when non-UK resident is yes" in {
        val userAnswers = UserAnswers(id = userAnswersId, storn = "TESTSTORN", returnId = Some("12345"), fullReturn = Some(completeFullReturn), data = ukResidencyData(crownEmployment = true))
          .set(NonUkResidentPurchaserPage, true).success.value
          .set(CrownEmploymentReliefPage, true).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.ukResidency.routes.UkResidencyCheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Crown employment relief")
        }
      }

      "must not include crown employment relief row when non-UK resident is no" in {
        val userAnswers = UserAnswers(id = userAnswersId, storn = "TESTSTORN", returnId = Some("12345"), fullReturn = Some(completeFullReturn), data = ukResidencyData(nonUkResident = false))
          .set(NonUkResidentPurchaserPage, false).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.ukResidency.routes.UkResidencyCheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must not include "Crown employment relief"
        }
      }

      "must always include non-UK resident purchaser row" in {
        val userAnswers = UserAnswers(id = userAnswersId, storn = "TESTSTORN", returnId = Some("12345"), fullReturn = Some(completeFullReturn), data = ukResidencyData(nonUkResident = false))
          .set(NonUkResidentPurchaserPage, false).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.ukResidency.routes.UkResidencyCheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Are any of the purchasers non-UK residents?")
        }
      }

      "must include close company row when close company answer is present" in {
        val userAnswers = UserAnswers(id = userAnswersId, storn = "TESTSTORN", returnId = Some("12345"), fullReturn = Some(completeFullReturn), data = ukResidencyData(closeCompany = true))
          .set(CloseCompanyPage, true).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.ukResidency.routes.UkResidencyCheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("close company")
        }
      }

      "must not include close company row when close company page has not been answered" in {
        val dataWithoutCloseCompany = Json.obj("ukResidencyCurrent" -> Json.obj("nonUkResidentPurchaser" -> false))
        val userAnswers = UserAnswers(id = userAnswersId, storn = "TESTSTORN", returnId = Some("12345"), fullReturn = Some(completeFullReturn), data = dataWithoutCloseCompany)

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.ukResidency.routes.UkResidencyCheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must not include "close company"
        }
      }
    }

    "onSubmit" - {

      "must call createResidency and redirect to ReturnTaskList when no residencyID exists" in {
        val userAnswers = UserAnswers(
          id         = userAnswersId,
          storn      = "TESTSTORN",
          returnId   = Some("12345"),
          fullReturn = Some(completeFullReturn.copy(residency = None)),
          data       = ukResidencyData(nonUkResident = true, crownEmployment = true)
        ).set(NonUkResidentPurchaserPage, true).success.value
          .set(CrownEmploymentReliefPage, true).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))
        when(mockBackendConnector.createResidency(any())(any(), any()))
          .thenReturn(Future.successful(CreateResidencyReturn(residencyResourceRef = "RES-001", residencyId = "1")))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .overrides(bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.ukResidency.routes.UkResidencyCheckYourAnswersController.onSubmit().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
          verify(mockBackendConnector, times(1)).createResidency(any())(any(), any())
        }
      }

      "must call updateResidency and redirect to ReturnTaskList when residencyID already exists" in {
        val userAnswers = UserAnswers(
          id         = userAnswersId,
          storn      = "TESTSTORN",
          returnId   = Some("12345"),
          fullReturn = Some(completeFullReturn),
          data       = ukResidencyData(nonUkResident = true, crownEmployment = true)
        ).set(NonUkResidentPurchaserPage, true).success.value
          .set(CrownEmploymentReliefPage, true).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))
        when(mockBackendConnector.updateResidency(any())(any(), any()))
          .thenReturn(Future.successful(UpdateResidencyReturn(updated = true)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .overrides(bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.ukResidency.routes.UkResidencyCheckYourAnswersController.onSubmit().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
          verify(mockBackendConnector, times(1)).updateResidency(any())(any(), any())
        }
      }

      "must redirect back to UkResidencyCheckYourAnswers when createResidency returns empty residencyId" in {
        val userAnswers = UserAnswers(
          id         = userAnswersId,
          storn      = "TESTSTORN",
          returnId   = Some("12345"),
          fullReturn = Some(completeFullReturn.copy(residency = None)),
          data       = ukResidencyData()
        ).set(NonUkResidentPurchaserPage, true).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))
        when(mockBackendConnector.createResidency(any())(any(), any()))
          .thenReturn(Future.successful(CreateResidencyReturn(residencyResourceRef = "RES-001", residencyId = "")))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .overrides(bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.ukResidency.routes.UkResidencyCheckYourAnswersController.onSubmit().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.ukResidency.routes.UkResidencyCheckYourAnswersController.onPageLoad().url
        }
      }

      "must redirect back to UkResidencyCheckYourAnswers when updateResidency returns false" in {
        val userAnswers = UserAnswers(
          id         = userAnswersId,
          storn      = "TESTSTORN",
          returnId   = Some("12345"),
          fullReturn = Some(completeFullReturn),
          data       = ukResidencyData()
        ).set(NonUkResidentPurchaserPage, true).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))
        when(mockBackendConnector.updateResidency(any())(any(), any()))
          .thenReturn(Future.successful(UpdateResidencyReturn(updated = false)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .overrides(bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.ukResidency.routes.UkResidencyCheckYourAnswersController.onSubmit().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.ukResidency.routes.UkResidencyCheckYourAnswersController.onPageLoad().url
        }
      }

      "must redirect back to UkResidencyCheckYourAnswers when NonUkResidentPurchaserPage is not in session data" in {
        val userAnswers = UserAnswers(
          id         = userAnswersId,
          storn      = "TESTSTORN",
          returnId   = Some("12345"),
          fullReturn = Some(completeFullReturn.copy(residency = None)),
          data       = Json.obj()
        )

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.ukResidency.routes.UkResidencyCheckYourAnswersController.onSubmit().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.ukResidency.routes.UkResidencyCheckYourAnswersController.onPageLoad().url
        }
      }

      "must redirect to JourneyRecovery when returnId is not set" in {
        val userAnswers = UserAnswers(id = userAnswersId, storn = "TESTSTORN", data = ukResidencyData())

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.ukResidency.routes.UkResidencyCheckYourAnswersController.onSubmit().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to JourneyRecovery when no user answers exist" in {
        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(POST, controllers.ukResidency.routes.UkResidencyCheckYourAnswersController.onSubmit().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
