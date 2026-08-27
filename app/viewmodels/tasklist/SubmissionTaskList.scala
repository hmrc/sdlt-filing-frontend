/*
 * Copyright 2025 HM Revenue & Customs
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

package viewmodels.tasklist

import config.FrontendAppConfig
import models.FullReturn
import play.api.i18n.Messages
import utils.LeaseHelper
import viewmodels.tasklist.TaskListSections.allComplete

object SubmissionTaskList {

  def build(fullReturn: FullReturn, precedingSections: Seq[TaskListSection])
           (implicit messages: Messages, appConfig: FrontendAppConfig): TaskListSection = {

    val readyToSubmit: Boolean = allComplete(precedingSections)

    TaskListSection(
      heading = messages("tasklist.submissionQuestion.heading"),
      rows = Seq(
        buildSubmissionRow(fullReturn, readyToSubmit)
      )
    )
  }

  def buildSubmissionRow(fullReturn: FullReturn, readyToSubmit: Boolean)
                        (implicit appConfig: FrontendAppConfig): TaskListSectionRow = {
    val url = fullReturn.submission match {
      case Some(submission) if submission.submissionID.isDefined =>
        controllers.submission.routes.SubmissionCompleteController.onPageLoad().url
      case _ =>
        controllers.submission.routes.SubmissionBeforeYouStartController.onPageLoad().url
    }

    TaskListRowBuilder(
      canEdit = _ => true,
      messageKey = _ => "tasklist.submissionQuestion.details",
      hint = _ => {
        if (!readyToSubmit)
          Some("tasklist.submissionQuestion.hint")
        else
          None
      },
      url = _ => _ => {
        url
      },
      tagId = "submissionQuestionDetailRow",
      checks = _ => Seq(
        fullReturn.submission.exists(_.submissionID.isDefined),
        fullReturn.submission.exists(_.submissionStatus.exists(_ != "STARTED"))
      ),
      prerequisites = _ => Seq.empty,
      canStart = _ => readyToSubmit
    ).build(fullReturn)
  }

  def isLeaseRequired(fullReturn: FullReturn): Boolean = {
    LeaseHelper.isLeaseType(fullReturn)
  }
}