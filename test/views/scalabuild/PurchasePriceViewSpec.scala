/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package views.scalabuild
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.html.scalabuild.PurchasePriceView

class PurchasePriceViewSpec extends ViewTestFixture {

  val view: PurchasePriceView = app.injector.instanceOf[views.html.scalabuild.PurchasePriceView]
  val form: Form[_] = app.injector.instanceOf[forms.scalabuild.PurchasePriceFormProvider].apply()
  override val htmlContent: HtmlFormat.Appendable = view.apply(form)(fakeRequest, messages)

  "PurchasePriceView" must {
    "render the correct content" in {

      heading mustBe messages("purchasePrice.heading")
      hintText must include(messages("purchasePrice.hint"))
      summaryText must include(messages("purchasePrice.detail.title"))
      bodyText must include(messages("purchasePrice.detail.content"))
    }
    "render the correct values for the radio button choices" in {
      inputFieldLabel.get(0) mustBe messages("purchasePrice.heading")
    }
    "render the correct button" in {
      buttonText mustBe  messages("site.continue")
    }
  }
}
