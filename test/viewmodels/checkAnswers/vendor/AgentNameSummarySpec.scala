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

package viewmodels.checkAnswers.vendor

import base.SpecBase
import models.CheckMode
import pages.vendor.AgentNamePage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent

class AgentNameSummarySpec extends SpecBase {

  "AgentNameSummary" - {

    "when agent name is present" - {

      "must return a summary list row with agent name label" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers.set(AgentNamePage, "Smith").success.value

          val result = AgentNameSummary.row(Some(userAnswers))

          result.key.content.asHtml.toString() mustEqual msgs("agent.checkYourAnswers.agentName.label")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent mustEqual "Smith"

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.vendor.routes.AgentNameController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("agent.agentName.change.hidden")
        }
      }

      "must properly escape special characters in name" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(AgentNamePage, "O'Brien & Sons <Ltd>").success.value

          val result = AgentNameSummary.row(Some(userAnswers))

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent must include("O&#x27;Brien")
          htmlContent must include("&amp;")
          htmlContent must include("&lt;")
          htmlContent must include("&gt;")
        }
      }
    }

    "must use CheckMode for the change link" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers
          .set(AgentNamePage, "Smith").success.value

        val result = AgentNameSummary.row(Some(userAnswers))

        result.actions.get.items.head.href mustEqual controllers.vendor.routes.AgentNameController.onPageLoad(CheckMode).url
      }
    }
  }
}