/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.checkanswerssummary

import base.ScalaSpecBase
import controllers.scalabuild.routes
import org.scalatest.wordspec.AnyWordSpec
import pages.scalabuild.PremiumPage
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Actions, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}

class PremiumSummarySpec extends AnyWordSpec with ScalaSpecBase {
  val messagesApi: MessagesApi = application().injector.instanceOf[MessagesApi]
  implicit val messages: Messages = MessagesImpl(Lang("en"), messagesApi)

  "Premium Summary" should {
    "not return a summary row for Premium" when {
      "there is no data for Premium in the UserAnswers" in {
        val userAnswers = emptyUserAnswers
        val result = PremiumSummary.row(userAnswers, withAction = true)
        result shouldBe None
      }
    }
    "return a summary row for Premium, correctly formatted with a change link" when {
      "answer is £300000 and 'withAction' is true " in {
        val userAnswers = emptyUserAnswers.set(PremiumPage, BigDecimal(300000)).toOption
        val expected = SummaryListRow(
          key = Key(Text("Premium"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_premium">£300,000</span>""")),
          actions = Some(
            Actions(
              items = List(
                ActionItem(
                  href = routes.PremiumController.onPageLoad().url,
                  content = Text("Change"),
                  visuallyHiddenText = Some("Change")
                )
              )
            )
          )
        )
        val result = PremiumSummary.row(userAnswers.get, withAction = true)
        result shouldBe Some(expected)
      }
    }
    "return a summary row for Premium, correctly formatted and without a change link" when {
      "answer is £300000 and 'withAction' is false " in {
        val userAnswers = emptyUserAnswers.set(PremiumPage, BigDecimal(300000)).toOption
        val expected = SummaryListRow(
          key = Key(Text("SDLT on Premium")),
          value = Value(content = HtmlContent(s"""<span id="td2_premium">£300,000</span>""")),
        )
        val result = PremiumSummary.row(userAnswers.get, withAction = false)
        result shouldBe Some(expected)
      }
    }
  }
}
