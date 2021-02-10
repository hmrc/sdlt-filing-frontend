/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package services

import data.{Dates, SlabRatesTables}
import data.FreeholdSliceRatesTables._
import exceptions.RequiredValueNotDefinedException
import factories.FreeholdResultFactory
import models.{Request, Result}
import javax.inject.{Inject, Singleton}

@Singleton
class FreeholdCalculationService @Inject()(val baseCalculationService: BaseCalculationService,
                                           val refundEntitlementService: RefundEntitlementService) {

  def freeholdResidentialApr21OnwardsFTBNonUKRes(request: Request): Seq[Result] = {

    val prevResult = freeholdResidentialNov17OnwardsFTB(request, prevResult = true)

    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      request.premium,
      freeholdResidentialApr21OnwardsFTBNonUKResRates.slices
    )
    Seq(FreeholdResultFactory.freeholdResidentialApr21OnwardsFTBResult(premiumResult, nonUKResident = true), prevResult)
  }

  def freeholdResidentialJuly20OnwardsFTB(request: Request): Result = {
    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      request.premium,
      freeholdResidentialJuly20OnwardsFTBRates.slices
    )
    FreeholdResultFactory.freeholdResidentialJuly20OnwardsFTBResult(premiumResult)
  }

  def freeholdResidentialNov17OnwardsFTB(request: Request, prevResult: Boolean = false): Result = {
    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      request.premium,
      freeholdResidentialNov17OnwardsFTBRates.slices
    )
    val effectDateAfter31March2020: Boolean = request.effectiveDate.isAfter(Dates.MAR2021_RESIDENTIAL_DATE)

    FreeholdResultFactory.freeholdResidentialNov17OnwardsFTBResult(premiumResult, effectDateAfter31March2020, prevResult)
  }

  def freeholdResidentialAddPropNonUKResApr21Onwards(request: Request): Seq[Result] = {
    val currentPremiumResult = baseCalculationService.calculateTaxDueSlice(
      if(request.premium < 40000) 0 else request.premium,
      freeholdResidentialAddPropNonUKResApr21OnwardsRates.slices
    )

    val prevResult = freeholdResidentialApr21OnwardsNonUKResAddPropPrev(request)
    val prevPrem = prevResult.taxCalcs.headOption.map(_.taxDue).getOrElse({
      throw new RequiredValueNotDefinedException("[FreeholdCalculationService] [freeholdResidentialAddPropNonUKResApr21Onwards] - " +
        "Premium result not defined in previous calculation")
    })

    val refundEntitlement = refundEntitlementService.calculateRefundEntitlement(currentPremiumResult.taxDue, prevPrem, request.propertyDetails)
    val individual: Boolean = request.propertyDetails.exists(_.individual)

    if(individual) {
      Seq(
        FreeholdResultFactory.freeholdResidentialAddPropApr21OnwardsResultNonUKRes(currentPremiumResult, refundEntitlement),
        prevResult)
    } else {
      Seq(FreeholdResultFactory.freeholdResidentialAddPropApr21OnwardsResultNonUKRes(currentPremiumResult, refundEntitlement))
    }
  }

  def freeholdResidentialAddPropJuly20Onwards(request: Request): Seq[Result] = {
    val currentPremiumResult = baseCalculationService.calculateTaxDueSlice(
      if(request.premium < 40000) 0 else request.premium,
      freeholdResidentialAddPropJuly20OnwardsRates.slices
    )

    val prevResult = freeholdResidentialJuly20Onwards(request, asPreviousResult = true)
    val prevPrem = prevResult.taxCalcs.headOption.map(_.taxDue).getOrElse({
      throw new RequiredValueNotDefinedException("[FreeholdCalculationService] [freeholdResidentialAddPropJul20Onwards] - " +
        "Premium result not defined in previous calculation")
    })

    val refundEntitlement = refundEntitlementService.calculateRefundEntitlement(currentPremiumResult.taxDue, prevPrem, request.propertyDetails)
    val individual: Boolean = request.propertyDetails.exists(_.individual)

    if(individual) {
      Seq(
        FreeholdResultFactory.freeholdResidentialAddPropJuly20OnwardsResult(currentPremiumResult, refundEntitlement),
        prevResult)
    } else {
      Seq(FreeholdResultFactory.freeholdResidentialAddPropJuly20OnwardsResult(currentPremiumResult, refundEntitlement))
    }
  }

  def freeholdResidentialAddPropApr16Onwards(request: Request): Seq[Result] = {
    val currentPremiumResult = baseCalculationService.calculateTaxDueSlice(
      if(request.premium < 40000) 0 else request.premium,
      freeholdResidentialAddPropApr16OnwardsRates.slices
    )

    val prevResult = freeholdResidentialDec14Onwards(request, asPreviousResult = true)
    val prevPrem = prevResult.taxCalcs.headOption.map(_.taxDue).getOrElse({
      throw new RequiredValueNotDefinedException("[FreeholdCalculationService] [freeholdResidentialAddPropApr16Onwards] - " +
        "Premium result not defined in previous calculation")
    })

    val individual: Boolean = request.propertyDetails.exists(_.individual)
    val effectiveDateAfter31March2020: Boolean = request.effectiveDate.isAfter(Dates.MAR2021_RESIDENTIAL_DATE)
    val refundEntitlement = refundEntitlementService.calculateRefundEntitlement(currentPremiumResult.taxDue, prevPrem, request.propertyDetails)

    (individual, effectiveDateAfter31March2020) match {
      case (true, _) => Seq(FreeholdResultFactory.freeholdResidentialAddPropApr16OnwardsResult(currentPremiumResult, refundEntitlement, effectiveDateAfter31March2020), prevResult)
      case (false, true) => Seq(FreeholdResultFactory.freeholdResidentialAddPropApr16OnwardsResult(currentPremiumResult, refundEntitlement, effectiveDateAfter31March2020))
      case (false, false) => Seq(FreeholdResultFactory.freeholdResidentialAddPropApr16OnwardsResult(currentPremiumResult, refundEntitlement, effectiveDateAfter31March2020), prevResult)
    }
  }

  def freeholdResidentialApr21OnwardsNonUKResAddPropPrev(request: Request): Result = {

    val currentPremiumResult = baseCalculationService.calculateTaxDueSlice(
      if(request.premium < 40000) 0 else request.premium,
        freeholdResidentialApr21OnwardsNonUKResRates.slices
    )

    val individual: Boolean = request.propertyDetails.exists(_.individual)

    FreeholdResultFactory.freeholdResidentialApr21OnwardsResultNonUKRes(currentPremiumResult, asPrevResult = true, individual)
  }

  def freeholdResidentialApr21OnwardsNonUKRes(request: Request): Seq[Result] = {
    val currentPremiumResult = baseCalculationService.calculateTaxDueSlice(
      if(request.premium < 40000) 0 else request.premium,
      freeholdResidentialApr21OnwardsNonUKResRates.slices
    )

    val prevResult = freeholdResidentialDec14Onwards(request, asPreviousResult = true, nonUKResident = true)
    val individual: Boolean = request.propertyDetails.exists(_.individual)

    Seq(FreeholdResultFactory.freeholdResidentialApr21OnwardsResultNonUKRes(
      currentPremiumResult, individual = individual), prevResult
    )

  }

  def freeholdResidentialJuly20Onwards(request: Request, asPreviousResult: Boolean = false): Result = {
    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      if(request.premium < 40000) 0 else request.premium,
      freeholdResidentialJuly20OnwardsRates.slices
    )

    FreeholdResultFactory.freeholdResidentialJuly20OnwardsResult(premiumResult, asPreviousResult)
  }

  def freeholdResidentialDec14Onwards(request: Request, asPreviousResult: Boolean = false, nonUKResident: Boolean = false): Result = {
    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      if(request.premium < 40000) 0 else request.premium,
      freeholdResidentialDec14OnwardsRates.slices
    )
    val effectiveDateAfter31March2020: Boolean = request.effectiveDate.isAfter(Dates.MAR2021_RESIDENTIAL_DATE)
    val individual: Boolean = request.propertyDetails.exists(_.individual)

    FreeholdResultFactory.freeholdResidentialDec14OnwardsResult(
      premiumResult, asPreviousResult, effectiveDateAfter31March2020, nonUKResident, individual
    )
  }

  def freeholdResidentialMar12toDec14(request: Request): Result = {
    val premiumResult = baseCalculationService.calculateTaxDueSlab(
      if(request.premium < 40000) 0 else request.premium,
      SlabRatesTables.freeholdResidentialMar12toDec14Rates.slabs
    )

    FreeholdResultFactory.freeholdResidentialMar12toDec14Result(premiumResult)
  }

  def freeholdNonResidentialMar16Onwards(request: Request): Seq[Result] = {
    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      request.premium,
      freeholdNonResidentialMar16OnwardsRates.slices
    )

    Seq(
      FreeholdResultFactory.freeholdNonResidentialMar16OnwardsResult(premiumResult),
      freeholdNonResidentialMar12toMar16(request, asPrevResult = true)
    )
  }

  def freeholdNonResidentialMar12toMar16(request: Request, asPrevResult: Boolean = false): Result = {
    val premiumResult = baseCalculationService.calculateTaxDueSlab(
      request.premium,
      SlabRatesTables.freeholdNonResidentialMar12toMar16Rates.slabs
    )

    FreeholdResultFactory.freeholdNonResidentialMar12toMar16Result(premiumResult, asPrevResult)
  }
}
