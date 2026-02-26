/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.checkanswerssummary

import base.ScalaSpecBase
import controllers.scalabuild.routes
import org.scalatest.wordspec.AnyWordSpec
import pages.scalabuild.PurchasePricePage
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Actions, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}

class PurchasePriceSummarySpec extends AnyWordSpec with ScalaSpecBase {
  val messagesApi: MessagesApi = application().injector.instanceOf[MessagesApi]
  implicit val messages: Messages = MessagesImpl(Lang("en"), messagesApi)
  
  "PurchasePrice Summary" should {
    "not return a summary row for PurchasePrice" when {
      "there is no data for PurchasePrice in the UserAnswers" in {
        val userAnswers = emptyUserAnswers
        val result = PurchasePriceSummary.row(userAnswers, withAction = true)
        result shouldBe None
      }
    }
    "return a summary row for PurchasePrice, correctly formatted with a change link" when {
      "answer is £300000 and 'withAction' is true " in {
        val userAnswers = emptyUserAnswers.set(PurchasePricePage, BigDecimal(300000)).toOption
        val expected = SummaryListRow(
          key = Key(Text("Purchase price"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_purchasePrice">£300,000</span>""")),
          actions = Some(
            Actions(
              items = List(
                ActionItem(
                  href = routes.PurchasePriceController.onPageLoad().url,
                  content = Text("Change"),
                  visuallyHiddenText = Some("Change")
                )
              )
            )
          )
        )
        val result = PurchasePriceSummary.row(userAnswers.get, withAction = true)
        result shouldBe Some(expected)
      }
    }
    "return a summary row for PurchasePrice, correctly formatted and without a change link" when {
      "answer is £300000 and 'withAction' is false " in {
        val userAnswers = emptyUserAnswers.set(PurchasePricePage, BigDecimal(300000)).toOption
        val expected = SummaryListRow(
          key = Key(Text("Purchase price"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_purchasePrice">£300,000</span>""")),
        )
        val result = PurchasePriceSummary.row(userAnswers.get, withAction = false)
        result shouldBe Some(expected)
      }
    }
    "return a summary row for PurchasePrice, correct id with index and amount displayed formatted without £" when {
      "it's in the first result and 'resultTable' is true" in {
        val userAnswers = emptyUserAnswers.set(PurchasePricePage, BigDecimal(300000)).toOption
        val expected = SummaryListRow(
          key = Key(Text("Purchase price (£)")),
          value = Value(content = HtmlContent(s"""<span id="premium0">300,000</span>""")),
        )
        val result = PurchasePriceSummary.row(userAnswers.get, withAction = false, resultTable = true, index = Some(0))
        result shouldBe Some(expected)
      }
      "it's in the second result and 'resultTable' is true" in {
        val userAnswers = emptyUserAnswers.set(PurchasePricePage, BigDecimal(300000)).toOption
        val expected = SummaryListRow(
          key = Key(Text("Purchase price (£)")),
          value = Value(content = HtmlContent(s"""<span id="premium1">300,000</span>""")),
        )
        val result = PurchasePriceSummary.row(userAnswers.get, withAction = false, resultTable = true, index = Some(1))
        result shouldBe Some(expected)
      }
    }
  }
}
