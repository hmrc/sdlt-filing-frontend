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

package controllers.submission

import base.SpecBase
import models.NormalMode
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import org.scalatestplus.mockito.MockitoSugar
import views.html.submission.DeclarationView

class DeclarationControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val declarationRoute = controllers.submission.routes.DeclarationController.onPageLoad().url

  "Declaration Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, declarationRoute)

        val result = route(application, request).value

        status(result) mustEqual OK

      }
    }

    // TODO sprint 17 - DTR-5721 - route to DS-2

    /* "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, declarationRoute)

        val result = route(application, request).value

        status(result) mustEqual OK

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url


      }
    } */

    "render DS-1 content in GET method" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, declarationRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DeclarationView]

        status(result) mustEqual OK

        view("DS-1", NormalMode)(request, messages(application)).toString must include (messages(application)("declaration.purchasers.authorised.bullet1"))
      }
    }

    "render DS-2 content in GET method" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, declarationRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DeclarationView]

        status(result) mustEqual OK
        view("DS-2", NormalMode)(request, messages(application)).toString must include(messages(application)("declaration.purchasers.approved.bullet1"))
      }
    }

    "render DS-3 content in GET method" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, declarationRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DeclarationView]

        status(result) mustEqual OK
        view("DS-3", NormalMode)(request, messages(application)).toString must include(messages(application)("declaration.purchasers.myself.p1"))
      }
    }
    /* TODO Implement POST calls*/

    "must redirect to ds-4 page post selection of ds-2 value" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, declarationRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }

  }
}
