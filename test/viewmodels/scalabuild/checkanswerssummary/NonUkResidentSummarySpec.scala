/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.checkanswerssummary

import base.ScalaSpecBase
import controllers.scalabuild.routes
import org.scalatest.wordspec.AnyWordSpec
import pages.scalabuild.NonUkResidentPage
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Actions, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}

class NonUkResidentSummarySpec extends AnyWordSpec with ScalaSpecBase {
  val messagesApi: MessagesApi = application().injector.instanceOf[MessagesApi]
  implicit val messages: Messages = MessagesImpl(Lang("en"), messagesApi)

  "NonUkResident Summary" should {
    "not return a summary row for NonUkResident" when {
      "there is no data for non-Uk Resident in the UserAnswers" in {
        val userAnswers = emptyUserAnswers
        val result = NonUkResidentSummary.row(userAnswers, withAction = true)
        result shouldBe None
      }
    }
    "return a summary row for NonUkResident with a change link" when {
      "answer is 'Yes' and 'withAction' is true " in {
        val userAnswers = emptyUserAnswers.set(NonUkResidentPage, true).toOption
        val expected = SummaryListRow(
          key = Key(Text("Non-UK resident"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_nonUKResident">Yes</span>""")),
          actions = Some(
            Actions(
              items = List(
                ActionItem(
                  href = routes.NonUkResidentController.onPageLoad().url,
                  content = Text("Change"),
                  visuallyHiddenText = Some("Change")
                )
              )
            )
          )
        )
        val result = NonUkResidentSummary.row(userAnswers.get, withAction = true)
        result shouldBe Some(expected)

      }
      "answer is 'No' and 'withAction' is true " in {
        val userAnswers = emptyUserAnswers.set(NonUkResidentPage, false).toOption
        val expected = SummaryListRow(
          key = Key(Text("Non-UK resident"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_nonUKResident">No</span>""")),
          actions = Some(
            Actions(
              items = List(
                ActionItem(
                  href = routes.NonUkResidentController.onPageLoad().url,
                  content = Text("Change"),
                  visuallyHiddenText = Some("Change")
                )
              )
            )
          )
        )
        val result = NonUkResidentSummary.row(userAnswers.get, withAction = true)
        result shouldBe Some(expected)
      }
    }
    "return a summary row for NonUkResident without a change link" when {
      "answer is 'Yes' and 'withAction' is false " in {
        val userAnswers = emptyUserAnswers.set(NonUkResidentPage, true).toOption
        val expected = SummaryListRow(
          key = Key(Text("Non-UK resident"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_nonUKResident">Yes</span>""")),
        )
        val result = NonUkResidentSummary.row(userAnswers.get, withAction = false)
        result shouldBe Some(expected)

      }
      "answer is 'No' and 'withAction' is false " in {
        val userAnswers = emptyUserAnswers.set(NonUkResidentPage, false).toOption
        val expected = SummaryListRow(
          key = Key(Text("Non-UK resident"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_nonUKResident">No</span>""")),
        )
        val result = NonUkResidentSummary.row(userAnswers.get, withAction = false)
        result shouldBe Some(expected)

      }
    }
  }
}
