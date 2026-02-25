/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild

import base.ScalaSpecBase
import models.scalabuild.HoldingTypes.{Freehold, Leasehold}
import org.scalatest.wordspec.AnyWordSpec
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}

class TotalDueSummarySpec extends AnyWordSpec with ScalaSpecBase {
  val messagesApi: MessagesApi = application().injector.instanceOf[MessagesApi]
  implicit val messages: Messages = MessagesImpl(Lang("en"), messagesApi)

  "TotalTax Summary" should {
    "Display the correct summary key" when {
      "result is received in the form of a slab" in {
        val expected = SummaryListRow(
          Key(Text("Total SDLT due (£)"), " govuk-!-width-one-half"),
          Value(Text("£3,000"), " ")
        )
        val result = TotalDueSummary.row(3000, Freehold, slab = true)
        result shouldBe expected

      }
      "holding type is 'Leasehold' and result is slice" in {
        val expected = SummaryListRow(
          Key(Text("Total amount of tax for this transaction"), " govuk-!-width-one-half"),
          Value(Text("£3,000"), " ")
        )
        val result = TotalDueSummary.row(3000, Leasehold, slab = false)
        result shouldBe expected

      }
      "holding type is 'Freehold' and result is slice" in {
        val expected = SummaryListRow(
          Key(Text("Total tax"), " govuk-!-width-one-half"),
          Value(Text("£3,000"), " ")
        )
        val result = TotalDueSummary.row(3000, Freehold, slab = false)
        result shouldBe expected

      }
    }
  }
}
