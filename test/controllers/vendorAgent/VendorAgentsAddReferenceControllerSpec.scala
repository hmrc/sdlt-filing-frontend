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

package controllers.vendorAgent

import base.SpecBase
import constants.FullReturnConstants
import controllers.routes
import forms.vendorAgent.VendorAgentsAddReferenceFormProvider
import models.vendorAgent.VendorAgentsAddReference
import models.{FullReturn, NormalMode, ReturnAgent, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.vendorAgent.VendorAgentsAddReferenceView

import scala.concurrent.Future

class VendorAgentsAddReferenceControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  lazy val vendorAgentsAddReferenceRouteGet: String = controllers.vendorAgent.routes.VendorAgentsAddReferenceController.onPageLoad(NormalMode).url
  lazy val vendorAgentsAddReferenceRoutePost: String = controllers.vendorAgent.routes.VendorAgentsAddReferenceController.onSubmit(NormalMode).url

  val fullReturnWithNonVendorAgent: FullReturn = FullReturnConstants.completeFullReturn.copy(returnAgent = None)
  val userAnswersWithName: UserAnswers = emptyUserAnswers.copy(
    data = Json.obj(
      "vendorAgentCurrent" -> Json.obj(
        "vendorAgentName" -> "test",
      )
    ),
    fullReturn = Some(fullReturnWithNonVendorAgent)
  )
  val userAnswersWithoutName: UserAnswers = emptyUserAnswers.copy(
    data = Json.obj(
      "vendorAgentCurrent" -> Json.obj()
    )
  )
  val fullReturnWithExistingVendorAgent: FullReturn =
    FullReturnConstants.completeFullReturn.copy(returnAgent = Some(Seq(ReturnAgent(agentType = Some("VENDOR")))))
  val userAnswersWithExistingVendorAgent: UserAnswers = userAnswersWithName.copy(fullReturn = Some(fullReturnWithExistingVendorAgent))

  val agentName = "test"
  val formProvider = new VendorAgentsAddReferenceFormProvider()
  val form: Form[VendorAgentsAddReference] = formProvider()
  val mockSessionRepository: SessionRepository = mock[SessionRepository]


  "VendorAgentsAddReference Controller" - {
    def customMessages(app: Application, request: FakeRequest[_]): Messages = app.injector.instanceOf[MessagesApi].preferred(request)

    "onPageLoad" - {
      "must return OK and correct view for a GET" in {
        val application = applicationBuilder(userAnswers = Some(userAnswersWithName)).build()

        running(application) {
          val request = FakeRequest(GET, vendorAgentsAddReferenceRouteGet)
          val result = route(application, request).value

          val view = application.injector.instanceOf[VendorAgentsAddReferenceView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode, agentName)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnWithNonVendorAgent),
          data = Json.obj(
            "vendorAgentCurrent" -> Json.obj(
              "vendorAgentName" -> "test",
              "vendorAgentsAddReference" -> "yes"
            )
          )
        )

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, vendorAgentsAddReferenceRouteGet)

          val view = application.injector.instanceOf[VendorAgentsAddReferenceView]

          val result = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view(form.fill(VendorAgentsAddReference.Yes), NormalMode, agentName)(request, customMessages(application, request)).toString

        }
      }
      
      "must redirect to Vendor Agent Overview for a GET when Vendor agent exists" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersWithExistingVendorAgent)).build()

        running(application) {
          val request = FakeRequest(GET, vendorAgentsAddReferenceRouteGet)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.vendorAgent.routes.VendorAgentOverviewController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, vendorAgentsAddReferenceRouteGet)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to agent name page for a GET if no agent name is found" in {
        val application = applicationBuilder(userAnswers = Some(userAnswersWithoutName)).build()

        running(application) {
          val request = FakeRequest(GET, vendorAgentsAddReferenceRouteGet)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.vendorAgent.routes.AgentNameController.onPageLoad(NormalMode).url
        }
      }
    }

    "onSubmit" - {
      "must redirect to the next page when 'yes' is selected" in {
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithName))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, vendorAgentsAddReferenceRoutePost)
              .withFormUrlEncodedBody(("value", VendorAgentsAddReference.Yes.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must redirect to check your answers when 'no' is selected" in {
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithName))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, vendorAgentsAddReferenceRoutePost)
              .withFormUrlEncodedBody(("value", VendorAgentsAddReference.No.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.vendorAgent.routes.VendorAgentCheckYourAnswersController.onPageLoad().url
        }
      }

      "must redirect to agent name page when agent name is missing" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersWithoutName)).build()

        running(application) {
          val request =
            FakeRequest(POST, vendorAgentsAddReferenceRoutePost)
              .withFormUrlEncodedBody(("value", VendorAgentsAddReference.Yes.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.vendorAgent.routes.AgentNameController.onPageLoad(NormalMode).url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {
        val application = applicationBuilder(userAnswers = Some(userAnswersWithName)).build()

        running(application) {
          val request =
            FakeRequest(POST, vendorAgentsAddReferenceRoutePost)
              .withFormUrlEncodedBody(("vendorAgentsAddReference", "invalid value"))

          val boundForm = form.bind(Map("vendorAgentsAddReference" -> "invalid value"))

          val view = application.injector.instanceOf[VendorAgentsAddReferenceView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode, agentName)(request, messages(application)).toString
        }
      }

      "redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, vendorAgentsAddReferenceRoutePost)
              .withFormUrlEncodedBody(("value", VendorAgentsAddReference.Yes.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
