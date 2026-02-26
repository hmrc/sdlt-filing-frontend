/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package views.scalabuild

import fixtures.scalabuild.TestObjects
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import views.html.scalabuild.DetailView

class DetailViewSpec extends ViewTestFixture with TestObjects {
  val view: DetailView = app.injector.instanceOf[views.html.scalabuild.DetailView]
  val summaryList: SummaryList = summaryListHelper(freeResNonIndAddMainJourney).getOrElse(SummaryList())
  val detailHeading = "Detailed calculation"
  val detailCaption = "This is a breakdown of how the total amount of SDLT was calculated"
  val detailFooterText = "Total SDLT due"
  override val htmlContent: HtmlFormat.Appendable = view.apply(
    captionText = detailCaption,
    detailFooter = "Total SDLT due",
    taxDue = 47500,
    rows = detailFreeResUkIndTwoMain )(fakeRequest, messages)

  "DetailView" must {
    "render the correct content when slice results are returned" in {
      heading mustBe detailHeading
      tableCaption mustBe detailCaption
      detailFooter mustBe detailFooterText
      totalSDLT mustBe "47,500"
    }

    "render the correct links" in {
      linkText must include(messages("site.print"))
      linkText must include (messages("govUk.link.text"))
    }


    "render the correct table rows headers in minimal freeholder path" in {
      tableRowHeader must include(messages("Purchase price bands (£)"))
      tableRowHeader must include(messages("Percentage rate (%)"))
      tableRowHeader must include(messages("SDLT due (£)"))
    }
  }
}
