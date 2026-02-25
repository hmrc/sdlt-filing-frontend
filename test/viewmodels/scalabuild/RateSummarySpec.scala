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

class RateSummarySpec extends AnyWordSpec with ScalaSpecBase {
  val messagesApi: MessagesApi = application().injector.instanceOf[MessagesApi]
  implicit val messages: Messages = MessagesImpl(Lang("en"), messagesApi)

  "Rate Summary" should {
    "not return a summary list" when {
      "a rate is not received" in {
        val result = RateSummary.row(None)
        result shouldBe None
      }
    }
    "return a summary list with correct key and value" when {
      "a rate is received" in {
        val rate = 3
        val expected = SummaryListRow(
          Key(Text("Percentage rate (%)"), " govuk-!-width-one-half"),
          Value(Text("3"), " ")
        )
        val result = RateSummary.row(Some(rate))
        result shouldBe Some(expected)
      }
    }
  }
}
