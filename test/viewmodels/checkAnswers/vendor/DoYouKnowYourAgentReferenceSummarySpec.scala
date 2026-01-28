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

package viewmodels.checkAnswers

import base.SpecBase
import models.CheckMode
import models.vendor.DoYouKnowYourAgentReference
import pages.vendor.DoYouKnowYourAgentReferencePage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.checkAnswers.vendor.DoYouKnowYourAgentReferenceSummary

class DoYouKnowYourAgentReferenceSummarySpec extends SpecBase {

  "DoYouKnowYourAgentReferenceSummary" - {

    "when purchaser name is present" - {

      "must return a summary list row with surname only" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(DoYouKnowYourAgentReferencePage, DoYouKnowYourAgentReference.Yes).success.value

          val result = DoYouKnowYourAgentReferenceSummary.row(userAnswers).getOrElse(fail("Failed to get summary list row"))

          result.key.content.asHtml.toString() mustEqual msgs("agent.doYouKnowYourAgentReference.checkYourAnswersLabel")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent mustEqual "agent.doYouKnowYourAgentReference.yes"

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.vendor.routes.DoYouKnowYourAgentReferenceController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("agent.doYouKnowYourAgentReference.change.hidden")
        }
      }


    }
  }
}