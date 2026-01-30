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

package utils

import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import models.ReturnAgent
import play.api.i18n.Messages

object PurchaserAgentHelper {

  def getPurchaserAgent(agents: Seq[ReturnAgent]): Option[ReturnAgent] =
    agents.find(_.agentType.contains("PURCHASER"))

  def buildSummary(returnAgentOpt: Option[ReturnAgent])(implicit messages: Messages): Option[SummaryList] =
    returnAgentOpt.map(buildSummaryList)

  def buildSummaryList(returnAgent: ReturnAgent)(implicit messages: Messages): SummaryList = {
    SummaryList(
      rows = Seq(
        SummaryListRow(
          key = Key(
            content = Text(returnAgent.name.getOrElse("")),
            classes = "govuk-!-width-one-third govuk-!-font-weight-regular hmrc-summary-list__key"
          ),
          actions = Some(
            Actions(
              items = Seq(
                ActionItem(
                  href = controllers.purchaserAgent.routes.PurchaserAgentOverviewController.changePurchaserAgent(returnAgent.returnAgentID.get).url,
                  content = Text(messages("site.change")),
                  visuallyHiddenText = returnAgent.name
                ),
                ActionItem(
                  href = controllers.purchaserAgent.routes.PurchaserAgentOverviewController.removePurchaserAgent(returnAgent.returnAgentID.get).url,
                  content = Text(messages("site.remove")),
                  visuallyHiddenText = returnAgent.name
                )
              ),
              classes = "govuk-!-width-one-third"
            )
          )
        )
      )
    )
  }
}