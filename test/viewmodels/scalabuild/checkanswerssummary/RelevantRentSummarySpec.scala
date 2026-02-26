/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.checkanswerssummary

import base.ScalaSpecBase
import controllers.scalabuild.routes
import org.scalatest.wordspec.AnyWordSpec
import pages.scalabuild.RelevantRentPage
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Actions, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}

class RelevantRentSummarySpec extends AnyWordSpec with ScalaSpecBase {
  val messagesApi: MessagesApi = application().injector.instanceOf[MessagesApi]
  implicit val messages: Messages = MessagesImpl(Lang("en"), messagesApi)

  "RelevantRent Summary" should {
    "not return a summary row for RelevantRent" when {
      "there is no data for RelevantRent in the UserAnswers" in {
        val userAnswers = emptyUserAnswers
        val result = RelevantRentSummary.row(userAnswers, withAction = true)
        result shouldBe None
      }
    }
    "return a summary row for RelevantRent, correctly formatted with a change link" when {
      "answer is £1800 and 'withAction' is true " in {
        val userAnswers = emptyUserAnswers.set(RelevantRentPage, BigDecimal(1800)).toOption
        val expected = SummaryListRow(
          key = Key(Text("Relevant rental figure"), " govuk-!-width-one-half previous-question-title"),
          value = Value(Text("£1,800"), " "),
          actions = Some(
            Actions(
              items = List(
                ActionItem(
                  href = routes.RelevantRentController.onPageLoad().url,
                  content = Text("Change"),
                  visuallyHiddenText = Some("Change")
                )
              )
            )
          )
        )
        val result = RelevantRentSummary.row(userAnswers.get, withAction = true)
        result shouldBe Some(expected)
      }
    }
    "return a summary row for RelevantRent, correctly formatted and without a change link" when {
      "answer is £1800 and 'withAction' is false " in {
        val userAnswers = emptyUserAnswers.set(RelevantRentPage, BigDecimal(1800)).toOption
        val expected = SummaryListRow(
          key = Key(Text("Relevant rental figure"), " govuk-!-width-one-half previous-question-title"),
          Value(Text("£1,800"), " ")
        )
        val result = RelevantRentSummary.row(userAnswers.get, withAction = false)
        result shouldBe Some(expected)
      }
    }
  }
}
