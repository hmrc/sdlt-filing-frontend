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
import models.{FullReturn, Land}
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

  def mandatoryFieldsDefined(land: Land): Seq[Boolean] =
    Seq(
      land.propertyType.isDefined,
      land.interestCreatedTransferred.isDefined,
      land.address1.isDefined,
      land.localAuthorityNumber.isDefined,
      land.willSendPlanByPost.isDefined,
      land.mineralRights.isDefined
    )

  def isLandComplete(land: Land): Boolean =
    mandatoryFieldsDefined(land).forall(identity)

  def lands(fullReturn: FullReturn): Seq[Land] =
    fullReturn.land.getOrElse(Seq.empty)

  def incompleteLands(fullReturn: FullReturn): Seq[Land] =
    lands(fullReturn).filterNot(land => isLandComplete(land))

  def mandatoryFieldsDefined(fullReturn: FullReturn): Seq[Boolean] = {
    val all = lands(fullReturn)
    if (all.isEmpty) Seq.fill(6)(false)
    else all.flatMap(land => mandatoryFieldsDefined(land))
  }

  def isLandComplete(fullReturn: FullReturn): Boolean = {
    val all = lands(fullReturn)
    all.nonEmpty && all.forall(land => isLandComplete(land))
  }

  def landRowBuilder(fullReturn: FullReturn, status: SectionStatus)
                    (implicit appConfig: FrontendAppConfig): TaskListRowBuilder = {

    val defaultUrl =
      if (isLandComplete(fullReturn))
        controllers.land.routes.LandOverviewController.onPageLoad().url
      else if (incompleteLands(fullReturn).nonEmpty)
        // TODO: Send to incomplete overview instead
        //controllers.land.routes.LandIncompleteOverviewController.onPageLoad().url
        controllers.land.routes.LandOverviewController.onPageLoad().url
      else
        controllers.land.routes.LandBeforeYouStartController.onPageLoad().url

    val errorUrl = controllers.land.routes.LandAuthorityCodeMultiEntityController.onPageLoad().url
    val cf6Url = controllers.land.routes.LandPropertyTypeMultiEntityController.onPageLoad().url

    val onlyCf6 = status.ruleIds.nonEmpty && status.ruleIds.forall(_ == "Cf-6")

    val url =
      if status.hasFailures && onlyCf6 && isLandComplete(fullReturn) then cf6Url
      else if status.hasFailures && isLandComplete(fullReturn) then errorUrl
      else defaultUrl

    TaskListRowBuilder(
      canEdit       = _ => true,
      messageKey    = _ => "tasklist.landQuestion.details",
      url           = _ => _ => url,
      tagId         = "landQuestionDetailRow",
      checks        = _ => mandatoryFieldsDefined(fullReturn),
      invalid       = _ => status.hasFailures,
      prerequisites = _ => Seq()
    )
  }

  def buildLandRow(fullReturn: FullReturn, status: SectionStatus)
                  (implicit appConfig: FrontendAppConfig): TaskListSectionRow  = {
    landRowBuilder(fullReturn, status).build(fullReturn)
  }
}