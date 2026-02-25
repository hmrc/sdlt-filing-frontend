/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild

import base.ScalaSpecBase
import controllers.scalabuild.routes
import enums.TaxTypes
import models.scalabuild.DisplayLeasehold
import org.scalatest.wordspec.AnyWordSpec
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.cookiebanner.Action
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, Actions, Key, SummaryListRow, Value}

class TaxDueByTypeSummarySpec extends AnyWordSpec with ScalaSpecBase {
  val messagesApi: MessagesApi = application().injector.instanceOf[MessagesApi]
  implicit val messages: Messages = MessagesImpl(Lang("en"), messagesApi)

  val totalTaxDue = 3000

  "TaxDueByType Summary" should {
    "Display the correct summary key" when {
      "tax type is Premium" in {
        val expected = SummaryListRow(
          key = Key(Text("SDLT on Premium"), " govuk-!-width-one-half"),
          value = Value(Text("£3,000"), " "),
          actions = Some(
            Actions(
              items = List(
                ActionItem(
                  href = routes.DetailController.onPageLoad(0).url,
                  content = Text("View calculation"),
                  visuallyHiddenText = Some("View calculation")
                )
              )
            )
          )
        )
        val result = TaxesDueByTypeSummary.row(TaxTypes.premium, totalTaxDue, 0)
        result shouldBe expected
      }
      "tax type is rent" in {
        val expected = SummaryListRow(
          Key(Text("SDLT on Rent"), " govuk-!-width-one-half"),
          Value(Text("£3,000"), " "),
          actions = Some(
            Actions(
              items = List(
                ActionItem(
                  href = routes.DetailController.onPageLoad(0).url,
                  content = Text("View calculation"),
                  visuallyHiddenText = Some("View calculation")
                )
              )
            )
          )
        )
        val result = TaxesDueByTypeSummary.row(TaxTypes.rent, totalTaxDue, 0)
        result shouldBe expected
      }
    }
  }
}
