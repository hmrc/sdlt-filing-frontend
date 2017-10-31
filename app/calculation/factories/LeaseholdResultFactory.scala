package calculation.factories

import calculation.data.ResultText._
import calculation.enums.{CalcTypes, TaxTypes}
import calculation.models.{CalculationDetails, Result}
import calculation.models.calculationtables.{SlabResult, SliceResult}

object LeaseholdResultFactory {

  def leaseholdResidentialDec14OnwardsResult(leaseResult: SliceResult, premiumResult: SliceResult, npv: BigDecimal): Result = {
    val leaseCalcDetails = CalculationDetails(
      taxType = TaxTypes.rent,
      calcType = CalcTypes.slice,
      detailHeading = Some(DETAIL_HEADING_SDLT_ON_RENT),
      bandHeading = Some(DETAIL_COL_HEADER_RENT),
      detailFooter = Some(DETAIL_FOOTER_RENT),
      taxDue = leaseResult.taxDue.toInt,
      slices = Some(leaseResult.slices)
    )
    val premiumCalcDetails = CalculationDetails(
      taxType = TaxTypes.premium,
      calcType = CalcTypes.slice,
      detailHeading = Some(DETAIL_HEADING_SDLT_ON_PREM),
      bandHeading = Some(DETAIL_COL_HEADER_PREM),
      detailFooter = Some(DETAIL_FOOTER_PREM),
      taxDue = premiumResult.taxDue.toInt,
      slices = Some(premiumResult.slices)
    )

    Result(
      totalTax = leaseCalcDetails.taxDue + premiumCalcDetails.taxDue,
      resultHeading = None,
      resultHint = None,
      npv = Some(npv.toInt),
      taxCalcs = Seq(
        leaseCalcDetails,
        premiumCalcDetails
      )
    )
  }

  def leaseholdResidentialMar12toDec14Result(leaseResult: SliceResult, premiumResult: SlabResult, npv: BigDecimal): Result = {
    val leaseCalcDetails = CalculationDetails(
      taxType = TaxTypes.rent,
      calcType = CalcTypes.slice,
      detailHeading = Some(DETAIL_HEADING_SDLT_ON_RENT),
      bandHeading = Some(DETAIL_COL_HEADER_RENT),
      detailFooter = Some(DETAIL_FOOTER_RENT),
      taxDue = leaseResult.taxDue.toInt,
      slices = Some(leaseResult.slices)
    )
    val premiumCalcDetails = CalculationDetails(
      taxType = TaxTypes.premium,
      calcType = CalcTypes.slab,
      detailHeading = None,
      bandHeading = None,
      detailFooter = None,
      taxDue = premiumResult.taxDue.toInt,
      slices = None,
      rate = Some(premiumResult.rate.toInt)
    )

    Result(
      totalTax = leaseCalcDetails.taxDue + premiumCalcDetails.taxDue,
      resultHeading = None,
      resultHint = None,
      npv = Some(npv.toInt),
      taxCalcs = Seq(
        leaseCalcDetails,
        premiumCalcDetails
      )
    )
  }

}
