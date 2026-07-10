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
object UkResidencyTaskList {

  def build(fullReturn: FullReturn)
           (implicit messages: Messages,
            appConfig: FrontendAppConfig): TaskListSection =
    TaskListSection(
      heading = messages("tasklist.ukResidencyQuestion.heading"),
      rows = Seq(
        buildUkResidencyRow(fullReturn)
      )
    )

  def isResidencyComplete(fullReturn: FullReturn): Boolean = {
    //TODO ADD ALL REQUIRED FIELDS FOR UK RESIDENCY
    fullReturn.residency.exists(res =>
      res.isNonUkResidents.isDefined &&
        res.isCloseCompany.isDefined &&
        res.isCrownRelief.isDefined
    )
  }
  
  def ukResidencyRowBuilder(fullReturn: FullReturn)
                                 (implicit appConfig: FrontendAppConfig): TaskListRowBuilder = {

    val residency = fullReturn.residency

    val url =
      residency match {
        case Some(residency) if residency.isNonUkResidents.isDefined =>
            controllers.ukResidency.routes.UkResidencyCheckYourAnswersController.onPageLoad().url
        case _ =>
          controllers.ukResidency.routes.UkResidencyBeforeYouStartController.onPageLoad().url
      }

    TaskListRowBuilder(
      canEdit = {
        case TLCompleted => true
        case _ => true
      },
      messageKey = _ => "tasklist.ukResidencyQuestion.details",
      url = _ => _ => url,
      tagId = "ukResidencyQuestionRow",
      checks = _ => Seq(isResidencyComplete(fullReturn)),
      prerequisites = _ =>
        Seq()
    )
  }

  def buildUkResidencyRow(fullReturn: FullReturn)(implicit appConfig: FrontendAppConfig): TaskListSectionRow =
    ukResidencyRowBuilder(fullReturn).build(fullReturn)
}