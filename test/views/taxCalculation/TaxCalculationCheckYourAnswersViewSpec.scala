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
import models.taxCalculation.TaxCalculationFlow.*
import models.taxCalculation.*
import models.{FullReturn, Transaction, UserAnswers}
import org.jsoup.nodes.Element
import org.jsoup.{Jsoup, nodes}
import org.mockito.Mockito.when
import org.scalatest.Assertion
import org.scalatestplus.mockito.MockitoSugar
import pages.taxCalculation.freeholdSelfAssessed.*
import pages.taxCalculation.freeholdTaxCalculated.*
import pages.taxCalculation.leaseholdSelfAssessed.*
import pages.taxCalculation.leaseholdTaxCalculated.*
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

    val sdltAmountDue = "27500"
    val freeholdTaxCalculatedSelfAssessedAmount = "12345"
    val freeholdSelfAssessedCalculatedSelfAssessedAmount = "56788"
    val leaseholdSelfAssessedPremiumPayableTaxPageAmount = "7013"

    val calculatedResultOfTaxTypePremium = TaxCalculationResult(
      totalTax = 43750,
      None,
      None,
      None,
      taxCalcs = Seq(CalculationDetails(
        TaxTypes.premium,
        CalcTypes.slab,
        taxDue = 43750,
        None, None, None, Some(5), None, None))
    )

    val calculatedResultOfTaxTypeRent = TaxCalculationResult(
      totalTax = 13758,
      None,
      None,
      None,
      taxCalcs = Seq(CalculationDetails(
        TaxTypes.rent,
        CalcTypes.slab,
        taxDue = 13758,
        None, None, None, Some(5), None, None))
    )

    val calculatedResultForBoth = TaxCalculationResult(
      totalTax = 43750,
      None,
      None,
      None,
      taxCalcs = Seq(
        CalculationDetails(
          TaxTypes.premium,
          CalcTypes.slab,
          taxDue = 43750,
          None, None, None, Some(5), None, None),
        CalculationDetails(
          TaxTypes.rent,
          CalcTypes.slab,
          taxDue = 13758,
          None, None, None, Some(5), None, None)
      )
    )

    val today: LocalDate = LocalDate.parse("2026-05-01")

    val freeHoldUserAnswers: UserAnswers = emptyUserAnswers
      .set(FreeholdTaxCalculatedSelfAssessedAmountPage, freeholdTaxCalculatedSelfAssessedAmount)
      .success.value
      .copy(fullReturn = Some(FullReturn(
        stornId = "STORN",
        returnResourceRef = "REF",
        transaction = Some(Transaction(effectiveDate = Some("2026-05-01")))
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

    // => Option to change current dateAndTime in order to show penalties
    def stubbedTimeMachine(date: LocalDate = today): TimeMachine = {
      val tm = mock[TimeMachine]
      when(tm.today).thenReturn(date)
      tm
    }

    def buildSummaryList(flow: TaxCalculationFlow, taxCalcResult: Option[TaxCalculationResult] = None)
                        (implicit messages: Messages): SummaryList = {
      val summaryRows = flow match {
        case FreeholdTaxCalculated =>
          Seq(
            CalculatedSdltDueSummary.row(sdltAmountDue),
            FreeholdTaxCalculatedSelfAssessedAmountSummary.row(Some(freeHoldUserAnswers)),
            PenaltiesDueSummary.row(Some(freeHoldUserAnswers), stubbedTimeMachine(today.plusDays(365))),
            FreeholdTaxCalculatedTotalAmountDueSummary.row(Some(freeHoldUserAnswers)),
            FreeholdTaxCalculatedDoesAmountIncludePenaltiesSummary.row(Some(freeHoldUserAnswers))
          ).collect {
            case Row(r) => r
          }
        case FreeholdSelfAssessed =>
          Seq(
            FreeholdSelfAssessedAmountSummary.row(Some(freeholdSelfAssessedUserAnswers)),
            PenaltiesDueSummary.row(Some(freeholdSelfAssessedUserAnswers), stubbedTimeMachine()),
            FreeholdSelfAssessedTotalAmountDueSummary.row(Some(freeholdSelfAssessedUserAnswers)),
            FreeholdSelfAssessedDoesAmountIncludePenaltiesSummary.row(Some(freeholdSelfAssessedUserAnswers))
          ).collect {
            case Row(r) => r
          }
        case LeaseholdSelfAssessed =>
          Seq(
            PremiumPayableTaxSummary.row(Some(leaseholdSelfAssessedUserAnswer)),
            TaxDueOnNpvSummary.row(leaseholdSelfAssessedUserAnswer),
            PenaltiesDueSummary.row(Some(leaseholdSelfAssessedUserAnswer), stubbedTimeMachine()),
            LeaseholdSelfAssessedTotalAmountDueSummary.row(Some(leaseholdSelfAssessedUserAnswer)),
            LeaseholdSelfAssessedDoesAmountIncludePenaltiesSummary.row(Some(leaseholdSelfAssessedUserAnswer))
          ).collect {
            case Row(r) => r
          }
        case LeaseholdTaxCalculated =>
          Seq(
            taxCalcResult.flatMap(LeaseholdTaxCalculatedPremiumPayableSummary.row(_)),
            taxCalcResult.flatMap(LeaseholdTaxCalculatedNpvSummary.row(_)),
            Some(CalculatedSdltDueSummary.row("7179")),
            Some(LeaseholdTaxCalculatedSelfAssessedAmountSummary.row(Some(leaseholdTaxCalculatedUserAnswer))),
            Some(PenaltiesDueSummary.row(Some(leaseholdTaxCalculatedUserAnswer), stubbedTimeMachine(today.plusDays(100)))),
            Some(LeaseholdTaxCalculatedTotalAmountDueSummary.row(Some(leaseholdTaxCalculatedUserAnswer))),
            Some(LeaseholdTaxCalculatedDoesAmountIncludePenaltiesSummary.row(Some(leaseholdTaxCalculatedUserAnswer)))
          ).flatten.collect {
            case Row(r) => r
          }
      }
      SummaryListViewModel(summaryRows)
    }

    def sharedViewTest(doc: nodes.Document): Assertion = {
      doc.select("title").first().text() must include(msgs("taxCalculation.checkYourAnswers.title"))

      doc.select("p").toArray().tail.head.asInstanceOf[Element].text() mustBe msgs("taxCalculation.checkYourAnswers.declaration.text")
      doc.select("h2").toArray().tail.head.asInstanceOf[Element].text() mustBe msgs("taxCalculation.checkYourAnswers.declaration")
      doc.select("button[type=submit]").first().text() mustBe msgs("Confirm and continue")
    }
  }

  "CheckYourAnswersView" - {

    "FreeholdTaxCalculated with expected user answers : must render the page" in new Fixture {
      running(application) {

        val view = application.injector.instanceOf[CheckYourAnswersView]
        val viewModel = buildSummaryList(FreeholdTaxCalculated)

        val doc: nodes.Document = Jsoup.parse(view(viewModel).toString())
        sharedViewTest(doc)

        doc.text() must include("HMRC calculated SDLT due £27,500")
        doc.text() must include("Self-assessed SDLT due £12,345")
        doc.text() must include("self-assessed SDLT due Penalties due £200")
        doc.text() must include("Amount to be paid £43,950")
        doc.text() must include("Change Amount to be paid Does the amount include penalties and interest? Yes")
      }
    }

    "FreeholdSelfAssessed with expected user answers : must render the page" in new Fixture {
      running(application) {

        val view = application.injector.instanceOf[CheckYourAnswersView]
        val viewModel = buildSummaryList(FreeholdSelfAssessed)

        val doc = Jsoup.parse(view(viewModel).toString())
        sharedViewTest(doc)

        doc.text() must include("Self-assessed SDLT due £43,957")
        doc.text() must include("Penalties due £0")
        doc.text() must include("Amount to be paid £12,358")
        doc.text() must include("Change Amount to be paid Does the amount include penalties and interest? Yes")
      }
    }

    "LeaseholdTaxCalculated with calcResult of TaxType::Premium : must render the page" in new Fixture {
      running(application) {

        val view = application.injector.instanceOf[CheckYourAnswersView]
        val viewModel = buildSummaryList(LeaseholdTaxCalculated, Some(calculatedResultOfTaxTypePremium))

        val doc = Jsoup.parse(view(viewModel).toString())
        sharedViewTest(doc)

        doc.text() must include("HMRC calculated SDLT due £7,179")
        doc.text() must include("Self-assessed SDLT due £1,191")
        doc.text() must include("Penalties due £100")
        doc.text() must include("Amount to be paid £389")
        doc.text() must include("Change Amount to be paid Does the amount include penalties and interest? No")
        doc.text() must include("Tax due on total premium payable £43,750")
        doc.text() mustNot include("Tax due on NPV £43,750")
      }
    }

    "LeaseholdTaxCalculated with calcResult of TaxType::Rent : must render the page" in new Fixture {
      running(application) {

        val view = application.injector.instanceOf[CheckYourAnswersView]
        val viewModel = buildSummaryList(LeaseholdTaxCalculated, Some(calculatedResultOfTaxTypeRent))

        val doc = Jsoup.parse(view(viewModel).toString())
        sharedViewTest(doc)

        doc.text() must include("HMRC calculated SDLT due £7,179")
        doc.text() must include("Self-assessed SDLT due £1,191")
        doc.text() must include("Penalties due £100")
        doc.text() must include("Amount to be paid £389")
        doc.text() must include("Change Amount to be paid Does the amount include penalties and interest? No")
        doc.text() mustNot include("Tax due on total premium payable £43,750")
        doc.text() must include("Tax due on NPV £13,758")
      }
    }

    "LeaseholdTaxCalculated with calcResult is None: must render the page" in new Fixture {
      running(application) {

        val view = application.injector.instanceOf[CheckYourAnswersView]
        val viewModel = buildSummaryList(LeaseholdTaxCalculated, None)

        val doc = Jsoup.parse(view(viewModel).toString())
        sharedViewTest(doc)

        doc.text() must include("HMRC calculated SDLT due £7,179")
        doc.text() must include("Self-assessed SDLT due £1,191")
        doc.text() must include("Penalties due £100")
        doc.text() must include("Amount to be paid £389")
        doc.text() must include("Change Amount to be paid Does the amount include penalties and interest? No")
        doc.text() mustNot include("Tax due on total premium payable £43,750")
        doc.text() mustNot include("Tax due on NPV £43,750")
      }
    }

    "LeaseholdTaxCalculated with calcResult of TaxType::Premium and Rent : must render the page" in new Fixture {
      running(application) {

        val view = application.injector.instanceOf[CheckYourAnswersView]
        val viewModel = buildSummaryList(LeaseholdTaxCalculated, Some(calculatedResultForBoth))

        val doc = Jsoup.parse(view(viewModel).toString())
        sharedViewTest(doc)

        doc.text() must include("Tax due on NPV £13,758")
        doc.text() must include("HMRC calculated SDLT due £7,179")
        doc.text() must include("Self-assessed SDLT due £1,191")
        doc.text() must include("Penalties due £100")
        doc.text() must include("Amount to be paid £389")
        doc.text() must include("Change Amount to be paid Does the amount include penalties and interest? No")
        doc.text() must include("Tax due on total premium payable £43,750")
        doc.text() mustNot include("Tax due on NPV £43,750")
      }
    }

    "LeaseholdSelfAssessed with expected user answers: must render the page" in new Fixture {
      running(application) {

        val view = application.injector.instanceOf[CheckYourAnswersView]
        val viewModel = buildSummaryList(LeaseholdSelfAssessed)

        val doc = Jsoup.parse(view(viewModel).toString())
        sharedViewTest(doc)

        doc.text() must include("Tax due on premium payable £7,013")
        doc.text() must include("Tax due on NPV £1,891")
        doc.text() must include("Tax due on NPV Penalties due £0")
        doc.text() must include("Amount to be paid £999")
        doc.text() must include("Change Amount to be paid Does the amount include penalties and interest? No")
      }
    }

  }

}
