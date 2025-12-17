/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package views.scalabuild

import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.html.scalabuild.CurrentValueView

class CurrentValueViewSpec extends ViewTestFixture {

  val view: CurrentValueView = app.injector.instanceOf[views.html.scalabuild.CurrentValueView]
  val form: Form[_] = app.injector.instanceOf[forms.scalabuild.CurrentValueFormProvider].apply()
  val ftbLimit = 625000
  override val htmlContent: HtmlFormat.Appendable = view.apply(form, ftbLimit)(fakeRequest, messages)

  "CurrentValueView" must {
    "render the correct content" in {
      caption mustBe messages("section.sharedOwnership")
      heading mustBe messages("currentValue.heading")
      bodyText must include(messages("currentValue.p1"))
    }

    "render the correct values for the radio button choices" in {
      radios must include(messages("currentValue.atOrBelowThreshold", ftbLimit))
      radios must include(messages("currentValue.aboveThreshold", ftbLimit))
      radiosHint must include(messages("currentValue.atOrBelowThreshold.hint"))
    }

    "render the correct button" in {
      buttonText mustBe  messages("site.continue")
    }
  }
}
