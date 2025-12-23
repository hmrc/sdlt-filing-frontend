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

package controllers

import base.SpecBase
import forms.NoReturnReferenceFormProvider
import models.NoReturnReference
//import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
//import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.NoReturnReferenceView
//import config.FrontendAppConfig
import play.api.mvc.Call

class NoReturnReferenceControllerSpec extends SpecBase with MockitoSugar {

  private lazy val noReturnReferenceRoute = routes.NoReturnReferenceController.onPageLoad().url
  private lazy val noReturnReferenceSubmitRoute = routes.NoReturnReferenceController.onSubmit().url

  def onwardRoute = Call("GET", "/foo")
  
  private val formProvider = new NoReturnReferenceFormProvider()
  private val form = formProvider()

  "NoReturnReference Controller" - {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, noReturnReferenceRoute)
        val result = route(application, request).value
        val view = application.injector.instanceOf[NoReturnReferenceView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form)(request, messages(application)).toString()
      }
    }

    "must redirect to the preliminary Before You Start page when FileNewReturn is selected" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, noReturnReferenceSubmitRoute)
          .withFormUrlEncodedBody(("value", NoReturnReference.FileNewReturn.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.preliminary.routes.BeforeStartReturnController.onPageLoad().url
      }
    }

    "must redirect to the Stamp Duty Land Tax Management service when ManageTaxes is selected" in {
//      val mockAppConfig = mock[FrontendAppConfig]
//      when(mockAppConfig.sdltManagementRedirectUrl) thenReturn onwardRoute.url
//
//      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
//        .overrides(bind[FrontendAppConfig].toInstance(mockAppConfig))
//        .build()

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, noReturnReferenceSubmitRoute)
          .withFormUrlEncodedBody(("value", NoReturnReference.ManageTaxes.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(POST, noReturnReferenceSubmitRoute)
          .withFormUrlEncodedBody(("value", NoReturnReference.FileNewReturn.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}