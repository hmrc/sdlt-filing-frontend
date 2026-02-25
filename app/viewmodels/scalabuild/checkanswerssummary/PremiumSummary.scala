/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.checkanswerssummary

import models.scalabuild.UserAnswers
import pages.scalabuild.PremiumPage
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

object PremiumSummary {

  def row(answers: UserAnswers, withAction: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(PremiumPage).map { answer =>
        if (withAction) {
          SummaryListRowViewModel(
            key = KeyViewModel("premium.checkYourAnswersLabel").withCssClass(keyCssClass),
            value = ValueViewModel(bigDecimalFormat(answer)).withCssClass(valueCssClass),
            actions = Seq(
              ActionItemViewModel(
                "site.change",
                controllers.scalabuild.routes.PremiumController.onPageLoad().url
              )
                .withVisuallyHiddenText(messages("site.change.hidden"))
            )
          )
        } else {
          SummaryListRowViewModel(
            key = KeyViewModel("premium.resultLabel").withCssClass(keyCssClass),
            value = ValueViewModel(bigDecimalFormat(answer)).withCssClass(valueCssClass)
          )
        }
    }
}
