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

package controllers.vendor

import base.SpecBase
import constants.FullReturnConstants
import controllers.routes
import forms.vendor.DoYouKnowYourAgentReferenceFormProvider
import models.prelimQuestions.TransactionType
import models.vendor.DoYouKnowYourAgentReference
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.vendor.{VendorOrBusinessNamePage, AgentNamePage, DoYouKnowYourAgentReferencePage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.vendor.DoYouKnowYourAgentReferenceView
import play.api.libs.json.Json
import play.api.i18n.{Messages, MessagesApi}
import play.api.Application

import scala.concurrent.Future

class DoYouKnowYourAgentReferenceControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val doYouKnowYourAgentReferenceRoute = controllers.vendor.routes.DoYouKnowYourAgentReferenceController.onPageLoad(NormalMode).url

  val userAnswersWithAgentSelectionKnown: UserAnswers = emptyUserAnswers.copy(
    data = Json.obj(
      "vendorCurrent" -> Json.obj(
        "whoIsTheVendor" -> "Business",
        "agentName" -> "test",
        "representedByAgent" -> false,
      )
    ))

  val userAnswersYesWithAgentSelectionKnown: UserAnswers = emptyUserAnswers.copy(
    data = Json.obj(
      "vendorCurrent" -> Json.obj(
        "whoIsTheVendor" -> "Business",
        "agentName" -> "test",
       "doYouKnowYourAgentReference" -> "yes",
  "representedByAgent" -> true,
      )
    ))

  val userAnswersWithAgentSelectionUnknown: UserAnswers = emptyUserAnswers.copy(
    data = Json.obj(
      "vendorCurrent" -> Json.obj(
        "whoIsTheVendor" -> "Business",
        "agentName" -> "test",
        "doYouKnowYourAgentReference" -> "no"
      )
    ))
  val formProvider = new DoYouKnowYourAgentReferenceFormProvider()
  val form = formProvider()

  "DoYouKnowYourAgentReference Controller" - {
    val agentsName: String = userAnswersWithAgentSelectionKnown.get(AgentNamePage).get

    def customMessages(app: Application, request: FakeRequest[_]): Messages = app.injector.instanceOf[MessagesApi].preferred(request)

    "onPageLoad" - {
      "when no existing data is found" - {

        "must redirect to Journey Recovery for a GET if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request = FakeRequest(GET, doYouKnowYourAgentReferenceRoute)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
          }
        }
      }
    }

    "must return OK and the correct view when non-vendor returnAgent exists and no vendor"  in {

      val multipleReturnAgents = Seq(
        FullReturnConstants.completeReturnAgent.copy(agentType = Some("SOLICITOR")),
        FullReturnConstants.completeReturnAgent.copy(agentType = Some("AGENT")),
        FullReturnConstants.completeReturnAgent.copy(agentType = Some(""))
      )
      val fullReturn = FullReturnConstants.completeFullReturn.copy(
        returnAgent = Some(multipleReturnAgents),
        vendor = None
      )
      val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn), data = Json.obj(
        "vendorCurrent" -> Json.obj(
          "agentName" -> "test",
          "representedByAgent" -> true,
        )
      )
      )

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, doYouKnowYourAgentReferenceRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DoYouKnowYourAgentReferenceView]

        status(result) mustEqual OK
        //def customMessages(app: Application, request: FakeRequest[_]): Messages = app.injector.instanceOf[MessagesApi].preferred(request)
        contentAsString(result) mustEqual view(form, NormalMode, "test")(request, messages(application)).toString
      }
    }

    "continue route" - {

      "must return OK and correct view when no returnAgent and no vendor" in {

        val fullReturnWithNonVendorAgent = FullReturnConstants.completeFullReturn.copy(
          returnAgent = None,
          vendor = None
        )
        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnWithNonVendorAgent),
          data = Json.obj(
            "vendorCurrent" -> Json.obj(
              "agentName" -> "test",
              "representedByAgent" -> true,

            )
          )
        )

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, doYouKnowYourAgentReferenceRoute)
          val result = route(application, request).value

          val view = application.injector.instanceOf[DoYouKnowYourAgentReferenceView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode, "test")(request, messages(application)).toString
        }
      }
      }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      // val userAnswers = userAnswersWithAgentSelectionKnown.set(DoYouKnowYourAgentReferencePage, DoYouKnowYourAgentReference.Yes).success.value

      val fullReturnWithNonVendorAgent = FullReturnConstants.completeFullReturn.copy(
        returnAgent = None,
        vendor = None
      )
      val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnWithNonVendorAgent),
        data = Json.obj(
          "vendorCurrent" -> Json.obj(
            "agentName" -> "test",
      "doYouKnowYourAgentReference" -> "yes",
            "representedByAgent" -> true,

          )
        )
      )

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, doYouKnowYourAgentReferenceRoute)

        val view = application.injector.instanceOf[DoYouKnowYourAgentReferenceView]

        val result = route(application, request).value
        implicit val messages: Messages =
          application.injector.instanceOf[MessagesApi].preferred(request)
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(DoYouKnowYourAgentReference.Yes), NormalMode, agentsName)(request, customMessages(application, request)).toString

      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val fullReturnWithNonVendorAgent = FullReturnConstants.completeFullReturn.copy(
        returnAgent = None,
        vendor = None
      )
      val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnWithNonVendorAgent),
        data = Json.obj(
          "vendorCurrent" -> Json.obj(
            "agentName" -> "test",

            "doYouKnowYourAgentReference" -> "yes",
            "representedByAgent" -> true


          )
        )
      )

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, doYouKnowYourAgentReferenceRoute)
            .withFormUrlEncodedBody(("value", DoYouKnowYourAgentReference.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual  SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val fullReturnWithNonVendorAgent = FullReturnConstants.completeFullReturn.copy(
        returnAgent = None,
        vendor = None
      )
      val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnWithNonVendorAgent),
        data = Json.obj(
          "vendorCurrent" -> Json.obj(
            "agentName" -> "test",

            "doYouKnowYourAgentReference" -> "yes",
            "representedByAgent" -> true
          )
        )
      )


      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, doYouKnowYourAgentReferenceRoute)
            .withFormUrlEncodedBody(("DoYouKnowYourAgentReference", "invalid value"), ("agentName", "test"))

        val boundForm = form.bind(Map("DoYouKnowYourAgentReference" -> "invalid value"))

        val view = application.injector.instanceOf[DoYouKnowYourAgentReferenceView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, "test")(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, doYouKnowYourAgentReferenceRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, doYouKnowYourAgentReferenceRoute)
            .withFormUrlEncodedBody(("value", DoYouKnowYourAgentReference.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
