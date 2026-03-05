/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package viewmodels.scalabuild

import enums.TaxTypes
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.scalabuild.FormatUtils.bigDecimalFormat
import viewmodels.scalabuild.govuk.summarylist.{
  ActionItemViewModel,
  FluentActionItem,
  KeyViewModel,
  SummaryListRowViewModel,
  ValueViewModel
}
import viewmodels.scalabuild.implicits._

object TaxesDueByTypeSummary {
  def row(taxType: TaxTypes.Value, total: Int, rate: Option[Int], resultIndex: Int, taxCalcIndex: Int)(implicit
      messages: Messages
  ): SummaryListRow = {
    val keyText: String = messages(s"${taxType.toString}.resultLabel")
    val keyId: String = s"taxType$resultIndex$taxCalcIndex"
    val valueText: String = bigDecimalFormat(total)
    val valueId: String = s"taxDue$resultIndex$taxCalcIndex"
    rate match {
      case None =>
        SummaryListRowViewModel(
          key = KeyViewModel.withId(text = keyText, id = keyId),
          value = ValueViewModel.withId(text = valueText, id = valueId),
          actions = Seq(
            ActionItemViewModel(
              "site.viewCalculation",
              controllers.scalabuild.routes.DetailController.onPageLoad(resultIndex).url
            )
              .withVisuallyHiddenText(messages("site.viewCalculation"))
              .withAttribute(attribute = ("id", s"detailCalc$taxCalcIndex"))
          )
        )
      case Some(percentageRate) =>
        SummaryListRowViewModel(
          key = KeyViewModel.withId(text = keyText + s" ($percentageRate%)", id = keyId),
          value = ValueViewModel.withId(text = valueText, id = valueId)
        )
    }

  }
}
