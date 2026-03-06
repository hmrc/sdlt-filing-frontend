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

object LeaseEndDateSummary {

  def row(answers: UserAnswers, withAction: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(LeaseDatesPage).map { answer =>
      val dateText = answer.endDate.format(localDateTimeFormatter())
      if (withAction) {
        SummaryListRowViewModel(
          key = KeyViewModel(s"leaseDates.endDate.checkYourAnswersLabel").withCssClass(keyCssClass),
          value = ValueViewModel.withId(text =s"$dateText", id = "td2_leaseEndDate"),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.scalabuild.routes.LeaseDatesController.onPageLoad().url
            )
              .withVisuallyHiddenText(messages("site.change.hidden.leaseEndDate"))
              .withAttribute(("id", "change_leaseEndDate"))
          )
        )
      } else {
        SummaryListRowViewModel(
          key = KeyViewModel(s"leaseDates.endDate.checkYourAnswersLabel").withCssClass(keyCssClass),
          value = ValueViewModel.withId(text =s"$dateText", id = "td2_leaseEndDate")
        )
      }
    }
}
