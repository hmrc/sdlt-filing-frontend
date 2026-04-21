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
import pages.transaction.TransactionPartialReliefPage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent

class TransactionPartialReliefSummarySpec extends SpecBase {

  "TransactionPartialReliefSummary" - {

    "when partial relief is present" - {

      "must return a SummaryListRow with 'yes' text and change link" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(TransactionPartialReliefPage, true).success.value

          val result = TransactionPartialReliefSummary.row(userAnswers)

          result.key.content.asHtml.toString() mustEqual msgs("transaction.transactionPartialRelief.checkYourAnswersLabel")

          val contentString = result.value.content.asHtml.toString()

          contentString mustEqual msgs("site.yes")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.transaction.routes.TransactionPartialReliefController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("transaction.transactionPartialRelief.change.hidden")
        }
      }

      "must return a SummaryListRow with 'no' text and change link" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(TransactionPartialReliefPage, false).success.value

          val result = TransactionPartialReliefSummary.row(userAnswers)

          result.key.content.asHtml.toString() mustEqual msgs("transaction.transactionPartialRelief.checkYourAnswersLabel")

          val contentString = result.value.content.asHtml.toString()

          contentString mustEqual msgs("site.no")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.transaction.routes.TransactionPartialReliefController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("transaction.transactionPartialRelief.change.hidden")
        }
      }
    }

    "when partial relief is not present" - {
      
      "must return a SummaryListRow with a link if they want partial relief" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = TransactionPartialReliefSummary.row(emptyUserAnswers)

          result.key.content.asHtml.toString() mustEqual
            msgs("transaction.transactionPartialRelief.checkYourAnswersLabel")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent must include("govuk-link")
          htmlContent must include(msgs("transaction.transactionPartialRelief.missing"))
          htmlContent must include(
            controllers.transaction.routes.TransactionPartialReliefController.onPageLoad(CheckMode).url
          )

          result.actions mustBe None
        }
      }
    }
  }
}
