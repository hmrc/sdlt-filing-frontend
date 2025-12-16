/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package views.scalabuild
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.html.scalabuild.OwnsOtherPropertiesView

class OwnsOtherPropertiesViewSpec extends ViewTestFixture {
  val view: OwnsOtherPropertiesView = app.injector.instanceOf[views.html.scalabuild.OwnsOtherPropertiesView]
  val form: Form[_] = app.injector.instanceOf[forms.scalabuild.OwnsOtherPropertiesFormProvider].apply()
  override val htmlContent: HtmlFormat.Appendable = view.apply(form)(fakeRequest, messages)

  "OwnsOtherPropertiesView" must {
    "render the correct content" in {

      heading mustBe messages("ownsOtherProperties.heading")
      bodyText must include(messages("ownsOtherProperties.content"))
      bullet must include(messages("ownsOtherProperties.bullet.b1"))
      bullet must include(messages("ownsOtherProperties.bullet.b2"))
      bullet must include(messages("ownsOtherProperties.bullet.b3"))
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

