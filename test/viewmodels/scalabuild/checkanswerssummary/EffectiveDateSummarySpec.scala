/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.checkanswerssummary

import base.ScalaSpecBase
import controllers.scalabuild.routes
import org.scalatest.wordspec.AnyWordSpec
import pages.scalabuild.EffectiveDatePage
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Actions, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}

import java.time.LocalDate

class EffectiveDateSummarySpec extends AnyWordSpec with ScalaSpecBase {
  val messagesApi: MessagesApi = application().injector.instanceOf[MessagesApi]
  implicit val messages: Messages = MessagesImpl(Lang("en"), messagesApi)

  "EffectiveDate Summary" should {
    "not return a summary row for EffectiveDate" when {
      "there is no data for EffectiveDate in the UserAnswers" in {
        val userAnswers = emptyUserAnswers
        val result = EffectiveDateSummary.row(userAnswers, withAction = true)
        result shouldBe None
      }
    }
    "return a summary row for EffectiveDate with a change link and key of 'Effective date of transaction'" when {
      "answer is '1 January 2025' and 'withAction' is true " in {
        val userAnswers = emptyUserAnswers.set(EffectiveDatePage, LocalDate.of(2025,1,1)).toOption
        val expected = SummaryListRow(
          key = Key(Text("Effective date of transaction")),
          value = Value(Text("1 January 2025"), " "),
          actions = Some(
            Actions(
              items = List(
                ActionItem(
                  href = routes.EffectiveDateController.onPageLoad().url,
                  content = Text("Change"),
                  visuallyHiddenText = Some("Change")
                )
              )
            )
          )
        )
        val result = EffectiveDateSummary.row(userAnswers.get, withAction = true)
        result shouldBe Some(expected)
      }
    }
    "return a summary row for EffectiveDate without a change link and Key of 'Effective date'" when {
      "answer is '1 January 2025' and 'withAction' is false " in {
        val userAnswers = emptyUserAnswers.set(EffectiveDatePage, LocalDate.of(2025,1,1)).toOption
        val expected = SummaryListRow(
          Key(Text("Effective date")),
          Value(Text("1 January 2025"), " ")
        )
        val result = EffectiveDateSummary.row(userAnswers.get, withAction = false)
        result shouldBe Some(expected)

      }
    }
  }
}
