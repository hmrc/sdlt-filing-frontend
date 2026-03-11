/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.checkanswerssummary

import base.ScalaSpecBase
import controllers.scalabuild.routes
import org.scalatest.wordspec.AnyWordSpec
import pages.scalabuild.ContractPost201603Page
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Actions, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}

class ContractPostMarch2016SummarySpec extends AnyWordSpec with ScalaSpecBase {
  val messagesApi: MessagesApi = application().injector.instanceOf[MessagesApi]
  implicit val messages: Messages = MessagesImpl(Lang("en"), messagesApi)

  "ContractPostMarch2016 Summary" should {
    "not return a summary row for ContractPostMarch2016" when {
      "there is no data for exchangeContracts in the UserAnswers" in {
        val userAnswers = emptyUserAnswers
        val result = ContractPostMarch2016Summary.row(userAnswers, withAction = true)
        result shouldBe None
      }
    }
    "return a summary row for ContractPostMarch2016 with a change link" when {
      "answer is 'Yes' and 'withAction' is true " in {
        val userAnswers = emptyUserAnswers.set(ContractPost201603Page, true).toOption
        val expected = SummaryListRow(
          key = Key(Text("Contract changed on or after 17 March 2016"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_contractVariedPost201603">Yes</span>""")),
          actions = Some(
            Actions(
              items = List(
                ActionItem(
                  href = routes.ContractPost201603Controller.onPageLoad().url,
                  content = Text("Change"),
                  visuallyHiddenText = Some("Contract changed on or after 17 March 2016?"),
                  attributes = Map(("id", "change_contractPost201603"))
                )
              )
            )
          )
        )
        val result = ContractPostMarch2016Summary.row(userAnswers.get, withAction = true)
        result shouldBe Some(expected)

      }
      "answer is 'No' and 'withAction' is true " in {
        val userAnswers = emptyUserAnswers.set(ContractPost201603Page, false).toOption
        val expected = SummaryListRow(
          key = Key(Text("Contract changed on or after 17 March 2016"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_contractVariedPost201603">No</span>""")),
          actions = Some(
            Actions(
              items = List(
                ActionItem(
                  href = routes.ContractPost201603Controller.onPageLoad().url,
                  content = Text("Change"),
                  visuallyHiddenText = Some("Contract changed on or after 17 March 2016?"),
                  attributes = Map(("id", "change_contractPost201603"))                )
              )
            )
          )
        )
        val result = ContractPostMarch2016Summary.row(userAnswers.get, withAction = true)
        result shouldBe Some(expected)
      }
    }
    "return a summary row for ContractPostMarch2016 without a change link" when {
      "answer is 'Yes' and 'withAction' is false " in {
        val userAnswers = emptyUserAnswers.set(ContractPost201603Page, true).toOption
        val expected = SummaryListRow(
          Key(Text("Contract changed on or after 17 March 2016"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_contractVariedPost201603">Yes</span>""")),
        )
        val result = ContractPostMarch2016Summary.row(userAnswers.get, withAction = false)
        result shouldBe Some(expected)

      }
      "answer is 'No' and 'withAction' is false " in {
        val userAnswers = emptyUserAnswers.set(ContractPost201603Page, false).toOption
        val expected = SummaryListRow(
          Key(Text("Contract changed on or after 17 March 2016"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_contractVariedPost201603">No</span>""")),
        )
        val result = ContractPostMarch2016Summary.row(userAnswers.get, withAction = false)
        result shouldBe Some(expected)

      }
    }
  }
}
