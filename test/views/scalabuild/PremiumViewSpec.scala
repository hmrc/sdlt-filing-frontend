/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package views.scalabuild

import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.html.scalabuild.PremiumView

class PremiumViewSpec extends ViewTestFixture {

  val view: PremiumView = app.injector.instanceOf[views.html.scalabuild.PremiumView]
  val form: Form[_] = app.injector.instanceOf[forms.scalabuild.PremiumFormProvider].apply()
  override val htmlContent: HtmlFormat.Appendable = view.apply(form)(fakeRequest, messages)

  "PremiumView" must {

    "render the correct content" in {
      heading mustBe messages("premium.heading")
      hintText must include(messages("premium.hint"))
      summaryText must include(messages("premium.details.title"))
      bodyText must include(messages("premium.details.para"))
    }

    "render the input field label correctly" in {
      inputFieldLabel.get(0) mustBe messages("premium.heading")
    }

    "render the correct button" in {
      buttonText mustBe  messages("site.continue")
    }
  }
}