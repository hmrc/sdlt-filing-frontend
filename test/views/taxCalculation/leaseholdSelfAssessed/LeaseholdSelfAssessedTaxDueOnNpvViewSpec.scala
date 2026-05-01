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

package views.taxCalculation.leaseholdSelfAssessed

import base.SpecBase
import forms.taxCalculation.TaxDueOnNpvFormProvider
import models.{Mode, NormalMode}
import org.jsoup.Jsoup
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.html.taxCalculation.leaseholdSelfAssessed.LeaseholdSelfAssessedTaxDueOnNpvView

class LeaseholdSelfAssessedTaxDueOnNpvViewSpec extends SpecBase {

  "LeaseholdSelfAssessedTaxDueOnNpvView" - {

    "must render the page with correct title, heading, and caption" in new Setup {
      val html = view(form, npv, normalMode)
      val doc = Jsoup.parse(html.toString())

      doc.title() must include(messages("taxCalculation.taxDueOnNpv.title"))

      val heading = doc.select("h1.govuk-heading-l")
      heading.size() mustBe 1
      heading.text() mustBe messages("taxCalculation.taxDueOnNpv.heading")

      val caption = doc.select("h2.govuk-caption-l")
      caption.size() mustBe 1
      caption.text mustBe s"This section is ${messages("site.taxCalculation.caption")}"
    }

    // TODO: Split into 2 tests
    "must render one paragraph without a link and one paragraph with a link" in new Setup {
      val html = view(form, npv, normalMode)
      val doc = Jsoup.parse(html.toString())

      val paragraphs = doc.select("p.govuk-body")

      val paragraphOne = paragraphs.get(0)
      paragraphOne.text mustBe messages("taxCalculation.taxDueOnNpv.p1", npv)

      val paragraphTwo = paragraphs.get(1)
      paragraphTwo.text mustBe s"You can ${messages("taxCalculation.taxDueOnNpv.p2.linkText")}."
      paragraphTwo.text must include(messages("taxCalculation.taxDueOnNpv.p2.linkText"))

      val link = doc.select("p.govuk-body a.govuk-link").first()
      link.text() mustBe messages("taxCalculation.taxDueOnNpv.p2.linkText")
      link.attr("href") mustBe messages("taxCalculation.taxDueOnNpv.p2.link")
      link.attr("target") mustBe "_blank"
      link.attr("rel") must include ("noopener")
    }

    // TODO: Add govukInput tests once use of component confirmed by UCD

    "must render the save and continue button" in new Setup {
      val html = view(form, npv, normalMode)
      val doc = Jsoup.parse(html.toString())

      doc.select("button[type=submit]").text mustBe messages("site.save.continue")
    }

    "must render the back link" in new Setup {
      val html = view(form, npv, normalMode)
      val doc = Jsoup.parse(html.toString())

      doc.select("a.govuk-back-link").size() mustBe 1
    }

    // TODO: add error summary test
  }

  trait Setup {
    val app: Application                           = applicationBuilder().build()
    val formProvider                               = new TaxDueOnNpvFormProvider()
    val form: Form[String]                         = formProvider()
    implicit val messages: Messages                =
      play.api.i18n.MessagesImpl(play.api.i18n.Lang.defaultLang, app.injector.instanceOf[play.api.i18n.MessagesApi])
    implicit val request: Request[?]               = FakeRequest()
    val view: LeaseholdSelfAssessedTaxDueOnNpvView = app.injector.instanceOf[LeaseholdSelfAssessedTaxDueOnNpvView]

    val npv = "1897"
    val normalMode: Mode = NormalMode
  }
}