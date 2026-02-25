/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.checkanswerssummary

import models.scalabuild.UserAnswers
import pages.scalabuild.LeaseTermPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.scalabuild.FormatUtils.{keyCssClass, valueCssClass}
import viewmodels.scalabuild.govuk.summarylist.{
  FluentKey,
  FluentValue,
  KeyViewModel,
  SummaryListRowViewModel,
  ValueViewModel
}
import viewmodels.scalabuild.implicits._

object LeaseTermSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(LeaseTermPage).map { answer =>
      {
        SummaryListRowViewModel(
          key = KeyViewModel("leaseDate.term.checkYourAnswersLabel").withCssClass(keyCssClass),
          value = ValueViewModel(
            s"${answer.years.toString} years ${answer.days} day${if (answer.days == 1) "" else "s"}"
          ).withCssClass(valueCssClass)
        )
      }
    }
}
