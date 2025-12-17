/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package views.scalabuild
import play.twirl.api.HtmlFormat
import views.html.scalabuild.StartGuidanceView

class StartGuidanceViewSpec extends ViewTestFixture {

  val view: StartGuidanceView = app.injector.instanceOf[views.html.scalabuild.StartGuidanceView]
  override val htmlContent: HtmlFormat.Appendable = view.apply()(fakeRequest, messages)

  "StartGuidanceView" must {
    "render the correct content" in {
      heading mustBe messages("Calculate Stamp Duty Land Tax (SDLT)")
      bodyText must include("This calculator can be used for property purchases that are:")
      bodyText must include("The calculator will work out the SDLT payable for most transactions. You should")
      bodyText must include("if you are uncertain about how SDLT applies to your purchase or if you believe it may qualify for a relief.")
      bodyText must include("There are")
      bodyText must include("purchasing residential property for more than £500,000.")
      buttonText mustBe messages("Start now")
    }

    "render the correct list items and link text" in {
      document.select("p").text  must include(messages("startGuidance.content.l1"))
      bullet must include(messages("startGuidance.bullet.b1"))
      bullet must include(messages("startGuidance.bullet.b2"))
      bullet must include(messages("startGuidance.bullet.b3"))
      bullet must include(messages("startGuidance.bullet.b4"))
      bullet must include(messages("startGuidance.bullet.b5"))
      bullet must include(messages("startGuidance.bullet.b6"))
      linkText must include(messages("check the guidance"))
      linkText must include(messages("different rules for a corporate body"))
    }
  }
}
