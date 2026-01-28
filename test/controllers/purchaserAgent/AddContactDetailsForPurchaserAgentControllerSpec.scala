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
import forms.purchaserAgent.AddContactDetailsForPurchaserAgentFormProvider
import models.purchaserAgent.PurchaserAgentsContactDetails
import models.{CheckMode, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.purchaserAgent.{AddContactDetailsForPurchaserAgentPage, PurchaserAgentNamePage, PurchaserAgentsContactDetailsPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.purchaserAgent.AddContactDetailsForPurchaserAgentView

import scala.concurrent.Future

class AddContactDetailsForPurchaserAgentControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new AddContactDetailsForPurchaserAgentFormProvider()
  val form = formProvider()

  lazy val addContactDetailsForPurchaserAgentRoute = controllers.purchaserAgent.routes.AddContactDetailsForPurchaserAgentController.onPageLoad(NormalMode).url

  val userAnswers = emptyUserAnswers.set(PurchaserAgentNamePage, "John Doe Agent").success.value


  "AddContactDetailsForPurchaserAgent Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addContactDetailsForPurchaserAgentRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddContactDetailsForPurchaserAgentView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, agentName = "John Doe Agent")(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswersWithData = userAnswers.set(AddContactDetailsForPurchaserAgentPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithData)).build()

      running(application) {
        val request = FakeRequest(GET, addContactDetailsForPurchaserAgentRoute)

        val view = application.injector.instanceOf[AddContactDetailsForPurchaserAgentView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, agentName = "John Doe Agent")(request, messages(application)).toString
      }
    }

    "must redirect to the next page when yes is chosen" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, addContactDetailsForPurchaserAgentRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to add purchaser agent reference number page when no is chosen" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, addContactDetailsForPurchaserAgentRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.purchaserAgent.routes.AddPurchaserAgentReferenceNumberController.onPageLoad(NormalMode).url
      }
    }

    "must clear contact details and redirect to next page when no is chosen in check mode" in {
      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswersWithContactDetails = userAnswers.set(PurchaserAgentsContactDetailsPage, PurchaserAgentsContactDetails(
        phoneNumber = Some("1234567890"),
        emailAddress = Some("test@email.com")
      )).success.value

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithContactDetails))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.purchaserAgent.routes.AddContactDetailsForPurchaserAgentController.onPageLoad(CheckMode).url)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
        val uaCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(uaCaptor.capture())
        uaCaptor.getValue.get(PurchaserAgentsContactDetailsPage).isDefined mustBe false
      }
    }

    "must redirect to next page when yes is chosen in check mode" in {
      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.purchaserAgent.routes.AddContactDetailsForPurchaserAgentController.onPageLoad(CheckMode).url)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to PurchaserAgentName page when PurchaserAgentName does not exist" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addContactDetailsForPurchaserAgentRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.purchaserAgent.routes.PurchaserAgentNameController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to PurchaserAgentName page on POST when PurchaserAgentName does not exist" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, addContactDetailsForPurchaserAgentRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.purchaserAgent.routes.PurchaserAgentNameController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to PurchaserAgentName page on POST when PurchaserAgentName does not exist (false value)" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, addContactDetailsForPurchaserAgentRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.purchaserAgent.routes.PurchaserAgentNameController.onPageLoad(NormalMode).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = emptyUserAnswers.set(PurchaserAgentNamePage, "John Doe Agent").success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, addContactDetailsForPurchaserAgentRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AddContactDetailsForPurchaserAgentView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, agentName = "John Doe Agent")(request, messages(application)).toString
      }
    }

    "must return a Bad Request and errors when no data is submitted" in {

      val userAnswers = emptyUserAnswers.set(PurchaserAgentNamePage, "John Doe Agent").success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, addContactDetailsForPurchaserAgentRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[AddContactDetailsForPurchaserAgentView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, agentName = "John Doe Agent")(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, addContactDetailsForPurchaserAgentRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, addContactDetailsForPurchaserAgentRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
