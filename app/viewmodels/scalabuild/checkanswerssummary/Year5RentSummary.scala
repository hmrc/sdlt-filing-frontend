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

object Year5RentSummary {

  def row(answers: UserAnswers, withAction: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(RentPage).flatMap { answer =>
      answer.year5Rent.map { rent =>
        if (withAction) {
          SummaryListRowViewModel(
            key = KeyViewModel("year5Rent.checkYourAnswersLabel").withCssClass(keyCssClass),
            value = ValueViewModel.withId(text = bigDecimalFormat(rent), id = "td2_year5Rent"),
            actions = Seq(
              ActionItemViewModel(
                "site.change",
                controllers.scalabuild.routes.RentController.onPageLoad().url
              )
                .withVisuallyHiddenText(messages("site.change.hidden.year5Rent"))
                .withAttribute(("id", "change_year5Rent"))
            )
          )
        } else {
          SummaryListRowViewModel(
            key = KeyViewModel("year5Rent.checkYourAnswersLabel").withCssClass(keyCssClass),
            value = ValueViewModel.withId(text = bigDecimalFormat(rent), id = "td2_year5Rent"),
          )
        }
      }
    }
}
