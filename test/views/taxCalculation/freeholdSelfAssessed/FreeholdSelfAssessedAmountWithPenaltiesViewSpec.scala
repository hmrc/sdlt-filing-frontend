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
import forms.taxCalculation.PenaltiesAndInterestFormProvider
import org.jsoup.Jsoup
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import views.html.taxCalculation.freeholdSelfAssessed.FreeholdSelfAssessedAmountWithPenaltiesView

class FreeholdSelfAssessedAmountWithPenaltiesViewSpec extends SpecBase {

  trait Fixture {
    val form = new PenaltiesAndInterestFormProvider()()
    val application: Application = applicationBuilder().build()
  }

  "FreeholdSelfAssessedAmountWithPenaltiesView" - {

    "must render the page: title | heading | caption" in new Fixture {

      running(application) {
        implicit val msgs: Messages = messages(application)
        implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
        val view = application.injector.instanceOf[FreeholdSelfAssessedAmountWithPenaltiesView]

        val doc = Jsoup.parse(view(form).toString())

        doc.select("title").first().text() must include( msgs("taxCalculation.penaltiesAndInterest.scenario-one.title") )
        doc.select("h1").first().text() mustBe msgs("taxCalculation.penaltiesAndInterest.scenario-one.heading")
        doc.text() must include(msgs("site.taxCalculation.caption"))
      }
    }

    "must render the save and continue button" in new Fixture {

      running(application) {
        implicit val msgs: Messages = messages(application)
        implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
        val view = application.injector.instanceOf[FreeholdSelfAssessedAmountWithPenaltiesView]

        val button = Jsoup.parse(view(form).toString()).select("button.govuk-button").first()
        button.text() mustBe msgs("taxCalculation.penaltiesAndInterest.button")
      }
    }

    "must render Yes / No radio button" in new Fixture {

      running(application) {
        implicit val msgs: Messages = messages(application)
        implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
        val view = application.injector.instanceOf[FreeholdSelfAssessedAmountWithPenaltiesView]

        val doc = Jsoup.parse(view(form).toString())

        val yesRadio = doc.getElementById("value_0")
        yesRadio.attr("value") mustBe "penaltiesAndInterestYes"
        yesRadio.parent().text() must include(messagesInstance("site.yes"))

        val noRadio = doc.getElementById("value_1")
        noRadio.attr("value") mustBe "penaltiesAndInterestNo"
        noRadio.parent().text() must include(messagesInstance("site.no"))
      }
    }
  }
}
