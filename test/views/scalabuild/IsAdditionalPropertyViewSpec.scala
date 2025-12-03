/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package views.scalabuild
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.html.scalabuild.IsAdditionalPropertyView


class IsAdditionalPropertyViewSpec extends ViewTestFixture {
  val view: IsAdditionalPropertyView = app.injector.instanceOf[views.html.scalabuild.IsAdditionalPropertyView]
  val form: Form[_] = app.injector.instanceOf[forms.scalabuild.IsAdditionalPropertyFormProvider].apply()
  override val htmlContent: HtmlFormat.Appendable = view.apply(form)(fakeRequest, messages)

  "IsAdditionalPropertyView" must {
    "render the correct content" in {
      heading mustBe messages("isAdditionalProperty.heading")
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