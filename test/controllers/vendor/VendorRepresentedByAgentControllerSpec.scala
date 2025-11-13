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
import forms.vendor.VendorRepresentedByAgentFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.scalatestplus.mockito.MockitoSugar
import pages.vendor.{VendorOrBusinessNamePage, VendorRepresentedByAgentPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.vendor.VendorRepresentedByAgentView

import scala.concurrent.Future

class VendorRepresentedByAgentControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new VendorRepresentedByAgentFormProvider()
  val form = formProvider()

  lazy val vendorRepresentedByAgentRoute: String = controllers.vendor.routes.VendorRepresentedByAgentController.onPageLoad(NormalMode).url

  "VendorRepresentedByAgent Controller" - {

    "onPageLoad" - {
      "when no existing data is found" - {

        "must redirect to Journey Recovery for a GET if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request = FakeRequest(GET, vendorRepresentedByAgentRoute)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
          }
        }
      }

      "continue route" - {

        "must return OK and correct view when no returnAgent and no vendor" in {

          val fullReturnWithNonVendorAgent = FullReturnConstants.completeFullReturn.copy(
            returnAgent = None,
            vendor = None
          )
          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnWithNonVendorAgent))

          val vendorName: String = userAnswers
            .get(VendorOrBusinessNamePage)
            .map(_.name)
            .getOrElse("the vendor")

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, vendorRepresentedByAgentRoute)
            val result = route(application, request).value

            val view = application.injector.instanceOf[VendorRepresentedByAgentView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form, NormalMode, Some(vendorName))(request, messages(application)).toString
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

          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

          val vendorName: String = userAnswers
            .get(VendorOrBusinessNamePage)
            .map(_.name)
            .getOrElse("the vendor")

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, vendorRepresentedByAgentRoute)
            val result = route(application, request).value

            val view = application.injector.instanceOf[VendorRepresentedByAgentView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form, NormalMode, Some(vendorName))(request, messages(application)).toString
          }
        }

        "must populate the view correctly on a GET when the question has previously been answered" in {

          val multipleReturnAgents = Seq(
            FullReturnConstants.completeReturnAgent.copy(agentType = Some("SOLICITOR")),
            FullReturnConstants.completeReturnAgent.copy(agentType = Some("AGENT")),
            FullReturnConstants.completeReturnAgent.copy(agentType = Some(""))
          )

          val fullReturn = FullReturnConstants.completeFullReturn.copy(
            returnAgent = Some(multipleReturnAgents),
            vendor = None
          )

          val userAnswers = emptyUserAnswers
            .copy(fullReturn = Some(fullReturn))
            .set(VendorRepresentedByAgentPage, true).success.value

          val vendorName: String = userAnswers
            .get(VendorOrBusinessNamePage)
            .map(_.name)
            .getOrElse("the vendor")

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, vendorRepresentedByAgentRoute)
            val result = route(application, request).value

            val view = application.injector.instanceOf[VendorRepresentedByAgentView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form.fill(true), NormalMode, Some(vendorName))(request, messages(application)).toString
          }
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

          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, vendorRepresentedByAgentRoute)
            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.ReturnTaskListController.onPageLoad().url
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

          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, vendorRepresentedByAgentRoute)
            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.ReturnTaskListController.onPageLoad().url
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

          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, vendorRepresentedByAgentRoute)
            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.ReturnTaskListController.onPageLoad().url
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

          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, vendorRepresentedByAgentRoute)
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

          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, vendorRepresentedByAgentRoute)
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

          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, vendorRepresentedByAgentRoute)
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

          val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, vendorRepresentedByAgentRoute)
            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.GenericErrorController.onPageLoad().url
          }
        }
      }
    }

    "onSubmit" - {

        "when valid data is submitted" - {

          "must redirect to the next page when 'yes' is selected" in {
            val userAnswers = emptyUserAnswers
            val application = applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
              .build()

            running(application) {
              val request =
                FakeRequest(POST, vendorRepresentedByAgentRoute)
                  .withFormUrlEncodedBody(("value", "true"))

              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual onwardRoute.url
            }
          }

          //TODO update to CYA route
          "must redirect to check your answers when 'no' is selected" in {

          }
            val userAnswers = emptyUserAnswers
            val application = applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
              .build()

            running(application) {
              val request =
                FakeRequest(POST, vendorRepresentedByAgentRoute)
                  .withFormUrlEncodedBody(("value", "false"))

              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual routes.ReturnTaskListController.onPageLoad().url
            }
          }
        }

        "when invalid data is submitted" - {

          "must return a Bad Request and errors when invalid data is submitted" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

            running(application) {
              val request =
                FakeRequest(POST, vendorRepresentedByAgentRoute)
                  .withFormUrlEncodedBody(("value", ""))

              val boundForm = form.bind(Map("value" -> ""))

              val view = application.injector.instanceOf[VendorRepresentedByAgentView]

              val result = route(application, request).value

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual view(boundForm, NormalMode, Some("the vendor"))(request, messages(application)).toString
            }
          }
        }

        "when no existing data is found" - {

          "must redirect to Journey Recovery for a POST if no existing data is found" in {

            val application = applicationBuilder(userAnswers = None).build()

            running(application) {
              val request =
                FakeRequest(POST, vendorRepresentedByAgentRoute)
                  .withFormUrlEncodedBody(("value", "true"))

              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
            }
          }

        }
      }
  }
