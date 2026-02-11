/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package views.scalabuild
import models.scalabuild.HoldingTypes
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.html.scalabuild.FreeholdOrLeaseholdView


class FreeholdOrLeaseholdViewSpec extends ViewTestFixture {

  val view: FreeholdOrLeaseholdView = app.injector.instanceOf[views.html.scalabuild.FreeholdOrLeaseholdView]
  val form: Form[_] = app.injector.instanceOf[forms.scalabuild.FreeholdOrLeaseholdFormProvider].apply()
  override val htmlContent: HtmlFormat.Appendable = view.apply(form)(fakeRequest, messages)

  "FreeholdOrLeaseholdView" must {
    "render the correct content" in {
      pagetitle must include(messages("holding.title"))
      heading mustBe messages("holding.heading")
      hintText must include(messages("holding.hint"))
    }
    "render the correct values for the radio button choices" in {
      HoldingTypes.values.map(value => radios must include(messages(s"$value")))

    }
    "render the correct button" in {
      buttonText mustBe  messages("site.continue")
    }
  }
}