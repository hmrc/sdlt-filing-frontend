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

package views.taxCalculation

import base.SpecBase
import models.taxCalculation.TaxCalculationFlow
import models.taxCalculation.TaxCalculationFlow.{FreeholdSelfAssessed, FreeholdTaxCalculated, LeaseholdSelfAssessed, LeaseholdTaxCalculated}
import models.{FullReturn, Transaction, UserAnswers}
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.taxCalculation.freeholdSelfAssessed.*
import pages.taxCalculation.freeholdTaxCalculated.{FreeholdTaxCalculatedPenaltiesAndInterestPage, FreeholdTaxCalculatedSelfAssessedAmountPage, FreeholdTaxCalculatedTotalAmountDuePage}
import pages.taxCalculation.leaseholdSelfAssessed.{LeaseholdSelfAssessedNpvTaxPage, LeaseholdSelfAssessedPenaltiesAndInterestPage, LeaseholdSelfAssessedPremiumPayableTaxPage, LeaseholdSelfAssessedTotalAmountDuePage}
import pages.taxCalculation.leaseholdTaxCalculated.{LeaseholdTaxCalculatedPenaltiesAndInterestPage, LeaseholdTaxCalculatedSelfAssessedAmountPage, LeaseholdTaxCalculatedTotalAmountDuePage}
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import utils.TimeMachine
import viewmodels.checkAnswers.summary.SummaryRowResult
import viewmodels.checkAnswers.summary.SummaryRowResult.Row
import viewmodels.checkAnswers.taxCalculation.*
import viewmodels.govuk.all.SummaryListViewModel
import views.html.taxCalculation.shared.CheckYourAnswersView

import java.time.LocalDate

class TaxCalculationCheckYourAnswersViewSpec extends SpecBase with MockitoSugar {

  trait Fixture {
    val application: Application = applicationBuilder().build()

    implicit val msgs: Messages = messages(application)
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

    val sdltAmountDue = "27,500.00"
    val freeholdTaxCalculatedSelfAssessedAmount = "12345"
    val freeholdSelfAssessedCalculatedSelfAssessedAmount = "56788"
    val leaseholdSelfAssessedPremiumPayableTaxPageAmount = "7013"

    val today: LocalDate = LocalDate.of(2026, 5, 1)

    val freeHoldUserAnswers: UserAnswers = emptyUserAnswers
      .set(FreeholdTaxCalculatedSelfAssessedAmountPage, freeholdTaxCalculatedSelfAssessedAmount)
      .success.value
      .copy(fullReturn = Some(FullReturn(
        stornId = "STORN",
        returnResourceRef = "REF",
        transaction = Some(Transaction(effectiveDate = Some(today.toString)))
      )))
      .set(FreeholdTaxCalculatedTotalAmountDuePage, "43950").success.value
      .set(FreeholdTaxCalculatedPenaltiesAndInterestPage, true).success.value

    val freeholdSelfAssessedUserAnswers: UserAnswers = emptyUserAnswers
      .copy(fullReturn = Some(FullReturn(
        stornId = "STORN",
        returnResourceRef = "REF",
        transaction = Some(Transaction(effectiveDate = Some(today.toString)))
      )))
      .set(FreeholdSelfAssessedAmountPage, "43957").success.value
      .set(FreeholdSelfAssessedPenaltiesAndInterestPage, true).success.value
      .set(FreeholdSelfAssessedTotalAmountDuePage, "12358").success.value


    val leaseholdSelfAssessedUserAnswer: UserAnswers = emptyUserAnswers
      .set(LeaseholdSelfAssessedPremiumPayableTaxPage, leaseholdSelfAssessedPremiumPayableTaxPageAmount)
      .success.value
      .copy(fullReturn = Some(FullReturn(
        stornId = "STORN",
        returnResourceRef = "REF",
        transaction = Some(Transaction(effectiveDate = Some(today.toString)))
      )))
      .set(LeaseholdSelfAssessedNpvTaxPage, "1891").success.value
      .set(LeaseholdSelfAssessedTotalAmountDuePage, "999").success.value
      .set(LeaseholdSelfAssessedPenaltiesAndInterestPage, false).success.value

    val leaseholdTaxCalculatedUserAnswer: UserAnswers = emptyUserAnswers
      .set(LeaseholdTaxCalculatedSelfAssessedAmountPage, "1191")
      .success.value
      .copy(fullReturn = Some(FullReturn(
        stornId = "STORN",
        returnResourceRef = "REF",
        transaction = Some(Transaction(effectiveDate = Some(today.toString)))
      )))
      .set(LeaseholdTaxCalculatedTotalAmountDuePage, "389").success.value
      .set(LeaseholdTaxCalculatedPenaltiesAndInterestPage, false).success.value

    def stubbedTimeMachine: TimeMachine = {
      val tm = mock[TimeMachine]
      when(tm.today).thenReturn(today)
      tm
    }

    def buildSummaryList(flow: TaxCalculationFlow)
                        (implicit messages: Messages): SummaryList = {
      val summaryRows = flow match {
        case FreeholdTaxCalculated =>
          Seq(
            CalculatedSdltDueSummary.row(sdltAmountDue),
            FreeholdTaxCalculatedSelfAssessedAmountSummary.row(Some(freeHoldUserAnswers)),
            PenaltiesDueSummary.row(Some(freeHoldUserAnswers), stubbedTimeMachine),
            FreeholdTaxCalculatedTotalAmountDueSummary.row(Some(freeHoldUserAnswers)),
            FreeholdTaxCalculatedDoesAmountIncludePenaltiesSummary.row(Some(freeHoldUserAnswers))
          ).collect {
            case Row(r) => r
          }
        case FreeholdSelfAssessed =>
          Seq(
            FreeholdSelfAssessedAmountSummary.row(Some(freeholdSelfAssessedUserAnswers)),
            PenaltiesDueSummary.row(Some(freeholdSelfAssessedUserAnswers), stubbedTimeMachine),
            FreeholdSelfAssessedTotalAmountDueSummary.row(Some(freeholdSelfAssessedUserAnswers)),
            FreeholdSelfAssessedDoesAmountIncludePenaltiesSummary.row(Some(freeholdSelfAssessedUserAnswers))
          ).collect {
            case Row(r) => r
          }
        case LeaseholdSelfAssessed =>
          Seq(
            PremiumPayableTaxSummary.row(Some(leaseholdSelfAssessedUserAnswer)),
            TaxDueOnNpvSummary.row(leaseholdSelfAssessedUserAnswer),
            LeaseholdSelfAssessedTotalAmountDueSummary.row(Some(leaseholdSelfAssessedUserAnswer)),
            LeaseholdSelfAssessedDoesAmountIncludePenaltiesSummary.row(Some(leaseholdSelfAssessedUserAnswer))
          ).collect {
            case Row(r) => r
          }
        case LeaseholdTaxCalculated =>
          Seq(
            CalculatedSdltDueSummary.row("7179"),
            LeaseholdTaxCalculatedSelfAssessedAmountSummary.row(Some(leaseholdTaxCalculatedUserAnswer)),
            PenaltiesDueSummary.row(Some(leaseholdTaxCalculatedUserAnswer), stubbedTimeMachine),
            LeaseholdTaxCalculatedTotalAmountDueSummary.row(Some(leaseholdTaxCalculatedUserAnswer)),
            LeaseholdTaxCalculatedDoesAmountIncludePenaltiesSummary.row(Some(leaseholdTaxCalculatedUserAnswer))
          ).collect {
            case Row(r) => r
          }
      }
      SummaryListViewModel(summaryRows)
    }
  }

  "CheckYourAnswersView" - {

    "FreeholdTaxCalculated :: must render the page: with expected user answers" in new Fixture {
      running(application) {

        val view = application.injector.instanceOf[CheckYourAnswersView]
        val viewModel = buildSummaryList(FreeholdTaxCalculated)

        val doc = Jsoup.parse(view(viewModel).toString())

        doc.select("title").first().text() must include(msgs("taxCalculation.checkYourAnswers.title"))
        doc.text() must include(sdltAmountDue)
        doc.text() must include(freeholdTaxCalculatedSelfAssessedAmount)
        doc.text() must include("Penalties due £0")
        doc.text() must include("Amount to be paid £43950")
        doc.text() must include("Change amount to be paid Does the amount include penalties and interest? Yes")

        doc.select("p").toArray().tail.head.asInstanceOf[Element].text() mustBe msgs("taxCalculation.checkYourAnswers.declaration.text")
        doc.select("h2").toArray().tail.head.asInstanceOf[Element].text() mustBe msgs("taxCalculation.checkYourAnswers.declaration")
      }
    }

    "FreeholdSelfAssessed :: must render the page: with expected user answers" in new Fixture {
      running(application) {

        val view = application.injector.instanceOf[CheckYourAnswersView]
        val viewModel = buildSummaryList(FreeholdSelfAssessed)

        val doc = Jsoup.parse(view(viewModel).toString())

        doc.select("title").first().text() must include(msgs("taxCalculation.checkYourAnswers.title"))

        doc.text() must include("Self-assessed SDLT due £43957")
        doc.text() must include("Penalties due £0")
        doc.text() must include("Amount to be paid £12358")
        doc.text() must include("Change amount to be paid Does the amount include penalties and interest? Yes")

        doc.select("p").toArray().tail.head.asInstanceOf[Element].text() mustBe msgs("taxCalculation.checkYourAnswers.declaration.text")
        doc.select("h2").toArray().tail.head.asInstanceOf[Element].text() mustBe msgs("taxCalculation.checkYourAnswers.declaration")
      }
    }

    "LeaseholdTaxCalculated :: must render the page: with expected user answers" in new Fixture {
      running(application) {

        val view = application.injector.instanceOf[CheckYourAnswersView]
        val viewModel = buildSummaryList(LeaseholdTaxCalculated)

        val doc = Jsoup.parse(view(viewModel).toString())

        doc.select("title").first().text() must include(msgs("taxCalculation.checkYourAnswers.title"))
        doc.text() must include("HMRC calculated SDLT due £7179")
        doc.text() must include("Self-assessed SDLT due £1191")
        doc.text() must include("Penalties due £0")
        doc.text() must include("Amount to be paid £389")
        doc.text() must include("Change amount to be paid Does the amount include penalties and interest? No")

        doc.select("p").toArray().tail.head.asInstanceOf[Element].text() mustBe msgs("taxCalculation.checkYourAnswers.declaration.text")
        doc.select("h2").toArray().tail.head.asInstanceOf[Element].text() mustBe msgs("taxCalculation.checkYourAnswers.declaration")
      }
    }

    "LeaseholdSelfAssessed :: must render the page: with expected user answers" in new Fixture {
      running(application) {

        val view = application.injector.instanceOf[CheckYourAnswersView]
        val viewModel = buildSummaryList(LeaseholdSelfAssessed)

        val doc = Jsoup.parse(view(viewModel).toString())

        doc.select("title").first().text() must include(msgs("taxCalculation.checkYourAnswers.title"))

        doc.text() must include("Tax due on premium payable £7013")
        doc.text() must include("tax due on premium payable Tax due on NPV £1891")
        doc.text() must include("tax due on NPV Amount to be paid £999")
        doc.text() must include("Change amount to be paid Does the amount include penalties and interest? No")

        doc.select("p").toArray().tail.head.asInstanceOf[Element].text() mustBe msgs("taxCalculation.checkYourAnswers.declaration.text")
        doc.select("h2").toArray().tail.head.asInstanceOf[Element].text() mustBe msgs("taxCalculation.checkYourAnswers.declaration")
      }
    }

  }

}
