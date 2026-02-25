/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.checkanswerssummary

import models.scalabuild.UserAnswers
import pages.scalabuild.ContractPost201603Page
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.scalabuild.FormatUtils.{keyCssClass, valueCssClass}
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

object ContractPostMarch2016Summary {

  def row(answers: UserAnswers, withAction: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ContractPost201603Page).map { answer =>
      val value = if (answer) "Yes" else "No"
      if (withAction) {
        SummaryListRowViewModel(
          key = KeyViewModel("contractPost201603.checkYourAnswersLabel").withCssClass(keyCssClass),
          value = ValueViewModel(value).withCssClass(valueCssClass),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.scalabuild.routes.ContractPost201603Controller.onPageLoad().url
            )
              .withVisuallyHiddenText(messages("site.change.hidden"))
          )
        )
      } else {
        SummaryListRowViewModel(
          key = KeyViewModel("contractPost201603.checkYourAnswersLabel").withCssClass(keyCssClass),
          value = ValueViewModel(value).withCssClass(valueCssClass)
        )

      }
    }
}
