package calculation.services

import calculation.data.{SlabRatesTables, SliceRatesTables}
import calculation.models.{Request, Result}
import calculation.factories.ResultFactory

object FreeholdCalculationService {

  def freeholdResidentialAddPropApr16Onwards(request: Request): Result = ???

  def freeholdResidentialDec14Onwards(request: Request): Result = {
    val premiumResult = BaseCalculationService.calculateTaxDueSlice(
      request.premium,
      SliceRatesTables.freeholdResidentialDec14OnwardsRates.slices
    )

    ResultFactory.freeholdResidentialDec14OnwardsResult(premiumResult)
  }

  def freeholdResidentialMar12toDec14(request: Request): Result = {
    val premiumResult = BaseCalculationService.calculateTaxDueSlab(
      request.premium,
      SlabRatesTables.freeholdResidentialMar12toDec14Rates.slabs
    )

    ResultFactory.freeholdResidentialMar12toDec14Result(premiumResult)
  }

  def freeholdNonResidentialMar16Onwards(request: Request): Result = ???

  def freeholdNonResidentialMar12toMar16(request: Request): Result = ???

}
