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
import forms.purchaserAgent.PurchaserAgentAuthorisedFormProvider
import models.purchaserAgent.PurchaserAgentAuthorised
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.purchaserAgent.{PurchaserAgentAuthorisedPage, PurchaserAgentNamePage, PurchaserAgentsContactDetailsPage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.purchaserAgent.PurchaserAgentAuthorisedView

import scala.concurrent.Future

class PurchaserAgentAuthorisedControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val purchaserAgentAuthorisedRoute = controllers.purchaserAgent.routes.PurchaserAgentAuthorisedController.onPageLoad(NormalMode).url
  val mockSessionRepository: SessionRepository = mock[SessionRepository]
  val formProvider = new PurchaserAgentAuthorisedFormProvider()
  val form = formProvider()

  val userAnswersWithAgentName: UserAnswers = UserAnswers(
    id = userAnswersId,
    storn = "ST0005",
    data = Json.obj(
      PurchaserAgentsContactDetailsPage.toString -> Json.obj(
        "phoneNumber" -> "07564736483",
        "emailAddress" -> "test@test.com"
      )
    )
  ).set(PurchaserAgentNamePage, "Secret Agent").success.value

  val userAnswersNoAgentName: UserAnswers = UserAnswers(
    id = userAnswersId,
    storn = "ST0005"
  )

  "PurchaserAgentAuthorised Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithAgentName)).build()

      running(application) {
        val request = FakeRequest(GET, purchaserAgentAuthorisedRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PurchaserAgentAuthorisedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, agentName = "Secret Agent")(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = userAnswersWithAgentName.set(PurchaserAgentAuthorisedPage, PurchaserAgentAuthorised.Yes).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, purchaserAgentAuthorisedRoute)

        val view = application.injector.instanceOf[PurchaserAgentAuthorisedView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(PurchaserAgentAuthorised.values.head), NormalMode, agentName = "Secret Agent")(request, messages(application)).toString
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
          FakeRequest(POST, purchaserAgentAuthorisedRoute)
            .withFormUrlEncodedBody(("value", PurchaserAgentAuthorised.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to agent name page when no name exists in session" in {
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(userAnswersNoAgentName)).build()

      running(application) {
        val request = FakeRequest(GET, purchaserAgentAuthorisedRoute)

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
          FakeRequest(POST, purchaserAgentAuthorisedRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.purchaserAgent.routes.PurchaserAgentNameController.onPageLoad(NormalMode).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithAgentName)).build()

      running(application) {
        val request =
          FakeRequest(POST, purchaserAgentAuthorisedRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[PurchaserAgentAuthorisedView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, agentName = "Secret Agent")(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, purchaserAgentAuthorisedRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, purchaserAgentAuthorisedRoute)
            .withFormUrlEncodedBody(("value", PurchaserAgentAuthorised.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
