/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package views.scalabuild

import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.html.scalabuild.RelevantRentView

class RelevantRentViewSpec extends ViewTestFixture {

  val view: RelevantRentView = app.injector.instanceOf[views.html.scalabuild.RelevantRentView]
  val form: Form[_] = app.injector.instanceOf[forms.scalabuild.RelevantRentFormProvider].apply()
  override val htmlContent: HtmlFormat.Appendable = view.apply(form)(fakeRequest, messages)

  "RelevantRentView" must {

    "render the correct content" in {
      pagetitle must include(messages("relevantRent.title"))
      heading mustBe messages("relevantRent.heading")
    }

    "render the input field label correctly" in {
      inputFieldLabel.get(0) mustBe messages("relevantRent.heading")
    }

    "render the correct button" in {
      buttonText mustBe  messages("site.continue")
    }
  }
}