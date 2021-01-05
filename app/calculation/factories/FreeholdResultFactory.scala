/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package calculation.factories

import calculation.data.ResultText._
import calculation.enums.{CalcTypes, TaxTypes}
import calculation.models.{CalculationDetails, Result}
import calculation.models.calculationtables.{SlabResult, SliceResult}
import calculation.utils.StringUtils

object FreeholdResultFactory {

  def freeholdResidentialJuly20OnwardsFTBResult(sliceResult: SliceResult): Result = {
    Result(
      totalTax = sliceResult.taxDue.toInt,
      resultHeading = Some(RESULT_HEADING_AFTER_JULY_2020_AND_BEFORE_MARCH_2021),
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

  def freeholdResidentialNov17OnwardsFTBResult(sliceResult: SliceResult, afterMarch2021: Boolean): Result = {

    val resHeading = if(afterMarch2021) {
      Some(RESULT_HEADING_AFTER_MARCH_2021)
    } else {
      (None)
    }

    Result(
      totalTax = sliceResult.taxDue.toInt,
      resultHeading = resHeading,
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

  def freeholdResidentialAddPropJuly20OnwardsResult(sliceResult: SliceResult, refundEntitlement: Option[Int]): Result = {

    val resHint = refundEntitlement.map { amount =>
      s"$RESULT_HINT_ADDNL_PROP_2020$RESULT_HINT_ADDNL_PROP_REFUND${StringUtils.intToMonetaryString(amount)}."
    }

    Result(
      totalTax = sliceResult.taxDue.toInt,
      resultHeading = Some(RESULT_HEADING_AFTER_JULY_2020_AND_BEFORE_MARCH_2021),
      resultHint = resHint,
      npv = None,
      taxCalcs = Seq(
        CalculationDetails(
          taxType = TaxTypes.premium,
          calcType = CalcTypes.slice,
          detailHeading = Some(DETAIL_HEADING_TOTAL_SDLT_FROM_JULY_2020),
          bandHeading = Some(DETAIL_COL_HEADER_PURCHASE_PRICE),
          detailFooter = Some(DETAIL_FOOTER_TOTAL),
          taxDue = sliceResult.taxDue.toInt,
          slices = Some(sliceResult.slices)
        )
      )
    )
  }


  def freeholdResidentialAddPropApr16OnwardsResult(sliceResult: SliceResult, refundEntitlement: Option[Int], afterMarch2021: Boolean): Result = {

    val (resHeading, resHint, detailHeading) = if(afterMarch2021) {
      (Some(RESULT_HEADING_AFTER_MARCH_2021),
        refundEntitlement.map { amount =>
        s"$RESULT_HINT_ADDNL_PROP_AFTER_MARCH_2021$RESULT_HINT_ADDNL_PROP_REFUND${StringUtils.intToMonetaryString(amount)}."},
        Some(DETAIL_HEADING_TOTAL_SDLT)
      )
    } else {
      (Some(RESULT_HEADING_FROM_APR_2016),
        refundEntitlement.map { amount =>
        s"$RESULT_HINT_ADDNL_PROP_REFUND${StringUtils.intToMonetaryString(amount)}."
      }, Some(DETAIL_HEADING_TOTAL_SDLT_FROM_APR_2016))
    }

      Result(
      totalTax = sliceResult.taxDue.toInt,
      resultHeading = resHeading,
      resultHint = resHint,
      npv = None,
      taxCalcs = Seq(
        CalculationDetails(
          taxType = TaxTypes.premium,
          calcType = CalcTypes.slice,
          detailHeading = detailHeading,
          bandHeading = Some(DETAIL_COL_HEADER_PURCHASE_PRICE),
          detailFooter = Some(DETAIL_FOOTER_TOTAL),
          taxDue = sliceResult.taxDue.toInt,
          slices = Some(sliceResult.slices)
        )
      )
    )
  }



  def freeholdResidentialJuly20OnwardsResult(sliceResult: SliceResult, asPrevResult: Boolean): Result = {

    val (resHeading, resHint, detailHeading) = if(asPrevResult) {
      (Some(RESULT_HEADING_BEFORE_JULY_2020),
        Some(RESULT_HINT_EXCHANGE_JULY_20),
        Some(DETAIL_HEADING_TOTAL_SDLT_FROM_JULY_2020))
    } else {
      (Some(RESULT_HEADING_AFTER_JULY_2020_AND_BEFORE_MARCH_2021), None, Some(DETAIL_HEADING_TOTAL_SDLT))
    }

    Result(
      totalTax = sliceResult.taxDue.toInt,
      resultHeading = resHeading,
      resultHint = resHint,
      npv = None,
      taxCalcs = Seq(
        CalculationDetails(
          taxType = TaxTypes.premium,
          calcType = CalcTypes.slice,
          detailHeading = detailHeading,
          bandHeading = Some(DETAIL_COL_HEADER_PURCHASE_PRICE),
          detailFooter = Some(DETAIL_FOOTER_TOTAL),
          taxDue = sliceResult.taxDue.toInt,
          slices = Some(sliceResult.slices)
        )
      )
    )
  }

  def freeholdResidentialDec14OnwardsResult(sliceResult: SliceResult, asPrevResult: Boolean, afterMarch2021: Boolean): Result = {

    val (resHeading, resHint, detailHeading) =
      (asPrevResult, afterMarch2021) match {
        case (true, false)  => (Some(RESULT_HEADING_BEFORE_APR_2016), Some(RESULT_HINT_EXCHANGE_BEFORE_NOV_2015), Some(DETAIL_HEADING_TOTAL_SDLT_BEFORE_APR_2016))
        case (false, true)  => (Some(RESULT_HEADING_AFTER_MARCH_2021), None, Some(DETAIL_HEADING_TOTAL_SDLT))
        case (true, true)   => (Some(RESULT_HEADING_BEFORE_JULY_2020), Some(RESULT_HINT_EXCHANGE_JULY_20), Some(DETAIL_HEADING_TOTAL_SDLT))
        case _ => (None, None, Some(DETAIL_HEADING_TOTAL_SDLT))
      }

    Result(
      totalTax = sliceResult.taxDue.toInt,
      resultHeading = resHeading,
      resultHint = resHint,
      npv = None,
      taxCalcs = Seq(
        CalculationDetails(
          taxType = TaxTypes.premium,
          calcType = CalcTypes.slice,
          detailHeading = detailHeading,
          bandHeading = Some(DETAIL_COL_HEADER_PURCHASE_PRICE),
          detailFooter = Some(DETAIL_FOOTER_TOTAL),
          taxDue = sliceResult.taxDue.toInt,
          slices = Some(sliceResult.slices)
        )
      )
    )
  }

  def freeholdResidentialMar12toDec14Result(slabResult: SlabResult): Result = {

    Result(
      totalTax = slabResult.taxDue.toInt,
      resultHeading = None,
      resultHint = None,
      npv = None,
      taxCalcs = Seq(
        CalculationDetails(
          taxType = TaxTypes.premium,
          calcType = CalcTypes.slab,
          detailHeading = None,
          bandHeading = None,
          detailFooter = None,
          taxDue = slabResult.taxDue.toInt,
          rate = Some(slabResult.rate.toInt),
          slices = None
        )
      )
    )
  }

  def freeholdNonResidentialMar12toMar16Result(slabResult: SlabResult, asPrevResult: Boolean): Result = {

    val (resHeading, resHint) = if(asPrevResult) {
      (Some(RESULT_HEADING_BEFORE_MAR_2016), Some(RESULT_HINT_EXCHANGE_BEFORE_MAR_2016))
    } else (None, None)

    Result(
      totalTax = slabResult.taxDue.toInt,
      resultHeading = resHeading,
      resultHint = resHint,
      npv = None,
      taxCalcs = Seq(
        CalculationDetails(
          taxType = TaxTypes.premium,
          calcType = CalcTypes.slab,
          detailHeading = None,
          bandHeading = None,
          detailFooter = None,
          taxDue = slabResult.taxDue.toInt,
          rate = Some(slabResult.rate.toInt),
          slices = None
        )
      )
    )
  }

  def freeholdNonResidentialMar16OnwardsResult(sliceResult: SliceResult): Result = {

    Result(
      totalTax = sliceResult.taxDue.toInt,
      resultHeading = Some(RESULT_HEADING_FROM_MAR_2016),
      resultHint = None,
      npv = None,
      taxCalcs = Seq(
        CalculationDetails(
          taxType = TaxTypes.premium,
          calcType = CalcTypes.slice,
          detailHeading = Some(DETAIL_HEADING_TOTAL_SDLT_FROM_MAR_2016),
          bandHeading = Some(DETAIL_COL_HEADER_PURCHASE_PRICE),
          detailFooter = Some(DETAIL_FOOTER_TOTAL),
          taxDue = sliceResult.taxDue.toInt,
          slices = Some(sliceResult.slices)
        )
      )
    )
  }

}
