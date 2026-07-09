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

package viewmodels.submission.summary

import models.address.Address
import models.address.Address.toHtml
import models.{AgentType, FullReturn, ReturnAgent}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.govuk.summarylist.*
import viewmodels.submission.summary.SummaryUtil.*

object VendorAgentSummary {

  def getSummaryCard(fullReturn: FullReturn)(implicit messages: Messages): Option[SummaryList] = {
    val vendorAgentOpt: Option[ReturnAgent] = fullReturn.returnAgent.flatMap(_.find(_.agentType.contains(AgentType.Vendor.toString)))
    vendorAgentOpt.flatMap { agent =>
      agent.name.map { agentName =>
        SummaryListViewModel(
          Seq(
            getOptSummaryRowHtml(
              messages("agent.checkYourAnswers.agentAddress.label"),
              agent.address1.map(address1 =>
                HtmlContent(toHtml(
                  Address(
                    line1 = address1,
                    line2 = agent.address2,
                    line3 = agent.address3,
                    line4 = agent.address4,
                    postcode = agent.postcode
                  )
                ))
              )
            ),
            getOptSummaryRowHtml(
              messages("vendorAgent.vendorAgentsContactDetails.checkYourAnswersLabel"),
              (agent.phone, agent.email) match {
                case (phoneOpt, emailOpt) if phoneOpt.isDefined || emailOpt.isDefined =>
                  Some(HtmlContent(
                    Seq(
                      phoneOpt.map("Tel: " + HtmlFormat.escape(_).toString),
                      emailOpt.map("Email: " + HtmlFormat.escape(_).toString)
                    ).flatten.mkString("<br>")
                  ))
                case _ => None
              }
            ),
            getOptSummaryRow(
              messages("vendorAgent.agentsReference.checkYourAnswersLabel"),
              agent.reference
            )
          ).flatMap(_.toSeq)
        ).withCard(
          messages(
            "submission.completedSdltReturn.vendorAgent.header",
            agentName
          )
        )
      }
    }
  }
}
