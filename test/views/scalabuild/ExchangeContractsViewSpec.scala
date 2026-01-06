/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package views.scalabuild

import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.html.scalabuild.ExchangeContractsView

class ExchangeContractsViewSpec extends ViewTestFixture {

  val view: ExchangeContractsView = app.injector.instanceOf[views.html.scalabuild.ExchangeContractsView]
  val form: Form[_] = app.injector.instanceOf[forms.scalabuild.ExchangeContractsFormProvider].apply()
  override val htmlContent: HtmlFormat.Appendable = view.apply(form)(fakeRequest, messages)

  "Exchange contracts view" must {
    "render the correct content" in {
      heading mustBe messages("exchangeContracts.heading")
    }

    "render the correct values for the radio button choices" in {
      radios must include(messages("site.yes"))
      radios must include(messages("site.no"))
    }

    "render the correct button" in {
      buttonText mustBe  messages("site.continue")
    }
  }
}