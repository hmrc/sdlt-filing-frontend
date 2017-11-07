package calculation.services

import javax.inject.{Inject, Singleton}

import calculation.data.{SlabRatesTables, SliceRatesTables}
import calculation.data.SignificantAmounts._
import calculation.exceptions.RequiredValueNotDefinedException
import calculation.factories.LeaseholdResultFactory
import calculation.models.calculationtables.SlabResult
import calculation.models.{LeaseDetails, Request, Result}
import calculation.validators.internal.ModelValidation

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

  def leaseholdNonResidentialMar16Onwards(request: Request): Seq[Result] = {
    val npv = getNPV("leaseholdNonResidentialMar16Onwards", request.leaseDetails)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, SliceRatesTables.leaseholdNonResidentialMar16OnwardsLeaseRates.slices)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(request.premium, SliceRatesTables.leaseholdNonResidentialMar16OnwardsPremiumRates.slices)

    if(nonResPrevCalcRequired(request))
      Seq(
        LeaseholdResultFactory.leaseholdNonResidentialMar16OnwardsResult(leaseResult, premiumResult, npv),
        leaseholdNonResidentialMar12toMar16(request, asPrevResult = true)
      )
    else
      Seq(LeaseholdResultFactory.leaseholdNonResidentialMar16OnwardsResult(leaseResult, premiumResult, npv))
  }

  def leaseholdNonResidentialMar12toMar16(request: Request, asPrevResult: Boolean = false): Result = {
    val npv = getNPV("leaseholdNonResidentialMar12toMar16", request.leaseDetails)

    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, SliceRatesTables.leaseholdNonResidentialMar12toMar16LeaseRates.slices)
    val premiumResult = if(eligibleForZeroRate(request)) {
      SlabResult(rate = 0, taxDue = 0)
    } else {
      baseCalculationService.calculateTaxDueSlab(request.premium, SlabRatesTables.leaseholdNonResidentialMar12toMar16PremiumRates.slabs)
    }

    LeaseholdResultFactory.leaseholdNonResidentialMar12toMar16Result(leaseResult, premiumResult, npv, asPrevResult)
  }

  private[services] def getNPV(func: String, oLeaseDetails: Option[LeaseDetails]): BigDecimal = {
    baseCalculationService.calculateNPV(
      oLeaseDetails.getOrElse{throw new RequiredValueNotDefinedException(
        s"[LeaseholdCalculationService] [$func] Lease details not defined when required"
      )}
    )
  }

  private[services] def eligibleForZeroRate(request: Request): Boolean = {
    if(request.premium >= RELEVANT_RENT_PREMIUM_THRESHOLD)
      false
    else
      request.leaseDetails.map(ModelValidation.allRentsBelow2000) match {
        case Some(false) => false
        case Some(true)  => extractRelevantRent(request) < RELEVANT_RENT_ZERO_RATE_LIMIT
        case None =>
          throw new RequiredValueNotDefinedException(
          "[LeaseholdCalculationService] [eligibleForZeroRate] - lease details not defined when premium less than £150,000"
        )
      }
  }

  private def extractRelevantRent(request: Request): BigDecimal = {
    request.relevantRentDetails.flatMap(_.relevantRent).getOrElse{
      throw new RequiredValueNotDefinedException("[LeaseholdCalculationService] [extractRelevantRent] - relevant rent not defined")
    }
  }

  private[services] def nonResPrevCalcRequired(request: Request): Boolean = {
    if(request.premium >= RELEVANT_RENT_PREMIUM_THRESHOLD)
      true
    else
      request.leaseDetails.map(ModelValidation.allRentsBelow2000) match {
        case Some(false) => true
        case Some(true)  => request.relevantRentDetails.map{ details =>
          (details.exchangedContractsBeforeMar16, details.contractChangedSinceMar16) match {
            case (Some(true), Some(false)) => true
            case _ => false
          }
        }.getOrElse(
          throw new RequiredValueNotDefinedException("[LeaseholdCalculationService] [nonResPrevCalcRequired] - relevant rent not defined")
        )
        case None =>
          throw new RequiredValueNotDefinedException(
            "[LeaseholdCalculationService] [nonResPrevCalcRequired] - lease details not defined when premium less than £150,000"
          )
      }
  }

}
