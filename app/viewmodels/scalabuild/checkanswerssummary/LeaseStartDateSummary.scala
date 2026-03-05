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
import viewmodels.scalabuild.govuk.summarylist.{
  ActionItemViewModel,
  FluentActionItem,
  KeyViewModel,
  SummaryListRowViewModel,
  ValueViewModel
}
import viewmodels.scalabuild.implicits._

object LeaseStartDateSummary {

  def row(answers: UserAnswers, withAction: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(LeaseDatesPage).map { answer =>
      val dateText = answer.startDate.format(localDateTimeFormatter())
      if (withAction) {
        SummaryListRowViewModel(
          key = KeyViewModel(s"leaseDates.startDate.checkYourAnswersLabel"),
          value = ValueViewModel.withId(text =s"$dateText", id = "td2_leaseStartDate"),
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
          key = KeyViewModel(s"leaseDates.startDate.checkYourAnswersLabel"),
          value = ValueViewModel.withId(text =s"$dateText", id = "td2_leaseStartDate"),
        )
      }
    }
}
