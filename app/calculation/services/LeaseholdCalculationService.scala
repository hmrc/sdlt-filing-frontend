package calculation.services

import javax.inject.{Inject, Singleton}

import calculation.data.{SlabRatesTables, SliceRatesTables}
import calculation.exceptions.RequiredValueNotDefinedException
import calculation.factories.LeaseholdResultFactory
import calculation.models.{LeaseDetails, Request, Result}

@Singleton
class LeaseholdCalculationService @Inject()(
  val baseCalculationService: BaseCalculationService,
  val refundEntitlementService: RefundEntitlementService
) extends LeaseholdCalculationSrv

trait LeaseholdCalculationSrv {

  val baseCalculationService: BaseCalculationSrv
  val refundEntitlementService: RefundEntitlementSrv

  def leaseholdResidentialAddPropApr16Onwards(request: Request): Seq[Result] = {
    val prevResult = leaseholdResidentialDec14Onwards(request, asPreviousResult = true)

    val npv = getNPV("leaseholdResidentialAddPropApr16Onwards", request.leaseDetails)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, SliceRatesTables.leaseholdResidentialAddPropApr16OnwardsLeaseRates.slices)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      if(request.premium < 40000) 0 else request.premium,
      SliceRatesTables.leaseholdResidentialAddPropApr16OnwardsPremiumRates.slices
    )

    val refundEntitlement = refundEntitlementService.calculateRefundEntitlement(premiumResult.taxDue, prevResult.totalTax, request.propertyDetails)
    val currResult = LeaseholdResultFactory.leaseholdResidentialAddPropApr16Onwards(leaseResult, premiumResult, npv, refundEntitlement)

    Seq(currResult, prevResult)
  }

  def leaseholdResidentialDec14Onwards(request: Request, asPreviousResult: Boolean = false): Result = {
    val npv = getNPV("leaseholdResidentialDec14Onwards", request.leaseDetails)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, SliceRatesTables.leaseholdResidentialDec14OnwardsLeaseRates.slices)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(request.premium, SliceRatesTables.leaseholdResidentialDec14OnwardsPremiumRates.slices)

    LeaseholdResultFactory.leaseholdResidentialDec14OnwardsResult(leaseResult, premiumResult, npv, asPreviousResult)
  }

  def leaseholdResidentialMar12toDec14(request: Request): Result = {
    val npv = getNPV("leaseholdResidentialMar12toDec14", request.leaseDetails)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, SliceRatesTables.leaseholdResidentialMar12toDec14LeaseRates.slices)
    val premiumResult = baseCalculationService.calculateTaxDueSlab(request.premium, SlabRatesTables.leaseholdResidentialMar12toDec14PremiumRates.slabs)

    LeaseholdResultFactory.leaseholdResidentialMar12toDec14Result(leaseResult, premiumResult, npv)
  }

  def leaseholdNonResidentialMar16Onwards(request: Request): Seq[Result] = ???

  def leaseholdNonResidentialMar12toMar16(request: Request, asPrevResult: Boolean = false): Result = ???

  private[services] def getNPV(func: String, oLeaseDetails: Option[LeaseDetails]): BigDecimal = {
    baseCalculationService.calculateNPV(
      oLeaseDetails.getOrElse{throw new RequiredValueNotDefinedException(
        s"[LeaseholdCalculationService] [$func] Lease details not defined when required"
      )}
    )
  }
}
