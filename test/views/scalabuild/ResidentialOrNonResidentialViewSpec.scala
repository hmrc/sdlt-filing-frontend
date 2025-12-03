/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package views.scalabuild

import models.scalabuild.PropertyType
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.html.scalabuild.ResidentialOrNonResidentialView

class ResidentialOrNonResidentialViewSpec extends ViewTestFixture{
  val view: ResidentialOrNonResidentialView = app.injector.instanceOf[views.html.scalabuild.ResidentialOrNonResidentialView]
  val form: Form[_] = app.injector.instanceOf[forms.scalabuild.ResidentialOrNonResidentialFormProvider].apply()
  override val htmlContent: HtmlFormat.Appendable = view.apply(form)(fakeRequest, messages)

  "ResidentialOrNonResidentialView" must {
    "render the correct content" in {

      heading mustBe messages("propertyType.heading")
      hintText must include(messages("propertyType.hint"))
    }
    "render the correct values for the radio button choices" in {
      PropertyType.values.map(value => radios must include(messages(s"propertyType.${value.toString}")))

    }
    "render the correct button" in {
      buttonText mustBe  messages("site.continue")
    }
  }
}
