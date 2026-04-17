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

package viewmodels.checkAnswers.transaction

import base.SpecBase
import models.CheckMode
import pages.transaction.TotalConsiderationOfLinkedTransactionPage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent

class TotalConsiderationOfLinkedTransactionSummarySpec extends SpecBase {

  "TotalConsiderationOfTransactionSummary" - {

    "when the total consideration of linked transactions is present" - {

      "must return a summary list row with value and change link" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val value = "100.00"

          val userAnswers = emptyUserAnswers
            .set(TotalConsiderationOfLinkedTransactionPage, value).success.value

          val result = TotalConsiderationOfLinkedTransactionSummary.row(userAnswers)

          result.key.content.asHtml.toString() mustEqual msgs("transaction.totalConsiderationOfLinkedTransaction.checkYourAnswersLabel")

          val valueHtml = result.value.content.asHtml.toString()
          valueHtml mustEqual "£100.00"

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.transaction.routes.TotalConsiderationOfLinkedTransactionController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("transaction.totalConsiderationOfLinkedTransaction.change.hidden")
        }
      }
    }

    "when the total consideration of linked transactions is not present" - {

      "must return a summary list row with a missing link" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = TotalConsiderationOfLinkedTransactionSummary.row(emptyUserAnswers)

          result.key.content.asHtml.toString() mustEqual msgs("transaction.totalConsiderationOfLinkedTransaction.checkYourAnswersLabel")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent must include("govuk-link")
          htmlContent must include(controllers.transaction.routes.TotalConsiderationOfLinkedTransactionController.onPageLoad(CheckMode).url)
          htmlContent must include(msgs("transaction.totalConsiderationOfLinkedTransaction.missing"))

          result.actions mustBe None
        }
      }
    }
  }
}