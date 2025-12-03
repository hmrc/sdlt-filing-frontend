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
import pages.vendor.{AgentNamePage, DoYouKnowYourAgentReferencePage}
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
  val agentName: Option[String] = userAnswersWithAgentSelectionKnown.get(AgentNamePage)

  val formProvider = new DoYouKnowYourAgentReferenceFormProvider()
  val form = formProvider()

  "DoYouKnowYourAgentReference Controller" - {
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
          contentAsString(result) mustEqual view(form, NormalMode, agentName)(request, messages(application)).toString
        }
      }
    }

       "must return OK and the correct view when non-vendor returnAgent exists and no vendor" in {

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
          contentAsString(result) mustEqual view(form, NormalMode, agentName)(request, messages(application)).toString
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
        contentAsString(result) mustEqual view(form.fill(DoYouKnowYourAgentReference.Yes), NormalMode, agentName)(request, customMessages(application, request)).toString

      }
    }

    "check your answers route" - {

      "must redirect to check your answers when returnAgent.agentType is VENDOR and main vendor is represented by an agent" in {

        val multipleReturnAgents = Seq(
          FullReturnConstants.completeReturnAgent.copy(agentType = Some("SOLICITOR")),
          FullReturnConstants.completeReturnAgent.copy(agentType = Some("VENDOR")),
          FullReturnConstants.completeReturnAgent.copy(agentType = Some(""))
        )

        val multipleVendors = Seq(
          FullReturnConstants.completeVendor.copy(vendorID = Some("VEN002"), isRepresentedByAgent = Some("false")),
          FullReturnConstants.completeVendor.copy(vendorID = Some("VEN001"), isRepresentedByAgent = Some("true")),
          FullReturnConstants.completeVendor.copy(vendorID = Some("VEN003"), isRepresentedByAgent = Some("false"))
        )

        val fullReturn = FullReturnConstants.completeFullReturn.copy(
          returnAgent = Some(multipleReturnAgents),
          vendor = Some(multipleVendors),
          returnInfo = Some(FullReturnConstants.completeReturnInfo.copy(mainVendorID = Some("VEN001")))
        )

        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn),
          data = Json.obj( "vendorCurrent" -> Json.obj("agentName" -> "test","doYouKnowYourAgentReference" -> "yes","representedByAgent" -> true))
          )


        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, doYouKnowYourAgentReferenceRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad().url
        }
      }

      "must redirect to check your answers with non-vendor returnAgent and main vendor is NOT represented by an agent" in {

        val multipleReturnAgents = Seq(
          FullReturnConstants.completeReturnAgent.copy(agentType = Some("SOLICITOR")),
          FullReturnConstants.completeReturnAgent.copy(agentType = Some("AGENT")),
          FullReturnConstants.completeReturnAgent.copy(agentType = Some(""))
        )

        val multipleVendors = Seq(
          FullReturnConstants.completeVendor.copy(vendorID = Some("VEN002"), isRepresentedByAgent = Some("false")),
          FullReturnConstants.completeVendor.copy(vendorID = Some("VEN001"), isRepresentedByAgent = Some("false")),
          FullReturnConstants.completeVendor.copy(vendorID = Some("VEN003"), isRepresentedByAgent = Some("true"))
        )

        val fullReturn = FullReturnConstants.completeFullReturn.copy(
          returnAgent = Some(multipleReturnAgents),
          vendor = Some(multipleVendors),
          returnInfo = Some(FullReturnConstants.completeReturnInfo.copy(mainVendorID = Some("VEN001")))
        )

        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn),
          data = Json.obj( "vendorCurrent" -> Json.obj("agentName" -> "test","doYouKnowYourAgentReference" -> "yes","representedByAgent" -> true))
        )

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, doYouKnowYourAgentReferenceRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad().url
        }
      }

      "must redirect to check your answers when returnAgent doesn't exist and main vendor is NOT represented by an agent" in {

        val multipleVendors = Seq(
          FullReturnConstants.completeVendor.copy(vendorID = Some("VEN002"), isRepresentedByAgent = Some("false")),
          FullReturnConstants.completeVendor.copy(vendorID = Some("VEN001"), isRepresentedByAgent = Some("false")),
          FullReturnConstants.completeVendor.copy(vendorID = Some("VEN003"), isRepresentedByAgent = Some("true"))
        )

        val fullReturn = FullReturnConstants.completeFullReturn.copy(
          returnAgent = None,
          vendor = Some(multipleVendors),
          returnInfo = Some(FullReturnConstants.completeReturnInfo.copy(mainVendorID = Some("VEN001")))
        )

        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn),
          data = Json.obj( "vendorCurrent" -> Json.obj("agentName" -> "test","doYouKnowYourAgentReference" -> "yes","representedByAgent" -> true))
        )
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, doYouKnowYourAgentReferenceRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad().url
        }
      }

    }

    "error route" - {

      "must redirect to error page when returnAgent VENDOR exists and main vendor is NOT represented by an agent" in {

        val multipleReturnAgents = Seq(
          FullReturnConstants.completeReturnAgent.copy(agentType = Some("SOLICITOR")),
          FullReturnConstants.completeReturnAgent.copy(agentType = Some("VENDOR")),
          FullReturnConstants.completeReturnAgent.copy(agentType = Some(""))
        )

        val multipleVendors = Seq(
          FullReturnConstants.completeVendor.copy(vendorID = Some("VEN002"), isRepresentedByAgent = Some("true")),
          FullReturnConstants.completeVendor.copy(vendorID = Some("VEN001"), isRepresentedByAgent = Some("false")),
          FullReturnConstants.completeVendor.copy(vendorID = Some("VEN003"), isRepresentedByAgent = Some("false"))
        )

        val fullReturn = FullReturnConstants.completeFullReturn.copy(
          returnAgent = Some(multipleReturnAgents),
          returnInfo = Some(FullReturnConstants.completeReturnInfo.copy(mainVendorID = Some("VEN001"))),
          vendor = Some(multipleVendors)
        )

        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn),
          data = Json.obj( "vendorCurrent" -> Json.obj("agentName" -> "test","doYouKnowYourAgentReference" -> "yes","representedByAgent" -> true))
        )

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, doYouKnowYourAgentReferenceRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.GenericErrorController.onPageLoad().url
        }
      }

      "must redirect to error page when no returnAgent exists and main vendor is represented by an agent" in {

        val multipleVendors = Seq(
          FullReturnConstants.completeVendor.copy(vendorID = Some("VEN002"), isRepresentedByAgent = Some("true")),
          FullReturnConstants.completeVendor.copy(vendorID = Some("VEN001"), isRepresentedByAgent = Some("true")),
          FullReturnConstants.completeVendor.copy(vendorID = Some("VEN003"), isRepresentedByAgent = Some("false"))
        )

        val fullReturn = FullReturnConstants.completeFullReturn.copy(
          returnAgent = None,
          returnInfo = Some(FullReturnConstants.completeReturnInfo.copy(mainVendorID = Some("VEN001"))),
          vendor = Some(multipleVendors)
        )

        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn),
          data = Json.obj( "vendorCurrent" -> Json.obj("agentName" -> "test", "doYouKnowYourAgentReference" -> "yes","representedByAgent" -> true))
        )
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, doYouKnowYourAgentReferenceRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.GenericErrorController.onPageLoad().url
        }
      }

      "must redirect to error page when non-vendor returnAgent exists and main vendor is represented by an agent" in {

        val multipleReturnAgents = Seq(
          FullReturnConstants.completeReturnAgent.copy(agentType = Some("SOLICITOR")),
          FullReturnConstants.completeReturnAgent.copy(agentType = Some("AGENT")),
          FullReturnConstants.completeReturnAgent.copy(agentType = Some(""))
        )

        val multipleVendors = Seq(
          FullReturnConstants.completeVendor.copy(vendorID = Some("VEN002"), isRepresentedByAgent = Some("true")),
          FullReturnConstants.completeVendor.copy(vendorID = Some("VEN001"), isRepresentedByAgent = Some("true")),
          FullReturnConstants.completeVendor.copy(vendorID = Some("VEN003"), isRepresentedByAgent = Some("false"))
        )

        val fullReturn = FullReturnConstants.completeFullReturn.copy(
          returnAgent = Some(multipleReturnAgents),
          returnInfo = Some(FullReturnConstants.completeReturnInfo.copy(mainVendorID = Some("VEN001"))),
          vendor = Some(multipleVendors)
        )

        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn),
          data = Json.obj( "vendorCurrent" -> Json.obj("agentName" -> "test", "doYouKnowYourAgentReference" -> "yes","representedByAgent" -> true))
        )
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, doYouKnowYourAgentReferenceRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.GenericErrorController.onPageLoad().url
        }
      }

      "must redirect to error page when VENDOR returnAgent exists and no vendor present" in {

        val multipleReturnAgents = Seq(
          FullReturnConstants.completeReturnAgent.copy(agentType = Some("SOLICITOR")),
          FullReturnConstants.completeReturnAgent.copy(agentType = Some("VENDOR")),
          FullReturnConstants.completeReturnAgent.copy(agentType = Some(""))
        )

        val fullReturn = FullReturnConstants.completeFullReturn.copy(
          returnAgent = Some(multipleReturnAgents),
          returnInfo = Some(FullReturnConstants.completeReturnInfo.copy(mainVendorID = Some("VEN001"))),
          vendor = None
        )

        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn),
          data = Json.obj( "vendorCurrent" -> Json.obj("agentName" -> "test", "doYouKnowYourAgentReference" -> "yes","representedByAgent" -> true))
        )
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, doYouKnowYourAgentReferenceRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.GenericErrorController.onPageLoad().url
        }
      }
    }

    "onSubmit" - {

    "must redirect to the next page when 'yes' is selected" in {

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
      //TODO update to CYA route
      "must redirect to check your answers when 'no' is selected" in {

      }

    "when invalid data is submitted" - {

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
          contentAsString(result) mustEqual view(boundForm, NormalMode, agentName)(request, messages(application)).toString
        }
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
}
