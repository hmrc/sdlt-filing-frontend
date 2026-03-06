/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.checkanswerssummary

import base.ScalaSpecBase
import controllers.scalabuild.routes
import models.scalabuild.RentPeriods
import org.scalatest.wordspec.AnyWordSpec
import pages.scalabuild.RentPage
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Actions, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}

class Year1RentSummarySpec extends AnyWordSpec with ScalaSpecBase {
  val messagesApi: MessagesApi = application().injector.instanceOf[MessagesApi]
  implicit val messages: Messages = MessagesImpl(Lang("en"), messagesApi)
  val rents = RentPeriods(
    year1Rent = 5000,
    year2Rent = None,
    year3Rent = None,
    year4Rent = None,
    year5Rent = None
  )

  "Year1Rent Summary" should {
    "not return a summary row for Year1Rent" when {
      "there is no data for Year1Rent in the UserAnswers" in {
        val userAnswers = emptyUserAnswers
        val result = Year1RentSummary.row(userAnswers, withAction = true)
        result shouldBe None
      }
    }
    "return a summary row for Year1Rent, correctly formatted with a change link" when {
      "answer is £5000 and 'withAction' is true " in {
        val userAnswers = emptyUserAnswers.set(RentPage, rents).toOption
        val expected = SummaryListRow(
          key = Key(Text("Year 1 rent"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_year1Rent">£5,000</span>""")),
          actions = Some(
            Actions(
              items = List(
                ActionItem(
                  href = routes.RentController.onPageLoad().url,
                  content = Text("Change"),
                  visuallyHiddenText = Some("Year 1 rent?"),
                  attributes = Map(("id", "change_year1Rent"))
                )
              )
            )
          )
        )
        val result = Year1RentSummary.row(userAnswers.get, withAction = true)
        result shouldBe Some(expected)
      }
    }
    "return a summary row for Year1Rent, correctly formatted and without a change link" when {
      "answer is £5000 and 'withAction' is false " in {
        val userAnswers = emptyUserAnswers.set(RentPage, rents).toOption
        val expected = SummaryListRow(
          Key(Text("Year 1 rent"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_year1Rent">£5,000</span>"""))
        )
        val result = Year1RentSummary.row(userAnswers.get, withAction = false)
        result shouldBe Some(expected)
      }
    }
  }
}
