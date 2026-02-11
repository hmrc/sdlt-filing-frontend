/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild

import models.scalabuild.HoldingTypes
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

object TotalDueSummary {

  def row(totalTax: Int, holding: HoldingTypes, slab: Boolean = false)(implicit messages: Messages): SummaryListRow = {
    val messageKey = if (slab) "slab" else holding.toString.toLowerCase
    SummaryListRowViewModel(
      key = KeyViewModel(s"totalTax.resultLabel.${messageKey}").withCssClass(keyCssClass),
      value = ValueViewModel(bigDecimalFormat(totalTax)).withCssClass(valueCssClass)
    )
  }

}
