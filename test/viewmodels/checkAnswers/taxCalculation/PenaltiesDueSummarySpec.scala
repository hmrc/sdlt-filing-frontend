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

package viewmodels.checkAnswers.taxCalculation

import base.SpecBase
import models.{FullReturn, Transaction}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import utils.TimeMachine
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}

import java.time.LocalDate

class PenaltiesDueSummarySpec extends SpecBase with MockitoSugar {

  private val today = LocalDate.of(2026, 5, 1)

  private def stubbedTimeMachine: TimeMachine = {
    val tm = mock[TimeMachine]
    when(tm.today).thenReturn(today)
    tm
  }

  private def answersWithEffectiveDate(effDate: String) =
    emptyUserAnswers.copy(fullReturn = Some(FullReturn(
      stornId           = "STORN",
      returnResourceRef = "REF",
      transaction       = Some(Transaction(effectiveDate = Some(effDate)))
    )))

  "PenaltiesDueSummary" - {

    "when the transaction is within the filing window" - {

      "must return a summary list row with £0 and no change link" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = answersWithEffectiveDate(today.toString)

          val row = PenaltiesDueSummary.row(Some(userAnswers), stubbedTimeMachine)

          val result = row match {
            case Row(r) => r
            case _ => fail("Expected Row but got Missing")
          }

          result.key.content.asHtml.toString() mustEqual msgs("taxCalculation.genericPenaltiesDue.checkYourAnswersLabel")
          result.value.content.asInstanceOf[HtmlContent].asHtml.toString() mustEqual "£0"
          result.actions mustBe None
        }
      }
    }

    "when the transaction is past the filing window but under 123 days" - {

      "must return a summary list row with £100 and no change link" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = answersWithEffectiveDate(today.minusDays(60).toString)

          val row = PenaltiesDueSummary.row(Some(userAnswers), stubbedTimeMachine)

          val result = row match {
            case Row(r) => r
            case _ => fail("Expected Row but got Missing")
          }

          result.value.content.asInstanceOf[HtmlContent].asHtml.toString() mustEqual "£100"
          result.actions mustBe None
        }
      }
    }

    "when the transaction is 123+ days old" - {

      "must return a summary list row with £200 and no change link" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val userAnswers = answersWithEffectiveDate(today.minusDays(200).toString)

          val row = PenaltiesDueSummary.row(Some(userAnswers), stubbedTimeMachine)

          val result = row match {
            case Row(r) => r
            case _ => fail("Expected Row but got Missing")
          }

          result.value.content.asInstanceOf[HtmlContent].asHtml.toString() mustEqual "£200"
          result.actions mustBe None
        }
      }
    }

    "when the effective date is not present" - {

      "must return a Missing pointing at the return task list when transaction is missing" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = PenaltiesDueSummary.row(Some(emptyUserAnswers), stubbedTimeMachine)

          result match {
            case Missing(call) =>
              call mustEqual controllers.routes.ReturnTaskListController.onPageLoad()

            case Row(_) =>
              fail("Expected Missing but got Row")
          }
        }
      }

      "must return a Missing when UserAnswers is None" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = PenaltiesDueSummary.row(None, stubbedTimeMachine)

          result match {
            case Missing(call) =>
              call mustEqual controllers.routes.ReturnTaskListController.onPageLoad()

            case Row(_) =>
              fail("Expected Missing but got Row")
          }
        }
      }

      "must return a Missing when the effective date in session isn't a parseable date" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val result = PenaltiesDueSummary.row(Some(answersWithEffectiveDate("not-a-date")), stubbedTimeMachine)

          result match {
            case Missing(call) =>
              call mustEqual controllers.routes.ReturnTaskListController.onPageLoad()

            case Row(_) =>
              fail("Expected Missing but got Row")
          }
        }
      }
    }
  }
}
