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
import constants.FullReturnConstants.{completeFullReturn, incompleteFullReturn}
import models.Submission
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.submission.ResubmitYourReturnView

class ResubmitYourReturnControllerSpec extends SpecBase {

  val testFullReturn = completeFullReturn.copy(submission = Some(Submission(None)))
  val testUserAnswers = emptyUserAnswers.copy(fullReturn = Some(testFullReturn))

  "ResubmitYourReturn Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(testUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.ResubmitYourReturnController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ResubmitYourReturnView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }

    "must redirect to the task list when no submission has started and the prerequisite sections are not complete" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(fullReturn = Some(incompleteFullReturn)))).build()

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.ResubmitYourReturnController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }

    "must return OK and not redirect when a submission already exists, even if the prerequisite sections are not complete" in {

      val alreadyStartedIncompleteReturn = incompleteFullReturn.copy(submission = Some(Submission(None)))
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(fullReturn = Some(alreadyStartedIncompleteReturn)))).build()

      running(application) {
        val request = FakeRequest(GET, controllers.submission.routes.ResubmitYourReturnController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }
  }
}
