package calculation.factories

import calculation.data.ResultText._
import calculation.enums.{CalcTypes, TaxTypes}
import calculation.models.{CalculationDetails, Result}
import calculation.models.calculationtables.{SlabResult, SliceResult}
import calculation.utils.StringUtils

object LeaseholdResultFactory {

  def leaseholdResidentialAddPropApr16Onwards(leaseResult: SliceResult, premiumResult: SliceResult, npv: BigDecimal, refundEntitlement: Option[Int]): Result = {
    val resHint = refundEntitlement.map { amount =>
      s"$RESULT_HINT_ADDNL_PROP_REFUND${StringUtils.intToMonetaryString(amount)}."
    }

    val leaseCalcDetails = CalculationDetails(
      taxType = TaxTypes.rent,
      calcType = CalcTypes.slice,
      detailHeading = Some(DETAIL_HEADING_SDLT_ON_RENT_FROM_APR_2016),
      bandHeading = Some(DETAIL_COL_HEADER_RENT),
      detailFooter = Some(DETAIL_FOOTER_RENT),
      taxDue = leaseResult.taxDue.toInt,
      slices = Some(leaseResult.slices)
    )
    val premiumCalcDetails = CalculationDetails(
      taxType = TaxTypes.premium,
      calcType = CalcTypes.slice,
      detailHeading = Some(DETAIL_HEADING_SDLT_ON_PREM_FROM_APR_2016),
      bandHeading = Some(DETAIL_COL_HEADER_PREM),
      detailFooter = Some(DETAIL_FOOTER_PREM),
      taxDue = premiumResult.taxDue.toInt,
      slices = Some(premiumResult.slices)
    )

    Result(
      totalTax = leaseCalcDetails.taxDue + premiumCalcDetails.taxDue,
      resultHeading = Some(RESULT_HEADING_FROM_APR_2016),
      resultHint = resHint,
      npv = Some(npv.toInt),
      taxCalcs = Seq(
        leaseCalcDetails,
        premiumCalcDetails
      )
    )
  }

  def leaseholdResidentialDec14OnwardsResult(leaseResult: SliceResult, premiumResult: SliceResult, npv: BigDecimal,  asPrevResult: Boolean): Result = {
    val (resHeading, resHint, leaseDetailHeading, premiumDetailHeading) = if(asPrevResult) {
      (Some(RESULT_HEADING_BEFORE_APR_2016),
        Some(RESULT_HINT_EXCHANGE_BEFORE_NOV_2015),
        Some(DETAIL_HEADING_SDLT_ON_RENT_BEFORE_APR_2016),
        Some(DETAIL_HEADING_SDLT_ON_PREM_BEFORE_APR_2016))
    } else {
      (None, None, Some(DETAIL_HEADING_SDLT_ON_RENT), Some(DETAIL_HEADING_SDLT_ON_PREM))
    }

    val leaseCalcDetails = CalculationDetails(
      taxType = TaxTypes.rent,
      calcType = CalcTypes.slice,
      detailHeading = leaseDetailHeading,
      bandHeading = Some(DETAIL_COL_HEADER_RENT),
      detailFooter = Some(DETAIL_FOOTER_RENT),
      taxDue = leaseResult.taxDue.toInt,
      slices = Some(leaseResult.slices)
    )
    val premiumCalcDetails = CalculationDetails(
      taxType = TaxTypes.premium,
      calcType = CalcTypes.slice,
      detailHeading = premiumDetailHeading,
      bandHeading = Some(DETAIL_COL_HEADER_PREM),
      detailFooter = Some(DETAIL_FOOTER_PREM),
      taxDue = premiumResult.taxDue.toInt,
      slices = Some(premiumResult.slices)
    )

    Result(
      totalTax = leaseCalcDetails.taxDue + premiumCalcDetails.taxDue,
      resultHeading = resHeading,
      resultHint = resHint,
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
