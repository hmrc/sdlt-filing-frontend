/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.checkanswerssummary

import base.ScalaSpecBase
import controllers.scalabuild.routes
import models.scalabuild.HoldingTypes.{Freehold, Leasehold}
import org.scalatest.wordspec.AnyWordSpec
import pages.scalabuild.HoldingPage
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Actions, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}

class HoldingSummarySpec extends AnyWordSpec with ScalaSpecBase {
  val messagesApi: MessagesApi = application().injector.instanceOf[MessagesApi]
  implicit val messages: Messages = MessagesImpl(Lang("en"), messagesApi)

  "Holding Summary" should {
    "not return a summary row for Holding" when {
      "there is no data for holdingType in the UserAnswers" in {
        val userAnswers = emptyUserAnswers
        val result = HoldingSummary.row(userAnswers, withAction = true)
        result shouldBe None
      }
    }
    "return a summary row for Holding with a change link" when {
      "answer is 'Freehold' and 'withAction' is true " in {
        val userAnswers = emptyUserAnswers.set(HoldingPage, Freehold).toOption
        val expected = SummaryListRow(
          key = Key(Text("Freehold or Leasehold"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_holdingType">Freehold</span>""")),
          actions = Some(
            Actions(
              items = List(
                ActionItem(
                  href = routes.FreeholdOrLeaseholdController.onPageLoad().url,
                  content = Text("Change"),
                  visuallyHiddenText = Some("Change")
                )
              )
            )
          )
        )
        val result = HoldingSummary.row(userAnswers.get, withAction = true)
        result shouldBe Some(expected)

      }
      "answer is 'Leasehold' and 'withAction' is true " in {
        val userAnswers = emptyUserAnswers.set(HoldingPage, Leasehold).toOption
        val expected = SummaryListRow(
          key = Key(Text("Freehold or Leasehold"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_holdingType">Leasehold</span>""")),
          actions = Some(
            Actions(
              items = List(
                ActionItem(
                  href = routes.FreeholdOrLeaseholdController.onPageLoad().url,
                  content = Text("Change"),
                  visuallyHiddenText = Some("Change")
                )
              )
            )
          )
        )
        val result = HoldingSummary.row(userAnswers.get, withAction = true)
        result shouldBe Some(expected)
      }
    }
    "return a summary row for Holding without a change link" when {
      "answer is 'Freehold' and 'withAction' is false " in {
        val userAnswers = emptyUserAnswers.set(HoldingPage, Freehold).toOption
        val expected = SummaryListRow(
          key = Key(Text("Freehold or Leasehold"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_holdingType">Freehold</span>""")),
        )
        val result = HoldingSummary.row(userAnswers.get, withAction = false)
        result shouldBe Some(expected)

      }
      "answer is 'Leasehold' and 'withAction' is false " in {
        val userAnswers = emptyUserAnswers.set(HoldingPage, Leasehold).toOption
        val expected = SummaryListRow(
          key = Key(Text("Freehold or Leasehold"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_holdingType">Leasehold</span>""")),
        )
        val result = HoldingSummary.row(userAnswers.get, withAction = false)
        result shouldBe Some(expected)

      }
    }
  }
}
