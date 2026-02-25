/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.checkanswerssummary

import models.scalabuild.UserAnswers
import pages.scalabuild.EffectiveDatePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.scalabuild.DateTimeFormats.localDateTimeFormatter
import viewmodels.scalabuild.FormatUtils.valueCssClass
import viewmodels.scalabuild.govuk.summarylist.{
  ActionItemViewModel,
  FluentActionItem,
  FluentValue,
  KeyViewModel,
  SummaryListRowViewModel,
  ValueViewModel
}
import viewmodels.scalabuild.implicits._

object EffectiveDateSummary {

  def row(answers: UserAnswers, withAction: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(EffectiveDatePage).map { answer =>
      val messageKey = if (withAction) "checkYourAnswersLabel" else "resultLabel"
      if (withAction) {
        SummaryListRowViewModel(
          key = KeyViewModel(s"effectiveDate.$messageKey"),
          value = ValueViewModel(answer.format(localDateTimeFormatter())).withCssClass(valueCssClass),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.scalabuild.routes.EffectiveDateController.onPageLoad().url
            )
              .withVisuallyHiddenText(messages("site.change.hidden"))
          )
        )
      } else {
        SummaryListRowViewModel(
          key = KeyViewModel(s"effectiveDate.$messageKey"),
          value = ValueViewModel(answer.format(localDateTimeFormatter())).withCssClass(valueCssClass)
        )
      }
    }
}
