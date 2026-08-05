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

package controllers.vendorAgent

import base.SpecBase
import constants.FullReturnConstants.completeFullReturn
import controllers.routes
import forms.vendorAgent.VendorAgentBeforeYouStartFormProvider
import models.{FullReturn, NormalMode, ReturnAgent, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.vendorAgent.VendorAgentBeforeYouStartPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.vendor.VendorCreateOrUpdateService
import views.html.vendorAgent.VendorAgentBeforeYouStartView

import scala.concurrent.Future

class VendorAgentBeforeYouStartControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider = new VendorAgentBeforeYouStartFormProvider()
  val form = formProvider()

  lazy val vendorAgentBeforeYouStartRoute: String =
    controllers.vendorAgent.routes.VendorAgentBeforeYouStartController.onPageLoad().url

  private val vendorAgent = ReturnAgent(
    returnAgentID = Some("AGENT003"),
    agentType = Some("VENDOR"),
    name = Some("Vendor Agent Ltd")
  )

  private val fullReturnNoAgent: FullReturn = completeFullReturn.copy(returnAgent = None, submission = None)
  private val fullReturnWithVendorAgent: FullReturn = completeFullReturn.copy(returnAgent = Some(Seq(vendorAgent)), submission = None)

  private val userAnswersNoAgent: UserAnswers =
    UserAnswers(userAnswersId, storn = "test-storn").copy(fullReturn = Some(fullReturnNoAgent))

  private val userAnswersWithVendorAgent: UserAnswers =
    UserAnswers(userAnswersId, storn = "test-storn").copy(fullReturn = Some(fullReturnWithVendorAgent))

  "VendorAgentBeforeYouStart Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersNoAgent)).build()

      running(application) {
        val request = FakeRequest(GET, vendorAgentBeforeYouStartRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[VendorAgentBeforeYouStartView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = userAnswersNoAgent.set(VendorAgentBeforeYouStartPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, vendorAgentBeforeYouStartRoute)

        val view = application.injector.instanceOf[VendorAgentBeforeYouStartView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, vendorAgentBeforeYouStartRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the next page when 'Yes' is selected and no existing vendor agent is found" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockVendorCreateOrUpdateService = mock[VendorCreateOrUpdateService]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockVendorCreateOrUpdateService.updateIsRepresentedByAgent(any(), any())(any(), any()))
        .thenReturn(Future.successful(Right(true)))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersNoAgent))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[VendorCreateOrUpdateService].toInstance(mockVendorCreateOrUpdateService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, vendorAgentBeforeYouStartRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to the Overview page when 'Yes' is selected and existing vendor agent is found" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockVendorCreateOrUpdateService = mock[VendorCreateOrUpdateService]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockVendorCreateOrUpdateService.updateIsRepresentedByAgent(any(), any())(any(), any()))
        .thenReturn(Future.successful(Right(true)))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithVendorAgent))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[VendorCreateOrUpdateService].toInstance(mockVendorCreateOrUpdateService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, vendorAgentBeforeYouStartRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.vendorAgent.routes.VendorAgentOverviewController.onPageLoad().url
      }
    }

    "must redirect to the update return version error page when the service reports a version bump failure" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockVendorCreateOrUpdateService = mock[VendorCreateOrUpdateService]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockVendorCreateOrUpdateService.updateIsRepresentedByAgent(any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(Redirect(controllers.routes.UpdateReturnVersionErrorController.onPageLoad()))))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersNoAgent))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[VendorCreateOrUpdateService].toInstance(mockVendorCreateOrUpdateService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, vendorAgentBeforeYouStartRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.UpdateReturnVersionErrorController.onPageLoad().url
      }
    }

    "must redirect to task list when 'No' is selected and no existing vendor agent is found" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockVendorCreateOrUpdateService = mock[VendorCreateOrUpdateService]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockVendorCreateOrUpdateService.updateIsRepresentedByAgent(any(), any())(any(), any()))
        .thenReturn(Future.successful(Right(true)))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersNoAgent))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[VendorCreateOrUpdateService].toInstance(mockVendorCreateOrUpdateService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, vendorAgentBeforeYouStartRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }

    "must redirect to task list when 'No' is selected and existing vendor agent is found" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockVendorCreateOrUpdateService = mock[VendorCreateOrUpdateService]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockVendorCreateOrUpdateService.updateIsRepresentedByAgent(any(), any())(any(), any()))
        .thenReturn(Future.successful(Right(true)))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithVendorAgent))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[VendorCreateOrUpdateService].toInstance(mockVendorCreateOrUpdateService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, vendorAgentBeforeYouStartRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersNoAgent)).build()

      running(application) {
        val request =
          FakeRequest(POST, vendorAgentBeforeYouStartRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[VendorAgentBeforeYouStartView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, vendorAgentBeforeYouStartRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}