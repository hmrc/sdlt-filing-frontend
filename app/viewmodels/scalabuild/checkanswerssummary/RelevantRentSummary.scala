/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.checkanswerssummary

import models.scalabuild.UserAnswers
import pages.scalabuild.RelevantRentPage
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

object RelevantRentSummary {

  def row(answers: UserAnswers, withAction: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(RelevantRentPage).map { answer =>
      if (withAction) {
        SummaryListRowViewModel(
          key = KeyViewModel("relevantRent.checkYourAnswersLabel").withCssClass(keyCssClass),
          value = ValueViewModel.withId(text = bigDecimalFormat(answer), id = "td2_relevantRent"),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.scalabuild.routes.RelevantRentController.onPageLoad().url
            )
              .withVisuallyHiddenText(messages("site.change.hidden.relevantRent"))
              .withAttribute(("id", "change_relevantRent"))
          )
        )
      } else {
        SummaryListRowViewModel(
          key = KeyViewModel("relevantRent.checkYourAnswersLabel").withCssClass(keyCssClass),
          value = ValueViewModel.withId(text = bigDecimalFormat(answer), id = "td2_relevantRent"),
        )
      }
    }
}
