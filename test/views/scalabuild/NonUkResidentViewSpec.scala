/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package views.scalabuild

import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.html.scalabuild.NonUkResidentView


class NonUkResidentViewSpec extends ViewTestFixture {
  val view: NonUkResidentView = app.injector.instanceOf[views.html.scalabuild.NonUkResidentView]
  val form: Form[_] = app.injector.instanceOf[forms.scalabuild.NonUkResidentFormProvider].apply()
  override val htmlContent: HtmlFormat.Appendable = view.apply(form)(fakeRequest, messages)

  "NonUkResidentView" must {
    "render the correct content" in {

      heading mustBe messages("nonUkResident.heading")
      summaryText must include(messages("nonUkResident.details.title"))
      bodyText must include(messages("nonUkResident.details.d1"))
      bodyText must include(messages("nonUkResident.details.d1.p1"))
      bodyText must include(messages("nonUkResident.details.d2"))
      bodyText must include(messages("nonUkResident.details.d2.p1"))
      bodyText must include(messages("nonUkResident.details.d2.p2"))
      bodyText must include(messages("nonUkResident.details.d2.p3"))

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
