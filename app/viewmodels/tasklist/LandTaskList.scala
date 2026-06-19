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

package viewmodels.tasklist

import config.FrontendAppConfig
import models.FullReturn
import play.api.i18n.Messages
import services.crossflow.{ReturnSection, SectionStatus}

import javax.inject.Singleton

@Singleton
object LandTaskList {

  val noFailures: SectionStatus =
    SectionStatus(ReturnSection.Land, hasFailures = false, ruleIds = Nil, messageKeys = Nil, targets = Nil)

  def build(fullReturn: FullReturn, status: SectionStatus = noFailures)
           (implicit messages: Messages, appConfig: FrontendAppConfig): TaskListSection = {
    TaskListSection(
      heading = messages("tasklist.landQuestion.heading"),
      rows    = Seq(buildLandRow(fullReturn, status))
    )
  }
  
  def isLandComplete(fullReturn: FullReturn): Boolean = {
    fullReturn.land.exists(_.nonEmpty)
    //TODO ADD ALL REQUIRED FIELDS FOR LAND
  }

  def landRowBuilder(fullReturn: FullReturn, status: SectionStatus)
                  (implicit appConfig: FrontendAppConfig): TaskListRowBuilder = {

    val mainLandID = fullReturn.returnInfo.flatMap(_.mainLandID)

    val defaultUrl = fullReturn.land match {
      case Some(list) if list.length > 1                                                => controllers.land.routes.LandOverviewController.onPageLoad().url
      case Some(list) if list.exists(x => x.landID == mainLandID && x.mineralRights.isEmpty)
      => controllers.land.routes.LandBeforeYouStartController.onPageLoad().url
      case Some(list) if list.nonEmpty                                                  => controllers.land.routes.LandOverviewController.onPageLoad().url
      case _                                                                            => controllers.land.routes.LandBeforeYouStartController.onPageLoad().url
    }

    val errorUrl = controllers.land.routes.LandAuthorityCodeMultiEntityController.onPageLoad().url
    val cf6Url = controllers.land.routes.LandPropertyTypeMultiEntityController.onPageLoad().url

    val onlyCf6 = status.ruleIds.nonEmpty && status.ruleIds.forall(_ == "Cf-6")

    val url =
      if (status.hasFailures && onlyCf6) cf6Url
      else if (status.hasFailures) errorUrl
      else defaultUrl

    TaskListRowBuilder(
      canEdit       = _ => true,
      messageKey    = _ => "tasklist.landQuestion.details",
      url           = _ => _ => url,
      tagId         = "landQuestionDetailRow",
      checks        = _ => Seq(isLandComplete(fullReturn)),
      invalid       = _ => status.hasFailures,
      prerequisites = _ => Seq(PrelimTaskList.buildPrelimRow(fullReturn))
    )
  }
  
  def buildLandRow(fullReturn: FullReturn, status: SectionStatus)
                  (implicit appConfig: FrontendAppConfig): TaskListSectionRow  = {
    landRowBuilder(fullReturn, status).build(fullReturn)
  }
}