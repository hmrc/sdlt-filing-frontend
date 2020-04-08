/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package calculation.factories

import calculation.data.ResultText._
import calculation.enums.{CalcTypes, TaxTypes}
import calculation.models.{CalculationDetails, Result}
import calculation.models.calculationtables.{SlabResult, SliceResult}
import calculation.utils.StringUtils

object FreeholdResultFactory {

  def freeholdResidentialNov17OnwardsFTBResult(sliceResult: SliceResult): Result = {
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


  def freeholdResidentialAddPropApr16OnwardsResult(sliceResult: SliceResult, refundEntitlement: Option[Int]): Result = {

    val resHint = refundEntitlement.map { amount =>
      s"$RESULT_HINT_ADDNL_PROP_REFUND${StringUtils.intToMonetaryString(amount)}."
    }

    Result(
      totalTax = sliceResult.taxDue.toInt,
      resultHeading = Some(RESULT_HEADING_FROM_APR_2016),
      resultHint = resHint,
      npv = None,
      taxCalcs = Seq(
        CalculationDetails(
          taxType = TaxTypes.premium,
          calcType = CalcTypes.slice,
          detailHeading = Some(DETAIL_HEADING_TOTAL_SDLT_FROM_APR_2016),
          bandHeading = Some(DETAIL_COL_HEADER_PURCHASE_PRICE),
          detailFooter = Some(DETAIL_FOOTER_TOTAL),
          taxDue = sliceResult.taxDue.toInt,
          slices = Some(sliceResult.slices)
        )
      )
    )
  }

  def freeholdResidentialDec14OnwardsResult(sliceResult: SliceResult, asPrevResult: Boolean): Result = {

    val (resHeading, resHint, detailHeading) = if(asPrevResult) {
      (Some(RESULT_HEADING_BEFORE_APR_2016),
      Some(RESULT_HINT_EXCHANGE_BEFORE_NOV_2015),
      Some(DETAIL_HEADING_TOTAL_SDLT_BEFORE_APR_2016))
    } else {
      (None, None, Some(DETAIL_HEADING_TOTAL_SDLT))
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
