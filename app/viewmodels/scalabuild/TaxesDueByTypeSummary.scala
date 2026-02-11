/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild

import enums.TaxTypes
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

object TaxesDueByTypeSummary {
  def row(taxType: TaxTypes.Value, total: Int)(implicit messages: Messages): SummaryListRow = {
    SummaryListRowViewModel(
      key = KeyViewModel(messages(s"${taxType.toString}.resultLabel")).withCssClass(keyCssClass),
      value = ValueViewModel(bigDecimalFormat(total)).withCssClass(valueCssClass)
    )
  }
}
