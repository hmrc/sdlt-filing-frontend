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
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.submission.AwaitingSubmissionPage
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.mvc.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.FullReturnService
import viewmodels.tasklist.TaskListBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class ResubmissionCheckActionSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  class Harness(messagesApi: MessagesApi, appConfig: FrontendAppConfig, fullReturnService: FullReturnService, taskListBuilder: TaskListBuilder, sessionRepository: SessionRepository)
    extends ResubmissionCheckAction(messagesApi, appConfig, taskListBuilder, fullReturnService, sessionRepository) {
    def callFilter[A](request: DataRequest[A]): Future[Option[Result]] = filter(request)
  }

  private val mockFullReturnService = mock[FullReturnService]
  private val mockSessionRepository = mock[SessionRepository]
  private val mockTaskListBuilder = mock[TaskListBuilder]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockFullReturnService)
    reset(mockSessionRepository)
    reset(mockTaskListBuilder)
  }

  "ResubmissionCheckAction" - {

    "must redirect to loading screen when awaiting submission flag is true" in {
      val userAnswers = emptyUserAnswers.copy(fullReturn = Some(completeFullReturn)).set(AwaitingSubmissionPage, true).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[FullReturnService].toInstance(mockFullReturnService),
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[TaskListBuilder].toInstance(mockTaskListBuilder)
        )
        .build()

      running(application) {
        val messagesApi = application.injector.instanceOf[MessagesApi]
        implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
        when(mockTaskListBuilder.allComplete(any[UserAnswers])(any[Messages], any[FrontendAppConfig])).thenReturn(true)

        val action = new Harness(messagesApi, appConfig, mockFullReturnService, mockTaskListBuilder, mockSessionRepository)
        val result = action.callFilter(DataRequest(FakeRequest(), "id", userAnswers = userAnswers)).futureValue
        val redirectResult = result.value

        redirectResult.header.status mustEqual SEE_OTHER
        redirectResult.header.headers("Location") mustEqual controllers.submission.routes.LoadingScreenController.show.url
      }
    }

    "must refresh fullReturn" in {
      val application = applicationBuilder()
        .overrides(
          bind[FullReturnService].toInstance(mockFullReturnService),
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[TaskListBuilder].toInstance(mockTaskListBuilder)
        )
        .build()

      val oldFullReturn = completeFullReturn.copy(submission = None)
      val newFullReturn = oldFullReturn.copy(submission = Some(completeSubmission.copy(submissionStatus = Some("STARTED"))))

      when(mockFullReturnService.getFullReturn(any())(any(), any())).thenReturn(Future.successful(newFullReturn))
      when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))
      when(mockTaskListBuilder.allComplete(any[UserAnswers])(any[Messages], any[FrontendAppConfig])).thenReturn(true)

      running(application) {
        val messagesApi = application.injector.instanceOf[MessagesApi]
        implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

        val action = new Harness(messagesApi, appConfig, mockFullReturnService, mockTaskListBuilder, mockSessionRepository)
        val oldUserAnswers = emptyUserAnswers.copy(fullReturn = Some(oldFullReturn), returnId = Some("12345"))
        val newUserAnswers = oldUserAnswers.copy(fullReturn = Some(newFullReturn))
        val result = action.callFilter(DataRequest(FakeRequest(), "id", userAnswers = oldUserAnswers)).futureValue

        result mustBe None
        val uaCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(uaCaptor.capture())
        uaCaptor.getValue mustBe newUserAnswers
      }
    }

    "must allow request to continue when submission status is STARTED" in {
      val application = applicationBuilder()
        .overrides(
          bind[FullReturnService].toInstance(mockFullReturnService),
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[TaskListBuilder].toInstance(mockTaskListBuilder)
        )
        .build()

      running(application) {
        val messagesApi = application.injector.instanceOf[MessagesApi]
        implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
        when(mockTaskListBuilder.allComplete(any[UserAnswers])(any[Messages], any[FrontendAppConfig])).thenReturn(true)

        val fullReturn = completeFullReturn.copy(
          submission = Some(completeSubmission.copy(
            submissionStatus = Some("STARTED")
          ))
        )
        
        val action = new Harness(messagesApi, appConfig, mockFullReturnService, mockTaskListBuilder, mockSessionRepository)
        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))
        val result = action.callFilter(DataRequest(FakeRequest(), "id", userAnswers = userAnswers)).futureValue

        result mustBe None
      }
    }

    "must allow request to continue when submission exists and submission status is empty" in {
      val application = applicationBuilder()
        .overrides(
        bind[FullReturnService].toInstance(mockFullReturnService),
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[TaskListBuilder].toInstance(mockTaskListBuilder)
      ).build()

      running(application) {
        val messagesApi = application.injector.instanceOf[MessagesApi]
        implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

        when(mockTaskListBuilder.allComplete(any[UserAnswers])(any[Messages], any[FrontendAppConfig])).thenReturn(true)

        val fullReturn = completeFullReturn.copy(
          submission = Some(completeSubmission.copy(
            submissionStatus = None
          ))
        )
        
        val action = new Harness(messagesApi, appConfig, mockFullReturnService, mockTaskListBuilder, mockSessionRepository)
        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturn))
        val result = action.callFilter(DataRequest(FakeRequest(), "id", userAnswers = userAnswers)).futureValue

        result mustBe None
      }
    }

    "must redirect to submission awaiting confirmation page when submission status is ACCEPTED" in {
      val application = applicationBuilder()
        .overrides(
          bind[FullReturnService].toInstance(mockFullReturnService),
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[TaskListBuilder].toInstance(mockTaskListBuilder)
        )
        .build()

      running(application) {
        val messagesApi = application.injector.instanceOf[MessagesApi]
        implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

        when(mockTaskListBuilder.allComplete(any[UserAnswers])(any[Messages], any[FrontendAppConfig])).thenReturn(true)

        val fullReturn = completeFullReturn.copy(
          submission = Some(completeSubmission.copy(
            submissionStatus = Some("ACCEPTED")
          ))
        )
        
        val action = new Harness(messagesApi, appConfig, mockFullReturnService, mockTaskListBuilder, mockSessionRepository)
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
      val application = applicationBuilder()
        .overrides(
          bind[FullReturnService].toInstance(mockFullReturnService),
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[TaskListBuilder].toInstance(mockTaskListBuilder)
        )
        .build()

      running(application) {
        val messagesApi = application.injector.instanceOf[MessagesApi]
        implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

        when(mockTaskListBuilder.allComplete(any[UserAnswers])(any[Messages], any[FrontendAppConfig])).thenReturn(true)

        val fullReturnWithSubmittedStatus = completeFullReturn.copy(
          submission = Some(completeSubmission.copy(
            submissionStatus = Some("SUBMITTED")
          ))
        )
        
        val action = new Harness(messagesApi, appConfig, mockFullReturnService, mockTaskListBuilder, mockSessionRepository)
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
      val application = applicationBuilder()
        .overrides(
          bind[FullReturnService].toInstance(mockFullReturnService),
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[TaskListBuilder].toInstance(mockTaskListBuilder)
        )
        .build()

      running(application) {
        val messagesApi = application.injector.instanceOf[MessagesApi]
        implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

        when(mockTaskListBuilder.allComplete(any[UserAnswers])(any[Messages], any[FrontendAppConfig])).thenReturn(true)

        val fullReturnWithSubmittedStatus = completeFullReturn.copy(
          submission = Some(completeSubmission.copy(
            submissionStatus = Some("SUBMITTED_NO_RECEIPT")
          ))
        )

        val action = new Harness(messagesApi, appConfig, mockFullReturnService, mockTaskListBuilder, mockSessionRepository)
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
      val application = applicationBuilder()
        .overrides(
          bind[FullReturnService].toInstance(mockFullReturnService),
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[TaskListBuilder].toInstance(mockTaskListBuilder)
        )
        .build()

      running(application) {
        val messagesApi = application.injector.instanceOf[MessagesApi]
        implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

        when(mockTaskListBuilder.allComplete(any[UserAnswers])(any[Messages], any[FrontendAppConfig])).thenReturn(true)

        val fullReturnWithSubmittedStatus = completeFullReturn.copy(
          submission = Some(completeSubmission.copy(
            submissionStatus = Some("DEPARTMENTAL_ERROR")
          ))
        )

        val action = new Harness(messagesApi, appConfig, mockFullReturnService, mockTaskListBuilder, mockSessionRepository)
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
      val application = applicationBuilder()
        .overrides(
          bind[FullReturnService].toInstance(mockFullReturnService),
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[TaskListBuilder].toInstance(mockTaskListBuilder)
        )
        .build()

      running(application) {
        val messagesApi = application.injector.instanceOf[MessagesApi]
        implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

        when(mockTaskListBuilder.allComplete(any[UserAnswers])(any[Messages], any[FrontendAppConfig])).thenReturn(true)

        val fullReturnWithSubmittedStatus = completeFullReturn.copy(
          submission = Some(completeSubmission.copy(
            submissionStatus = Some("FATAL_ERROR")
          ))
        )

        val action = new Harness(messagesApi, appConfig, mockFullReturnService, mockTaskListBuilder, mockSessionRepository)
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
      val application = applicationBuilder()
        .overrides(
          bind[FullReturnService].toInstance(mockFullReturnService),
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[TaskListBuilder].toInstance(mockTaskListBuilder)
        )
        .build()

      running(application) {
        val messagesApi = application.injector.instanceOf[MessagesApi]
        implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

        when(mockTaskListBuilder.allComplete(any[UserAnswers])(any[Messages], any[FrontendAppConfig])).thenReturn(true)

        val fullReturnWithSubmittedStatus = completeFullReturn.copy(
          submission = Some(completeSubmission.copy(
            submissionStatus = Some("BANANA")
          ))
        )

        val action = new Harness(messagesApi, appConfig, mockFullReturnService, mockTaskListBuilder, mockSessionRepository)
        val userAnswers = emptyUserAnswers.copy(fullReturn = Some(fullReturnWithSubmittedStatus))
        val result = action.callFilter(DataRequest(FakeRequest(), "id", userAnswers = userAnswers)).futureValue

        result mustBe None
      }
    }

    "must redirect to return task list when the return has errors, regardless of submission status" in {
      val application = applicationBuilder()
        .overrides(
          bind[FullReturnService].toInstance(mockFullReturnService),
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[TaskListBuilder].toInstance(mockTaskListBuilder)
        )
        .build()

      running(application) {
        val messagesApi = application.injector.instanceOf[MessagesApi]
        implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

        when(mockTaskListBuilder.allComplete(any[UserAnswers])(any[Messages], any[FrontendAppConfig])).thenReturn(false)

        val fullReturnIncomplete = completeFullReturn.copy(
          vendor = None,
          submission = Some(completeSubmission.copy(
            submissionStatus = Some("BANANA")
          ))
        )

        val action = new Harness(messagesApi, appConfig, mockFullReturnService, mockTaskListBuilder, mockSessionRepository)
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
      val application = applicationBuilder()
        .overrides(
          bind[FullReturnService].toInstance(mockFullReturnService),
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[TaskListBuilder].toInstance(mockTaskListBuilder)
        )
        .build()

      running(application) {
        val messagesApi = application.injector.instanceOf[MessagesApi]
        implicit val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
        when(mockTaskListBuilder.allComplete(any[UserAnswers])(any[Messages], any[FrontendAppConfig])).thenReturn(false)
        
        val action = new Harness(messagesApi, appConfig, mockFullReturnService, mockTaskListBuilder, mockSessionRepository)
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