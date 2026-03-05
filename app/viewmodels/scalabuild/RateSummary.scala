/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.scalabuild.govuk.summarylist.{KeyViewModel, SummaryListRowViewModel, ValueViewModel}
import viewmodels.scalabuild.implicits._

object RateSummary {

  def row(rate: Option[Int], index: Int)(implicit messages: Messages): Option[SummaryListRow] = {
    rate.map { rate =>
      SummaryListRowViewModel(
        key = KeyViewModel(messages("rateSummary.resultLabel")),
        value = ValueViewModel(content = HtmlContent(s"""<span id="taxRate$index">$rate</span>"""))
      )
    }
  }
}
