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
import forms.purchaserAgent.AddPurchaserAgentReferenceNumberFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.purchaserAgent.{AddPurchaserAgentReferenceNumberPage, PurchaserAgentNamePage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.purchaserAgent.AddPurchaserAgentReferenceNumberView

import scala.concurrent.Future

class AddPurchaserAgentReferenceNumberControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  val mockSessionRepository: SessionRepository = mock[SessionRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository)
  }

  def onwardRoute = Call("GET", "/foo")

  lazy val addPurchaserAgentReferenceNumberRoute: String = controllers.purchaserAgent.routes.AddPurchaserAgentReferenceNumberController.onPageLoad(NormalMode).url

  val formProvider = new AddPurchaserAgentReferenceNumberFormProvider()
  val form = formProvider()

  val userAnswersNoAgentName: UserAnswers = UserAnswers(
    id = userAnswersId,
    storn = "ST0005"
  )

  val userAnswersWithAgentName: UserAnswers = UserAnswers(
    id = userAnswersId,
    storn = "ST0005"
  ).set(PurchaserAgentNamePage, "Bob the Agent").success.value

  "AddPurchaserAgentReferenceNumber Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithAgentName)).build()

      running(application) {
        val request = FakeRequest(GET, addPurchaserAgentReferenceNumberRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddPurchaserAgentReferenceNumberView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, agentName = "Bob the Agent", NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered 'yes'" in {

      val userAnswers = userAnswersWithAgentName.set(AddPurchaserAgentReferenceNumberPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addPurchaserAgentReferenceNumberRoute)

        val view = application.injector.instanceOf[AddPurchaserAgentReferenceNumberView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), agentName = "Bob the Agent", NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered 'no'" in {

      val userAnswers = userAnswersWithAgentName.set(AddPurchaserAgentReferenceNumberPage, false).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addPurchaserAgentReferenceNumberRoute)

        val view = application.injector.instanceOf[AddPurchaserAgentReferenceNumberView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(false), agentName = "Bob the Agent", NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to agent name page when no name exists in session" in {
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(userAnswersNoAgentName)).build()

      running(application) {
        val request = FakeRequest(GET, addPurchaserAgentReferenceNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.purchaserAgent.routes.PurchaserAgentNameController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to WhatIsTheAgentsReferencePage when valid data 'yes' is submitted" in {
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
          FakeRequest(POST, addPurchaserAgentReferenceNumberRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        //TODO - DTR-1829 - replace with proper route
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to IsAgentAuthorisedToHandleCorrespondencePage when valid data 'no' is submitted" in {
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
          FakeRequest(POST, addPurchaserAgentReferenceNumberRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        //TODO - DTR-1832 - replace with proper route
        redirectLocation(result).value mustEqual controllers.purchaserAgent.routes.PurchaserAgentAuthorisedController.onPageLoad(NormalMode).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithAgentName)).build()

      running(application) {
        val request =
          FakeRequest(POST, addPurchaserAgentReferenceNumberRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AddPurchaserAgentReferenceNumberView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, agentName = "Bob the Agent", NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, addPurchaserAgentReferenceNumberRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(POST, addPurchaserAgentReferenceNumberRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
