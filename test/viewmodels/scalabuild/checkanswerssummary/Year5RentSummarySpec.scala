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

class Year5RentSummarySpec extends AnyWordSpec with ScalaSpecBase {
  val messagesApi: MessagesApi = application().injector.instanceOf[MessagesApi]
  implicit val messages: Messages = MessagesImpl(Lang("en"), messagesApi)

  private val rent4Year = RentPeriods(
    year1Rent = 5000,
    year2Rent = Some(5000),
    year3Rent = Some(5000),
    year4Rent = Some(5000),
    year5Rent = None
  )
  private val rentAllYears = RentPeriods(
    year1Rent = 5000,
    year2Rent = Some(5000),
    year3Rent = Some(5000),
    year4Rent = Some(5000),
    year5Rent = Some(5000)
  )

  "Year5Rent Summary" should {
    "not return a summary row for Year 5 Rent" when {
      "there is no data for rents in the UserAnswers" in {
        val userAnswers = emptyUserAnswers
        val result = Year1RentSummary.row(userAnswers, withAction = true)
        result shouldBe None
      }
      "there is only data for years 1, 2, 3 and 4 rent in UserAnswers" in {
        val userAnswers = emptyUserAnswers.set(RentPage, rent4Year).toOption
        val result = Year5RentSummary.row(userAnswers.get, withAction = true)
        result shouldBe None
      }
    }
    "return a summary row for Year 5 Rent, correctly formatted with a change link" when {
      "all years are defined and 'withAction' is true " in {
        val userAnswers = emptyUserAnswers.set(RentPage, rentAllYears).toOption
        val expected = SummaryListRow(
          key = Key(Text("Year 5 rent"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_year5Rent">£5,000</span>""")),
          actions = Some(
            Actions(
              items = List(
                ActionItem(
                  href = routes.RentController.onPageLoad().url,
                  content = Text("Change"),
                  visuallyHiddenText = Some("Year 5 rent?"),
                  attributes = Map(("id", "change_year5Rent"))
                )
              )
            )
          )
        )
        val result = Year5RentSummary.row(userAnswers.get, withAction = true)
        result shouldBe Some(expected)
      }
    }
    "return a summary row for Year5Rent, correctly formatted and without a change link" when {
      "all years are defined and 'withAction' is false" in {
        val userAnswers = emptyUserAnswers.set(RentPage, rentAllYears).toOption
        val expected = SummaryListRow(
          key = Key(Text("Year 5 rent"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_year5Rent">£5,000</span>""")),
        )
        val result = Year5RentSummary.row(userAnswers.get, withAction = false)
        result shouldBe Some(expected)
      }
    }
  }
}
