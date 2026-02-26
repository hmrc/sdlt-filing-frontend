/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.checkanswerssummary

import models.scalabuild.UserAnswers
import pages.scalabuild.ExchangeContractsPage
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

object ExchangeContractPreMarch2016Summary {

  def row(answers: UserAnswers, withAction: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ExchangeContractsPage).map { answer =>
      val value = if (answer) "Yes" else "No"
      if (withAction) {
        SummaryListRowViewModel(
          key = KeyViewModel("exchangeContracts.checkYourAnswersLabel").withCssClass(keyCssClass),
          value = ValueViewModel.withId(text = s"$value",id = "td2_contractPre201603"),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.scalabuild.routes.ExchangeContractsController.onPageLoad().url
            )
              .withVisuallyHiddenText(messages("site.change.hidden"))
          )
        )
      } else {
        SummaryListRowViewModel(
          key = KeyViewModel("exchangeContracts.checkYourAnswersLabel").withCssClass(keyCssClass),
          value = ValueViewModel.withId(text = s"$value",id = "td2_contractPre201603"),
        )

      }
    }
}
