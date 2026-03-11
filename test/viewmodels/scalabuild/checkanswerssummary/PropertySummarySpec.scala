/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.checkanswerssummary

import base.ScalaSpecBase
import controllers.scalabuild.routes
import models.scalabuild.PropertyType.{NonResidential, Residential}
import org.scalatest.wordspec.AnyWordSpec
import pages.scalabuild.ResidentialOrNonResidentialPage
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Actions, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}

class PropertySummarySpec extends AnyWordSpec with ScalaSpecBase {
  val messagesApi: MessagesApi = application().injector.instanceOf[MessagesApi]
  implicit val messages: Messages = MessagesImpl(Lang("en"), messagesApi)

  "Property Summary" should {
    "not return a summary row for Property" when {
      "there is no data for propertyType in the UserAnswers" in {
        val userAnswers = emptyUserAnswers
        val result = PropertySummary.row(userAnswers, withAction = true)
        result shouldBe None
      }
    }
    "return a summary row for Property with a change link" when {
      "answer is 'Residential' and 'withAction' is true " in {
        val userAnswers = emptyUserAnswers.set(ResidentialOrNonResidentialPage, Residential).toOption
        val expected = SummaryListRow(
          Key(Text("Residential or non-residential"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_propertyType">Residential</span>""")),
          actions = Some(
            Actions(
              items = List(
                ActionItem(
                  href = routes.ResidentialOrNonResidentialController.onPageLoad().url,
                  content = Text("Change"),
                  visuallyHiddenText = Some("Is property residential or non-residential?"),
                  attributes = Map(("id", "change_propertyType"))
                )
              )
            )
          )
        )
        val result = PropertySummary.row(userAnswers.get, withAction = true)
        result shouldBe Some(expected)

      }
      "answer is 'Non-residential' and 'withAction' is true " in {
        val userAnswers = emptyUserAnswers.set(ResidentialOrNonResidentialPage, NonResidential).toOption
        val expected = SummaryListRow(
          Key(Text("Residential or non-residential"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_propertyType">Non-residential</span>""")),
          actions = Some(
            Actions(
              items = List(
                ActionItem(
                  href = routes.ResidentialOrNonResidentialController.onPageLoad().url,
                  content = Text("Change"),
                  visuallyHiddenText = Some("Is property residential or non-residential?"),
                  attributes = Map(("id", "change_propertyType"))
                )
              )
            )
          )
        )
        val result = PropertySummary.row(userAnswers.get, withAction = true)
        result shouldBe Some(expected)
      }
    }
    "return a summary row for Property without a change link" when {
      "answer is 'Residential' and 'withAction' is false " in {
        val userAnswers = emptyUserAnswers.set(ResidentialOrNonResidentialPage, Residential).toOption
        val expected = SummaryListRow(
          Key(Text("Residential or non-residential"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_propertyType">Residential</span>"""))
        )
        val result = PropertySummary.row(userAnswers.get, withAction = false)
        result shouldBe Some(expected)

      }
      "answer is 'Non-residential' and 'withAction' is false " in {
        val userAnswers = emptyUserAnswers.set(ResidentialOrNonResidentialPage, NonResidential).toOption
        val expected = SummaryListRow(
          Key(Text("Residential or non-residential"), " govuk-!-width-one-half previous-question-title"),
          value = Value(content = HtmlContent(s"""<span id="td2_propertyType">Non-residential</span>"""))
        )
        val result = PropertySummary.row(userAnswers.get, withAction = false)
        result shouldBe Some(expected)

      }
    }
  }
}
