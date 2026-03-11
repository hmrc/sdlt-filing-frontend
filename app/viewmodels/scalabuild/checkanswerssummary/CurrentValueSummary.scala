/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.checkanswerssummary

import models.scalabuild.UserAnswers
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.scalabuild.FormatUtils.{bigDecimalFormat, keyCssClass, valueCssClass}
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

object CurrentValueSummary {

  def row(answers: UserAnswers, withAction: Boolean, threshold: Option[(Int, Boolean)] = None)(implicit messages: Messages): Option[SummaryListRow] =
    threshold.map { answer =>
      val value = if (answer._2) s"${bigDecimalFormat(answer._1)} or less" else "No"
        if (withAction) {
          SummaryListRowViewModel(
            key = KeyViewModel("currentValue.checkYourAnswersLabel").withCssClass(keyCssClass),
            value = ValueViewModel(value).withCssClass(valueCssClass),
            actions = Seq(
              ActionItemViewModel(
                "site.change",
                controllers.scalabuild.routes.CurrentValueController.onPageLoad().url
              )
                .withVisuallyHiddenText(messages("site.change.hidden.currentValue"))
                .withAttribute(("id", "change_currentValue"))
            )
          )
        } else {
          SummaryListRowViewModel(
            key = KeyViewModel("currentValue.checkYourAnswersLabel").withCssClass(keyCssClass),
            value = ValueViewModel(value).withCssClass(valueCssClass)
          )
        }
    }
}
