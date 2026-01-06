/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package factories

import data.ResultText._
import enums.{CalcTypes, TaxTypes}
import models.{CalculationDetails, CalculationResponse, Result}
import models.calculationtables.{SlabResult, SliceResult}
import utils.StringUtils

object FreeholdResultFactory {

  //Standard Residential

  def freeholdResidentialJuly21OnwardsResult(sliceResult: SliceResult, asPrevResult: Boolean): Result = {

    val (resHeading, resHint, detailHeading) = if(asPrevResult) {
      (Some(DETAIL_ADDITIONAL_DWELLINGS),
        Some(RESULT_HINT_EXCHANGE_JULY_20),
        Some(DETAIL_HEADING_TOTAL_SDLT))
    } else {
      (Some(RESULT_HEADING_GENERIC), None, Some(DETAIL_HEADING_TOTAL_SDLT))
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
      (Some(DETAIL_ADDITIONAL_DWELLINGS),
        Some(RESULT_HINT_EXCHANGE_JULY_20),
        Some(DETAIL_HEADING_TOTAL_SDLT))
    } else {
      (Some(RESULT_HEADING_GENERIC), None, Some(DETAIL_HEADING_TOTAL_SDLT))
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

  def freeholdResidentialDec14OnwardsResult(sliceResult: SliceResult, asPrevResult: Boolean, afterMarch2021: Boolean,
                                            nonUKResident: Boolean = false, individual: Boolean): Result = {

    val (resHeading, resHint, detailHeading) =
      (asPrevResult, afterMarch2021, nonUKResident) match {
        case (true, false, false) => (Some(RESULT_HEADING_BEFORE_APR_2016), Some(RESULT_HINT_EXCHANGE_BEFORE_NOV_2015), Some(DETAIL_HEADING_TOTAL_SDLT_BEFORE_APR_2016))
        case (false, _, false) => (Some(RESULT_HEADING_GENERIC), None, Some(DETAIL_HEADING_TOTAL_SDLT))
        case (true, true, true) if individual => (Some(RESULT_HEADING_AFTER_MARCH_2021_NON_RES), Some(RESULT_HINT_NON_UK_RES_WITHIN_12M), Some(DETAIL_HEADING_TOTAL_SDLT))
        case (true, true, true) if !individual => (Some(RESULT_HEADING_AFTER_MARCH_2021_NON_RES), None, Some(DETAIL_HEADING_TOTAL_SDLT))
        case (true, true, false) => (Some(DETAIL_ADDITIONAL_DWELLINGS), Some(RESULT_HINT_EXCHANGE_JULY_20), Some(DETAIL_HEADING_TOTAL_SDLT))
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
      resultHeading = Some(RESULT_HEADING_GENERIC),
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

  //Standard Non-Residential

  def freeholdNonResidentialMar12toMar16Result(slabResult: SlabResult, asPrevResult: Boolean): Result = {

    val (resHeading, resHint) = if(asPrevResult) {
      (Some(RESULT_HEADING_BEFORE_MAR_2016), Some(RESULT_HINT_EXCHANGE_BEFORE_MAR_2016))
    } else (Some(RESULT_HEADING_GENERIC), None)

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

  //FTB

  //suitable for Post Sept 22
  def freeholdResidentialNov17OnwardsFTBResult(sliceResult: SliceResult, afterMarch2021: Boolean, prevResult: Boolean = false): Result = {

    val resHeading = if(afterMarch2021) {
      if(prevResult){
        Some(RESULT_HEADING_AFTER_MARCH_2021_NON_RES)
      } else {
        Some(RESULT_HEADING_GENERIC)
      }
    } else {
      Some(RESULT_HEADING_GENERIC)
    }

    val resHint = if(afterMarch2021) {
      if(prevResult){
        Some(RESULT_HINT_NON_UK_RES_WITHIN_12M)
      } else {
        None
      }
    } else {
      None
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
          detailHeading = Some(DETAIL_HEADING_TOTAL_SDLT),
          bandHeading = Some(DETAIL_COL_HEADER_PURCHASE_PRICE),
          detailFooter = Some(DETAIL_FOOTER_TOTAL),
          taxDue = sliceResult.taxDue.toInt,
          slices = Some(sliceResult.slices)
        )
      )
    )
  }

  //suitable for Post Sept 22
  def freeholdResidentialJuly21OnwardsFTBResult(sliceResult: SliceResult, nonUKResident: Boolean): Result = {
    Result(
      totalTax = sliceResult.taxDue.toInt,
      resultHeading = Some(RESULT_HEADING_GENERIC),
      resultHint = if(nonUKResident) Some(RESULT_HINT_NON_UK_RES_AFTER_MARCH_2021) else None,
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

  //HRAD

  def freeholdResidentialAddPropJuly20OnwardsResult(sliceResult: SliceResult, refundEntitlement: Option[Int]): Result = {

    val resHint = refundEntitlement.map { amount =>
      s"$RESULT_HINT_ADDNL_PROP_2020$RESULT_HINT_ADDNL_PROP_REFUND${StringUtils.intToMonetaryString(amount)}."
    }

    Result(
      totalTax = sliceResult.taxDue.toInt,
      resultHeading = Some(RESULT_HEADING_GENERIC),
      resultHint = resHint,
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

  def freeholdResidentialAddPropApr16OnwardsResult(sliceResult: SliceResult, refundEntitlement: Option[Int], afterMarch2021: Boolean): Result = {

    val (resHeading, resHint, detailHeading) = if(afterMarch2021) {
      (Some(RESULT_HEADING_GENERIC),
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

  //NRSDLT

  def freeholdResidentialApril21OnwardsResultNonUKRes(sliceResult: SliceResult, asPrevResult: Boolean = false, individual: Boolean, additionalDwellings: Boolean): Result = {

    val (resHeading, resHint, detailHeading) = (asPrevResult, additionalDwellings, individual) match {
      case (true, true, _) =>
        (Some(DETAIL_ADDITIONAL_DWELLINGS), Some(RESULT_HINT_EXCHANGE_APR_21), Some(DETAIL_HEADING_TOTAL_SDLT))
      case (true, false, _) =>
        (Some(RESULT_HEADING_AFTER_MARCH_2021_NON_RES), Some(RESULT_HINT_NON_UK_RES_WITHIN_12M), Some(DETAIL_HEADING_TOTAL_SDLT))
      case (_, _, true) =>
        (Some(RESULT_HEADING_GENERIC), Some(RESULT_HINT_NON_UK_RES_AFTER_MARCH_2021), Some(DETAIL_HEADING_TOTAL_SDLT))
      case (_, _, false) =>
        (Some(RESULT_HEADING_GENERIC), None, Some(DETAIL_HEADING_TOTAL_SDLT))
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

  def freeholdResidentialJuly21OnwardsResultNonUKResPrev(sliceResult: SliceResult, individual: Boolean): Result = {

    val (resHeading, resHint, detailHeading) =
      (Some(RESULT_HEADING_AFTER_MARCH_2021_NON_RES),
        Some(RESULT_HINT_NON_UK_RES_WITHIN_12M),
        Some(DETAIL_HEADING_TOTAL_SDLT))

    Result(
      totalTax = sliceResult.taxDue.toInt,
      resultHeading = resHeading,
      resultHint = if(individual) resHint else None,
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

  def freeholdResidentialApril21OnwardsResultNonUKResPrev(sliceResult: SliceResult, individual: Boolean): Result = {

    val (resHeading, resHint, detailHeading) =
      (Some(RESULT_HEADING_AFTER_MARCH_2021_NON_RES),
        Some(RESULT_HINT_NON_UK_RES_WITHIN_12M),
        Some(DETAIL_HEADING_TOTAL_SDLT))

    Result(
      totalTax = sliceResult.taxDue.toInt,
      resultHeading = resHeading,
      resultHint = if(individual) resHint else None,
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

  //HRAD + NRSDLT

  def freeholdResidentialAddPropJuly21OnwardsResultNonUKRes(sliceResult: SliceResult, refundEntitlement: Option[Int]): Result = {

    val (resHeading, resHint, detailHeading) =
      (Some(RESULT_HEADING_GENERIC),
        refundEntitlement.map { amount =>
          s"$RESULT_HINT_ADDNL_PROP_AFTER_MARCH_2021$RESULT_HINT_ADDNL_PROP_REFUND${StringUtils.intToMonetaryString(amount)}.$RESULT_HINT_NRSDLT_REFUND"},
        Some(DETAIL_HEADING_TOTAL_SDLT)
      )

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

  def freeholdResidentialAddPropApril2016OnwardsResultWithBudgetTaxRelief: Result = {

    Result(
      totalTax = 0,
      resultHeading = Some(RESULT_HEADING_TAX_RELIEF),
      resultHint = None,
      npv = None,
      taxCalcs = Seq(
        CalculationDetails(
          taxType = TaxTypes.premium,
          calcType = CalcTypes.slab,
          taxDue = 0,
          detailHeading = None,
          bandHeading = None,
          detailFooter = None,
          rate = Some(0),
          slices = None
        )
      )
    )
  }

  def freeholdApril2013OnwardsResultWithBudgetTaxRelief: Result = {

    Result(
      totalTax = 0,
      resultHeading = Some(RESULT_HEADING_TAX_RELIEF),
      resultHint = None,
      npv = None,
      taxCalcs = Seq(
        CalculationDetails(
          taxType = TaxTypes.premium,
          calcType = CalcTypes.slab,
          taxDue = 0,
          detailHeading = None,
          bandHeading = None,
          detailFooter = None,
          rate = Some(0),
          slices = None
        )
      )
    )
  }

  def freeholdAcquisitionTaxReliefRes(slabResult: SlabResult): CalculationResponse = {
    CalculationResponse(
      Seq(
        Result(
          totalTax = slabResult.taxDue.toInt,
          resultHeading = Some(RESULT_HEADING_TAX_RELIEF),
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
              bigDecRate = Some(slabResult.rate),
              slices = None
            )
          )
        )
      )
    )
  }
}