/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.checkanswerssummary

import models.scalabuild.UserAnswers
import pages.scalabuild.HoldingPage
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

object HoldingSummary {

  def row(answers: UserAnswers, withAction: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(HoldingPage).map { answer =>
      if (withAction) {
        SummaryListRowViewModel(
          key = KeyViewModel("holding.checkYourAnswersLabel").withCssClass(keyCssClass),
          value = ValueViewModel.withId(text = s"$answer",id = "td2_holdingType"),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.scalabuild.routes.FreeholdOrLeaseholdController.onPageLoad().url
            )
              .withVisuallyHiddenText(messages("site.change.hidden.holdingType"))
              .withAttribute(("id", "change_holdingType"))
          )
        )
      } else {
        SummaryListRowViewModel(
          key = KeyViewModel("holding.checkYourAnswersLabel").withCssClass(keyCssClass),
          value = ValueViewModel.withId(text = s"$answer",id = "td2_holdingType"),
        )
      }
    }
}
