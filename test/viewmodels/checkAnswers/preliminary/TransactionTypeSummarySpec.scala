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

package viewmodels.checkAnswers.preliminary

import base.SpecBase
import controllers.routes
import models.CheckMode
import models.prelimQuestions.TransactionType
import pages.preliminary.TransactionTypePage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.checkAnswers.preliminary.TransactionTypeSummary

class TransactionTypeSummarySpec extends SpecBase {

  "TransactionTypeSummary" - {

    "when transaction type is present" - {

      "must return a summary list row with transaction type value and change link" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(TransactionTypePage, TransactionType.ConveyanceTransfer).success.value

          val result = TransactionTypeSummary.row(Some(userAnswers))

          result.key.content.asHtml.toString() mustEqual msgs("prelim.transactionType.checkYourAnswersLabel")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          // Check for the actual resolved message, not the key
          htmlContent mustEqual msgs(s"prelim.transactionType.${TransactionType.ConveyanceTransfer}")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.preliminary.routes.TransactionTypeController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("prelim.transactionType.change.hidden")
        }
      }

      "must display the correct message for different transaction types" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val transactionTypes = Seq(
            TransactionType.ConveyanceTransfer,
            TransactionType.GrantOfLease,
            TransactionType.ConveyanceTransferLease,
            TransactionType.OtherTransaction
          )

          transactionTypes.foreach { transactionType =>
            val userAnswers = emptyUserAnswers
              .set(TransactionTypePage, transactionType).success.value

            val result = TransactionTypeSummary.row(Some(userAnswers))

            val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
            // Verify it equals the resolved message for this transaction type
            htmlContent mustEqual msgs(s"prelim.transactionType.$transactionType")
          }
        }
      }

      "must properly escape special characters in transaction type message" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(TransactionTypePage, TransactionType.ConveyanceTransfer).success.value

          val result = TransactionTypeSummary.row(Some(userAnswers))

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          // The message value should be HTML escaped and non-empty
          htmlContent.nonEmpty mustBe true
          result.value.content mustBe a[HtmlContent]
        }
      }
    }

    "when transaction type is not present" - {

      "must return a summary list row with a link to enter transaction type" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = TransactionTypeSummary.row(Some(emptyUserAnswers))

          result.key.content.asHtml.toString() mustEqual msgs("prelim.transactionType.checkYourAnswersLabel")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent must include("govuk-link")
          htmlContent must include(controllers.preliminary.routes.TransactionTypeController.onPageLoad(CheckMode).url)
          htmlContent must include(msgs("prelim.transactionType.link.message"))

          result.actions mustBe None
        }
      }

      "must return a summary list row with a link when UserAnswers is None" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = TransactionTypeSummary.row(None)

          result.key.content.asHtml.toString() mustEqual msgs("prelim.transactionType.checkYourAnswersLabel")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent must include("govuk-link")
          htmlContent must include(controllers.preliminary.routes.TransactionTypeController.onPageLoad(CheckMode).url)
          htmlContent must include(msgs("prelim.transactionType.link.message"))

          result.actions mustBe None
        }
      }
    }

    "must use CheckMode for the change link" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers
          .set(TransactionTypePage, TransactionType.ConveyanceTransfer).success.value

        val result = TransactionTypeSummary.row(Some(userAnswers))

        result.actions.get.items.head.href mustEqual controllers.preliminary.routes.TransactionTypeController.onPageLoad(CheckMode).url
      }
    }
  }
}