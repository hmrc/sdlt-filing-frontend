/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package views.scalabuild
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.html.scalabuild.IsPurchaserIndividualView


class IsPurchaserIndividualViewSpec extends ViewTestFixture {
  val view: IsPurchaserIndividualView = app.injector.instanceOf[views.html.scalabuild.IsPurchaserIndividualView]
  val form: Form[_] = app.injector.instanceOf[forms.scalabuild.IsPurchaserIndividualFormProvider].apply()
  override val htmlContent: HtmlFormat.Appendable = view.apply(form)(fakeRequest, messages)

  "IsPurchaserIndividualView" must {
    "render the correct content" in {
      pagetitle must include(messages("isPurchaserIndividual.title"))
      heading mustBe messages("isPurchaserIndividual.heading")
      hintText must include(messages("isPurchaserIndividual.hint"))

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

