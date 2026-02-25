/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package views.scalabuild

import fixtures.scalabuild.TestObjects
import models.scalabuild.PrintDisplayTable
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import views.html.scalabuild.PrintView

class PrintViewSpec extends ViewTestFixture with TestObjects {
  val view: PrintView = app.injector.instanceOf[views.html.scalabuild.PrintView]
  val summaryList: SummaryList = summaryListHelper(freeResNonIndAddMainJourney, withAction = false).getOrElse(SummaryList())
  val printHeading = "Summary"

  val printTableFreeResUkIndTwoMain: PrintDisplayTable = PrintDisplayTable(
    summaryList = summaryList,
    resultsTables = Seq(minResultDisplayTable)
  )
  override val htmlContent: HtmlFormat.Appendable = view.apply(printTableFreeResUkIndTwoMain)(fakeRequest, messages)

  "PrintView" must {
    "render the correct heading when slice results are returned" in {
      heading must include(printHeading)
    }

    "render the correct links" in {
      buttonText must include ("Print")
    }

    "render the correct summary rows to display user answers in Freeholder -> Residential -> NonUk -> Ind -> Add -> Main path" in {
      summaryRow must include(messages("holding.checkYourAnswersLabel"))
      summaryRow must include(messages("propertyType.checkYourAnswersLabel"))
      summaryRow must include(messages("effectiveDate.resultLabel"))
      summaryRow must include(messages("nonUkResident.checkYourAnswersLabel"))
      summaryRow must include(messages("isPurchaserIndividual.checkYourAnswersLabel"))
      summaryRow must include(messages("isAdditionalProperty.checkYourAnswersLabel"))
      summaryRow must include(messages("replaceMainResidence.checkYourAnswersLabel"))
    }

    "render the correct summary rows for the result calculation" in {
      summaryRow must include(messages("effectiveDate.resultLabel"))
      summaryRow must include(messages("purchasePrice.resultLabel"))
      summaryRow must include(messages("totalTax.resultLabel"))
    }
  }
}
