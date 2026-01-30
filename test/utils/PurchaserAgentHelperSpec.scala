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

import base.SpecBase
import controllers.purchaserAgent.routes
import models.ReturnAgent
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

class PurchaserAgentHelperSpec extends SpecBase {

  private def testReturnAgent(
                               agentType: String,
                               name: Option[String] = Some("Test Agent")
                             ): ReturnAgent =
    ReturnAgent(
      returnAgentID = Some("AGENT123"),
      agentType = Some(agentType),
      name = name,
      houseNumber = None,
      address1 = Some("Line 1"),
      address2 = None,
      address3 = None,
      address4 = None,
      postcode = Some("AA1 1AA"),
      phone = None,
      email = None,
      reference = None
    )

  "PurchaserAgentHelper.getPurchaserAgent" - {

    "must return the PURCHASER agent when present" in {
      val agents = Seq(
        testReturnAgent("VENDOR"),
        testReturnAgent("PURCHASER"),
        testReturnAgent("OTHER")
      )

      val result = PurchaserAgentHelper.getPurchaserAgent(agents)

      result.value.agentType.value mustBe "PURCHASER"
    }

    "must return None when no PURCHASER agent exists" in {
      val agents = Seq(
        testReturnAgent("VENDOR"),
        testReturnAgent("OTHER")
      )

      PurchaserAgentHelper.getPurchaserAgent(agents) mustBe None
    }
  }

  "PurchaserAgentHelper.buildSummary" - {

    "must return None when no agent is provided" in {
      val application = applicationBuilder().build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        PurchaserAgentHelper.buildSummary(None) mustBe None
      }
    }

    "must return a SummaryList when an agent is provided" in {
      val application = applicationBuilder().build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val agent = testReturnAgent("PURCHASER")

        val result = PurchaserAgentHelper.buildSummary(Some(agent))

        result mustBe defined
        result.get.rows.size mustBe 1
      }
    }
  }

  "PurchaserAgentHelper.buildSummaryList" - {

    "must build a SummaryList with agent name as the key" in {
      val application = applicationBuilder().build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val agent = testReturnAgent("PURCHASER", Some("Jane Agent"))

        val summary = PurchaserAgentHelper.buildSummaryList(agent)
        val row = summary.rows.head

        row.key.content mustBe Text("Jane Agent")
      }
    }

    "must include Change and Remove action items with correct URLs" in {
      val application = applicationBuilder().build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val agent = testReturnAgent("PURCHASER")
        val testReturnId: String = agent.returnAgentID.getOrElse("NotFound")

        val summary = PurchaserAgentHelper.buildSummaryList(agent)
        val actions = summary.rows.head.actions.value.items

        actions.map(_.content) mustBe Seq(
          Text(msgs("site.change")),
          Text(msgs("site.remove"))
        )

        actions.map(_.href) mustBe Seq(
          routes.PurchaserAgentOverviewController.changePurchaserAgent(testReturnId).url,
          routes.PurchaserAgentOverviewController.removePurchaserAgent(testReturnId).url
        )
      }
    }

    "must set visuallyHiddenText to the agent name for accessibility" in {
      val application = applicationBuilder().build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val agent = testReturnAgent("PURCHASER", Some("Hidden Name"))

        val summary = PurchaserAgentHelper.buildSummaryList(agent)
        val actions = summary.rows.head.actions.value.items

        actions.foreach { action =>
          action.visuallyHiddenText.value mustBe "Hidden Name"
        }
      }
    }
  }
}
