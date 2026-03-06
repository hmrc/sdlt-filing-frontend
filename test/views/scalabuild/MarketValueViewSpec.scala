/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package views.scalabuild

import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.html.scalabuild.MarketValueView

class MarketValueViewSpec extends ViewTestFixture {

  val view: MarketValueView = app.injector.instanceOf[views.html.scalabuild.MarketValueView]
  val form: Form[_] = app.injector.instanceOf[forms.scalabuild.MarketValueFormProvider].apply(625000)
  override val htmlContent: HtmlFormat.Appendable = view.apply(form)(fakeRequest, messages)

  "MarketValueView" must {
    "render the correct content" in {
      caption mustBe messages("section.sharedOwnership")
      pagetitle must include(messages("marketValue.title"))
      heading mustBe messages("marketValue.heading")
      hintText must include(messages("marketValue.hint"))
      bodyText must include(messages("marketValue.p2"))
    }

    "render the correct values for the details component" in {
      bodyText must include(messages("marketValue.details.d1.p1"))
      bodyText must include(messages("marketValue.details.d1.p2"))
      bullets.size() mustBe 2
      bullets.get(0) mustBe messages("marketValue.details.d1.b1")
      bullets.get(1) mustBe messages("marketValue.details.d1.b2")
      bodyText must include(messages("marketValue.details.d1.p3"))
      bodyText must include(messages("marketValue.details.d2.p1"))
      bodyText must include(messages("marketValue.details.d2.p2"))
      bodyText must include(messages("marketValue.details.d2.p3"))
    }

    "render the correct values for the radio button choices" in {
      radios must include(messages("marketValue.PayUpfront"))
      radios must include(messages("marketValue.PayInStages"))
    }

    "render the input field labels correctly" in {
      inputFieldLabel.get(1) mustBe messages("marketValue.PayUpfront.input")
      inputFieldLabel.get(3) mustBe messages("marketValue.PayInStages.input")
    }

    "render the correct button" in {
      buttonText mustBe  messages("site.continue")
    }
  }
}
