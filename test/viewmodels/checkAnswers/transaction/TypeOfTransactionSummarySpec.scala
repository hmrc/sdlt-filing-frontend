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
import models.prelimQuestions.TransactionType
import pages.transaction.TypeOfTransactionPage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}

class TypeOfTransactionSummarySpec extends SpecBase {

  "TypeOfTransactionSummary" - {

    "when transaction type is present" - {

      "must return a Row with transaction type value and change link" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(TypeOfTransactionPage, TransactionType.ConveyanceTransfer).success.value

          val row = TypeOfTransactionSummary.row(userAnswers)

          val result = row match {
            case Row(r) => r
            case _      => fail("Expected Row but got Missing")
          }

          result.key.content.asHtml.toString() mustEqual msgs("prelim.transactionType.checkYourAnswersLabel")

          val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
          htmlContent mustEqual msgs(s"prelim.transactionType.${TransactionType.ConveyanceTransfer}")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.transaction.routes.TypeOfTransactionController.onPageLoad(CheckMode).url
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
              .set(TypeOfTransactionPage, transactionType).success.value

            val row = TypeOfTransactionSummary.row(userAnswers)

            val result = row match {
              case Row(r) => r
              case _      => fail("Expected Row but got Missing")
            }

            val htmlContent = result.value.content.asInstanceOf[HtmlContent].asHtml.toString()
            htmlContent mustEqual msgs(s"prelim.transactionType.$transactionType")
          }
        }
      }
    }

    "when transaction type is not present" - {

      "must return Missing with the correct redirect call" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = TypeOfTransactionSummary.row(emptyUserAnswers)

          result match {
            case Missing(call) =>
              call mustEqual controllers.transaction.routes.TypeOfTransactionController.onPageLoad(CheckMode)

            case Row(_) =>
              fail("Expected Missing but got Row")
          }
        }
      }
    }

    "must use CheckMode for the change link" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val userAnswers = emptyUserAnswers
          .set(TypeOfTransactionPage, TransactionType.ConveyanceTransfer).success.value

        val row = TypeOfTransactionSummary.row(userAnswers)

        val result = row match {
          case Row(r) => r
          case _      => fail("Expected Row but got Missing")
        }

        result.actions.get.items.head.href mustEqual controllers.transaction.routes.TypeOfTransactionController.onPageLoad(CheckMode).url
      }
    }
  }
}
