/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.checkanswerssummary

import base.ScalaSpecBase
import controllers.scalabuild.routes
import org.scalatest.wordspec.AnyWordSpec
import pages.scalabuild.IsPurchaserIndividualPage
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Actions, HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}

class IsPurchaserIndividualSummarySpec extends AnyWordSpec with ScalaSpecBase {
  val messagesApi: MessagesApi = application().injector.instanceOf[MessagesApi]
  implicit val messages: Messages = MessagesImpl(Lang("en"), messagesApi)

  "IsPurchaserIndividual Summary" should {
    "not return a summary row for IsPurchaserIndividual" when {
      "there is no data for isPurchaserIndividual in the UserAnswers" in {
        val userAnswers = emptyUserAnswers
        val result = IsPurchaserIndividualSummary.row(userAnswers, withAction = true)
        result shouldBe None
      }
    }
    "return a summary row for IsPurchaserIndividual with a change link" when {
      "answer is 'Yes' and 'withAction' is true " in {
        val userAnswers = emptyUserAnswers.set(IsPurchaserIndividualPage, true).toOption
        val expected = SummaryListRow(
          key = Key(Text("Individual"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_individual">Yes</span>""")),
          actions = Some(
            Actions(
              items = List(
                ActionItem(
                  href = routes.IsPurchaserIndividualController.onPageLoad().url,
                  content = Text("Change"),
                  visuallyHiddenText = Some("Are you purchasing the property as an individual?"),
                  attributes = Map(("id", "change_individual"))
                )
              )
            )
          )
        )
        val result = IsPurchaserIndividualSummary.row(userAnswers.get, withAction = true)
        result shouldBe Some(expected)

      }
      "answer is 'No' and 'withAction' is true " in {
        val userAnswers = emptyUserAnswers.set(IsPurchaserIndividualPage, false).toOption
        val expected = SummaryListRow(
          key = Key(Text("Individual"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_individual">No</span>""")),
          actions = Some(
            Actions(
              items = List(
                ActionItem(
                  href = routes.IsPurchaserIndividualController.onPageLoad().url,
                  content = Text("Change"),
                  visuallyHiddenText = Some("Are you purchasing the property as an individual?"),
                  attributes = Map(("id", "change_individual"))
                )
              )
            )
          )
        )
        val result = IsPurchaserIndividualSummary.row(userAnswers.get, withAction = true)
        result shouldBe Some(expected)
      }
    }
    "return a summary row for IsPurchaserIndividual without a change link" when {
      "answer is 'Yes' and 'withAction' is false " in {
        val userAnswers = emptyUserAnswers.set(IsPurchaserIndividualPage, true).toOption
        val expected = SummaryListRow(
          key = Key(Text("Individual"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_individual">Yes</span>"""))
        )
        val result = IsPurchaserIndividualSummary.row(userAnswers.get, withAction = false)
        result shouldBe Some(expected)

      }
      "answer is 'No' and 'withAction' is false " in {
        val userAnswers = emptyUserAnswers.set(IsPurchaserIndividualPage, false).toOption
        val expected = SummaryListRow(
          key = Key(Text("Individual"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_individual">No</span>"""))
        )
        val result = IsPurchaserIndividualSummary.row(userAnswers.get, withAction = false)
        result shouldBe Some(expected)

      }
    }
  }
}
