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

package views.taxCalculation.freeholdTaxCalculated

import base.SpecBase
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.taxCalculation.freeholdTaxCalculated.FreeholdTaxCalculatedBYSView

class FreeholdTaxCalculatedBYSViewSpec extends SpecBase {

  "FreeholdTaxCalculatedBYSView" - {

    "must render the page title and heading" in {
      val application = applicationBuilder().build()

      running(application) {
        implicit val msgs: Messages = messages(application)
        implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
        val view = application.injector.instanceOf[FreeholdTaxCalculatedBYSView]

        val doc = Jsoup.parse(view().toString())

        doc.select("title").first().text() must include(msgs("taxCalculation.beforeStart.title.freehold.taxCalculated"))
        doc.select("h1").first().text() mustBe msgs("site.beforeYouStart.heading")
        doc.text() must include(msgs("site.taxCalculation.caption"))
      }
    }

    "must replace bullets 2 and 3 with the selfAssessed bullet" in {
      val application = applicationBuilder().build()

      running(application) {
        implicit val msgs: Messages = messages(application)
        implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
        val view = application.injector.instanceOf[FreeholdTaxCalculatedBYSView]

        val bullets = Jsoup.parse(view().toString()).select("ul.govuk-list--bullet li").eachText()

        bullets must contain inOrderOnly (
          msgs("taxCalculation.beforeStart.bullet1"),
          msgs("taxCalculation.beforeStart.selfAssessed.bullet"),
          msgs("taxCalculation.beforeStart.bullet4"),
          msgs("taxCalculation.beforeStart.bullet5")
        )

        bullets must not contain msgs("taxCalculation.beforeStart.bullet2")
        bullets must not contain msgs("taxCalculation.beforeStart.bullet3")
      }
    }

    "must render the continue button" in {
      val application = applicationBuilder().build()

      running(application) {
        implicit val msgs: Messages = messages(application)
        implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
        val view = application.injector.instanceOf[FreeholdTaxCalculatedBYSView]

        val button = Jsoup.parse(view().toString()).select("a.govuk-button").first()

        button must not be null
        button.text() mustBe msgs("site.continue")
      }
    }
  }
}
