/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package views.scalabuild

import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.html.scalabuild.SharedOwnershipView

class SharedOwnershipViewSpec extends ViewTestFixture {

  val view: SharedOwnershipView = app.injector.instanceOf[views.html.scalabuild.SharedOwnershipView]
  val form: Form[_] = app.injector.instanceOf[forms.scalabuild.SharedOwnershipFormProvider].apply()
  override val htmlContent: HtmlFormat.Appendable = view.apply(form)(fakeRequest, messages)

  "Shared ownership view" must {
    "render the correct content" in {
      heading mustBe messages("sharedOwnership.heading")
      caption mustBe messages("section.sharedOwnership")
      bodyText mustBe messages("sharedOwnership.p1")

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
