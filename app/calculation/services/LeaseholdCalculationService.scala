package calculation.services

import javax.inject.{Inject, Singleton}
import calculation.data.{SlabRatesTables, SliceRatesTables}
import calculation.data.SignificantAmounts._
import calculation.exceptions.RequiredValueNotDefinedException
import calculation.factories.LeaseholdResultFactory
import calculation.models.calculationtables.SlabResult
import calculation.models._
import calculation.validators.internal.ModelValidation

@Singleton
class LeaseholdCalculationService @Inject()(val baseCalculationService: BaseCalculationService,
                                            val refundEntitlementService: RefundEntitlementService) {

  def checkIfShared(propertyDetails: Option[PropertyDetails]) : Boolean = {
   propertyDetails.exists(propDetails => propDetails.sharedOwnership.getOrElse(false))
  }

  def leaseholdResidentialNov17OnwardsFTB(request: Request): Result = {
    val sliceRateTable = if(checkIfShared(request.propertyDetails)) SliceRatesTables.leaseholdResidentialNov17FTBSharedLeaseRates.slices else SliceRatesTables.leaseholdResidentialNov17FTBLeaseRates.slices
    val npv = getNPV("leaseholdResidentialNov17Onwards", request.leaseDetails)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, sliceRateTable)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(request.premium, SliceRatesTables.leaseholdResidentialNov17OnwardsFTBPremiumRates.slices)
    LeaseholdResultFactory.leaseholdResidentialNov17OnwardsFTBResult(leaseResult, premiumResult, npv)
  }

  def leaseholdResidentialAddPropApr16Onwards(request: Request): Seq[Result] = {
    val npv = getNPV("leaseholdResidentialAddPropApr16Onwards", request.leaseDetails)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, SliceRatesTables.leaseholdResidentialAddPropApr16OnwardsLeaseRates.slices)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      if(request.premium < 40000) 0 else request.premium,
      SliceRatesTables.leaseholdResidentialAddPropApr16OnwardsPremiumRates.slices
    )
    val prevResult = leaseholdResidentialDec14Onwards(request, asPreviousResult = true, Some(npv))
    val prevPrem = prevResult.taxCalcs.lift(1).map(_.taxDue).getOrElse({
      throw new RequiredValueNotDefinedException("[LeaseholdCalculationService] [leaseholdResidentialAddPropApr16Onwards] - " +
        "Premium result not defined in previous calculation")
    })

    val refundEntitlement = refundEntitlementService.calculateRefundEntitlement(premiumResult.taxDue, prevPrem, request.propertyDetails)
    val currResult = LeaseholdResultFactory.leaseholdResidentialAddPropApr16Onwards(leaseResult, premiumResult, npv, refundEntitlement)

    Seq(currResult, prevResult)
  }

  def leaseholdResidentialDec14Onwards(request: Request, asPreviousResult: Boolean = false, preCalculatedNPV: Option[BigDecimal] = None): Result = {
    val npv = preCalculatedNPV.getOrElse(getNPV("leaseholdResidentialDec14Onwards", request.leaseDetails))
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
        leaseholdNonResidentialMar12toMar16(request, asPrevResult = true, preCalculatedNPV = Some(npv))
      )
    else
      Seq(LeaseholdResultFactory.leaseholdNonResidentialMar16OnwardsResult(leaseResult, premiumResult, npv))
  }

  def leaseholdNonResidentialMar12toMar16(request: Request, asPrevResult: Boolean = false, preCalculatedNPV: Option[BigDecimal] = None): Result = {
    val npv = preCalculatedNPV.getOrElse(getNPV("leaseholdNonResidentialMar12toMar16", request.leaseDetails))

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
    filterToRelevantRentCondition("eligibleForZeroRate", request, filterOutcome = false) {
      details => details.relevantRent match {
        case Some(relRent) => relRent < RELEVANT_RENT_ZERO_RATE_LIMIT
        case None => throw new RequiredValueNotDefinedException(
          "[LeaseholdCalculationService] [eligibleForZeroRate] - relevant rent amount not defined"
        )
      }
    }
  }

  private[services] def nonResPrevCalcRequired(request: Request): Boolean = {
    filterToRelevantRentCondition("nonResPrevCalcRequired", request, filterOutcome = true) {
      details => (details.exchangedContractsBeforeMar16, details.contractChangedSinceMar16) match {
        case (Some(true), Some(false)) => true
        case _ => false
      }
    }
  }

  private def filterToRelevantRentCondition(callingFunction: String, request: Request, filterOutcome: Boolean)(condition: RelevantRentDetails => Boolean): Boolean = {
    if(request.premium >= RELEVANT_RENT_PREMIUM_THRESHOLD)
      filterOutcome
    else
      request.leaseDetails.map(ModelValidation.allRentsBelow2000) match {
        case Some(false) => filterOutcome
        case Some(true)  => request.relevantRentDetails.map{ details =>
          condition(details)
        }.getOrElse(
          throw new RequiredValueNotDefinedException(s"[LeaseholdCalculationService] [$callingFunction] - relevant rent details not defined")
        )
        case None =>
          throw new RequiredValueNotDefinedException(
            s"[LeaseholdCalculationService] [$callingFunction] - lease details not defined when premium less than £150,000"
          )
      }
  }

}
