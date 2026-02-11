/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.scalabuild.FormatUtils.{bigDecimalFormat, keyCssClass, valueCssClass}
import viewmodels.scalabuild.govuk.summarylist.{
  FluentKey,
  FluentValue,
  KeyViewModel,
  SummaryListRowViewModel,
  ValueViewModel
}
import viewmodels.scalabuild.implicits._

object NpvSummary {

  def row(npv: Option[Int])(implicit messages: Messages): Option[SummaryListRow] = {
    npv.map { npv =>
      SummaryListRowViewModel(
        key = KeyViewModel(messages("npvSummary.resultLabel")).withCssClass(keyCssClass),
        value = ValueViewModel(bigDecimalFormat(npv)).withCssClass(valueCssClass)
      )
    }
  }

}
