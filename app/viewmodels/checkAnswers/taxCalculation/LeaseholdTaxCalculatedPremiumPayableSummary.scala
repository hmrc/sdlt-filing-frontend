package viewmodels.checkAnswers.taxCalculation

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import viewmodels.checkAnswers.summary.SummaryRowResult
import viewmodels.checkAnswers.summary.SummaryRowResult.Row
import viewmodels.taxCalculation.CalculationResultViewModel

object LeaseholdTaxCalculatedPremiumPayableSummary {

  def row(vm: CalculationResultViewModel)(implicit messages: Messages): SummaryRowResult = {
    val label = messages("taxCalculation.leaseholdSelfAssessed.premiumPayable.checkYourAnswers")

    val premium = vm.totalPremiumPayable

    val value = ValueViewModel(HtmlContent(s"$premium"))

    Row(
      SummaryListRowViewModel(
        key = label,
        value = value
      )
    )
  }
}
