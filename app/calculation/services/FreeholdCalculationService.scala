/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package calculation.services

import calculation.data.{Dates, SlabRatesTables, SliceRatesTables}
import calculation.exceptions.RequiredValueNotDefinedException
import calculation.factories.FreeholdResultFactory
import calculation.models.{Request, Result}
import javax.inject.{Inject, Singleton}

@Singleton
class FreeholdCalculationService @Inject()(val baseCalculationService: BaseCalculationService,
                                           val refundEntitlementService: RefundEntitlementService) {

  def freeholdResidentialJuly20OnwardsFTB(request: Request): Result = {
    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      request.premium,
      SliceRatesTables.freeholdResidentialJuly20OnwardsFTBRates.slices
    )
    FreeholdResultFactory.freeholdResidentialJuly20OnwardsFTBResult(premiumResult)
  }

  def freeholdResidentialNov17OnwardsFTB(request: Request): Result = {
    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      request.premium,
      SliceRatesTables.freeholdResidentialNov17OnwardsFTBRates.slices
    )
    val effectDateAfter31March2020: Boolean = request.effectiveDate.isAfter(Dates.MAR2021_RESIDENTIAL_DATE)

    FreeholdResultFactory.freeholdResidentialNov17OnwardsFTBResult(premiumResult, effectDateAfter31March2020)
  }

  def freeholdResidentialAddPropJuly20Onwards(request: Request): Seq[Result] = {
    val currentPremiumResult = baseCalculationService.calculateTaxDueSlice(
      if(request.premium < 40000) 0 else request.premium,
      SliceRatesTables.freeholdResidentialAddPropJuly20OnwardsRates.slices
    )

    val prevResult = freeholdResidentialJuly20Onwards(request, asPreviousResult = true)
    val prevPrem = prevResult.taxCalcs.headOption.map(_.taxDue).getOrElse({
      throw new RequiredValueNotDefinedException("[FreeholdCalculationService] [freeholdResidentialAddPropApr16Onwards] - " +
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
      SliceRatesTables.freeholdResidentialAddPropApr16OnwardsRates.slices
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

  def freeholdResidentialJuly20Onwards(request: Request, asPreviousResult: Boolean = false): Result = {
    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      request.premium,
      SliceRatesTables.freeholdResidentialJuly20OnwardsRates.slices
    )

    FreeholdResultFactory.freeholdResidentialJuly20OnwardsResult(premiumResult, asPreviousResult)
  }

  def freeholdResidentialDec14Onwards(request: Request, asPreviousResult: Boolean = false): Result = {
    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      request.premium,
      SliceRatesTables.freeholdResidentialDec14OnwardsRates.slices
    )
    val effectiveDateAfter31March2020: Boolean = request.effectiveDate.isAfter(Dates.MAR2021_RESIDENTIAL_DATE)

    FreeholdResultFactory.freeholdResidentialDec14OnwardsResult(premiumResult, asPreviousResult, effectiveDateAfter31March2020)
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
