/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.checkanswerssummary

import base.ScalaSpecBase
import controllers.scalabuild.routes
import org.scalatest.wordspec.AnyWordSpec
import pages.scalabuild.IsAdditionalPropertyPage
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Actions, HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}

class IsAdditionalPropertySummarySpec extends AnyWordSpec with ScalaSpecBase {
  val messagesApi: MessagesApi = application().injector.instanceOf[MessagesApi]
  implicit val messages: Messages = MessagesImpl(Lang("en"), messagesApi)

  "IsAdditionalProperty Summary" should {
    "not return a summary row for IsAdditionalProperty" when {
      "there is no data for isAdditionalProperty in the UserAnswers" in {
        val userAnswers = emptyUserAnswers
        val result = IsAdditionalPropertySummary.row(userAnswers, withAction = true)
        result shouldBe None
      }
    }
    "return a summary row for IsAdditionalProperty with a change link" when {
      "answer is 'Yes' and 'withAction' is true " in {
        val userAnswers = emptyUserAnswers.set(IsAdditionalPropertyPage, true).toOption
        val expected = SummaryListRow(
          key = Key(Text("Additional residential property"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_twoOrMoreProperties">Yes</span>""")),
          actions = Some(
            Actions(
              items = List(
                ActionItem(
                  href = routes.IsAdditionalPropertyController.onPageLoad().url,
                  content = Text("Change"),
                  visuallyHiddenText = Some("Change")
                )
              )
            )
          )
        )
        val result = IsAdditionalPropertySummary.row(userAnswers.get, withAction = true)
        result shouldBe Some(expected)

      }
      "answer is 'No' and 'withAction' is true " in {
        val userAnswers = emptyUserAnswers.set(IsAdditionalPropertyPage, false).toOption
        val expected = SummaryListRow(
          key = Key(Text("Additional residential property"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_twoOrMoreProperties">No</span>""")),
          actions = Some(
            Actions(
              items = List(
                ActionItem(
                  href = routes.IsAdditionalPropertyController.onPageLoad().url,
                  content = Text("Change"),
                  visuallyHiddenText = Some("Change")
                )
              )
            )
          )
        )
        val result = IsAdditionalPropertySummary.row(userAnswers.get, withAction = true)
        result shouldBe Some(expected)
      }
    }
    "return a summary row for IsAdditionalProperty without a change link" when {
      "answer is 'Yes' and 'withAction' is false " in {
        val userAnswers = emptyUserAnswers.set(IsAdditionalPropertyPage, true).toOption
        val expected = SummaryListRow(
          key = Key(Text("Additional residential property"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_twoOrMoreProperties">Yes</span>""")),
        )
        val result = IsAdditionalPropertySummary.row(userAnswers.get, withAction = false)
        result shouldBe Some(expected)

      }
      "answer is 'No' and 'withAction' is false " in {
        val userAnswers = emptyUserAnswers.set(IsAdditionalPropertyPage, false).toOption
        val expected = SummaryListRow(
          key = Key(Text("Additional residential property"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_twoOrMoreProperties">No</span>""")),
        )
        val result = IsAdditionalPropertySummary.row(userAnswers.get, withAction = false)
        result shouldBe Some(expected)

      }
    }
  }
}
