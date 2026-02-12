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
object LandTaskList {

  def build(fullReturn: FullReturn)
           (implicit messages: Messages,
            appConfig: FrontendAppConfig): TaskListSection =
    TaskListSection(
      heading = messages("tasklist.landQuestion.heading"),
      rows = Seq(
        buildLandRow(fullReturn)
      )
    )

  def buildLandRow(fullReturn: FullReturn)(implicit appConfig: FrontendAppConfig): TaskListSectionRow = {
    val mainLandId = fullReturn.returnInfo.flatMap(_.mainLandID)

    val url = fullReturn.land match {
      case Some(list) if list.exists(x => x.landID == mainLandId && x.landArea.isEmpty)
      => controllers.land.routes.LandBeforeYouStartController.onPageLoad().url
      case Some(list) if list.nonEmpty
      => controllers.purchaser.routes.PurchaserOverviewController.onPageLoad().url // TODO - DTR-2498 - SPRINT-10 - Update to LandOverview when created
      case _ => controllers.land.routes.LandBeforeYouStartController.onPageLoad().url
    }

    TaskListRowBuilder(
      canEdit = {
        case TLCompleted => true
        case _ => true
      },
      messageKey = _ => "tasklist.landQuestion.details",
      url = _ => _ => {
        url
      },
      tagId = "landQuestionDetailRow",
      checks = scheme => Seq(fullReturn.land.exists(_.nonEmpty)),
      prerequisites = _ => Seq(PrelimTaskList.buildPrelimRow(fullReturn))
    ).build(fullReturn)
  }

}
