/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.checkanswerssummary

import base.ScalaSpecBase
import controllers.scalabuild.routes
import models.scalabuild.LeaseDates
import org.scalatest.wordspec.AnyWordSpec
import pages.scalabuild.LeaseDatesPage
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Actions, HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}

import java.time.LocalDate

class LeaseEndDateSummarySpec extends AnyWordSpec with ScalaSpecBase {
  val messagesApi: MessagesApi = application().injector.instanceOf[MessagesApi]
  implicit val messages: Messages = MessagesImpl(Lang("en"), messagesApi)

  val leaseDates = LeaseDates(startDate = LocalDate.of(2025, 1, 1), endDate = LocalDate.of(2225, 1, 1))

  "LeaseEndDate Summary" should {
    "not return a summary row for LeaseEndDate" when {
      "there is no data for LeaseEndDate in the UserAnswers" in {
        val userAnswers = emptyUserAnswers
        val result = LeaseEndDateSummary.row(userAnswers, withAction = true)
        result shouldBe None
      }
    }
    "return a summary row for LeaseEndDate with a change link" when {
      "answer is '1 January 2225' and 'withAction' is true " in {
        val userAnswers = emptyUserAnswers.set(LeaseDatesPage, leaseDates).toOption
        val expected = SummaryListRow(
          key = Key(Text("End date as specified in lease"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_leaseEndDate">1 January 2225</span>""")),
          actions = Some(
            Actions(
              items = List(
                ActionItem(
                  href = routes.LeaseDatesController.onPageLoad().url,
                  content = Text("Change"),
                  visuallyHiddenText = Some("End date as specified in lease?"),
                  attributes = Map(("id", "change_leaseEndDate"))
                )
              )
            )
          )
        )
        val result = LeaseEndDateSummary.row(userAnswers.get, withAction = true)
        result shouldBe Some(expected)
      }
    }
    "return a summary row for LeaseEndDate without a change link" when {
      "answer is '1 January 2225' and 'withAction' is false " in {
        val userAnswers = emptyUserAnswers.set(LeaseDatesPage, leaseDates).toOption
        val expected = SummaryListRow(
          Key(Text("End date as specified in lease"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_leaseEndDate">1 January 2225</span>"""))
        )
        val result = LeaseEndDateSummary.row(userAnswers.get, withAction = false)
        result shouldBe Some(expected)

      }
    }
  }
}
