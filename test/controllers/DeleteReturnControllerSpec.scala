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

package controllers

import base.SpecBase
import config.FrontendAppConfig
import connectors.StampDutyLandTaxConnector
import forms.DeleteReturnFormProvider
import models._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.bind
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.land.LandService
import services.purchaser.PurchaserService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.DeleteReturnView

import scala.concurrent.Future

class DeleteReturnControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new DeleteReturnFormProvider()
  val form = formProvider()

  private val testReturnId = "RRF-2024-001"
  private val testStorn    = "STORN123456"

  lazy val deleteReturnRoute       = controllers.routes.DeleteReturnController.onPageLoad().url
  lazy val deleteReturnSubmitRoute = controllers.routes.DeleteReturnController.onSubmit().url

  val baseAnswers           = emptyUserAnswers.copy(returnId = Some(testReturnId), storn = testStorn)
  val answersWithNoReturnId = emptyUserAnswers.copy(returnId = None, storn = testStorn)

  // fresh mocks per test so verify(...) never sees interactions from other tests
  private def setup(
                     userAnswers: Option[UserAnswers],
                     purchaser: Option[Purchaser] = None,
                     land: Option[Land] = None
                   ): (Application, StampDutyLandTaxConnector) = {

    val mockConnector        = mock[StampDutyLandTaxConnector]
    val mockPurchaserService = mock[PurchaserService]
    val mockLandService      = mock[LandService]

    when(mockPurchaserService.getMainPurchaser(any())).thenReturn(purchaser)
    when(mockLandService.getMainLand(any())).thenReturn(land)
    when(mockConnector.deleteReturn(any())(any[HeaderCarrier], any[Request[_]]))
      .thenReturn(Future.successful(DeleteReturnResponse(deleted = true)))

    val app =
      applicationBuilder(userAnswers = userAnswers)
        .overrides(
          bind[StampDutyLandTaxConnector].toInstance(mockConnector),
          bind[PurchaserService].toInstance(mockPurchaserService),
          bind[LandService].toInstance(mockLandService)
        )
        .build()

    (app, mockConnector)
  }

  "DeleteReturn Controller" - {

    "must return OK and the correct view for a GET" in {

      val (app, _) = setup(Some(baseAnswers))

      running(app) {
        val request = FakeRequest(GET, deleteReturnRoute)

        val view   = app.injector.instanceOf[DeleteReturnView]
        val result = route(app, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, None, None)(request, messages(app)).toString
      }
    }

    "must include the purchaser name and land address in the view when present" in {

      val (app, _) = setup(Some(baseAnswers), purchaser = Some(Purchaser(surname = Some("Smith"), forename1 = Some("John"))))

      running(app) {
        val request = FakeRequest(GET, deleteReturnRoute)
        val result  = route(app, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("Smith")
      }
    }

    "must delete the return and redirect to the SDLT management page when yes is submitted" in {

      val (app, mockConnector) = setup(Some(baseAnswers))
      val appConfig            = app.injector.instanceOf[FrontendAppConfig]

      running(app) {
        val request = FakeRequest(POST, deleteReturnSubmitRoute).withFormUrlEncodedBody(("value", "true"))
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual appConfig.sdltManagementRedirectUrl

        val captor: ArgumentCaptor[DeleteReturnRequest] = ArgumentCaptor.forClass(classOf[DeleteReturnRequest])
        verify(mockConnector).deleteReturn(captor.capture())(any[HeaderCarrier], any[Request[_]])
        captor.getValue mustEqual DeleteReturnRequest(storn = testStorn, returnResourceRef = testReturnId)
      }
    }

    "must not call the connector and redirect to the task list when no is submitted" in {

      val (app, mockConnector) = setup(Some(baseAnswers))

      running(app) {
        val request = FakeRequest(POST, deleteReturnSubmitRoute).withFormUrlEncodedBody(("value", "false"))
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        verify(mockConnector, never()).deleteReturn(any())(any[HeaderCarrier], any[Request[_]])
      }
    }

    "must redirect to NoReturnReference when yes is submitted but no returnId is present" in {

      val (app, mockConnector) = setup(Some(answersWithNoReturnId))

      running(app) {
        val request = FakeRequest(POST, deleteReturnSubmitRoute).withFormUrlEncodedBody(("value", "true"))
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.NoReturnReferenceController.onPageLoad().url
        verify(mockConnector, never()).deleteReturn(any())(any[HeaderCarrier], any[Request[_]])
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val (app, _) = setup(Some(baseAnswers))

      running(app) {
        val request   = FakeRequest(POST, deleteReturnSubmitRoute).withFormUrlEncodedBody(("value", "invalid value"))
        val boundForm = form.bind(Map("value" -> "invalid value"))
        val view      = app.injector.instanceOf[DeleteReturnView]
        val result    = route(app, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, None, None)(request, messages(app)).toString
      }
    }

    "must return a Bad Request and errors when no data is submitted" in {

      val (app, _) = setup(Some(baseAnswers))

      running(app) {
        val request   = FakeRequest(POST, deleteReturnSubmitRoute).withFormUrlEncodedBody(("value", ""))
        val boundForm = form.bind(Map("value" -> ""))
        val view      = app.injector.instanceOf[DeleteReturnView]
        val result    = route(app, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, None, None)(request, messages(app)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val (app, _) = setup(None)

      running(app) {
        val request = FakeRequest(GET, deleteReturnRoute)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val (app, _) = setup(None)

      running(app) {
        val request = FakeRequest(POST, deleteReturnSubmitRoute).withFormUrlEncodedBody(("value", "true"))
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}