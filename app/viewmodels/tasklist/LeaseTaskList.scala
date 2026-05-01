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

import javax.inject.Singleton

@Singleton
object LeaseTaskList {

  def build(fullReturn: FullReturn)
           (implicit messages: Messages,
            appConfig: FrontendAppConfig): TaskListSection =
    TaskListSection(
      heading = messages("tasklist.leaseQuestion.heading"),
      rows = Seq(
        buildLeaseRow(fullReturn)
      )
    )

  def buildLeaseRow(fullReturn: FullReturn)(implicit appConfig: FrontendAppConfig): TaskListSectionRow = {
    val url = fullReturn.lease match {
      case Some(lease) if lease.leaseType.isDefined => // TODO: DTR-3545 - change to LeaseCheckYourAnswersController once implemented
        controllers.routes.ReturnTaskListController.onPageLoad().url
      case _ =>
        controllers.lease.routes.LeaseBeforeYouStartController.onPageLoad().url
    }

    TaskListRowBuilder(
      canEdit = {
        case TLCompleted => true
        case _ => true
      },
      messageKey = _ => "tasklist.leaseQuestion.details",
      url = _ => _ => {
        url
      },
      tagId = "leaseQuestionDetailRow",
      checks = scheme => Seq(fullReturn.lease.exists(_.leaseType.isDefined)),
      prerequisites = _ => Seq(PrelimTaskList.buildPrelimRow(fullReturn))
    ).build(fullReturn)
  }

}
