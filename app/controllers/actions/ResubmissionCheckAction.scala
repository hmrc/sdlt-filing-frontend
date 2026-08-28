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
import models.requests.DataRequest
import play.api.i18n.MessagesApi
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFilter, Result}
import viewmodels.submission.SubmissionState
import viewmodels.submission.SubmissionState.{AwaitingConfirmation, ReSubmit, SubmissionFailed, Submitted}
import viewmodels.tasklist.TaskListBuilder

import scala.concurrent.{ExecutionContext, Future}

class ResubmissionCheckAction @Inject()(
                                         messagesApi: MessagesApi,
                                         appConfig: FrontendAppConfig,
                                         taskListBuilder: TaskListBuilder
                                       )(implicit val executionContext: ExecutionContext) extends ActionFilter[DataRequest] {

  override protected def filter[A](request: DataRequest[A]): Future[Option[Result]] = {
    implicit val messages: play.api.i18n.Messages = messagesApi.preferred(request)
    implicit val implicitAppConfig: FrontendAppConfig = appConfig

    val submissionStatus = request.userAnswers.fullReturn.flatMap(_.submission).flatMap(_.submissionStatus)
    val submissionExists = request.userAnswers.fullReturn.flatMap(_.submission).isDefined
    val submissionState = SubmissionState.parse(submissionStatus)

    val allComplete: Boolean = taskListBuilder.allComplete(request.userAnswers)

    submissionState match {
      case Some(ReSubmit) =>
        Future.successful(None)

      case _ if submissionExists && submissionStatus.isEmpty =>
        Future.successful(None)

      case Some(AwaitingConfirmation) =>
        Future.successful(Some(Redirect(controllers.submission.routes.SubmissionAwaitingConfirmationController.onPageLoad())))

      case Some(Submitted) =>
        Future.successful(Some(Redirect(controllers.submission.routes.SubmissionCompleteController.onPageLoad())))

      case Some(SubmissionFailed) =>
        Future.successful(Some(Redirect(controllers.submission.routes.SubmissionFailedController.onPageLoad())))

      case _ if !allComplete =>
        Future.successful(Some(Redirect(controllers.routes.ReturnTaskListController.onPageLoad())))

      case _ =>
        Future.successful(None)
    }
  }
}