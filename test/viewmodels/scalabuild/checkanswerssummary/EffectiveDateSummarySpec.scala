/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.checkanswerssummary

import base.ScalaSpecBase
import controllers.scalabuild.routes
import org.scalatest.wordspec.AnyWordSpec
import pages.scalabuild.EffectiveDatePage
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Actions, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}

import java.time.LocalDate

class EffectiveDateSummarySpec extends AnyWordSpec with ScalaSpecBase {
  val messagesApi: MessagesApi = application().injector.instanceOf[MessagesApi]
  implicit val messages: Messages = MessagesImpl(Lang("en"), messagesApi)

  "EffectiveDate Summary" should {
    "not return a summary row for EffectiveDate" when {
      "there is no data for EffectiveDate in the UserAnswers" in {
        val userAnswers = emptyUserAnswers
        val result = EffectiveDateSummary.row(userAnswers, withAction = true)
        result shouldBe None
      }
    }
    //cya
    "return a summary row for EffectiveDate with a change link and key of 'Effective date of transaction'" when {
      "'withAction' is true " in {
        val userAnswers = emptyUserAnswers.set(EffectiveDatePage, LocalDate.of(2025, 1, 1)).toOption
        val expected = SummaryListRow(
          key = Key(Text("Effective date of transaction"), " govuk-!-width-one-half previous-question-title"),
          value = Value(HtmlContent(s"""<span id="td2_effectiveDate">1 January 2025</span>""")),
          actions = Some(
            Actions(
              items = List(
                ActionItem(
                  href = routes.EffectiveDateController.onPageLoad().url,
                  content = Text("Change"),
                  visuallyHiddenText = Some("Effective date of your transaction?"),
                  attributes = Map(("id", "change_effectiveDate"))
                )
              )
            )
          )
        )
        val result = EffectiveDateSummary.row(userAnswers.get, withAction = true, index = Some(0), resultTable = false)
        result shouldBe Some(expected)
      }
    }
    "return a summary row for EffectiveDate without a change link" when {
      //summary on print page
      "'withAction' is false " in {
        val userAnswers = emptyUserAnswers.set(EffectiveDatePage, LocalDate.of(2025, 1, 1)).toOption
        val expected = SummaryListRow(
          key = Key(Text("Effective date of transaction"), " govuk-!-width-one-half previous-question-title"),
          value = Value(HtmlContent(s"""<span id="td2_effectiveDate">1 January 2025</span>"""))
        )
        val result = EffectiveDateSummary.row(userAnswers.get, withAction = false, index = Some(0), resultTable = false)
        result shouldBe Some(expected)
      }
    }
    "return a summary row for EffectiveDate without a change link and id of 'effDate00' for the value" when {
      // first result summary
      "resultTable is true and it's the first result" in {
        val userAnswers = emptyUserAnswers.set(EffectiveDatePage, LocalDate.of(2025, 1, 1)).toOption
        val expected = SummaryListRow(
          Key(Text("Effective date")),
          value = Value(HtmlContent(s"""<span id="effDate0">1 January 2025</span>"""))
        )
        val result = EffectiveDateSummary.row(userAnswers.get, withAction = false, index = Some(0), resultTable = true)
        result shouldBe Some(expected)

      }
    }
    // second result summary
    "return a summary row for EffectiveDate without a change link and id of 'effDate01' for the value" when {
      "resultTable is true and it's the second result" in {
        val userAnswers = emptyUserAnswers.set(EffectiveDatePage, LocalDate.of(2025, 1, 1)).toOption
        val expected = SummaryListRow(
          Key(Text("Effective date")),
          value = Value(HtmlContent(s"""<span id="effDate1">1 January 2025</span>"""))
        )
        val result = EffectiveDateSummary.row(userAnswers.get, withAction = false, index = Some(1), resultTable = true)
        result shouldBe Some(expected)

      }
    }
  }
}
