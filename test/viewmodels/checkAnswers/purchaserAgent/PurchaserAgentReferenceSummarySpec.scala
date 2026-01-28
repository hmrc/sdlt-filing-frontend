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

package viewmodels.checkAnswers.purchaserAgent

import base.SpecBase
import models.CheckMode
import pages.purchaserAgent.{AddPurchaserAgentReferenceNumberPage, PurchaserAgentReferencePage}
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent

class PurchaserAgentReferenceSummarySpec extends SpecBase {

  "PurchaserAgentReferenceSummary" - {

    "when reference number is present" - {

      "must return a SummaryListRow with change link" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers.set(PurchaserAgentReferencePage, "123456").success.value

          val result = PurchaserAgentReferenceSummary.row(userAnswers).getOrElse(fail("Failed to get summary list row"))

          result.key.content.asHtml.toString() mustEqual msgs("purchaserAgent.reference.checkYourAnswersLabel")

          val contentString = result.value.content.asHtml.toString()

          contentString mustEqual "123456"

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.purchaserAgent.routes.PurchaserAgentReferenceController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("purchaserAgent.reference.change.hidden")
        }
      }
    }

    "when reference number is not present" - {

      "must return a SummaryListRow with a link to enter reference number when add reference number is true" in {
        val userAnswers = emptyUserAnswers.set(AddPurchaserAgentReferenceNumberPage, true).success.value
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = PurchaserAgentReferenceSummary.row(userAnswers).getOrElse(fail("Failed to get summary list row"))

          result.key.content.asHtml.toString() mustEqual msgs("purchaserAgent.reference.checkYourAnswersLabel")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent must include("govuk-link")
          htmlContent must include(controllers.purchaserAgent.routes.PurchaserAgentReferenceController.onPageLoad(CheckMode).url)
          htmlContent must include(msgs("purchaserAgent.checkYourAnswers.referenceNumber.missing"))

          result.actions mustBe None
        }
      }

      "must return None when add reference number is false" in {
        val userAnswers = emptyUserAnswers.set(AddPurchaserAgentReferenceNumberPage, false).success.value
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          PurchaserAgentReferenceSummary.row(emptyUserAnswers) mustBe None
        }
      }
    }
  }
}