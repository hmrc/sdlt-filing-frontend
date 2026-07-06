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
import constants.FullReturnConstants.{completeFullReturn, completeSubmissionErrorDetails}
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.test.FakeRequest
import play.api.inject.bind
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.submission.SubmissionFailedView


class SubmissionFailedControllerSpec extends SpecBase {

  private val fullReturnWithError = completeFullReturn.copy(
    submissionErrorDetails = Some(completeSubmissionErrorDetails.copy(
      errorMessage = Some("The STORN supplied either is not of the correct length or type (10 numeric).")
    )))

  private val fullReturnWithNoError = completeFullReturn.copy(
    submissionErrorDetails = Some(completeSubmissionErrorDetails.copy(
      errorMessage = None
    )))
      
  private val errorMessage = Some("The STORN supplied either is not of the correct length or type (10 numeric).")

  "SubmissionFailed Controller" - {

    "must return OK and the correct view for a GET when error message exists" in {
      val mockSessionRepository = mock[SessionRepository]

      val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnWithError))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.SubmissionFailedController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SubmissionFailedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(errorMessage)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when error message does not exist" in {
      val mockSessionRepository = mock[SessionRepository]

      val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnWithNoError))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.SubmissionFailedController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SubmissionFailedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(None)(request, messages(application)).toString
        contentAsString(result) mustNot include ("The STORN supplied either is not of the correct length or type (10 numeric).")
      }
    }
  }
}
