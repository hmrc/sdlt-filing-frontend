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

  //STANDARD RESIDENTIAL

  def freeholdResidentialJuly21Onwards(request: Request, asPreviousResult: Boolean = false): Result = {
    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      if(request.premium < 40000) 0 else request.premium,
      freeholdResidentialJuly21OnwardsRates.slices
    )

    FreeholdResultFactory.freeholdResidentialJuly21OnwardsResult(premiumResult, asPreviousResult)
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

  //STANDARD NON-RESIDENTIAL

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

  //FTB

  def freeholdResidentialOct21OnwardsFTBNonUKRes(request: Request): Seq[Result] = {

    val prevResult = freeholdResidentialNov17OnwardsFTB(request, prevResult = true)

    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      request.premium,
      freeholdResidentialOct21OnwardsFTBNonUKResRates.slices
    )
    Seq(FreeholdResultFactory.freeholdResidentialJuly21OnwardsFTBResult(premiumResult, nonUKResident = true), prevResult)
  }

  def freeholdResidentialJuly21OnwardsFTBNonUKRes(request: Request): Seq[Result] = {

    val prevResult = freeholdResidentialNov17OnwardsFTB(request, prevResult = true)

    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      request.premium,
      freeholdResidentialJuly21OnwardsFTBNonUKResRates.slices
    )
    Seq(FreeholdResultFactory.freeholdResidentialJuly21OnwardsFTBResult(premiumResult, nonUKResident = true), prevResult)
  }

  def freeholdResidentialNov17OnwardsFTB(request: Request, prevResult: Boolean = false): Result = {
    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      request.premium,
      freeholdResidentialNov17OnwardsFTBRates.slices
    )
    val effectDateAfter31March2021: Boolean = request.effectiveDate.isAfter(Dates.MAR2021_RESIDENTIAL_DATE)

    FreeholdResultFactory.freeholdResidentialNov17OnwardsFTBResult(premiumResult, effectDateAfter31March2021, prevResult)
  }

  //HRAD

  def freeholdResidentialAddPropJuly21Onwards(request: Request): Seq[Result] = {
    val currentPremiumResult = baseCalculationService.calculateTaxDueSlice(
      if(request.premium < 40000) 0 else request.premium,
      freeholdResidentialAddPropJuly21OnwardsRates.slices
    )

    val prevResult = freeholdResidentialJuly21Onwards(request, asPreviousResult = true)
    val prevPrem = prevResult.taxCalcs.headOption.map(_.taxDue).get

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

  def freeholdResidentialAddPropJuly20Onwards(request: Request): Seq[Result] = {
    val currentPremiumResult = baseCalculationService.calculateTaxDueSlice(
      if(request.premium < 40000) 0 else request.premium,
      freeholdResidentialAddPropJuly20OnwardsRates.slices
    )

    val prevResult = freeholdResidentialJuly20Onwards(request, asPreviousResult = true)
    val prevPrem = prevResult.taxCalcs.headOption.map(_.taxDue).get

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

  //NRSDLT

  def freeholdResidentialOct21OnwardsNonUKRes(request: Request): Seq[Result] = {
    val currentPremiumResult = baseCalculationService.calculateTaxDueSlice(
      if(request.premium < 40000) 0 else request.premium,
      freeholdResidentialOct21OnwardsNonUKResRates.slices
    )

    val prevResult = freeholdResidentialDec14Onwards(request, asPreviousResult = true, nonUKResident = true)
    val individual: Boolean = request.propertyDetails.exists(_.individual)

    Seq(FreeholdResultFactory.freeholdResidentialApril21OnwardsResultNonUKRes(
      currentPremiumResult, individual = individual, additionalDwellings = false), prevResult
    )
  }

  def freeholdResidentialJuly21OnwardsNonUKRes(request: Request): Seq[Result] = {
    val currentPremiumResult = baseCalculationService.calculateTaxDueSlice(
      if(request.premium < 40000) 0 else request.premium,
      freeholdResidentialJuly21OnwardsNonUKResRates.slices
    )

    val prevResult = freeholdResidentialJuly21OnwardsNonUKResPrev(request)
    val individual: Boolean = request.propertyDetails.exists(_.individual)

    Seq(FreeholdResultFactory.freeholdResidentialApril21OnwardsResultNonUKRes(
      currentPremiumResult, individual = individual, additionalDwellings = false), prevResult
    )
  }

  def freeholdResidentialJuly21OnwardsNonUKResPrev(request: Request): Result = {
    val currentPremiumResult = baseCalculationService.calculateTaxDueSlice(
      if(request.premium < 40000) 0 else request.premium,
      freeholdResidentialJuly21OnwardsRates.slices
    )

    val individual: Boolean = request.propertyDetails.exists(_.individual)

    FreeholdResultFactory.freeholdResidentialJuly21OnwardsResultNonUKResPrev(currentPremiumResult, individual = individual)
  }

  def freeholdResidentialApril21OnwardsNonUKRes(request: Request): Seq[Result] = {
    val currentPremiumResult = baseCalculationService.calculateTaxDueSlice(
      if(request.premium < 40000) 0 else request.premium,
      freeholdResidentialApril21OnwardsNonUKResRates.slices
    )

    val prevResult = freeholdResidentialApril21OnwardsNonUKResPrev(request)
    val individual: Boolean = request.propertyDetails.exists(_.individual)

    Seq(FreeholdResultFactory.freeholdResidentialApril21OnwardsResultNonUKRes(
      currentPremiumResult, individual = individual, additionalDwellings = false), prevResult
    )
  }

  def freeholdResidentialApril21OnwardsNonUKResPrev(request: Request): Result = {
    val currentPremiumResult = baseCalculationService.calculateTaxDueSlice(
      if(request.premium < 40000) 0 else request.premium,
      freeholdResidentialJuly20OnwardsRates.slices
    )

    val individual: Boolean = request.propertyDetails.exists(_.individual)

    FreeholdResultFactory.freeholdResidentialApril21OnwardsResultNonUKResPrev(currentPremiumResult, individual = individual)
  }

  //NRSDLT + HRAD

  def freeholdResidentialAddPropNonUKResOct21Onwards(request: Request): Seq[Result] = {
    val currentPremiumResult = baseCalculationService.calculateTaxDueSlice(
      if(request.premium < 40000) 0 else request.premium,
      freeholdResidentialAddPropNonUKResOct21OnwardsRates.slices
    )

    val prevResult = freeholdResidentialAddPropNonUKResOct21OnwardsPrev(request)
    val prevPrem = prevResult.taxCalcs.headOption.map(_.taxDue).get

    val refundEntitlement = refundEntitlementService.calculateRefundEntitlement(currentPremiumResult.taxDue, prevPrem, request.propertyDetails)
    val individual: Boolean = request.propertyDetails.exists(_.individual)

    if(individual) {
      Seq(
        FreeholdResultFactory.freeholdResidentialAddPropJuly21OnwardsResultNonUKRes(currentPremiumResult, refundEntitlement),
        prevResult)
    } else {
      Seq(FreeholdResultFactory.freeholdResidentialAddPropJuly21OnwardsResultNonUKRes(currentPremiumResult, refundEntitlement))
    }
  }

  def freeholdResidentialAddPropNonUKResOct21OnwardsPrev(request: Request): Result = {

    val currentPremiumResult = baseCalculationService.calculateTaxDueSlice(
      if(request.premium < 40000) 0 else request.premium,
      freeholdResidentialOct21OnwardsNonUKResRates.slices
    )

    val individual: Boolean = request.propertyDetails.exists(_.individual)

    FreeholdResultFactory.freeholdResidentialApril21OnwardsResultNonUKRes(currentPremiumResult, asPrevResult = true, individual, additionalDwellings = true)
  }

  def freeholdResidentialAddPropNonUKResJuly21Onwards(request: Request): Seq[Result] = {
    val currentPremiumResult = baseCalculationService.calculateTaxDueSlice(
      if(request.premium < 40000) 0 else request.premium,
      freeholdResidentialAddPropNonUKResJuly21OnwardsRates.slices
    )

    val prevResult = freeholdResidentialAddPropNonUKResJuly21OnwardsPrev(request)
    val prevPrem = prevResult.taxCalcs.headOption.map(_.taxDue).get

    val refundEntitlement = refundEntitlementService.calculateRefundEntitlement(currentPremiumResult.taxDue, prevPrem, request.propertyDetails)
    val individual: Boolean = request.propertyDetails.exists(_.individual)

    if(individual) {
      Seq(
        FreeholdResultFactory.freeholdResidentialAddPropJuly21OnwardsResultNonUKRes(currentPremiumResult, refundEntitlement),
        prevResult)
    } else {
      Seq(FreeholdResultFactory.freeholdResidentialAddPropJuly21OnwardsResultNonUKRes(currentPremiumResult, refundEntitlement))
    }
  }

  def freeholdResidentialAddPropNonUKResJuly21OnwardsPrev(request: Request): Result = {

    val currentPremiumResult = baseCalculationService.calculateTaxDueSlice(
      if(request.premium < 40000) 0 else request.premium,
        freeholdResidentialJuly21OnwardsNonUKResRates.slices
    )

    val individual: Boolean = request.propertyDetails.exists(_.individual)

    FreeholdResultFactory.freeholdResidentialApril21OnwardsResultNonUKRes(currentPremiumResult, asPrevResult = true, individual, additionalDwellings = true)
  }

  def freeholdResidentialAddPropNonUKResApril21Onwards(request: Request): Seq[Result] = {
    val currentPremiumResult = baseCalculationService.calculateTaxDueSlice(
      if(request.premium < 40000) 0 else request.premium,
      freeholdResidentialAddPropNonUKResApril21OnwardsRates.slices
    )

    val prevResult = freeholdResidentialAddPropNonUKResApril21OnwardsPrev(request)
    val prevPrem = prevResult.taxCalcs.headOption.map(_.taxDue).get

    val refundEntitlement = refundEntitlementService.calculateRefundEntitlement(currentPremiumResult.taxDue, prevPrem, request.propertyDetails)
    val individual: Boolean = request.propertyDetails.exists(_.individual)

    if(individual) {
      Seq(
        FreeholdResultFactory.freeholdResidentialAddPropJuly21OnwardsResultNonUKRes(currentPremiumResult, refundEntitlement),
        prevResult)
    } else {
      Seq(FreeholdResultFactory.freeholdResidentialAddPropJuly21OnwardsResultNonUKRes(currentPremiumResult, refundEntitlement))
    }
  }

  def freeholdResidentialAddPropNonUKResApril21OnwardsPrev(request: Request): Result = {

    val currentPremiumResult = baseCalculationService.calculateTaxDueSlice(
      if(request.premium < 40000) 0 else request.premium,
      freeholdResidentialApril21OnwardsNonUKResRates.slices
    )

    val individual: Boolean = request.propertyDetails.exists(_.individual)

    FreeholdResultFactory.freeholdResidentialApril21OnwardsResultNonUKRes(currentPremiumResult, asPrevResult = true, individual, additionalDwellings = true)
  }
}
