/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.scalabuild.FormatUtils.bigDecimalFormat
import viewmodels.scalabuild.govuk.summarylist.{KeyViewModel, SummaryListRowViewModel, ValueViewModel}
import viewmodels.scalabuild.implicits._

object NpvSummary {

  def row(npv: Option[Int], index: Int)(implicit messages: Messages): Option[SummaryListRow] = {
    npv.map { npv =>
      SummaryListRowViewModel(
        key = KeyViewModel(messages("npvSummary.resultLabel")),
        value = ValueViewModel(content = HtmlContent(s"""<span id="npv$index">${bigDecimalFormat(npv)}</span>"""))
      )
    }
  }
}
