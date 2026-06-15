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
import models.submission.WhoAreYouSubmittingFor
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import pages.submission.WhoAreYouSubmittingForPage
import views.html.submission.DeclarationView

class DeclarationControllerSpec extends SpecBase {

  lazy val declarationRoute = controllers.submission.routes.DeclarationController.onPageLoad().url

  "Declaration Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.set(WhoAreYouSubmittingForPage, WhoAreYouSubmittingFor.PurchaserAuthorised).success.value)).build()

      running(application) {

        val request = FakeRequest(GET, declarationRoute)

        val result = route(application, request).value

        status(result) mustEqual OK

      }
    }

    "must return JourneyRecoveryController when empty userAnswers for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, declarationRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

      }
    }

    "render 'purchaserAuthorised' content in GET method" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.set(WhoAreYouSubmittingForPage, WhoAreYouSubmittingFor.PurchaserAuthorised).success.value)).build()

      running(application) {
        val request = FakeRequest(GET, declarationRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DeclarationView]

        status(result) mustEqual OK

        view("purchaserAuthorised", NormalMode)(request, messages(application)).toString must include (messages(application)("submission.declaration.purchasers.authorised.bullet1"))
      }
    }

    "render 'purchaserApproved' content in GET method" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.set(WhoAreYouSubmittingForPage, WhoAreYouSubmittingFor.PurchaserApproved).success.value)).build()

      running(application) {
        val request = FakeRequest(GET, declarationRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DeclarationView]

        status(result) mustEqual OK
        view("purchaserApproved", NormalMode)(request, messages(application)).toString must include(messages(application)("submission.declaration.purchasers.approved.bullet1"))
      }
    }

    "render 'myself' content in GET method" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.set(WhoAreYouSubmittingForPage, WhoAreYouSubmittingFor.Myself).success.value)).build()

      running(application) {
        val request = FakeRequest(GET, declarationRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DeclarationView]

        status(result) mustEqual OK
        view("myself", NormalMode)(request, messages(application)).toString must include(messages(application)("submission.declaration.purchasers.myself.p1"))
      }
    }

    "must redirect to JourneyRecoveryController page when empty userAnswers for POST" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, declarationRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

      }
    }

    "must redirect to ds-4 page post selection of myself value" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.set(WhoAreYouSubmittingForPage, WhoAreYouSubmittingFor.Myself).success.value)).build()

      running(application) {
        val request =
          FakeRequest(POST, declarationRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.submission.routes.DeclarationController.onPageLoad().url //TODO Sprint17 make changes for ds-4 routing

      }
    }

  }
}
