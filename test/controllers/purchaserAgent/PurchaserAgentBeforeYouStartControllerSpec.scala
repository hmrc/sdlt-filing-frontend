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

package controllers.purchaserAgent

import base.SpecBase
import connectors.StampDutyLandTaxConnector
import constants.FullReturnConstants.{completeFullReturn, completePurchaser1, completeReturnAgent}
import controllers.routes
import forms.purchaserAgent.PurchaserAgentBeforeYouStartFormProvider
import models.purchaser.*
import models.{FullReturn, NormalMode, DeleteReturnAgentRequest, DeleteReturnAgentReturn, ReturnVersionUpdateRequest, ReturnVersionUpdateReturn, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.purchaserAgent.PurchaserAgentBeforeYouStartPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.purchaser.PurchaserCreateOrUpdateService
import views.html.purchaserAgent.PurchaserAgentBeforeYouStartView

import scala.concurrent.Future

class PurchaserAgentBeforeYouStartControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", controllers.purchaserAgent.routes.SelectPurchaserAgentController.onPageLoad(NormalMode).url)

  val formProvider = new PurchaserAgentBeforeYouStartFormProvider()
  val form = formProvider()

  lazy val purchaserAgentBeforeYouStartRoute = controllers.purchaserAgent.routes.PurchaserAgentBeforeYouStartController.onPageLoad(NormalMode).url

  "PurchaserAgentBeforeYouStart Controller" - {

    val fullReturn: FullReturn = completeFullReturn.copy(returnAgent = None, submission = None)

    val userAnswersWithIndividualPurchaser: UserAnswers =
      UserAnswers(userAnswersId, storn = "test-storn")
        .copy(fullReturn = Some(fullReturn))

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithIndividualPurchaser)).build()

      running(application) {
        val request = FakeRequest(GET, purchaserAgentBeforeYouStartRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PurchaserAgentBeforeYouStartView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = userAnswersWithIndividualPurchaser.set(PurchaserAgentBeforeYouStartPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, purchaserAgentBeforeYouStartRoute)

        val view = application.injector.instanceOf[PurchaserAgentBeforeYouStartView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when 'Yes' is selected and no existing purchaser agent is found" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockPurchaserCreateOrUpdateService = mock[PurchaserCreateOrUpdateService]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockPurchaserCreateOrUpdateService.updateIsRepresentedByAgent(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[PurchaserCreateOrUpdateService].toInstance(mockPurchaserCreateOrUpdateService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, purchaserAgentBeforeYouStartRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to the Overview page when 'Yes' is selected and existing purchaser agent is found" in {

      val userAnswers = emptyUserAnswers.copy(
        fullReturn = Some(completeFullReturn.copy(
          returnAgent = Some(Seq(completeReturnAgent)),
          submission = None
        )))

      val mockSessionRepository = mock[SessionRepository]
      val mockBackendConnector = mock[StampDutyLandTaxConnector]
      val mockPurchaserCreateOrUpdateService = mock[PurchaserCreateOrUpdateService]

      val returnVersionResponse = ReturnVersionUpdateReturn(newVersion = Some(2))
      val updatePurchaserReturn = UpdatePurchaserReturn(true)

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockBackendConnector.updateReturnVersion(any[ReturnVersionUpdateRequest])(any(), any()))
        .thenReturn(Future.successful(returnVersionResponse))
      when(mockBackendConnector.updatePurchaser(any[UpdatePurchaserRequest])(any(), any()))
        .thenReturn(Future.successful(updatePurchaserReturn))
      when(mockPurchaserCreateOrUpdateService.updateIsRepresentedByAgent(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, purchaserAgentBeforeYouStartRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.purchaserAgent.routes.PurchaserAgentOverviewController.onPageLoad().url
      }
    }

    "must redirect to the return task list page when 'No' is selected and purchaser agent is absent" in {

      val userAnswers = emptyUserAnswers.copy(
        fullReturn = Some(completeFullReturn.copy(
          purchaser = Some(Seq(completePurchaser1.copy(
            isRepresentedByAgent = None))),
          returnAgent = None,
          submission = None
        )))

      val returnVersionResponse = ReturnVersionUpdateReturn(newVersion = Some(2))
      val updatePurchaserReturn = UpdatePurchaserReturn(true)

      val mockSessionRepository = mock[SessionRepository]
      val mockBackendConnector = mock[StampDutyLandTaxConnector]
      val mockPurchaserCreateOrUpdateService = mock[PurchaserCreateOrUpdateService]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockBackendConnector.updateReturnVersion(any[ReturnVersionUpdateRequest])(any(), any()))
        .thenReturn(Future.successful(returnVersionResponse))
      when(mockBackendConnector.updatePurchaser(any[UpdatePurchaserRequest])(any(), any()))
        .thenReturn(Future.successful(updatePurchaserReturn))
      when(mockPurchaserCreateOrUpdateService.updateIsRepresentedByAgent(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, purchaserAgentBeforeYouStartRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }

    "must redirect to the return task list page when 'No' is selected and purchaser agent is present" in {

      val userAnswers = emptyUserAnswers.copy(
        fullReturn = Some(completeFullReturn.copy(
          purchaser = Some(Seq(completePurchaser1.copy(
            isRepresentedByAgent = None))),
          returnAgent = Some(Seq(completeReturnAgent)),
          submission = None
        )))

      val returnVersionResponse = ReturnVersionUpdateReturn(newVersion = Some(2))
      val updatePurchaserReturn = UpdatePurchaserReturn(true)

      val mockSessionRepository = mock[SessionRepository]
      val mockBackendConnector = mock[StampDutyLandTaxConnector]
      val mockPurchaserCreateOrUpdateService = mock[PurchaserCreateOrUpdateService]
      val deleteReturnAgentReturn = DeleteReturnAgentReturn(true)

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockBackendConnector.updateReturnVersion(any[ReturnVersionUpdateRequest])(any(), any()))
        .thenReturn(Future.successful(returnVersionResponse))
      when(mockBackendConnector.updatePurchaser(any[UpdatePurchaserRequest])(any(), any()))
        .thenReturn(Future.successful(updatePurchaserReturn))
      when(mockBackendConnector.deleteReturnAgent(any[DeleteReturnAgentRequest])(any(), any()))
        .thenReturn(Future.successful(deleteReturnAgentReturn))
      when(mockPurchaserCreateOrUpdateService.updateIsRepresentedByAgent(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[StampDutyLandTaxConnector].toInstance(mockBackendConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, purchaserAgentBeforeYouStartRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, purchaserAgentBeforeYouStartRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[PurchaserAgentBeforeYouStartView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, purchaserAgentBeforeYouStartRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, purchaserAgentBeforeYouStartRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
