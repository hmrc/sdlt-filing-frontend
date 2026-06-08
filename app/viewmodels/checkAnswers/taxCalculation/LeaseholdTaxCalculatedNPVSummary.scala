package viewmodels.checkAnswers.taxCalculation

import models.taxCalculation.{BuildRequestError, MissingNPVCalcError}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import viewmodels.checkAnswers.summary.SummaryRowResult
import viewmodels.checkAnswers.summary.SummaryRowResult.Row
import viewmodels.taxCalculation.CalculationResultViewModel

object LeaseholdTaxCalculatedNPVSummary {

  def row(vm: CalculationResultViewModel)(implicit messages: Messages): SummaryRowResult = {
    val label = messages("taxCalculation.taxDueOnNpv.checkYourAnswersLabel")

    val npv: Either[BuildRequestError, String] = vm.totalNPVTax.toRight(MissingNPVCalcError)

    val value = ValueViewModel(HtmlContent(s"${npv.toOption.get}"))

    Row(
      SummaryListRowViewModel(
        key = label,
        value = value
      )
    )
  }
}
