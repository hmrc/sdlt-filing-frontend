/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package views.scalabuild
import models.scalabuild.Tenancy
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.html.scalabuild.FreeholdOrLeaseholdView


class FreeholdOrLeaseholdViewSpec extends ViewTestFixture {
  val view: FreeholdOrLeaseholdView = app.injector.instanceOf[views.html.scalabuild.FreeholdOrLeaseholdView]
  val form: Form[_] = app.injector.instanceOf[forms.scalabuild.FreeholdOrLeaseholdFormProvider].apply()
  override val htmlContent: HtmlFormat.Appendable = view.apply(form)(fakeRequest, messages)

  "FreeholdOrLeaseholdView" must {
    "render the correct content" in {

      heading mustBe messages("tenancy.heading")
      hintText must include(messages("tenancy.hint"))
    }
    "render the correct values for the radio button choices" in {
      Tenancy.values.map( value => radios must include(messages(s"$value")))

    }
    "render the correct button" in {
      buttonText mustBe  messages("site.continue")
    }
  }

}
