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
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Actions, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}

import java.time.LocalDate

class LeaseStartDateSummarySpec extends AnyWordSpec with ScalaSpecBase {
  val messagesApi: MessagesApi = application().injector.instanceOf[MessagesApi]
  implicit val messages: Messages = MessagesImpl(Lang("en"), messagesApi)

  val leaseDates = LeaseDates(startDate = LocalDate.of(2025,1,1), endDate = LocalDate.of(2225,1,1))

  "LeaseStartDate Summary" should {
    "not return a summary row for LeaseStartDate" when {
      "there is no data for LeaseStartDate in the UserAnswers" in {
        val userAnswers = emptyUserAnswers
        val result = LeaseStartDateSummary.row(userAnswers, withAction = true)
        result shouldBe None
      }
    }
    "return a summary row for LeaseStartDate with a change link" when {
      "answer is '1 January 2025' and 'withAction' is true " in {
        val userAnswers = emptyUserAnswers.set(LeaseDatesPage, leaseDates).toOption
        val expected = SummaryListRow(
          key = Key(Text("Start date as specified in lease")),
          value = Value(Text("1 January 2025"), " "),
          actions = Some(
            Actions(
              items = List(
                ActionItem(
                  href = routes.LeaseDatesController.onPageLoad().url,
                  content = Text("Change"),
                  visuallyHiddenText = Some("Change")
                )
              )
            )
          )
        )
        val result = LeaseStartDateSummary.row(userAnswers.get, withAction = true)
        result shouldBe Some(expected)
      }
    }
    "return a summary row for LeaseStartDate without a change link" when {
      "answer is '1 January 2025' and 'withAction' is false " in {
        val userAnswers = emptyUserAnswers.set(LeaseDatesPage, leaseDates).toOption
        val expected = SummaryListRow(
          Key(Text("Start date as specified in lease")),
          Value(Text("1 January 2025"), " ")
        )
        val result = LeaseStartDateSummary.row(userAnswers.get, withAction = false)
        result shouldBe Some(expected)

      }
    }
  }
}
