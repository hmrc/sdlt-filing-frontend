/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.checkanswerssummary

import models.scalabuild.UserAnswers
import pages.scalabuild.ResidentialOrNonResidentialPage
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

object PropertySummary {

  def row(answers: UserAnswers, withAction: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ResidentialOrNonResidentialPage).map { answer =>
      if (withAction) {
        SummaryListRowViewModel(
          key = KeyViewModel("propertyType.checkYourAnswersLabel").withCssClass(keyCssClass),
          value = ValueViewModel(answer.displayCya).withCssClass(valueCssClass),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.scalabuild.routes.ResidentialOrNonResidentialController.onPageLoad().url
            )
              .withVisuallyHiddenText(messages("site.change.hidden"))
          )
        )
      } else {
        SummaryListRowViewModel(
          key = KeyViewModel("propertyType.checkYourAnswersLabel").withCssClass(keyCssClass),
          value = ValueViewModel(answer.displayCya).withCssClass(valueCssClass)
        )
      }
    }
}
