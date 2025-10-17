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
import models.{FullReturn, NormalMode}
import play.api.i18n.Messages

import javax.inject.Singleton

@Singleton
object PrelimTaskList {

  def build(fullReturn: FullReturn)
           (implicit messages: Messages,
            appConfig: FrontendAppConfig): TaskListSection =
    TaskListSection(
      heading = messages("tasklist.prelimQuestion.heading"),
      rows = Seq(
        buildPrelimRow(fullReturn)
      )
    )

  def buildPrelimRow(fullReturn: FullReturn)(implicit appConfig: FrontendAppConfig): TaskListSectionRow = {
    TaskListRowBuilder(
      messageKey = _ => "tasklist.prelimQuestion.details",
      url = _ => _ => {
        controllers.routes.PurchaserSurnameOrCompanyNameController.onPageLoad(NormalMode).url
      },
      tagId = "prelimQuestionDetailRow",
      checks = scheme => Seq(fullReturn.prelimReturn.isDefined),
      prerequisites = _ => Seq()
    ).build(fullReturn)
  }

}
