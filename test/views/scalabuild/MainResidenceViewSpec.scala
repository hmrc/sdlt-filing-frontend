/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package views.scalabuild
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.html.scalabuild.MainResidenceView

class MainResidenceViewSpec extends ViewTestFixture {
  val view: MainResidenceView = app.injector.instanceOf[views.html.scalabuild.MainResidenceView]
  val form: Form[_] = app.injector.instanceOf[forms.scalabuild.MainResidenceFormProvider].apply()
  override val htmlContent: HtmlFormat.Appendable = view.apply(form)(fakeRequest, messages)

  "MainResidenceView" must {
    "render the correct content" in {
      heading mustBe messages("mainResidence.heading")
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


