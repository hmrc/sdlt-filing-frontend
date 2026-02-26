/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.checkanswerssummary

import models.scalabuild.UserAnswers
import pages.scalabuild.EffectiveDatePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.scalabuild.DateTimeFormats.localDateTimeFormatter
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

object EffectiveDateSummary {

  def row(answers: UserAnswers, withAction: Boolean, index: Option[Int] = None, resultTable: Boolean = false)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(EffectiveDatePage).map { answer =>
      val dateText = answer.format(localDateTimeFormatter())
      val messageKey = if (withAction) "checkYourAnswersLabel" else "resultLabel"
      if (withAction) {
        SummaryListRowViewModel(
          key = KeyViewModel(s"effectiveDate.$messageKey").withCssClass(keyCssClass),
          value = ValueViewModel.withId(text =s"$dateText", id = "td2_effectiveDate"),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.scalabuild.routes.EffectiveDateController.onPageLoad().url
            )
              .withVisuallyHiddenText(messages("site.change.hidden"))
          )
        )
      } else if (resultTable) {
        SummaryListRowViewModel(
          key = KeyViewModel(s"effectiveDate.$messageKey"),
          value = ValueViewModel.withId(text =s"$dateText", id = s"effDate${index.getOrElse(0)}")
        )
      } else {
        val dateText = answer.format(localDateTimeFormatter())
        SummaryListRowViewModel(
          key = KeyViewModel(s"effectiveDate.checkYourAnswersLabel").withCssClass(keyCssClass),
          value = ValueViewModel.withId(text =s"$dateText", id = "td2_effectiveDate")
        )
      }
    }
}
