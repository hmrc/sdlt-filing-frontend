/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.checkanswerssummary

import models.scalabuild.UserAnswers
import pages.scalabuild.LeaseDatesPage
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

object LeaseEndDateSummary {

  def row(answers: UserAnswers, withAction: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(LeaseDatesPage).map { answer =>
      if (withAction) {
        SummaryListRowViewModel(
          key = KeyViewModel(s"leaseDates.endDate.checkYourAnswersLabel"),
          value = ValueViewModel(answer.endDate.format(localDateTimeFormatter())).withCssClass(valueCssClass),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.scalabuild.routes.LeaseDatesController.onPageLoad().url
            )
              .withVisuallyHiddenText(messages("site.change.hidden"))
          )
        )
      } else {
        SummaryListRowViewModel(
          key = KeyViewModel(s"leaseDates.endDate.checkYourAnswersLabel"),
          value = ValueViewModel(answer.endDate.format(localDateTimeFormatter())).withCssClass(valueCssClass)
        )
      }
    }
}
