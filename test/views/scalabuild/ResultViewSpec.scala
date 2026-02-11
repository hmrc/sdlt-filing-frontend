/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package views.scalabuild

import fixtures.scalabuild.TestObjects
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import views.html.scalabuild.ResultView

class ResultViewSpec extends ViewTestFixture with TestObjects {
  val view: ResultView = app.injector.instanceOf[views.html.scalabuild.ResultView]
  val freeSummaryList: SummaryList = summaryListHelper(freeResNonIndAddMainJourney, false).getOrElse(SummaryList())
  val leaseSummaryList: SummaryList = summaryListHelper(leaseResNonIndAddMainJourney, false).getOrElse(SummaryList())
  override val htmlContent: HtmlFormat.Appendable = view.apply(Seq(minResultDisplayTable()))(fakeRequest, messages)

  "ResultView" must {
    "render the correct content" in {
      heading mustBe "Results of calculation based on SDLT rules for the effective date entered"
    }

    "render the correct links" in {
      linkText must include(messages("site.viewCalculation"))
      linkText must include (messages("site.print"))
    }

    "render the correct summary rows in freeholder path" in {
      summaryRow must include(messages("effectiveDate.resultLabel"))
      summaryRow must include(messages("purchasePrice.resultLabel"))
      summaryRow must include(messages("totalTax.resultLabel"))

    }
    "render the correct summary rows in Leaseholder path path" in {
      summaryRow must include(messages("effectiveDate.resultLabel"))
      summaryRow must include(messages("purchasePrice.resultLabel"))
      summaryRow must include(messages("totalTax.resultLabel"))

    }
  }
}
