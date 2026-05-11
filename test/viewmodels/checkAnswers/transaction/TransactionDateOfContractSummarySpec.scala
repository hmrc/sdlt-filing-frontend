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
import pages.transaction.{TransactionAddDateOfContractPage, TransactionDateOfContractPage}
import play.api.i18n.Messages
import play.api.test.Helpers.running
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}

import java.time.LocalDate

class TransactionDateOfContractSummarySpec extends SpecBase {

  "TransactionDateOfContractSummary" - {

    "when date of contract is present" - {

      "must return Some(Row) with the contract date and change link" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(TransactionDateOfContractPage, LocalDate.of(2022, 10, 26)).success.value

          val row = TransactionDateOfContractSummary.row(userAnswers).getOrElse(fail("Failed to get summary list row"))

          val result = row match {
            case Row(r) => r
            case _      => fail("Expected Row but got Missing")
          }

          result.key.content.asHtml.toString() mustEqual msgs("transaction.transactionDateOfContract.checkYourAnswersLabel")
          result.value.content.asHtml.toString() mustEqual msgs("26 10 2022")
          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.transaction.routes.TransactionDateOfContractController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("transaction.transactionDateOfContract.change.hidden")
        }
      }
    }

    "when date of contract is absent but add date of contract is true" - {

      "must return Some(Missing) with the correct redirect call" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = emptyUserAnswers
            .set(TransactionAddDateOfContractPage, true).success.value

          val result = TransactionDateOfContractSummary.row(userAnswers).getOrElse(fail("Failed to get summary list row"))

          result match {
            case Missing(call) =>
              call mustEqual controllers.transaction.routes.TransactionDateOfContractController.onPageLoad(CheckMode)

            case Row(_) =>
              fail("Expected Missing but got Row")
          }
        }
      }
    }

    "when add date of contract is false or absent" - {

      "must return None" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          TransactionDateOfContractSummary.row(emptyUserAnswers) mustBe None
        }
      }
    }
  }
}
