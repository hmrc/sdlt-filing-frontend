/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.checkanswerssummary

import models.scalabuild.UserAnswers
import pages.scalabuild.PremiumPage
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

object PurchasePriceSummary {

  def row(answers: UserAnswers, withAction: Boolean, index: Option[Int] = None, resultTable: Boolean = false)(implicit
      messages: Messages
  ): Option[SummaryListRow] =
    answers.get(PremiumPage).map { answer =>
      val valueText = bigDecimalFormat(answer)
      if (withAction) {
        SummaryListRowViewModel(
          key = KeyViewModel("purchasePrice.checkYourAnswersLabel").withCssClass(keyCssClass),
          value = ValueViewModel.withId(text = valueText, id = "td2_purchasePrice"),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.scalabuild.routes.PurchasePriceController.onPageLoad().url
            )
              .withVisuallyHiddenText(messages("site.change.hidden"))
          )
        )
      } else if (resultTable) {
        val valueTextWithoutSymbol = bigDecimalFormat(value = answer, currencySymbol = "")
        SummaryListRowViewModel(
          key = KeyViewModel("purchasePrice.resultLabel"),
          value = ValueViewModel.withId(text =valueTextWithoutSymbol, id = s"premium${index.getOrElse(0)}")
        )
      } else {
        SummaryListRowViewModel(
          key = KeyViewModel("purchasePrice.checkYourAnswersLabel").withCssClass(keyCssClass),
          value = ValueViewModel.withId(text = valueText, id = "td2_purchasePrice")
        )
      }
    }
}
