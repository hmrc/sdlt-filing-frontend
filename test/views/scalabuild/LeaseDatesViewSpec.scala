/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package views.scalabuild

import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.html.scalabuild.LeaseDatesView

import java.time.LocalDate

class LeaseDatesViewSpec extends ViewTestFixture {

  val view: LeaseDatesView = app.injector.instanceOf[views.html.scalabuild.LeaseDatesView]
  val effectiveDate = LocalDate.of(2025,1,1)
  val form: Form[_] = app.injector.instanceOf[forms.scalabuild.LeaseDatesFormProvider].apply(effectiveDate = effectiveDate)
  override val htmlContent: HtmlFormat.Appendable = view.apply(form)(fakeRequest, messages)

  "LeaseDatesView" must {
    "render the correct content" in {
      lazy val startDateHeading = document.select("#startDate").text()
      lazy val endDateHeading = document.select("#endDate").text()
      pagetitle must include(messages("leaseDates.startDate.title"))
      startDateHeading mustBe messages("leaseDates.startDate.heading")
      hintText must include(messages("leaseDates.startDate.hint"))
      hintText must include(messages("effectiveDate.hint.h2"))
      endDateHeading mustBe messages("leaseDates.endDate.heading")
      hintText must include(messages("leaseDates.endDate.hint1"))
      hintText must include(messages("leaseDates.endDate.hint2"))
    }

    "render the correct values for the date inputs" in {
      dateField must include(messages("date.day"))
      dateField must include(messages("date.month"))
      dateField must include(messages("date.year"))
    }

    "render the correct button" in {
      buttonText mustBe  messages("site.continue")
    }
  }
}