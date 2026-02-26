/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.checkanswerssummary

import base.ScalaSpecBase
import controllers.scalabuild.routes
import org.scalatest.wordspec.AnyWordSpec
import pages.scalabuild.SharedOwnershipPage
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Actions, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}

class SharedOwnershipSummarySpec extends AnyWordSpec with ScalaSpecBase {
  val messagesApi: MessagesApi = application().injector.instanceOf[MessagesApi]
  implicit val messages: Messages = MessagesImpl(Lang("en"), messagesApi)

  "SharedOwnership Summary" should {
    "not return a summary row for SharedOwnership" when {
      "there is no data for sharedOwnership in the UserAnswers" in {
        val userAnswers = emptyUserAnswers
        val result = SharedOwnershipSummary.row(userAnswers, withAction = true)
        result shouldBe None
      }
    }
    "return a summary row for SharedOwnership with a change link" when {
      "answer is 'Yes' and 'withAction' is true " in {
        val userAnswers = emptyUserAnswers.set(SharedOwnershipPage, true).toOption
        val expected = SummaryListRow(
          key = Key(Text("Shared ownership"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_sharedOwnership">Yes</span>""")),
          actions = Some(
            Actions(
              items = List(
                ActionItem(
                  href = routes.SharedOwnershipController.onPageLoad().url,
                  content = Text("Change"),
                  visuallyHiddenText = Some("Change")
                )
              )
            )
          )
        )
        val result = SharedOwnershipSummary.row(userAnswers.get, withAction = true)
        result shouldBe Some(expected)

      }
      "answer is 'No' and 'withAction' is true " in {
        val userAnswers = emptyUserAnswers.set(SharedOwnershipPage, false).toOption
        val expected = SummaryListRow(
          key = Key(Text("Shared ownership"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_sharedOwnership">No</span>""")),
          actions = Some(
            Actions(
              items = List(
                ActionItem(
                  href = routes.SharedOwnershipController.onPageLoad().url,
                  content = Text("Change"),
                  visuallyHiddenText = Some("Change")
                )
              )
            )
          )
        )
        val result = SharedOwnershipSummary.row(userAnswers.get, withAction = true)
        result shouldBe Some(expected)
      }
    }
    "return a summary row for SharedOwnership without a change link" when {
      "answer is 'Yes' and 'withAction' is false " in {
        val userAnswers = emptyUserAnswers.set(SharedOwnershipPage, true).toOption
        val expected = SummaryListRow(
          Key(Text("Shared ownership"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_sharedOwnership">Yes</span>""")),
        )
        val result = SharedOwnershipSummary.row(userAnswers.get, withAction = false)
        result shouldBe Some(expected)

      }
      "answer is 'No' and 'withAction' is false " in {
        val userAnswers = emptyUserAnswers.set(SharedOwnershipPage, false).toOption
        val expected = SummaryListRow(
          Key(Text("Shared ownership"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_sharedOwnership">No</span>""")),
        )
        val result = SharedOwnershipSummary.row(userAnswers.get, withAction = false)
        result shouldBe Some(expected)

      }
    }
  }
}
