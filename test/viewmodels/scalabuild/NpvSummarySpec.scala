/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild

import base.ScalaSpecBase
import org.scalatest.wordspec.AnyWordSpec
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}

class NpvSummarySpec extends AnyWordSpec with ScalaSpecBase {
  val messagesApi: MessagesApi = application().injector.instanceOf[MessagesApi]
  implicit val messages: Messages = MessagesImpl(Lang("en"), messagesApi)


  "NpvSummary Summary" should {
    "not return a summary list" when {
      "a npv is not received" in {
        val result = NpvSummary.row(None)
        result shouldBe None
      }
    }
    "return a summary list with correct key and value" when {
      "a npv is received" in {
        val npv = 50000
        val expected = SummaryListRow(
          Key(Text("Net Present Value"), " govuk-!-width-one-half"),
          Value(Text("£50,000"), " ")
        )
        val result = NpvSummary.row(Some(npv))
        result shouldBe Some(expected)
      }
    }
  }
}
