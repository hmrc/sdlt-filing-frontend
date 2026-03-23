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

package controllers.purchaserAgent

import base.SpecBase
import controllers.routes
import forms.purchaserAgent.PurchaserAgentsContactDetailsFormProvider
import models.purchaserAgent.PurchaserAgentsContactDetails
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.purchaserAgent.{AddContactDetailsForPurchaserAgentPage, PurchaserAgentNamePage, PurchaserAgentsContactDetailsPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.purchaserAgent.PurchaserAgentsContactDetailsView

import scala.concurrent.Future

class PurchaserAgentsContactDetailsControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  val mockSessionRepository: SessionRepository = mock[SessionRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository)
  }

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new PurchaserAgentsContactDetailsFormProvider()
  val form = formProvider()

  lazy val purchaserAgentsContactDetailsRoute: String = controllers.purchaserAgent.routes.PurchaserAgentsContactDetailsController.onPageLoad(NormalMode).url

  val userAnswersNoAgentName: UserAnswers = emptyUserAnswers

  val userAnswersWithAgentName: UserAnswers = userAnswersNoAgentName
    .set(PurchaserAgentNamePage, "Bob the Agent").success.value
    .set(AddContactDetailsForPurchaserAgentPage, true).success.value

  val userAnswersWithNoAddContactDetails: UserAnswers = userAnswersWithAgentName
    .set(AddContactDetailsForPurchaserAgentPage, false).success.value


  "PurchaserAgentsContactDetails Controller" - {

    "must return OK and the correct view for a GET" in {
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(userAnswersWithAgentName)).build()

      running(application) {
        val request = FakeRequest(GET, purchaserAgentsContactDetailsRoute)

        val view = application.injector.instanceOf[PurchaserAgentsContactDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, agentName = "Bob the Agent", NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to agent name page when no name exists in session" in {
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(userAnswersNoAgentName)).build()

      running(application) {
        val request = FakeRequest(GET, purchaserAgentsContactDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.purchaserAgent.routes.PurchaserAgentNameController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to check your answers when add agent contact details is 'no' for a GET" in {
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(userAnswersWithNoAddContactDetails)).build()

      running(application) {
        val request = FakeRequest(GET, purchaserAgentsContactDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.purchaserAgent.routes.PurchaserAgentCheckYourAnswersController.onPageLoad().url
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswersWithData = userAnswersWithAgentName
          .set(PurchaserAgentsContactDetailsPage, PurchaserAgentsContactDetails(
            Some("07564738493"), Some("test@test.com"))
          ).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithData)).build()

      running(application) {
        val request = FakeRequest(GET, purchaserAgentsContactDetailsRoute)

        val view = application.injector.instanceOf[PurchaserAgentsContactDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form.fill(
            PurchaserAgentsContactDetails(Some("07564738493"), Some("test@test.com"))),
            agentName = "Bob the Agent",
            NormalMode)
            (request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithAgentName))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, purchaserAgentsContactDetailsRoute)
            .withFormUrlEncodedBody(("phoneNumber", "07564758695"), ("emailAddress", "test@test.com"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to DoYouWantToAddReferenceForThisReturnPage when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithAgentName))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, purchaserAgentsContactDetailsRoute)
            .withFormUrlEncodedBody(
              ("phoneNumber", "07564758695"),
              ("emailAddress", "test@test.com")
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.purchaserAgent.routes.AddPurchaserAgentReferenceNumberController.onPageLoad(NormalMode).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithAgentName)).build()

      running(application) {
        val request =
          FakeRequest(POST, purchaserAgentsContactDetailsRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[PurchaserAgentsContactDetailsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, agentName = "Bob the Agent", NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, purchaserAgentsContactDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, purchaserAgentsContactDetailsRoute)
            .withFormUrlEncodedBody(("phoneNumber", "07564758695"), ("emailAddress", "test@test.com"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
