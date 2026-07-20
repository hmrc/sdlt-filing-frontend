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

package controllers.actions

import base.SpecBase
import constants.FullReturnConstants.{completeFullReturn, completeSubmission}
import models.requests.DataRequest
import models.UserAnswers
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.*
import play.api.test.Helpers.*
import play.api.test.FakeRequest
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}



class CheckSubmissionStatusActionSpec extends SpecBase with MockitoSugar {

  class Harness extends CheckSubmissionStatusAction {
    def callFilter[A](request: DataRequest[A]): Future[Option[Result]] = filter(request)
  }

  "CheckSubmissionAction" - {

    "must allow request to continue when submission object does not exists" in {
      val fullReturnWithNoSubmission = completeFullReturn.copy(
        submission = None
      )

      val action = new Harness()
      val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnWithNoSubmission))
      val result = action.callFilter(DataRequest(FakeRequest(), "id", userAnswers = userAnswers)).futureValue

      result mustBe None
    }

    "must redirect to submission before you start submission exists but submission status is empty" in {
      val fullReturnWithNoSubmittedStatus = completeFullReturn.copy(
        submission = Some(completeSubmission.copy(
          submissionStatus = None
        ))
      )

      val action = new Harness()
      val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnWithNoSubmittedStatus))
      val result = action.callFilter(DataRequest(FakeRequest(), "id", userAnswers = userAnswers)).futureValue

      result mustBe defined
      val redirectResult = result.value

      redirectResult.header.status mustEqual SEE_OTHER

      redirectResult.header.headers("Location") mustEqual
        controllers.submission.routes.SubmissionBeforeYouStartController.onPageLoad().url
    }

    "must redirect to submission before you start when submission status is STARTED" in {
      val fullReturn = completeFullReturn.copy(
        submission = Some(completeSubmission.copy(
          submissionStatus = Some("STARTED")
        ))
      )

      val action = new Harness()
      val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))
      val result = action.callFilter(DataRequest(FakeRequest(), "id", userAnswers = userAnswers)).futureValue

      result mustBe defined
      val redirectResult = result.value

      redirectResult.header.status mustEqual SEE_OTHER

      redirectResult.header.headers("Location") mustEqual
        controllers.submission.routes.SubmissionBeforeYouStartController.onPageLoad().url
    }

    "must redirect to submission awaiting confirmation page when submission status is ACCEPTED" in {
      val fullReturn = completeFullReturn.copy(
        submission = Some(completeSubmission.copy(
          submissionStatus = Some("ACCEPTED")
        ))
      )

      val action = new Harness()
      val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))
      val result = action.callFilter(DataRequest(FakeRequest(), "id", userAnswers = userAnswers)).futureValue

      result mustBe defined
      val redirectResult = result.value

      redirectResult.header.status mustEqual SEE_OTHER

      redirectResult.header.headers("Location") mustEqual
        controllers.submission.routes.SubmissionAwaitingConfirmationController.onPageLoad().url
    }

    "must redirect to submission complete page when submissionStatus is SUBMITTED" in {
      val fullReturnWithSubmittedStatus = completeFullReturn.copy(
        submission = Some(completeSubmission.copy(
          submissionStatus = Some("SUBMITTED")
        ))
      )
      val action = new Harness()
      val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnWithSubmittedStatus))
      val result = action.callFilter(DataRequest(FakeRequest(), "id", userAnswers = userAnswers)).futureValue

      result mustBe defined
      val redirectResult = result.value

      redirectResult.header.status mustEqual SEE_OTHER

      redirectResult.header.headers("Location") mustEqual
        controllers.submission.routes.SubmissionCompleteController.onPageLoad().url
    }

    "must redirect to submission complete page when submissionStatus is SUBMITTED_NO_RECEIPT" in {
      val fullReturnWithSubmittedStatus = completeFullReturn.copy(
        submission = Some(completeSubmission.copy(
          submissionStatus = Some("SUBMITTED_NO_RECEIPT")
        ))
      )
      val action = new Harness()
      val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnWithSubmittedStatus))
      val result = action.callFilter(DataRequest(FakeRequest(), "id", userAnswers = userAnswers)).futureValue

      result mustBe defined
      val redirectResult = result.value

      redirectResult.header.status mustEqual SEE_OTHER

      redirectResult.header.headers("Location") mustEqual
        controllers.submission.routes.SubmissionCompleteController.onPageLoad().url
    }

    "must redirect to submission failed page when submissionStatus is DEPARTMENTAL_ERROR" in {
      val fullReturnWithSubmittedStatus = completeFullReturn.copy(
        submission = Some(completeSubmission.copy(
          submissionStatus = Some("DEPARTMENTAL_ERROR")
        ))
      )
      val action = new Harness()
      val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnWithSubmittedStatus))
      val result = action.callFilter(DataRequest(FakeRequest(), "id", userAnswers = userAnswers)).futureValue

      result mustBe defined
      val redirectResult = result.value

      redirectResult.header.status mustEqual SEE_OTHER

      redirectResult.header.headers("Location") mustEqual
        controllers.submission.routes.SubmissionFailedController.onPageLoad().url
    }

    "must redirect to submission failed page when submissionStatus is FATAL_ERROR" in {
      val fullReturnWithSubmittedStatus = completeFullReturn.copy(
        submission = Some(completeSubmission.copy(
          submissionStatus = Some("FATAL_ERROR")
        ))
      )
      val action = new Harness()
      val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnWithSubmittedStatus))
      val result = action.callFilter(DataRequest(FakeRequest(), "id", userAnswers = userAnswers)).futureValue

      result mustBe defined
      val redirectResult = result.value

      redirectResult.header.status mustEqual SEE_OTHER

      redirectResult.header.headers("Location") mustEqual
        controllers.submission.routes.SubmissionFailedController.onPageLoad().url
    }

    "must redirect to submission before you start when status does not match defined cases" in {
      val fullReturnWithSubmittedStatus = completeFullReturn.copy(
        submission = Some(completeSubmission.copy(
          submissionStatus = Some("BANANA")
        ))
      )
      val action = new Harness()
      val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnWithSubmittedStatus))
      val result = action.callFilter(DataRequest(FakeRequest(), "id", userAnswers = userAnswers)).futureValue

      result mustBe defined
      val redirectResult = result.value

      redirectResult.header.status mustEqual SEE_OTHER

      redirectResult.header.headers("Location") mustEqual
        controllers.submission.routes.SubmissionBeforeYouStartController.onPageLoad().url
    }
  }
}
