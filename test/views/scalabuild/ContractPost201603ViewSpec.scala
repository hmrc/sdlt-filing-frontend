/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package views.scalabuild

import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.html.scalabuild.ContractPost201603View

class ContractPost201603ViewSpec extends ViewTestFixture {

  val view: ContractPost201603View = app.injector.instanceOf[views.html.scalabuild.ContractPost201603View]
  val form: Form[_] = app.injector.instanceOf[forms.scalabuild.ContractPost201603FormProvider].apply()
  override val htmlContent: HtmlFormat.Appendable = view.apply(form)(fakeRequest, messages)

  "ContractPost201603 view" must {
    "render the correct content" in {
      pagetitle must include(messages("contractPost201603.title"))
      heading mustBe messages("contractPost201603.heading")
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