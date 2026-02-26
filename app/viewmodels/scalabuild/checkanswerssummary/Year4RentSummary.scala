/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.checkanswerssummary

import models.scalabuild.UserAnswers
import pages.scalabuild.RentPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.scalabuild.FormatUtils.{bigDecimalFormat, keyCssClass}
import viewmodels.scalabuild.govuk.summarylist.{
  ActionItemViewModel,
  FluentActionItem,
  FluentKey,
  KeyViewModel,
  SummaryListRowViewModel,
  ValueViewModel
}
import viewmodels.scalabuild.implicits._

object Year4RentSummary {

  def row(answers: UserAnswers, withAction: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(RentPage).flatMap { answer =>
      answer.year4Rent.map { rent =>
        if (withAction) {
          SummaryListRowViewModel(
            key = KeyViewModel("year4Rent.checkYourAnswersLabel").withCssClass(keyCssClass),
            value = ValueViewModel.withId(text = bigDecimalFormat(rent), id = "td2_year4Rent"),
            actions = Seq(
              ActionItemViewModel(
                "site.change",
                controllers.scalabuild.routes.RentController.onPageLoad().url
              )
                .withVisuallyHiddenText(messages("site.change.hidden"))
            )
          )
        } else {
          SummaryListRowViewModel(
            key = KeyViewModel("year4Rent.checkYourAnswersLabel").withCssClass(keyCssClass),
            value = ValueViewModel.withId(text = bigDecimalFormat(rent), id = "td2_year4Rent"),
          )
        }
      }
    }
}
