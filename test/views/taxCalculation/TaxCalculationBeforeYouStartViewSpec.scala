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
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.CSRFTokenHelper.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.taxCalculation.TaxCalculationBeforeYouStartView

class TaxCalculationBeforeYouStartViewSpec extends SpecBase {

  "TaxCalculationBeforeYouStartView" - {

    "must render the page title" in {
      val application = applicationBuilder().build()

      running(application) {
        implicit val messagesInstance: Messages = messages(application)
        implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
        val view = application.injector.instanceOf[TaxCalculationBeforeYouStartView]

        val doc = Jsoup.parse(view(isLeaseholdAndSelfAssessed = false).toString())

        doc.select("title").first().text() must include(messagesInstance("taxCalculation.beforeStart.title"))
      }
    }

    "must render the heading and caption" in {
      val application = applicationBuilder().build()

      running(application) {
        implicit val messagesInstance: Messages = messages(application)
        implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
        val view = application.injector.instanceOf[TaxCalculationBeforeYouStartView]

        val doc = Jsoup.parse(view(isLeaseholdAndSelfAssessed = false).toString())

        doc.select("h1").first().text() mustBe messagesInstance("site.beforeYouStart.heading")
        doc.text() must include(messagesInstance("site.taxCalculation.caption"))
      }
    }

    "must render the two introductory paragraphs" in {
      val application = applicationBuilder().build()

      running(application) {
        implicit val messagesInstance: Messages = messages(application)
        implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
        val view = application.injector.instanceOf[TaxCalculationBeforeYouStartView]

        val doc = Jsoup.parse(view(isLeaseholdAndSelfAssessed = false).toString())

        doc.text() must include(messagesInstance("taxCalculation.beforeStart.p1"))
        doc.text() must include(messagesInstance("taxCalculation.beforeStart.p2"))
      }
    }

    "when isLeaseholdAndSelfAssessed is false" - {

      "must render the standard bullet list (bullet1, bullet2, bullet3, bullet4, bullet5) and omit the leaseholdAndSelfAssessed bullets" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
          val view = application.injector.instanceOf[TaxCalculationBeforeYouStartView]

          val doc = Jsoup.parse(view(isLeaseholdAndSelfAssessed = false).toString())
          val bullets = doc.select("ul.govuk-list--bullet li").eachText()

          bullets must contain(messagesInstance("taxCalculation.beforeStart.bullet1"))
          bullets must contain(messagesInstance("taxCalculation.beforeStart.bullet2"))
          bullets must contain(messagesInstance("taxCalculation.beforeStart.bullet3"))
          bullets must contain(messagesInstance("taxCalculation.beforeStart.bullet4"))
          bullets must contain(messagesInstance("taxCalculation.beforeStart.bullet5"))

          bullets must not contain messagesInstance("taxCalculation.beforeStart.leaseholdAndSelfAssessed.bullet")
        }
      }
    }

    "when isLeaseholdAndSelfAssessed is true" - {

      "must swap bullet2 and bullet3 for the leaseholdAndSelfAssessed bullet" in {
        val application = applicationBuilder().build()

        running(application) {
          implicit val messagesInstance: Messages = messages(application)
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
          val view = application.injector.instanceOf[TaxCalculationBeforeYouStartView]

          val doc = Jsoup.parse(view(isLeaseholdAndSelfAssessed = true).toString())
          val bullets = doc.select("ul.govuk-list--bullet li").eachText()

          bullets must contain(messagesInstance("taxCalculation.beforeStart.bullet1"))
          bullets must contain(messagesInstance("taxCalculation.beforeStart.leaseholdAndSelfAssessed.bullet"))
          bullets must contain(messagesInstance("taxCalculation.beforeStart.bullet4"))
          bullets must contain(messagesInstance("taxCalculation.beforeStart.bullet5"))

          bullets must not contain messagesInstance("taxCalculation.beforeStart.bullet2")
        }
      }
    }

    "must render the continue button inside a form posting to onSubmit" in {
      val application = applicationBuilder().build()

      running(application) {
        implicit val messagesInstance: Messages = messages(application)
        implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
        val view = application.injector.instanceOf[TaxCalculationBeforeYouStartView]

        val doc = Jsoup.parse(view(isLeaseholdAndSelfAssessed = false).toString())

        val form = doc.select("form").first()
        form.attr("method").toLowerCase mustBe "post"
        form.attr("action") mustBe controllers.taxCalculation.routes.TaxCalculationBeforeYouStartController.onSubmit().url

        val button = doc.select("button[type=submit]").first()
        button.text() mustBe messagesInstance("site.continue")
      }
    }
  }
}
