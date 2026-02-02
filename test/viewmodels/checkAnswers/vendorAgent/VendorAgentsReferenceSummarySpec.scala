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

package viewmodels.checkAnswers.vendorAgent

import base.SpecBase
import models.CheckMode
import pages.vendorAgent.VendorAgentsReferencePage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent


class VendorAgentsReferenceSummarySpec extends SpecBase {

  "VendorAgentReference Summary" - {

    "when agent reference is present" - {

      "must return a summary list row with agent referecne label" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers.set(VendorAgentsReferencePage, "12345678").success.value

          val result = VendorAgentsReferenceSummary.row(Some(userAnswers))

          result.key.content.asHtml.toString() mustEqual msgs("vendorAgent.agentsReference.checkYourAnswersLabel")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent mustEqual "12345678"

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.vendorAgent.routes.VendorAgentsReferenceController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("vendorAgent.agentsReference.change.hidden")
        }
      }
    }
    "must use CheckMode for the change link" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers
          .set(VendorAgentsReferencePage, "12345678").success.value

        val result = VendorAgentsReferenceSummary.row(Some(userAnswers))

        result.actions.get.items.head.href mustEqual controllers.vendorAgent.routes.VendorAgentsReferenceController.onPageLoad(CheckMode).url
      }
    }

    "must return a SummaryListRow with a link to if they want to add agent reference details" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val result = VendorAgentsReferenceSummary.row(Some(emptyUserAnswers))

        result.key.content.asHtml.toString() mustEqual msgs("vendorAgent.agentsReference.checkYourAnswersLabel", "the agent")

        val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
        htmlContent must include("govuk-link")
        htmlContent must include(controllers.vendorAgent.routes.VendorAgentsReferenceController.onPageLoad(CheckMode).url)
        htmlContent must include(msgs("vendorAgent.checkYourAnswers.agentsReference.agentMissing"))

        result.actions mustBe None
      }
    }
  }
}