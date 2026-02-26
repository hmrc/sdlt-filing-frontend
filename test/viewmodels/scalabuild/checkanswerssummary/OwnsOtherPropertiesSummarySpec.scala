/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.checkanswerssummary

import base.ScalaSpecBase
import controllers.scalabuild.routes
import org.scalatest.wordspec.AnyWordSpec
import pages.scalabuild.OwnsOtherPropertiesPage
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Actions, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}

class OwnsOtherPropertiesSummarySpec extends AnyWordSpec with ScalaSpecBase {
  val messagesApi: MessagesApi = application().injector.instanceOf[MessagesApi]
  implicit val messages: Messages = MessagesImpl(Lang("en"), messagesApi)

  "OwnsOtherProperties Summary" should {
    "not return a summary row for OwnsOtherProperties" when {
      "there is no data for ownsOtherProperties in the UserAnswers" in {
        val userAnswers = emptyUserAnswers
        val result = OwnsOtherPropertiesSummary.row(userAnswers, withAction = true)
        result shouldBe None
      }
    }
    "return a summary row for OwnsOtherProperties with a change link" when {
      "answer is 'Yes' and 'withAction' is true " in {
        val userAnswers = emptyUserAnswers.set(OwnsOtherPropertiesPage, true).toOption
        val expected = SummaryListRow(
          key = Key(Text("Owned other property"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_ownedOtherProperties">Yes</span>""")),
          actions = Some(
            Actions(
              items = List(
                ActionItem(
                  href = routes.OwnsOtherPropertiesController.onPageLoad().url,
                  content = Text("Change"),
                  visuallyHiddenText = Some("Change")
                )
              )
            )
          )
        )
        val result = OwnsOtherPropertiesSummary.row(userAnswers.get, withAction = true)
        result shouldBe Some(expected)

      }
      "answer is 'No' and 'withAction' is true " in {
        val userAnswers = emptyUserAnswers.set(OwnsOtherPropertiesPage, false).toOption
        val expected = SummaryListRow(
          key = Key(Text("Owned other property"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_ownedOtherProperties">No</span>""")),
          actions = Some(
            Actions(
              items = List(
                ActionItem(
                  href = routes.OwnsOtherPropertiesController.onPageLoad().url,
                  content = Text("Change"),
                  visuallyHiddenText = Some("Change")
                )
              )
            )
          )
        )
        val result = OwnsOtherPropertiesSummary.row(userAnswers.get, withAction = true)
        result shouldBe Some(expected)
      }
    }
    "return a summary row for OwnsOtherProperties without a change link" when {
      "answer is 'Yes' and 'withAction' is false " in {
        val userAnswers = emptyUserAnswers.set(OwnsOtherPropertiesPage, true).toOption
        val expected = SummaryListRow(
          key = Key(Text("Owned other property"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_ownedOtherProperties">Yes</span>""")),
        )
        val result = OwnsOtherPropertiesSummary.row(userAnswers.get, withAction = false)
        result shouldBe Some(expected)

      }
      "answer is 'No' and 'withAction' is false " in {
        val userAnswers = emptyUserAnswers.set(OwnsOtherPropertiesPage, false).toOption
        val expected = SummaryListRow(
          key = Key(Text("Owned other property"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_ownedOtherProperties">No</span>""")),
        )
        val result = OwnsOtherPropertiesSummary.row(userAnswers.get, withAction = false)
        result shouldBe Some(expected)

      }
    }
  }
}
