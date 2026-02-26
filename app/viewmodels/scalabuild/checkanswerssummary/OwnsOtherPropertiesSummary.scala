/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.checkanswerssummary

import models.scalabuild.UserAnswers
import pages.scalabuild.OwnsOtherPropertiesPage
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

object OwnsOtherPropertiesSummary {

  def row(answers: UserAnswers, withAction: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(OwnsOtherPropertiesPage).map { answer =>
      val value = if (answer) "Yes" else "No"
      if (withAction) { SummaryListRowViewModel(
        key = KeyViewModel("ownsOtherProperties.checkYourAnswersLabel").withCssClass(keyCssClass),
        value = ValueViewModel.withId(text = s"$value",id = "td2_ownedOtherProperties"),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            controllers.scalabuild.routes.OwnsOtherPropertiesController.onPageLoad().url
          )
            .withVisuallyHiddenText(messages("site.change.hidden"))
        )
      )} else {
        SummaryListRowViewModel(
          key = KeyViewModel("ownsOtherProperties.checkYourAnswersLabel").withCssClass(keyCssClass),
          value = ValueViewModel.withId(text = s"$value",id = "td2_ownedOtherProperties")
        )
      }
    }
}
