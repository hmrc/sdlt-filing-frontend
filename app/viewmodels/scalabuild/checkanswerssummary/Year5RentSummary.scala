/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.checkanswerssummary

import models.scalabuild.UserAnswers
import pages.scalabuild.RentPage
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

object Year5RentSummary {

  def row(answers: UserAnswers, withAction: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(RentPage).flatMap { answer =>
      answer.year5Rent.map { rent =>
        if (withAction) {
          SummaryListRowViewModel(
            key = KeyViewModel("year5Rent.checkYourAnswersLabel").withCssClass(keyCssClass),
            value = ValueViewModel(bigDecimalFormat(rent)).withCssClass(valueCssClass),
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
            key = KeyViewModel("year5Rent.checkYourAnswersLabel").withCssClass(keyCssClass),
            value = ValueViewModel(bigDecimalFormat(rent)).withCssClass(valueCssClass)
          )
        }
      }
    }
}
