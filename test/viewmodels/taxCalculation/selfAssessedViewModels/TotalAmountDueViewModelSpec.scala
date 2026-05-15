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

package viewmodels.taxCalculation.selfAssessedViewModels

import base.SpecBase
import models.taxCalculation.*
import models.{FullReturn, Transaction, UserAnswers}
import org.mockito.Mockito.when
import org.scalatest.EitherValues
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import utils.TimeMachine
import viewmodels.taxCalculation.selfAssessedViewModels.TotalAmountDueViewModel.toViewModel

import java.time.LocalDate

class TotalAmountDueViewModelSpec extends SpecBase with EitherValues with MockitoSugar {

  private implicit val messages: Messages = stubMessages()

  private val today = LocalDate.of(2026, 5, 1)

  private def stubbedTimeMachine: TimeMachine = {
    val tm = mock[TimeMachine]
    when(tm.today).thenReturn(today)
    tm
  }

  private def answersWith(effectiveDate: String = today.toString): UserAnswers =
    emptyUserAnswers.copy(fullReturn = Some(FullReturn(
      stornId           = "STORN",
      returnResourceRef = "REF",
      transaction       = Some(Transaction(effectiveDate = Some(effectiveDate)))
    )))

  ".toViewModel" - {

    "renders SDLT due, no penalty, and total when the transaction is within the 14-day filing window" in {
      val vm = toViewModel(
        BigDecimal(25000),
        answersWith(),
        stubbedTimeMachine
      ).value

      vm.totalAmountDueSummary.rows.map(_.value.content) mustEqual Seq(
        Text("£25,000"),
        Text("£0"),
        Text("£25,000")
      )
    }

    "applies the £100 penalty band when the transaction is past the filing window but under 123 days old" in {
      val vm = toViewModel(
        BigDecimal(25000),
        answersWith(today.minusDays(60).toString),
        stubbedTimeMachine
      ).value

      vm.totalAmountDueSummary.rows.last.value.content mustBe Text("£25,100")
      vm.totalAmountDueSummary.rows(1).value.content   mustBe Text("£100")
    }

    "applies the £200 penalty band when the transaction is 123+ days old" in {
      val vm = toViewModel(
        BigDecimal(25000),
        answersWith(today.minusDays(200).toString),
        stubbedTimeMachine
      ).value

      vm.totalAmountDueSummary.rows.last.value.content mustBe Text("£25,200")
      vm.totalAmountDueSummary.rows(1).value.content   mustBe Text("£200")
    }

    "labels the rows in the correct order: SDLT due, Penalties, Total" in {
      val vm = toViewModel(
        BigDecimal(25000),
        answersWith(),
        stubbedTimeMachine
      ).value

      vm.totalAmountDueSummary.rows.map(_.key.content) mustEqual Seq(
        Text("taxCalculation.totalAmountDue.sdltDue"),
        Text("taxCalculation.totalAmountDue.penalties"),
        Text("taxCalculation.totalAmountDue.total")
      )
    }

    "right-aligns every value cell so amounts line up against the column" in {
      val vm = toViewModel(
        BigDecimal(25000),
        answersWith(),
        stubbedTimeMachine
      ).value

      vm.totalAmountDueSummary.rows.map(_.value.classes).distinct mustEqual Seq("govuk-!-text-align-right")
    }

    "Left(MissingFullReturnError) when the session has no fullReturn" in {
      toViewModel(
        BigDecimal(25000),
        emptyUserAnswers,
        stubbedTimeMachine
      ) mustBe Left(MissingFullReturnError)
    }

    "Left(MissingAboutTheTransactionError) when fullReturn has no transaction" in {
      val noTransaction = emptyUserAnswers.copy(fullReturn = Some(FullReturn(stornId = "STORN", returnResourceRef = "REF")))

      toViewModel(
        BigDecimal(25000),
        noTransaction,
        stubbedTimeMachine
      ) mustBe Left(MissingAboutTheTransactionError)
    }

    "Left(MissingTransactionAnswerError) when effectiveDate is missing from the transaction" in {
      val noEffDate = emptyUserAnswers.copy(fullReturn = Some(FullReturn(
        stornId = "STORN", returnResourceRef = "REF",
        transaction = Some(Transaction(effectiveDate = None))
      )))

      toViewModel(
        BigDecimal(25000),
        noEffDate,
        stubbedTimeMachine
      ) mustBe Left(MissingTransactionAnswerError("effectiveDate"))
    }

    "Left(InvalidDateError) when the effectiveDate string isn't a parseable date" in {
      toViewModel(
        BigDecimal(25000),
        answersWith("not-a-date"),
        stubbedTimeMachine
      ) mustBe Left(InvalidDateError("not-a-date"))
    }

  }
}