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
import config.FrontendAppConfig
import constants.FullReturnConstants.{completeFullReturn, completeSubmission}
import models.UserAnswers
import models.requests.DataRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewmodels.tasklist.TaskListBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class ResubmissionCheckActionSpec extends SpecBase with MockitoSugar {

  class Harness(messagesApi: MessagesApi, appConfig: FrontendAppConfig, taskListBuilder: TaskListBuilder)
    extends ResubmissionCheckAction(messagesApi, appConfig, taskListBuilder) {
    def callFilter[A](request: DataRequest[A]): Future[Option[Result]] = filter(request)
  }

  "ResubmissionCheckAction" - {

    "must allow request to continue when submission status is STARTED" in {
      val application = applicationBuilder().build()

      running(application) {
        val messagesApi = application.injector.instanceOf[MessagesApi]
        implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
        val taskListBuilder = mock[TaskListBuilder]
        when(taskListBuilder.allComplete(any[UserAnswers])(any[Messages], any[FrontendAppConfig])).thenReturn(true)

        val fullReturn = completeFullReturn.copy(
          submission = Some(completeSubmission.copy(
            submissionStatus = Some("STARTED")
          ))
        )

        val action = new Harness(messagesApi, appConfig, taskListBuilder)
        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))
        val result = action.callFilter(DataRequest(FakeRequest(), "id", userAnswers = userAnswers)).futureValue

        result mustBe None
      }
    }

    "must allow request to continue when submission exists and submission status is empty" in {
      val application = applicationBuilder().build()

      running(application) {
        val messagesApi = application.injector.instanceOf[MessagesApi]
        implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
        val taskListBuilder = mock[TaskListBuilder]
        when(taskListBuilder.allComplete(any[UserAnswers])(any[Messages], any[FrontendAppConfig])).thenReturn(true)

        val fullReturn = completeFullReturn.copy(
          submission = Some(completeSubmission.copy(
            submissionStatus = None
          ))
        )

        val action = new Harness(messagesApi, appConfig, taskListBuilder)
        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))
        val result = action.callFilter(DataRequest(FakeRequest(), "id", userAnswers = userAnswers)).futureValue

        result mustBe None
      }
    }

    "must redirect to submission awaiting confirmation page when submission status is ACCEPTED" in {
      val application = applicationBuilder().build()

      running(application) {
        val messagesApi = application.injector.instanceOf[MessagesApi]
        implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
        val taskListBuilder = mock[TaskListBuilder]
        when(taskListBuilder.allComplete(any[UserAnswers])(any[Messages], any[FrontendAppConfig])).thenReturn(true)

        val fullReturn = completeFullReturn.copy(
          submission = Some(completeSubmission.copy(
            submissionStatus = Some("ACCEPTED")
          ))
        )

        val action = new Harness(messagesApi, appConfig, taskListBuilder)
        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))
        val result = action.callFilter(DataRequest(FakeRequest(), "id", userAnswers = userAnswers)).futureValue

        result mustBe defined
        val redirectResult = result.value

        redirectResult.header.status mustEqual SEE_OTHER

        redirectResult.header.headers("Location") mustEqual
          controllers.submission.routes.SubmissionAwaitingConfirmationController.onPageLoad().url
      }
    }

    "must redirect to submission complete page when submissionStatus is SUBMITTED" in {
      val application = applicationBuilder().build()

      running(application) {
        val messagesApi = application.injector.instanceOf[MessagesApi]
        implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
        val taskListBuilder = mock[TaskListBuilder]
        when(taskListBuilder.allComplete(any[UserAnswers])(any[Messages], any[FrontendAppConfig])).thenReturn(true)

        val fullReturnWithSubmittedStatus = completeFullReturn.copy(
          submission = Some(completeSubmission.copy(
            submissionStatus = Some("SUBMITTED")
          ))
        )
        val action = new Harness(messagesApi, appConfig, taskListBuilder)
        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnWithSubmittedStatus))
        val result = action.callFilter(DataRequest(FakeRequest(), "id", userAnswers = userAnswers)).futureValue

        result mustBe defined
        val redirectResult = result.value

        redirectResult.header.status mustEqual SEE_OTHER

        redirectResult.header.headers("Location") mustEqual
          controllers.submission.routes.SubmissionCompleteController.onPageLoad().url
      }
    }

    "must redirect to submission complete page when submissionStatus is SUBMITTED_NO_RECEIPT" in {
      val application = applicationBuilder().build()

      running(application) {
        val messagesApi = application.injector.instanceOf[MessagesApi]
        implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
        val taskListBuilder = mock[TaskListBuilder]
        when(taskListBuilder.allComplete(any[UserAnswers])(any[Messages], any[FrontendAppConfig])).thenReturn(true)

        val fullReturnWithSubmittedStatus = completeFullReturn.copy(
          submission = Some(completeSubmission.copy(
            submissionStatus = Some("SUBMITTED_NO_RECEIPT")
          ))
        )
        val action = new Harness(messagesApi, appConfig, taskListBuilder)
        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnWithSubmittedStatus))
        val result = action.callFilter(DataRequest(FakeRequest(), "id", userAnswers = userAnswers)).futureValue

        result mustBe defined
        val redirectResult = result.value

        redirectResult.header.status mustEqual SEE_OTHER

        redirectResult.header.headers("Location") mustEqual
          controllers.submission.routes.SubmissionCompleteController.onPageLoad().url
      }
    }

    "must redirect to submission failed page when submissionStatus is DEPARTMENTAL_ERROR" in {
      val application = applicationBuilder().build()

      running(application) {
        val messagesApi = application.injector.instanceOf[MessagesApi]
        implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
        val taskListBuilder = mock[TaskListBuilder]
        when(taskListBuilder.allComplete(any[UserAnswers])(any[Messages], any[FrontendAppConfig])).thenReturn(true)

        val fullReturnWithSubmittedStatus = completeFullReturn.copy(
          submission = Some(completeSubmission.copy(
            submissionStatus = Some("DEPARTMENTAL_ERROR")
          ))
        )
        val action = new Harness(messagesApi, appConfig, taskListBuilder)
        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnWithSubmittedStatus))
        val result = action.callFilter(DataRequest(FakeRequest(), "id", userAnswers = userAnswers)).futureValue

        result mustBe defined
        val redirectResult = result.value

        redirectResult.header.status mustEqual SEE_OTHER

        redirectResult.header.headers("Location") mustEqual
          controllers.submission.routes.SubmissionFailedController.onPageLoad().url
      }
    }

    "must redirect to submission failed page when submissionStatus is FATAL_ERROR" in {
      val application = applicationBuilder().build()

      running(application) {
        val messagesApi = application.injector.instanceOf[MessagesApi]
        implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
        val taskListBuilder = mock[TaskListBuilder]
        when(taskListBuilder.allComplete(any[UserAnswers])(any[Messages], any[FrontendAppConfig])).thenReturn(true)

        val fullReturnWithSubmittedStatus = completeFullReturn.copy(
          submission = Some(completeSubmission.copy(
            submissionStatus = Some("FATAL_ERROR")
          ))
        )
        val action = new Harness(messagesApi, appConfig, taskListBuilder)
        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnWithSubmittedStatus))
        val result = action.callFilter(DataRequest(FakeRequest(), "id", userAnswers = userAnswers)).futureValue

        result mustBe defined
        val redirectResult = result.value

        redirectResult.header.status mustEqual SEE_OTHER

        redirectResult.header.headers("Location") mustEqual
          controllers.submission.routes.SubmissionFailedController.onPageLoad().url
      }
    }

    "must allow user to resubmit when status does not match defined cases" in {
      val application = applicationBuilder().build()

      running(application) {
        val messagesApi = application.injector.instanceOf[MessagesApi]
        implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
        val taskListBuilder = mock[TaskListBuilder]
        when(taskListBuilder.allComplete(any[UserAnswers])(any[Messages], any[FrontendAppConfig])).thenReturn(true)

        val fullReturnWithSubmittedStatus = completeFullReturn.copy(
          submission = Some(completeSubmission.copy(
            submissionStatus = Some("BANANA")
          ))
        )
        val action = new Harness(messagesApi, appConfig, taskListBuilder)
        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnWithSubmittedStatus))
        val result = action.callFilter(DataRequest(FakeRequest(), "id", userAnswers = userAnswers)).futureValue

        result mustBe None
      }
    }

    "must redirect to return task list when the return has errors, regardless of submission status" in {
      val application = applicationBuilder().build()

      running(application) {
        val messagesApi = application.injector.instanceOf[MessagesApi]
        implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
        val taskListBuilder = mock[TaskListBuilder]
        when(taskListBuilder.allComplete(any[UserAnswers])(any[Messages], any[FrontendAppConfig])).thenReturn(false)

        val fullReturnIncomplete = completeFullReturn.copy(
          vendor = None,
          submission = Some(completeSubmission.copy(
            submissionStatus = Some("BANANA")
          ))
        )
        val action = new Harness(messagesApi, appConfig, taskListBuilder)
        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnIncomplete))
        val result = action.callFilter(DataRequest(FakeRequest(), "id", userAnswers = userAnswers)).futureValue

        result mustBe defined
        val redirectResult = result.value

        redirectResult.header.status mustEqual SEE_OTHER

        redirectResult.header.headers("Location") mustEqual
          controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }

    "must redirect to return task list when fullReturn is absent" in {
      val application = applicationBuilder().build()

      running(application) {
        val messagesApi = application.injector.instanceOf[MessagesApi]
        implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
        val taskListBuilder = mock[TaskListBuilder]
        when(taskListBuilder.allComplete(any[UserAnswers])(any[Messages], any[FrontendAppConfig])).thenReturn(false)

        val action = new Harness(messagesApi, appConfig, taskListBuilder)
        val userAnswers = emptyUserAnswers.copy(fullReturn = None)
        val result = action.callFilter(DataRequest(FakeRequest(), "id", userAnswers = userAnswers)).futureValue

        result mustBe defined
        val redirectResult = result.value

        redirectResult.header.status mustEqual SEE_OTHER

        redirectResult.header.headers("Location") mustEqual
          controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }
  }
}