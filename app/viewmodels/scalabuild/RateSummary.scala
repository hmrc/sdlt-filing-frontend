/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.scalabuild.FormatUtils.{keyCssClass, valueCssClass}
import viewmodels.scalabuild.govuk.summarylist.{
  FluentKey,
  FluentValue,
  KeyViewModel,
  SummaryListRowViewModel,
  ValueViewModel
}
import viewmodels.scalabuild.implicits._

object RateSummary {

  def row(rate: Option[Int])(implicit messages: Messages): Option[SummaryListRow] = {
    rate.map { rate =>
      SummaryListRowViewModel(
        key = KeyViewModel(messages("rateSummary.resultLabel")).withCssClass(keyCssClass),
        value = ValueViewModel(rate.toString).withCssClass(valueCssClass)
      )
    }
  }

}
