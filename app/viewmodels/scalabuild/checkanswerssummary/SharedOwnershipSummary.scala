/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.checkanswerssummary

import models.scalabuild.UserAnswers
import pages.scalabuild.SharedOwnershipPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
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

object SharedOwnershipSummary {

  def row(answers: UserAnswers, withAction: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(SharedOwnershipPage).map { answer =>
      val value = if (answer) "Yes" else "No"
      if (withAction) {
        SummaryListRowViewModel(
          key = KeyViewModel("sharedOwnership.checkYourAnswersLabel").withCssClass(keyCssClass),
          value = ValueViewModel.withId(text = s"$value",id = "td2_sharedOwnership"),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.scalabuild.routes.SharedOwnershipController.onPageLoad().url
            )
              .withVisuallyHiddenText(messages("site.change.hidden.sharedOwnership"))
              .withAttribute(("id", "change_sharedOwnership"))
          )
        )
      } else {
        SummaryListRowViewModel(
          key = KeyViewModel("sharedOwnership.checkYourAnswersLabel").withCssClass(keyCssClass),
          value = ValueViewModel.withId(text = s"$value",id = "td2_sharedOwnership"),
        )
      }
    }
}
