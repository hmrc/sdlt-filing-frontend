/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild.checkanswerssummary

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.scalabuild.govuk.summarylist.{ActionItemViewModel, FluentActionItem, KeyViewModel, SummaryListRowViewModel, ValueViewModel}

object StartAgainActionSummaryRow {

  def row()(implicit messages: Messages): SummaryListRow =
      SummaryListRowViewModel(
        key = KeyViewModel(Text("")),
        value = ValueViewModel(Text("")),
        actions = Seq(
          ActionItemViewModel(
            Text(messages("site.startAgain")),
            controllers.scalabuild.routes.StartGuidanceController.onPageLoad().url
          )
            .withVisuallyHiddenText(messages("site.startAgain"))
            .withAttribute(("id", "startAgain"))
        )
      )

}
