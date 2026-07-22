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
import models.{AgentType, FullReturn, ReturnAgent, Vendor}
import play.api.i18n.Messages

import javax.inject.Singleton

@Singleton
object VendorAgentTaskList {

  def build(fullReturn: FullReturn)
           (implicit messages: Messages,
            appConfig: FrontendAppConfig): TaskListSection =
    TaskListSection(
      heading = messages("tasklist.vendorAgentQuestion.heading"),
      rows = Seq(
        buildVendorAgentRow(fullReturn)
      )
    )

  def vendorAgents(fullReturn: FullReturn): Seq[ReturnAgent] =
    fullReturn.returnAgent.getOrElse(Seq.empty)
      .filter(_.agentType.contains(AgentType.Vendor.toString))
      
  def isVendorAgentStarted(fullReturn: FullReturn): Boolean = {
    vendorAgents(fullReturn).nonEmpty
  }

  private def mainVendor(fullReturn: FullReturn): Option[Vendor] =
    for {
      mainVendorId <- fullReturn.returnInfo.flatMap(_.mainVendorID)
      vendors <- fullReturn.vendor
      vendor <- vendors.find(_.vendorID.contains(mainVendorId))
    } yield vendor

  private def vendorAgentChecks(fullReturn: FullReturn): Seq[Boolean] = {
    val agents = vendorAgents(fullReturn)

    Seq(
      true,
      agents.exists(_.name.isDefined),
      agents.exists(_.address1.isDefined)
    )
  }

  def mandatoryFieldsDefined(fullReturn: FullReturn): Seq[Boolean] = {
    val hasAgentDetails = fullReturn.returnAgent.exists(_.nonEmpty)
    val isRepresentedByAgent = mainVendor(fullReturn).flatMap(_.isRepresentedByAgent)

    (isRepresentedByAgent, hasAgentDetails) match {
      case (Some("YES"), true) => vendorAgentChecks(fullReturn)
      case (Some("NO"), true) => Seq(false)
      case (Some("NO"), false) => Seq(true)
      case _ => Seq(false)
    }
  }

  def isVendorAgentComplete(fullReturn: FullReturn): Boolean = {
    mandatoryFieldsDefined(fullReturn).forall(identity)
  }

  def vendorAgentRowBuilder(fullReturn: FullReturn)(implicit appConfig: FrontendAppConfig): TaskListRowBuilder = {

    val hasAgentDetails = fullReturn.returnAgent.exists(_.nonEmpty)
    val isNotRepresentedByAnAgent = mainVendor(fullReturn).flatMap(_.isRepresentedByAgent).exists(_.equalsIgnoreCase("NO"))

    val url = {
      if (isNotRepresentedByAnAgent && hasAgentDetails) {
        controllers.vendorAgent.routes.RemoveVendorAgentController.onPageLoad().url
      } else if (isNotRepresentedByAnAgent) {
        controllers.vendorAgent.routes.VendorAgentBeforeYouStartController.onPageLoad().url
      } else if (isVendorAgentComplete(fullReturn)) {
        controllers.vendorAgent.routes.VendorAgentOverviewController.onPageLoad().url
      } else {
        controllers.vendorAgent.routes.VendorAgentBeforeYouStartController.onPageLoad().url
      }
    }

    TaskListRowBuilder(
      canEdit = {
        case TLCompleted => true
        case _ => true
      },
      messageKey = _ => "tasklist.vendorAgentQuestion.details",
      url = _ => _ => {url},
      tagId = "vendorAgentQuestionDetailRow",
      checks = scheme => mandatoryFieldsDefined(fullReturn),
      prerequisites = _ => Seq()
    )
  }

  def buildVendorAgentRow(fullReturn: FullReturn)(implicit appConfig: FrontendAppConfig): TaskListSectionRow =
    vendorAgentRowBuilder(fullReturn).build(fullReturn)

}
