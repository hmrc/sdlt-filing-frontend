/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package views.scalabuild
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.html.scalabuild.EffectiveDateView


class EffectiveDateViewSpec extends ViewTestFixture {
  val view: EffectiveDateView = app.injector.instanceOf[views.html.scalabuild.EffectiveDateView]
  val form: Form[_] = app.injector.instanceOf[forms.scalabuild.EffectiveDateFormProvider].apply()
  override val htmlContent: HtmlFormat.Appendable = view.apply(form)(fakeRequest, messages)

  "EffectiveDateView" must {
    "render the correct content" in {

      heading mustBe messages("effectiveDate.heading")
      hintText must include(messages("effectiveDate.hint.h1"))
      hintText must include(messages("effectiveDate.hint.h2"))
    }
    "render the correct values for the radio button choices" in {
      dateField must include ("Day")
      dateField must include ("Month")
      dateField must include ("Year")

    }
    "render the correct button" in {
      buttonText mustBe  messages("site.continue")
    }
  }
}

