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

package views.taxCalculation.freeholdSelfAssessed

import base.SpecBase
import controllers.taxCalculation.PenaltiesAndInterestExtension
import forms.taxCalculation.PenaltiesAndInterestFormProvider
import models.taxCalculation.TaxCalculationFlow
import models.{NormalMode, UserAnswers}
import models.taxCalculation.TaxCalculationFlow.*
import org.jsoup.Jsoup
import pages.taxCalculation.TaxCalculationFlowPage
import pages.taxCalculation.freeholdTaxCalculated.FreeholdTaxCalculatedPenaltiesAndInterestPage
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import views.html.taxCalculation.AmountWithPenaltiesView

class FreeholdCalculatedAmountWithPenaltiesViewSpec extends SpecBase {

  trait Fixture extends PenaltiesAndInterestExtension{
    val form = new PenaltiesAndInterestFormProvider()()
    val answersFreeHoldWithUserChoice: UserAnswers = emptyUserAnswers.set(TaxCalculationFlowPage,
        TaxCalculationFlow.FreeholdTaxCalculated).success.value
      .set(FreeholdTaxCalculatedPenaltiesAndInterestPage, true).success.value
  }

  "Scenario 2 page: Tax calculation – Tax calculation – Freehold not calculated" - {

    "must render the page: title | heading | caption" in new Fixture {
      val application: Application = applicationBuilder().build()
      running(application) {
        implicit val msgs: Messages = messages(application)
        implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
        val view = application.injector.instanceOf[AmountWithPenaltiesView]

        val doc = Jsoup.parse(view(form, pageTitle = getPageTitle(flow = FreeholdSelfAssessed), postAction(FreeholdSelfAssessed, NormalMode)).toString())

        doc.select("title").first().text() must include( msgs("taxCalculation.penaltiesAndInterest.freehold-tax-not-calculated.title") )
        doc.select("h1").first().text() mustBe msgs("taxCalculation.penaltiesAndInterest.freehold-tax-calculated.heading")
        doc.text() must include(msgs("site.taxCalculation.caption"))
      }
    }

    "must render the save and continue button" in new Fixture {
      val application: Application = applicationBuilder().build()
      running(application) {
        implicit val msgs: Messages = messages(application)
        implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
        val view = application.injector.instanceOf[AmountWithPenaltiesView]

        val button = Jsoup.parse(view(form, pageTitle = getPageTitle(flow = FreeholdSelfAssessed), postAction(FreeholdSelfAssessed, NormalMode)).toString()).select("button.govuk-button").first()
        button.text() mustBe msgs("taxCalculation.penaltiesAndInterest.button")
      }
    }

    "must render Yes / No radio button: with preselected userAnswer" in new Fixture {
      val application: Application = applicationBuilder(userAnswers = Some(answersFreeHoldWithUserChoice)).build()
      running(application) {
        implicit val msgs: Messages = messages(application)
        implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
        val view = application.injector.instanceOf[AmountWithPenaltiesView]

        val doc = Jsoup.parse(view(form, pageTitle = getPageTitle(flow = FreeholdSelfAssessed), postAction(FreeholdSelfAssessed, NormalMode)).toString())

        val yesRadio = doc.getElementById("value")
        yesRadio.attr("value") mustBe "true"
        yesRadio.parent().text() must include(msgs("site.yes"))

        val noRadio = doc.getElementById("value-2")
        noRadio.attr("value") mustBe "false"
        noRadio.parent().text() must include(msgs("site.no"))
      }
    }
  }
}
