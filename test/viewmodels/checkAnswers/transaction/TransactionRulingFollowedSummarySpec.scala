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

package viewmodels.checkAnswers.transaction

import base.SpecBase
import models.CheckMode
import pages.transaction.TransactionRulingFollowedPage
import models.transaction.TransactionRulingFollowed
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}

class TransactionRulingFollowedSummarySpec extends SpecBase {

  "TransactionRulingFollowedSummary" - {

    "when ruling followed is answered" - {

      "must return a summary list row with yes text and change link" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers.set(TransactionRulingFollowedPage, TransactionRulingFollowed.Yes).success.value

          val row = TransactionRulingFollowedSummary.row(userAnswers)

          val result = row match {
            case Row(r) => r
            case _ => fail("Expected Row but got Missing")
          }

          result.key.content.asHtml.toString() mustEqual msgs("transaction.rulingFollowed.checkYourAnswersLabel")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent mustEqual msgs("transaction.rulingFollowed.yes")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.transaction.routes.TransactionRulingFollowedController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("transaction.rulingFollowed.change.hidden")
        }
      }

      "must display the correct message for all answers" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val answers = Seq(
            TransactionRulingFollowed.Yes,
            TransactionRulingFollowed.No,
            TransactionRulingFollowed.RulingNotReceived
          )

          answers.foreach { answer =>
            val userAnswers = emptyUserAnswers
              .set(TransactionRulingFollowedPage, answer).success.value

            val row = TransactionRulingFollowedSummary.row(userAnswers)

            val result = row match {
              case Row(r) => r
              case _ => fail("Expected Row but got Missing")
            }

            val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
            htmlContent mustEqual msgs(s"transaction.rulingFollowed.$answer")
          }
        }
      }
    }

    "when ruling followed data is not present" - {

      "must return a Missing and redirect call to missing page when data is missing" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = TransactionRulingFollowedSummary.row(emptyUserAnswers)

          result match {
            case Missing(call) =>
              call mustEqual controllers.transaction.routes.TransactionRulingFollowedController.onPageLoad(CheckMode)

            case Row(_) =>
              fail("Expected Missing but got Row")
          }
        }
      }
    }
  }
}