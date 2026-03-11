/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild

import models.scalabuild.HoldingTypes
import models.scalabuild.HoldingTypes.Leasehold
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.scalabuild.FormatUtils.bigDecimalFormat
import viewmodels.scalabuild.govuk.summarylist.{KeyViewModel, SummaryListRowViewModel, ValueViewModel}
import viewmodels.scalabuild.implicits._

object TotalDueSummary {

  def row(totalTax: Int, holding: HoldingTypes, slab: Boolean = false, index: Int)(implicit messages: Messages): SummaryListRow = {
    val messageKey = if (slab) "slab" else holding.toString.toLowerCase
    val valueText = if (holding == Leasehold) bigDecimalFormat(value = totalTax) else bigDecimalFormat(value = totalTax, currencySymbol = "")
    SummaryListRowViewModel(
      key = KeyViewModel(s"totalTax.resultLabel.${messageKey}"),
      value = ValueViewModel(content = HtmlContent(s"""<span id="totalTax$index">$valueText</span>"""))
    )
  }
}
