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
import models.{AgentType, FullReturn, NormalMode, ReturnAgent}
import play.api.i18n.Messages

import javax.inject.Singleton

@Singleton
object PurchaserAgentTaskList {

  def build(fullReturn: FullReturn)
           (implicit messages: Messages,
            appConfig: FrontendAppConfig): TaskListSection =
    TaskListSection(
      heading = messages("tasklist.purchaserAgentQuestion.heading"),
      rows = Seq(
        buildPurchaserAgentRow(fullReturn)
      )
    )

  def purchaserAgents(fullReturn: FullReturn): Seq[ReturnAgent] =
    fullReturn.returnAgent.getOrElse(Seq.empty)
      .filter(_.agentType.contains(AgentType.Purchaser.toString))
  
  def isPurchaserAgentStarted(fullReturn: FullReturn): Boolean = {
    purchaserAgents(fullReturn).nonEmpty
  }
  def isPurchaserAgentComplete(fullReturn: FullReturn): Boolean = {
    purchaserAgents(fullReturn).nonEmpty &&
      purchaserAgents(fullReturn).forall( agent =>
        agent.name.isDefined
        //TODO ADD ALL MANDATORY CONDITIONS FOR PURCHASER AGENT
      )
  }

  def purchaserAgentRowBuilder(fullReturn: FullReturn)(implicit appConfig: FrontendAppConfig): TaskListRowBuilder = {

    val url = if (isPurchaserAgentComplete(fullReturn)) {
      controllers.purchaserAgent.routes.PurchaserAgentOverviewController.onPageLoad().url
    } else {
      controllers.purchaserAgent.routes.PurchaserAgentBeforeYouStartController.onPageLoad(NormalMode).url
    }

    TaskListRowBuilder(
      isOptional = true,
      canEdit = {
        case TLCompleted => true
        case _ => true
      },
      messageKey = _ => "tasklist.purchaserAgentQuestion.details",
      url = _ => _ => {
        url
      },
      tagId = "purchaserAgentQuestionDetailRow",
      checks = scheme => Seq(isPurchaserAgentComplete(fullReturn)),
      prerequisites = _ => Seq()
    )
  }

  def buildPurchaserAgentRow(fullReturn: FullReturn)(implicit appConfig: FrontendAppConfig): TaskListSectionRow =
    purchaserAgentRowBuilder(fullReturn).build(fullReturn)
}
