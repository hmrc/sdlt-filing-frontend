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
import models.UserAnswers
import models.submission.WhoAreYouSubmittingFor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify}
import org.scalatestplus.mockito.MockitoSugar
import pages.submission.WhoAreYouSubmittingForPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.submission.ChrisSubmissionService

class DeclarationControllerSpec extends SpecBase with MockitoSugar {

  lazy val declarationRoute: String = controllers.submission.routes.DeclarationController.onPageLoad().url
  lazy val submitRoute: String      = controllers.submission.routes.DeclarationController.onSubmit().url

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

        status(result) mustEqual OK

        contentAsString(result) must include(messages(application)("submission.declaration.purchasers.authorised.bullet1"))
      }
    }

    "render 'purchaserApproved' content in GET method" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.set(WhoAreYouSubmittingForPage, WhoAreYouSubmittingFor.PurchaserApproved).success.value)).build()

      running(application) {
        val request = FakeRequest(GET, declarationRoute)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include(messages(application)("submission.declaration.purchasers.approved.bullet1"))
      }
    }

    "render 'myself' content in GET method" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.set(WhoAreYouSubmittingForPage, WhoAreYouSubmittingFor.Myself).success.value)).build()

      running(application) {
        val request = FakeRequest(GET, declarationRoute)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include(messages(application)("submission.declaration.purchasers.myself.p1"))
      }
    }

    "must redirect to JourneyRecoveryController page when empty userAnswers for POST" in {

      val mockChrisSubmissionService = mock[ChrisSubmissionService]

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[ChrisSubmissionService].toInstance(mockChrisSubmissionService))
        .build()

      running(application) {
        val request = FakeRequest(POST, submitRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockChrisSubmissionService, never).submitInBackground(any[UserAnswers])(any(), any())
      }
    }

    "must submit in the background and redirect to the loading screen when WhoAreYouSubmittingFor is set for POST" in {

      val mockChrisSubmissionService = mock[ChrisSubmissionService]

      val answers = emptyUserAnswers.set(WhoAreYouSubmittingForPage, WhoAreYouSubmittingFor.Myself).success.value

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(bind[ChrisSubmissionService].toInstance(mockChrisSubmissionService))
        .build()

      running(application) {
        val request = FakeRequest(POST, submitRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.submission.routes.LoadingScreenController.show.url

        verify(mockChrisSubmissionService).submitInBackground(any[UserAnswers])(any(), any())
      }
    }
  }
}