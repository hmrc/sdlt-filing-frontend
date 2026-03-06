/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.checkanswerssummary

import models.scalabuild.UserAnswers
import pages.scalabuild.ContractPost201603Page
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

object ContractPostMarch2016Summary {

  def row(answers: UserAnswers, withAction: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ContractPost201603Page).map { answer =>
      val value = if (answer) "Yes" else "No"
      if (withAction) {
        SummaryListRowViewModel(
          key = KeyViewModel("contractPost201603.checkYourAnswersLabel").withCssClass(keyCssClass),
          value = ValueViewModel.withId(text = s"$value",id = "td2_contractVariedPost201603"),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.scalabuild.routes.ContractPost201603Controller.onPageLoad().url
            )
              .withVisuallyHiddenText(messages("site.change.hidden.contractPost201603"))
              .withAttribute(("id", "change_contractPost201603"))
          )
        )
      } else {
        SummaryListRowViewModel(
          key = KeyViewModel("contractPost201603.checkYourAnswersLabel").withCssClass(keyCssClass),
          value = ValueViewModel.withId(text = s"$value",id = "td2_contractVariedPost201603"),
        )

      }
    }
}
