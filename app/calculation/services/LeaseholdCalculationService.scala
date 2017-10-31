package calculation.services

import javax.inject.{Inject, Singleton}

import calculation.data.SliceRatesTables
import calculation.exceptions.RequiredValueNotDefinedException
import calculation.factories.LeaseholdResultFactory
import calculation.models.{Request, Result}

@Singleton
class LeaseholdCalculationService @Inject()(
  val baseCalculationService: BaseCalculationService
) extends LeaseholdCalculationSrv

trait LeaseholdCalculationSrv {

  val baseCalculationService: BaseCalculationSrv

  def leaseholdResidentialAddPropApr16Onwards(request: Request): Seq[Result] = ???

  def leaseholdResidentialDec14Onwards(request: Request, asPreviousResult: Boolean = false): Result = {
    val leaseDetails = request.leaseDetails.getOrElse{throw new RequiredValueNotDefinedException(
      "[LeaseholdCalculationService] [leaseholdResidentialDec14Onwards] Lease details not defined when required"
    )}
    val npv = baseCalculationService.calculateNPV(leaseDetails)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, SliceRatesTables.leaseholdResidentialDec14OnwardsLeaseRates.slices)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(request.premium, SliceRatesTables.leaseholdResidentialDec14OnwardsPremiumRates.slices)

    LeaseholdResultFactory.leaseholdResidentialDec14OnwardsResult(leaseResult, premiumResult, npv)
  }

  def leaseholdResidentialMar12toDec14(request: Request): Result = ???

  def leaseholdNonResidentialMar16Onwards(request: Request): Seq[Result] = ???

  def leaseholdNonResidentialMar12toMar16(request: Request, asPrevResult: Boolean = false): Result = ???
}
