/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.checkanswerssummary

import models.scalabuild.UserAnswers
import pages.scalabuild.MarketValuePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.scalabuild.FormatUtils.keyCssClass
import viewmodels.scalabuild.govuk.summarylist.{
  ActionItemViewModel,
  FluentActionItem,
  FluentKey,
  KeyViewModel,
  SummaryListRowViewModel,
  ValueViewModel
}
import viewmodels.scalabuild.implicits._

object PaySdltSummary {

  def row(answers: UserAnswers, withAction: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(MarketValuePage).map { answer =>
      if (withAction) {
        SummaryListRowViewModel(
          key = KeyViewModel("marketValue.checkYourAnswersLabel").withCssClass(keyCssClass),
          value = ValueViewModel.withId(text = s"${answer.displayCya}",id = "td2_marketValue"),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.scalabuild.routes.MarketValueController.onPageLoad().url
            )
              .withVisuallyHiddenText(messages("site.change.hidden.marketValue"))
              .withAttribute(("id", "change_marketValue"))
          )
        )
      } else {
        SummaryListRowViewModel(
          key = KeyViewModel("marketValue.checkYourAnswersLabel").withCssClass(keyCssClass),
          value = ValueViewModel.withId(text = s"${answer.displayCya}",id = "td2_marketValue"),
        )
      }
    }
}
