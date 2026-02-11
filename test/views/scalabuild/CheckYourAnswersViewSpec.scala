/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package views.scalabuild

import fixtures.scalabuild.TestObjects
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import views.html.scalabuild.CheckYourAnswersView

class CheckYourAnswersViewSpec extends ViewTestFixture with TestObjects {
  val view: CheckYourAnswersView = app.injector.instanceOf[views.html.scalabuild.CheckYourAnswersView]
  val summaryList: SummaryList = summaryListHelper(freeResNonIndAddMainJourney).getOrElse(SummaryList())
  override val htmlContent: HtmlFormat.Appendable = view.apply(summaryList)(fakeRequest, messages)

  "CheckYourAnswersView" must {
    "render the correct content" in {
      heading mustBe messages("checkYourAnswers.heading")
    }

    "render the correct links and buttons" in {
      linkText must include(messages("site.startAgain"))
      buttonText mustBe messages("site.calculate")
    }
  }
  "render the correct summary rows in minimal freeholder path" in {
    summaryRow must include(messages("holding.checkYourAnswersLabel"))
    summaryRow must include(messages("propertyType.checkYourAnswersLabel"))
    summaryRow must include(messages("effectiveDate.checkYourAnswersLabel"))
    summaryRow must include(messages("nonUkResident.checkYourAnswersLabel"))
    summaryRow must include(messages("isPurchaserIndividual.checkYourAnswersLabel"))
    summaryRow must include(messages("isAdditionalProperty.checkYourAnswersLabel"))
    summaryRow must include(messages("mainResidence.checkYourAnswersLabel"))
  }

}
