/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package views.scalabuild
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.html.scalabuild.ReplaceMainResidenceView

class ReplaceMainResidenceViewSpec extends ViewTestFixture {
  val view: ReplaceMainResidenceView = app.injector.instanceOf[views.html.scalabuild.ReplaceMainResidenceView]
  val form: Form[_] = app.injector.instanceOf[forms.scalabuild.ReplaceMainResidenceFormProvider].apply()
  override val htmlContent: HtmlFormat.Appendable = view.apply(form)(fakeRequest, messages)

  "ReplaceMainResidenceView" must {
    "render the correct content" in {

      heading mustBe messages("replaceMainResidence.heading")
      hintText must include(messages("replaceMainResidence.hint"))

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

