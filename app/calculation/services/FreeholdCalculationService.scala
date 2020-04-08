/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package calculation.services

import javax.inject.{Inject, Singleton}

import calculation.data.{SlabRatesTables, SliceRatesTables}
import calculation.exceptions.RequiredValueNotDefinedException
import calculation.models.{Request, Result}
import calculation.factories.FreeholdResultFactory

@Singleton
class FreeholdCalculationService @Inject()(val baseCalculationService: BaseCalculationService,
                                           val refundEntitlementService: RefundEntitlementService) {

  def freeholdResidentialNov17OnwardsFTB(request: Request): Result = {
    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      request.premium,
      SliceRatesTables.freeholdResidentialNov17OnwardsFTBRates.slices
    )
    FreeholdResultFactory.freeholdResidentialNov17OnwardsFTBResult(premiumResult)
  }

  def freeholdResidentialAddPropApr16Onwards(request: Request): Seq[Result] = {
    val currentPremiumResult = baseCalculationService.calculateTaxDueSlice(
      if(request.premium < 40000) 0 else request.premium,
      SliceRatesTables.freeholdResidentialAddPropApr16OnwardsRates.slices
    )

    val prevResult = freeholdResidentialDec14Onwards(request, asPreviousResult = true)
    val prevPrem = prevResult.taxCalcs.headOption.map(_.taxDue).getOrElse({
      throw new RequiredValueNotDefinedException("[FreeholdCalculationService] [freeholdResidentialAddPropApr16Onwards] - " +
        "Premium result not defined in previous calculation")
    })

    val refundEntitlement = refundEntitlementService.calculateRefundEntitlement(currentPremiumResult.taxDue, prevPrem, request.propertyDetails)

    Seq(
      FreeholdResultFactory.freeholdResidentialAddPropApr16OnwardsResult(currentPremiumResult, refundEntitlement),
      prevResult
    )
  }

  def freeholdResidentialDec14Onwards(request: Request, asPreviousResult: Boolean = false): Result = {
    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      request.premium,
      SliceRatesTables.freeholdResidentialDec14OnwardsRates.slices
    )

    FreeholdResultFactory.freeholdResidentialDec14OnwardsResult(premiumResult, asPreviousResult)
  }

  def freeholdResidentialMar12toDec14(request: Request): Result = {
    val premiumResult = baseCalculationService.calculateTaxDueSlab(
      request.premium,
      SlabRatesTables.freeholdResidentialMar12toDec14Rates.slabs
    )

    FreeholdResultFactory.freeholdResidentialMar12toDec14Result(premiumResult)
  }

  def freeholdNonResidentialMar16Onwards(request: Request): Seq[Result] = {
    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      request.premium,
      SliceRatesTables.freeholdNonResidentialMar16OnwardsRates.slices
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
