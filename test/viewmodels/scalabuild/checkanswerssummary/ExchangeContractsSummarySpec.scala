/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.checkanswerssummary

import base.ScalaSpecBase
import controllers.scalabuild.routes
import org.scalatest.wordspec.AnyWordSpec
import pages.scalabuild.ExchangeContractsPage
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Actions, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}

class ExchangeContractsSummarySpec extends AnyWordSpec with ScalaSpecBase {
  val messagesApi: MessagesApi = application().injector.instanceOf[MessagesApi]
  implicit val messages: Messages = MessagesImpl(Lang("en"), messagesApi)

  "ExchangeContracts Summary" should {
    "not return a summary row for ExchangeContracts" when {
      "there is no data for exchangeContracts in the UserAnswers" in {
        val userAnswers = emptyUserAnswers
        val result = ExchangeContractPreMarch2016Summary.row(userAnswers, withAction = true)
        result shouldBe None
      }
    }
    "return a summary row for ExchangeContracts with a change link" when {
      "answer is 'Yes' and 'withAction' is true " in {
        val userAnswers = emptyUserAnswers.set(ExchangeContractsPage, true).toOption
        val expected = SummaryListRow(
          key = Key(Text("Exchange of contracts before 17 March 2016"), " govuk-!-width-one-half"),
          value = Value(Text("Yes"), " "),
          actions = Some(
            Actions(
              items = List(
                ActionItem(
                  href = routes.ExchangeContractsController.onPageLoad().url,
                  content = Text("Change"),
                  visuallyHiddenText = Some("Change")
                )
              )
            )
          )
        )
        val result = ExchangeContractPreMarch2016Summary.row(userAnswers.get, withAction = true)
        result shouldBe Some(expected)

      }
      "answer is 'No' and 'withAction' is true " in {
        val userAnswers = emptyUserAnswers.set(ExchangeContractsPage, false).toOption
        val expected = SummaryListRow(
          key = Key(Text("Exchange of contracts before 17 March 2016"), " govuk-!-width-one-half"),
          value = Value(Text("No"), " "),
          actions = Some(
            Actions(
              items = List(
                ActionItem(
                  href = routes.ExchangeContractsController.onPageLoad().url,
                  content = Text("Change"),
                  visuallyHiddenText = Some("Change")
                )
              )
            )
          )
        )
        val result = ExchangeContractPreMarch2016Summary.row(userAnswers.get, withAction = true)
        result shouldBe Some(expected)
      }
    }
    "return a summary row for ExchangeContracts without a change link" when {
      "answer is 'Yes' and 'withAction' is false " in {
        val userAnswers = emptyUserAnswers.set(ExchangeContractsPage, true).toOption
        val expected = SummaryListRow(
          Key(Text("Exchange of contracts before 17 March 2016"), " govuk-!-width-one-half"),
          Value(Text("Yes"), " ")
        )
        val result = ExchangeContractPreMarch2016Summary.row(userAnswers.get, withAction = false)
        result shouldBe Some(expected)

      }
      "answer is 'No' and 'withAction' is false " in {
        val userAnswers = emptyUserAnswers.set(ExchangeContractsPage, false).toOption
        val expected = SummaryListRow(
          Key(Text("Exchange of contracts before 17 March 2016"), " govuk-!-width-one-half"),
          Value(Text("No"), " ")
        )
        val result = ExchangeContractPreMarch2016Summary.row(userAnswers.get, withAction = false)
        result shouldBe Some(expected)

      }
    }
  }
}
