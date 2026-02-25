/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.checkanswerssummary

import models.scalabuild.UserAnswers
import pages.scalabuild.HoldingPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.scalabuild.FormatUtils.{keyCssClass, valueCssClass}
import viewmodels.scalabuild.govuk.summarylist.{
  ActionItemViewModel,
  FluentActionItem,
  FluentKey,
  FluentValue,
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
          value = ValueViewModel(answer.toString).withCssClass(valueCssClass),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.scalabuild.routes.FreeholdOrLeaseholdController.onPageLoad().url
            )
              .withVisuallyHiddenText(messages("site.change.hidden"))
          )
        )
      } else {
        SummaryListRowViewModel(
          key = KeyViewModel("holding.checkYourAnswersLabel").withCssClass(keyCssClass),
          value = ValueViewModel(answer.toString).withCssClass(valueCssClass)
        )
      }
    }
}
