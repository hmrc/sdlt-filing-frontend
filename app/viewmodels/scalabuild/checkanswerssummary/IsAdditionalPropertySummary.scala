/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.checkanswerssummary

import models.scalabuild.UserAnswers
import pages.scalabuild.IsAdditionalPropertyPage
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

object IsAdditionalPropertySummary {

  def row(answers: UserAnswers, withAction: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(IsAdditionalPropertyPage).map { answer =>
      val value = if (answer) "Yes" else "No"
      if (withAction) {
        SummaryListRowViewModel(
          key = KeyViewModel("isAdditionalProperty.checkYourAnswersLabel").withCssClass(keyCssClass),
          value = ValueViewModel.withId(text = s"$value",id = "td2_twoOrMoreProperties"),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              //todo: change controller when double page removed
              controllers.scalabuild.routes.AdditionalPropAndReplaceController.onPageLoad().url
            )
              .withVisuallyHiddenText(messages("site.change.hidden.twoOrMoreProperties"))
              .withAttribute(("id", "change_twoOrMoreProperties"))
          )
        )
      } else {
        SummaryListRowViewModel(
          key = KeyViewModel("isAdditionalProperty.checkYourAnswersLabel").withCssClass(keyCssClass),
          value = ValueViewModel.withId(text = s"$value",id = "td2_twoOrMoreProperties")
        )
      }
    }
}
