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

import com.google.inject.Inject
import config.FrontendAppConfig
import models.{GetReturnByRefRequest, UserAnswers}
import models.requests.DataRequest
import pages.submission.AwaitingSubmissionPage
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFilter, Request, Result}
import repositories.SessionRepository
import services.FullReturnService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import viewmodels.submission.SubmissionState
import viewmodels.submission.SubmissionState.*
import viewmodels.tasklist.TaskListBuilder

import scala.concurrent.{ExecutionContext, Future}

class ResubmissionCheckAction @Inject()(
                                         messagesApi: MessagesApi,
                                         appConfig: FrontendAppConfig,
                                         taskListBuilder: TaskListBuilder,
                                         fullReturnService: FullReturnService,
                                         sessionRepository: SessionRepository
                                       )(implicit val executionContext: ExecutionContext) extends ActionFilter[DataRequest] {

  override protected def filter[A](dataRequest: DataRequest[A]): Future[Option[Result]] = {
    implicit val messages: Messages = messagesApi.preferred(dataRequest)
    implicit val implicitAppConfig: FrontendAppConfig = appConfig
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(dataRequest, dataRequest.session)
    implicit val request: Request[_] = dataRequest

    val awaitingSubmission = dataRequest.userAnswers.get(AwaitingSubmissionPage).getOrElse(false)

    if (awaitingSubmission) {
      Future.successful(Some(Redirect(controllers.submission.routes.LoadingScreenController.show)))
    } else {
      refreshUserAnswers(dataRequest.userAnswers).map(checkStatus)
    }
  }

  private def refreshUserAnswers(userAnswers: UserAnswers)(implicit hc: HeaderCarrier, request: Request[_]): Future[UserAnswers] =
    userAnswers.returnId match {
      case Some(ref) =>
        (for {
          fullReturn <- fullReturnService.getFullReturn(GetReturnByRefRequest(returnResourceRef = ref, storn = userAnswers.storn))
          updated     = userAnswers.copy(fullReturn = Some(fullReturn))
          _          <- sessionRepository.set(updated)
        } yield updated)
          .recover { case _ => userAnswers }

      case None =>
        Future.successful(userAnswers)
    }

  private def checkStatus(userAnswers: UserAnswers)
                         (implicit messages: Messages, appConfig: FrontendAppConfig): Option[Result] = {
    val submission       = userAnswers.fullReturn.flatMap(_.submission)
    val submissionStatus = submission.flatMap(_.submissionStatus)
    val submissionExists = submission.isDefined
    val submissionState  = SubmissionState.parse(submissionStatus)
    val allComplete      = taskListBuilder.allComplete(userAnswers)

    submissionState match {
      case Some(ReSubmit) =>
        None

      case _ if submissionExists && submissionStatus.isEmpty =>
        None

      case Some(AwaitingConfirmation) =>
        Some(Redirect(controllers.submission.routes.SubmissionAwaitingConfirmationController.onPageLoad()))

      case Some(Submitted) | Some(SubmittedNoReceipt) =>
        Some(Redirect(controllers.submission.routes.SubmissionCompleteController.onPageLoad()))

      case Some(SubmissionFailed) =>
        Some(Redirect(controllers.submission.routes.SubmissionFailedController.onPageLoad()))

      case _ if !allComplete =>
        Some(Redirect(controllers.routes.ReturnTaskListController.onPageLoad()))

      case _ =>
        None
    }
  }
}