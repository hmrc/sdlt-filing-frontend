/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package factories

import data.ResultText._
import enums.{CalcTypes, TaxTypes}
import models.{CalculationDetails, Result}
import models.calculationtables.{SlabResult, SliceResult}
import utils.StringUtils

object LeaseholdResultFactory {

  def leaseholdResidentialJuly20OnwardsFTBResult(leaseResult: SliceResult, premiumResult: SliceResult, npv: BigDecimal): Result = {
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
      npv = Some(npv.toInt),
      taxCalcs = Seq(
        leaseCalcDetails,
        premiumCalcDetails
      )
    )
  }

  def leaseholdResidentialNov17OnwardsFTBResult(leaseResult: SliceResult, premiumResult: SliceResult, npv: BigDecimal, afterMarch2021: Boolean): Result = {

    val resHeading = if(afterMarch2021) {
      Some(RESULT_HEADING_AFTER_MARCH_2021)
    } else {
      (None)
    }

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
      npv = Some(npv.toInt),
      taxCalcs = Seq(
        leaseCalcDetails,
        premiumCalcDetails
      ),
      resultHeading = resHeading
    )
  }

  def leaseholdResidentialAddPropApr21OnwardsNonUKRes(leaseResult: SliceResult, premiumResult: SliceResult, npv: BigDecimal, refundEntitlement: Option[Int]): Result = {

    val (resHeading, resHint, leaseDetailHeading, premDetailHeading) =
      (Some(RESULT_HEADING_AFTER_MARCH_2021),
        refundEntitlement.map { amount =>
          s"$RESULT_HINT_ADDNL_PROP_AFTER_MARCH_2021$RESULT_HINT_ADDNL_PROP_REFUND${StringUtils.intToMonetaryString(amount)}."},
        Some(DETAIL_HEADING_SDLT_ON_RENT), Some(DETAIL_HEADING_SDLT_ON_PREM))

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
      detailHeading = premDetailHeading,
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

  def leaseholdResidentialAddPropJuly20Onwards(leaseResult: SliceResult, premiumResult: SliceResult, npv: BigDecimal, refundEntitlement: Option[Int]): Result = {
    val resHint = refundEntitlement.map { amount =>
      s"$RESULT_HINT_ADDNL_PROP_2020$RESULT_HINT_ADDNL_PROP_REFUND${StringUtils.intToMonetaryString(amount)}."
    }

    val leaseCalcDetails = CalculationDetails(
      taxType = TaxTypes.rent,
      calcType = CalcTypes.slice,
      detailHeading = Some(DETAIL_HEADING_TOTAL_SDLT_FROM_JULY_2020_AND_BEFORE_MARCH_2021),
      bandHeading = Some(DETAIL_COL_HEADER_RENT),
      detailFooter = Some(DETAIL_FOOTER_RENT),
      taxDue = leaseResult.taxDue.toInt,
      slices = Some(leaseResult.slices)
    )
    val premiumCalcDetails = CalculationDetails(
      taxType = TaxTypes.premium,
      calcType = CalcTypes.slice,
      detailHeading = Some(DETAIL_HEADING_TOTAL_SDLT_FROM_JULY_2020_AND_BEFORE_MARCH_2021),
      bandHeading = Some(DETAIL_COL_HEADER_PREM),
      detailFooter = Some(DETAIL_FOOTER_PREM),
      taxDue = premiumResult.taxDue.toInt,
      slices = Some(premiumResult.slices)
    )

    Result(
      totalTax = leaseCalcDetails.taxDue + premiumCalcDetails.taxDue,
      resultHeading = Some(RESULT_HEADING_AFTER_JULY_2020_AND_BEFORE_MARCH_2021),
      resultHint = resHint,
      npv = Some(npv.toInt),
      taxCalcs = Seq(
        leaseCalcDetails,
        premiumCalcDetails
      )
    )
  }

  def leaseholdResidentialAddPropApr16Onwards(leaseResult: SliceResult, premiumResult: SliceResult, npv: BigDecimal, refundEntitlement: Option[Int], afterMarch2021: Boolean): Result = {

    val (resHeading, resHint, leaseDetailHeading, premDetailHeading) = if(afterMarch2021){
      (Some(RESULT_HEADING_AFTER_MARCH_2021),
      refundEntitlement.map { amount =>
        s"$RESULT_HINT_ADDNL_PROP_AFTER_MARCH_2021$RESULT_HINT_ADDNL_PROP_REFUND${StringUtils.intToMonetaryString(amount)}."},
      Some(DETAIL_HEADING_SDLT_ON_RENT), Some(DETAIL_HEADING_SDLT_ON_PREM))
    } else {
      (Some(RESULT_HEADING_FROM_APR_2016),
        refundEntitlement.map { amount =>
          s"$RESULT_HINT_ADDNL_PROP_REFUND${StringUtils.intToMonetaryString(amount)}."},
      Some(DETAIL_HEADING_SDLT_ON_RENT_FROM_APR_2016), Some(DETAIL_HEADING_SDLT_ON_PREM_FROM_APR_2016))
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
      detailHeading = premDetailHeading,
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

  def leaseholdResidentialApr21OnwardsResultNonUKRes(leaseResult: SliceResult, premiumResult: SliceResult, npv: BigDecimal, asPrevResult: Boolean = false, additonalProp: Boolean = false): Result = {
    val (resHeading, resHint, leaseDetailHeading, premiumDetailHeading) =
      (if(asPrevResult) {
        if(additonalProp) {
          Some(DETAIL_ADDITIONAL_DWELLINGS)
        }else{
          Some(RESULT_HEADING_AFTER_MARCH_2021_NON_RES)
        }
      } else{
          Some(RESULT_HEADING_AFTER_MARCH_2021)
        }, None, Some(DETAIL_HEADING_SDLT_ON_RENT), Some(DETAIL_HEADING_SDLT_ON_PREM))

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

  def leaseholdResidentialJuly20OnwardsResult(leaseResult: SliceResult, premiumResult: SliceResult, npv: BigDecimal,  asPrevResult: Boolean): Result = {
    val (resHeading, resHint, leaseDetailHeading, premiumDetailHeading) = if(asPrevResult) {
      (Some(RESULT_HEADING_BEFORE_JULY_2020),
        Some(RESULT_HINT_EXCHANGE_JULY_20),
        Some(DETAIL_HEADING_TOTAL_SDLT_FROM_JULY_2020_AND_BEFORE_MARCH_2021),
        Some(DETAIL_HEADING_TOTAL_SDLT_FROM_JULY_2020_AND_BEFORE_MARCH_2021))
    } else {
      (Some(RESULT_HEADING_AFTER_JULY_2020_AND_BEFORE_MARCH_2021), None, Some(DETAIL_HEADING_SDLT_ON_RENT), Some(DETAIL_HEADING_SDLT_ON_PREM))
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

  def leaseholdResidentialDec14OnwardsResult(leaseResult: SliceResult, premiumResult: SliceResult, npv: BigDecimal, asPrevResult: Boolean, afterMarch2021: Boolean, nonUKRes: Boolean = false): Result = {

    val (resHeading, resHint, leaseDetailHeading, premiumDetailHeading) =
      (asPrevResult, afterMarch2021, nonUKRes) match {
        case (true, false, false)  =>
          (Some(RESULT_HEADING_BEFORE_APR_2016), Some(RESULT_HINT_EXCHANGE_BEFORE_NOV_2015),
          Some(DETAIL_HEADING_SDLT_ON_RENT_BEFORE_APR_2016), Some(DETAIL_HEADING_SDLT_ON_PREM_BEFORE_APR_2016))
        case (true, true, true) =>
          (Some(RESULT_HEADING_AFTER_MARCH_2021_NON_RES), None, Some(DETAIL_HEADING_SDLT_ON_RENT), Some(DETAIL_HEADING_SDLT_ON_PREM))
        case (false, true, false)  =>
          (Some(RESULT_HEADING_AFTER_MARCH_2021), None, Some(DETAIL_HEADING_SDLT_ON_RENT), Some(DETAIL_HEADING_SDLT_ON_PREM))
        case (true, true, false)   =>
          (Some(RESULT_HEADING_PREVIOUS_AFTER_MARCH_2021), Some(RESULT_HINT_EXCHANGE_AFTER_MARCH_21),
            Some(DETAIL_HEADING_SDLT_ON_RENT), Some(DETAIL_HEADING_SDLT_ON_PREM))
        case _ =>
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

  def leaseholdNonResidentialMar16OnwardsResult(leaseResult: SliceResult, premiumResult: SliceResult, npv: BigDecimal): Result = {
    val leaseCalcDetails = CalculationDetails(
      taxType = TaxTypes.rent,
      calcType = CalcTypes.slice,
      detailHeading = Some(DETAIL_HEADING_SDLT_ON_RENT_FROM_MAR_2016),
      bandHeading = Some(DETAIL_COL_HEADER_RENT),
      detailFooter = Some(DETAIL_FOOTER_RENT),
      taxDue = leaseResult.taxDue.toInt,
      slices = Some(leaseResult.slices)
    )
    val premiumCalcDetails = CalculationDetails(
      taxType = TaxTypes.premium,
      calcType = CalcTypes.slice,
      detailHeading = Some(DETAIL_HEADING_SDLT_ON_PREM_FROM_MAR_2016),
      bandHeading = Some(DETAIL_COL_HEADER_PREM),
      detailFooter = Some(DETAIL_FOOTER_PREM),
      taxDue = premiumResult.taxDue.toInt,
      slices = Some(premiumResult.slices)
    )

    Result(
      totalTax = leaseCalcDetails.taxDue + premiumCalcDetails.taxDue,
      resultHeading = Some(RESULT_HEADING_FROM_MAR_2016),
      resultHint = None,
      npv = Some(npv.toInt),
      taxCalcs = Seq(
        leaseCalcDetails,
        premiumCalcDetails
      )
    )
  }

  def leaseholdNonResidentialMar12toMar16Result(leaseResult: SliceResult, premiumResult: SlabResult,
                                                npv: BigDecimal, asPrevResult: Boolean = false): Result = {
    val (resultHeading, resultHint, leaseDetailHeading) = if(asPrevResult) {
      (Some(RESULT_HEADING_BEFORE_MAR_2016),
        Some(RESULT_HINT_EXCHANGE_BEFORE_MAR_2016),
        Some(DETAIL_HEADING_SDLT_ON_RENT_BEFORE_MAR_2016))
    } else {
      (None, None, Some(DETAIL_HEADING_SDLT_ON_RENT))
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
      resultHeading = resultHeading,
      resultHint = resultHint,
      npv = Some(npv.toInt),
      taxCalcs = Seq(
        leaseCalcDetails,
        premiumCalcDetails
      )
    )
  }

}
