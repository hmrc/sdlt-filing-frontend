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
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import views.html.taxCalculation.TaxCalculationConfirmEffectiveDateOfTransactionView

class TaxCalculationConfirmEffectiveDateOfTransactionControllerViewSpec extends SpecBase {

  trait Fixture {
    val application: Application = applicationBuilder().build()
  }

  "TaxCalculationConfirmEffectiveDateOfTransactionView" - {

    "must render the page: title | heading | caption" in new Fixture {

      running(application) {
        implicit val msgs: Messages = messages(application)
        implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
        val view = application.injector.instanceOf[TaxCalculationConfirmEffectiveDateOfTransactionView]

        val doc = Jsoup.parse(view(nextPageUrl = "someUrl").toString())

        doc.select("title").first().text() must include( msgs("taxCalculation.confirm.effectiveDateOfTransaction.title") )
        doc.select("h1").first().text() mustBe msgs("taxCalculation.confirm.effectiveDateOfTransaction.heading")
        doc.select("p").toArray().tail.head.asInstanceOf[Element].text() mustBe msgs("taxCalculation.confirm.effectiveDateOfTransaction.p1")
        doc.select("p").toArray().tail.tail.head.asInstanceOf[Element].text() mustBe msgs("taxCalculation.confirm.effectiveDateOfTransaction.p2")
        doc.text() must include(msgs("site.taxCalculation.caption"))
      }
    }

  }
}

