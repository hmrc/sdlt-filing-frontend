package calculation.factories

import calculation.data.ResultText._
import calculation.enums.{CalcTypes, TaxTypes}
import calculation.models.{CalculationDetails, Result}
import calculation.models.calculationtables.SliceResult

object ResultFactory {

  def freeholdResidentialDec14OnwardsResult(sliceResult: SliceResult): Result = {

    Result(
      totalTax = sliceResult.taxDue.toInt,
      resultHeading = None,
      resultHint = None,
      npv = None,
      taxCalcs = Seq(
        CalculationDetails(
          taxType = TaxTypes.premium,
          calcType = CalcTypes.slice,
          detailHeading = Some(DETAIL_HEADING_TOTAL_SDLT),
          bandHeading = Some(DETAIL_COL_HEADER_PURCHASE_PRICE),
          detailFooter = Some(DETAIL_FOOTER_TOTAL),
          taxDue = sliceResult.taxDue.toInt,
          slices = Some(sliceResult.slices)
        )
      )
    )
  }

}
