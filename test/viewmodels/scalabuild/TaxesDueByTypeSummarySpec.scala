/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild

import base.ScalaSpecBase
import controllers.scalabuild.routes
import enums.TaxTypes
import org.scalatest.wordspec.AnyWordSpec
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

class TaxesDueByTypeSummarySpec extends AnyWordSpec with ScalaSpecBase {
  val messagesApi: MessagesApi = application().injector.instanceOf[MessagesApi]
  implicit val messages: Messages = MessagesImpl(Lang("en"), messagesApi)

  val totalTaxDue = 3000

  "TaxDueByType Summary" should {
    "Display the correct summary key and id for the Value" when {
      "tax type is Premium and it's the first taxCalc in the first result" in {
        val expected = SummaryListRow(
          Key(HtmlContent(s"""<span id="taxType00">SDLT on Premium</span>""")),
          value = Value(HtmlContent(s"""<span id="taxDue00">£3,000</span>""")),
          actions = Some(
            Actions(
              items = List(
                ActionItem(
                  href = routes.DetailController.onPageLoad(Some(0), Some(0)).url,
                  content = Text("View calculation"),
                  visuallyHiddenText = Some("View calculation"),
                  attributes = Map("id" -> "detailCalc00")
                )
              )
            )
          )
        )
        val result =
          TaxesDueByTypeSummary.row(TaxTypes.premium, totalTaxDue, rate = None, resultIndex = 0, taxCalcIndex = 0)
        result shouldBe expected
      }
      "tax type is rent and it's the second taxCalc of the second result" in {
        val expected = SummaryListRow(
          Key(HtmlContent(s"""<span id="taxType11">SDLT on Rent</span>""")),
          value = Value(HtmlContent(s"""<span id="taxDue11">£3,000</span>""")),
          actions = Some(
            Actions(
              items = List(
                ActionItem(
                  href = routes.DetailController.onPageLoad(Some(1), Some(1)).url,
                  content = Text("View calculation"),
                  visuallyHiddenText = Some("View calculation"),
                  attributes = Map("id" -> "detailCalc11")
                )
              )
            )
          )
        )
        val result =
          TaxesDueByTypeSummary.row(TaxTypes.rent, totalTaxDue, rate = None, resultIndex = 1, taxCalcIndex = 1)
        result shouldBe expected
      }
      "Display the correct summary key and no view calculation link" when {
        "tax type is premium with a rate of 3" in {
          val expected = SummaryListRow(
            Key(HtmlContent(s"""<span id="taxType11">SDLT on premium (3%)</span>""")),
            value = Value(HtmlContent(s"""<span id="taxDue11">£3,000</span>"""))
          )
          val result =
            TaxesDueByTypeSummary.row(TaxTypes.premium, totalTaxDue, rate = Some(3), resultIndex = 1, taxCalcIndex = 1)
          result shouldBe expected
        }
      }
    }
  }
}
