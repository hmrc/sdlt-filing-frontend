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
import constants.FullReturnConstants.completeFullReturn
import controllers.routes
import forms.vendorAgent.VendorAgentsReferenceFormProvider
import models.{NormalMode, ReturnInfo, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.vendorAgent.{AgentNamePage, VendorAgentsReferencePage}
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.vendorAgent.VendorAgentsReferenceView

import java.time.Instant
import scala.concurrent.Future

class VendorAgentsReferenceControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new VendorAgentsReferenceFormProvider()

  lazy val vendorAgentsReferenceRoute: String = controllers.vendorAgent.routes.VendorAgentsReferenceController.onPageLoad(NormalMode).url

  val userAnswersWithAgentReferenceKnown: UserAnswers =
    UserAnswers(
      userAnswersId,
      storn = "TESTSTORN",
      data = Json.obj(
        "vendorAgentCurrent" -> Json.obj(
          "vendorAgentName" -> "test",
          "vendorAgentsAddReference" -> "yes"
        )
        ),
      fullReturn = Some(completeFullReturn.copy(returnAgent = None))
    )

  val userAnswersWithAgentReferenceUnknown: UserAnswers =
    UserAnswers(
      userAnswersId, storn = "TESTSTORN",
      data = Json.obj(
        "vendorAgentCurrent" -> Json.obj(
          "vendorAgentName" -> "test",
          "vendorAgentsAddReference" -> "no"
        )),
      fullReturn = Some(completeFullReturn.copy(returnAgent = None, vendor = None))
    )

  val userAnswersWithoutAgentReferenceEmptyAnswer: UserAnswers =
    UserAnswers(
      userAnswersId,
      storn = "TESTSTORN",
      data = Json.obj(
        "vendorAgentCurrent" -> Json.obj(
          "vendorAgentName" -> "test",
        ),
      ),
      fullReturn = Some(completeFullReturn.copy(returnAgent = None, vendor = None))
    )

  val userAnswersWithoutAgentName: UserAnswers =
    UserAnswers(userAnswersId,
      storn = "TESTSTORN",
      data = Json.obj(
        "vendorCurrent" -> Json.obj(
          "whoIsTheVendor" -> "Company",
          "representedByAgent" -> true
        )),
      fullReturn = Some(completeFullReturn.copy(returnAgent = None, vendor = None))
    )

  val agentsName: String = userAnswersWithAgentReferenceKnown.get(AgentNamePage).get

  def customMessages(app: Application, request: FakeRequest[_]): Messages = app.injector.instanceOf[MessagesApi].preferred(request)

  "VendorAgentsReference Controller" - {

    "validating Get methods" - {
      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersWithAgentReferenceKnown)).build()

        running(application) {
          val request = FakeRequest(GET, vendorAgentsReferenceRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[VendorAgentsReferenceView]

          implicit val messages: Messages =
            application.injector.instanceOf[MessagesApi].preferred(request)

          val form = formProvider(agentsName)

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, agentsName, NormalMode)(request, customMessages(application, request)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = userAnswersWithAgentReferenceKnown.set(VendorAgentsReferencePage, "answer").success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, vendorAgentsReferenceRoute)

          val view = application.injector.instanceOf[VendorAgentsReferenceView]

          val result = route(application, request).value

          implicit val messages: Messages =
            application.injector.instanceOf[MessagesApi].preferred(request)

          val form = formProvider(agentsName)

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill("answer"), agentsName, NormalMode)(request, customMessages(application, request)).toString
        }
      }

      "must redirect to agent name page for a GET if no agent name is found" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersWithoutAgentName)).build()

        running(application) {
          val request = FakeRequest(GET, vendorAgentsReferenceRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.vendorAgent.routes.AgentNameController.onPageLoad(NormalMode).url
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, vendorAgentsReferenceRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to CYA page for a GET if no data existing for vendorAgentsAddReference" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersWithAgentReferenceUnknown)).build()

        running(application) {
          val request = FakeRequest(GET, vendorAgentsReferenceRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must redirect to CYA page for a GET if none data existing for doYouKnowYourAgentReference" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersWithoutAgentReferenceEmptyAnswer)).build()

        running(application) {
          val request = FakeRequest(GET, vendorAgentsReferenceRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.ReturnTaskListController.onPageLoad().url
        }
      }

    }


    "validating Post methods" - {
      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersWithAgentReferenceKnown)).build()

        running(application) {
          val request =
            FakeRequest(POST, vendorAgentsReferenceRoute)
              .withFormUrlEncodedBody(("agentReference", ""), ("agentName", "test"))

          implicit val messages: Messages =
            application.injector.instanceOf[MessagesApi].preferred(request)

          val form = formProvider(agentsName)

          val boundForm = form.bind(Map("agentReference" -> ""))

          val view = application.injector.instanceOf[VendorAgentsReferenceView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, agentsName, NormalMode)(request, customMessages(application, request)).toString
        }
      }

      "must redirect to the next page when user selects Yes" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithAgentReferenceKnown))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, vendorAgentsReferenceRoute)
              .withFormUrlEncodedBody(("agentReference", "reference"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must redirect to agent name page for a POST if no agent name is found" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersWithoutAgentName)).build()

        running(application) {
          val request =
            FakeRequest(POST, vendorAgentsReferenceRoute)
              .withFormUrlEncodedBody(("value", "answer"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.vendorAgent.routes.AgentNameController.onPageLoad(NormalMode).url
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, vendorAgentsReferenceRoute)
              .withFormUrlEncodedBody(("value", "answer"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
