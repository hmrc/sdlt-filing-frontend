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

package viewmodels.checkAnswers.purchaserAgent

import base.SpecBase
import models.CheckMode
import models.purchaserAgent.PurchaserAgentAuthorised
import pages.purchaserAgent.{PurchaserAgentAuthorisedPage, PurchaserAgentNamePage}
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent

class PurchaserAgentAuthorisedSummarySpec extends SpecBase {

  "PurchaserAgentAuthorisedSummary" - {

    "when purchaser authorised is present" - {

      "must return a summary list row with 'yes' text and change link" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(PurchaserAgentAuthorisedPage, PurchaserAgentAuthorised.Yes).success.value
            .set(PurchaserAgentNamePage, "Agent name").success.value

          val result = PurchaserAgentAuthorisedSummary.row(userAnswers)

          result.key.content.asHtml.toString() mustEqual msgs("purchaserAgent.purchaserAgentAuthorised.checkYourAnswersLabel", "Agent name")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent mustEqual "Yes"

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.purchaserAgent.routes.PurchaserAgentAuthorisedController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("purchaserAgent.purchaserAgentAuthorised.change.hidden", "Agent name")
        }
      }

      "must return a summary list row with 'yes' text and change link when agent name is not set" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(PurchaserAgentAuthorisedPage, PurchaserAgentAuthorised.Yes).success.value

          val result = PurchaserAgentAuthorisedSummary.row(userAnswers)

          result.key.content.asHtml.toString() mustEqual msgs("purchaserAgent.purchaserAgentAuthorised.checkYourAnswersLabel", "the agent")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent mustEqual "Yes"

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.purchaserAgent.routes.PurchaserAgentAuthorisedController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("purchaserAgent.purchaserAgentAuthorised.change.hidden", "the agent")
        }
      }

      "must return a summary list row with 'no' text and change link" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(PurchaserAgentAuthorisedPage, PurchaserAgentAuthorised.No).success.value
            .set(PurchaserAgentNamePage, "Agent name").success.value

          val result = PurchaserAgentAuthorisedSummary.row(userAnswers)

          result.key.content.asHtml.toString() mustEqual msgs("purchaserAgent.purchaserAgentAuthorised.checkYourAnswersLabel", "Agent name")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent mustEqual "No"

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.purchaserAgent.routes.PurchaserAgentAuthorisedController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("purchaserAgent.purchaserAgentAuthorised.change.hidden", "Agent name")
        }
      }
    }

    "when purchaser authorised is not present" - {

      "must return a summary list row with a link to enter is authorised" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = PurchaserAgentAuthorisedSummary.row(emptyUserAnswers)

          result.key.content.asHtml.toString() mustEqual msgs("purchaserAgent.purchaserAgentAuthorised.checkYourAnswersLabel", "the agent")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent must include("govuk-link")
          htmlContent must include(controllers.purchaserAgent.routes.PurchaserAgentAuthorisedController.onPageLoad(CheckMode).url)
          htmlContent must include(msgs("purchaserAgent.checkYourAnswers.authorised.missing"))

          result.actions mustBe None
        }
      }
    }
  }
}
