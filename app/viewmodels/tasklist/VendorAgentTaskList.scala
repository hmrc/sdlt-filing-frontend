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
import models.{AgentType, FullReturn, ReturnAgent}
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

  def isVendorAgentComplete(fullReturn: FullReturn): Boolean = {
    vendorAgents(fullReturn).nonEmpty &&
      vendorAgents(fullReturn).forall( agent =>
        agent.name.isDefined
        //TODO ADD ALL MANDATORY CONDITIONS FOR VENDOR AGENT
      )
  }

  def vendorAgentRowBuilder(fullReturn: FullReturn)(implicit appConfig: FrontendAppConfig): TaskListRowBuilder = {

    val url = if(isVendorAgentComplete(fullReturn)) {
        controllers.vendorAgent.routes.VendorAgentOverviewController.onPageLoad().url
    } else {
      controllers.vendorAgent.routes.VendorAgentBeforeYouStartController.onPageLoad().url
    }

    TaskListRowBuilder(
      isOptional = true,
      canEdit = {
        case TLCompleted => true
        case _ => true
      },
      messageKey = _ => "tasklist.vendorAgentQuestion.details",
      url = _ => _ => {url},
      tagId = "vendorAgentQuestionDetailRow",
      checks = scheme => Seq(isVendorAgentComplete(fullReturn)),
      prerequisites = _ => Seq()
    )
  }

  def buildVendorAgentRow(fullReturn: FullReturn)(implicit appConfig: FrontendAppConfig): TaskListSectionRow =
    vendorAgentRowBuilder(fullReturn).build(fullReturn)

}
