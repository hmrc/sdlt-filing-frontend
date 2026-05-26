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
import pages.transaction.TransactionDeferringPaymentPage
import play.api.i18n.Messages
import play.api.test.Helpers.running
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}

class TransactionDeferringPaymentSummarySpec extends SpecBase {

  "TransactionDeferringPaymentSummary" - {

    "when deferring payment is present" - {

      "must return a Row with 'yes' text and change link" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(TransactionDeferringPaymentPage, true).success.value

          val result = TransactionDeferringPaymentSummary.row(userAnswers)

          result mustBe a[Row]
          val row = result.asInstanceOf[Row].row

          row.key.content.asHtml.toString() mustEqual msgs("transaction.deferringPayment.checkYourAnswersLabel")

          val contentString = row.value.content.asHtml.toString()
          contentString mustEqual msgs("site.yes")

          row.actions.get.items.size mustEqual 1
          row.actions.get.items.head.href mustEqual controllers.transaction.routes.TransactionDeferringPaymentController.onPageLoad(CheckMode).url
          row.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          row.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("transaction.deferringPayment.change.hidden")
        }
      }

      "must return a Row with 'no' text and change link" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(TransactionDeferringPaymentPage, false).success.value

          val result = TransactionDeferringPaymentSummary.row(userAnswers)

          result mustBe a[Row]
          val row = result.asInstanceOf[Row].row

          row.key.content.asHtml.toString() mustEqual msgs("transaction.deferringPayment.checkYourAnswersLabel")

          val contentString = row.value.content.asHtml.toString()
          contentString mustEqual msgs("site.no")

          row.actions.get.items.size mustEqual 1
          row.actions.get.items.head.href mustEqual controllers.transaction.routes.TransactionDeferringPaymentController.onPageLoad(CheckMode).url
          row.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          row.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("transaction.deferringPayment.change.hidden")
        }
      }
    }

    "when deferring payment is not present" - {

      "must return a Missing and redirect call to missing page" in {
        val userAnswers = emptyUserAnswers
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = TransactionDeferringPaymentSummary.row(userAnswers)

          result match {
            case Missing(call) =>
              call mustEqual controllers.transaction.routes.TransactionDeferringPaymentController.onPageLoad(CheckMode)

            case Row(_) =>
              fail("Expected Missing but got Row")
          }
        }
      }
    }
  }
}

