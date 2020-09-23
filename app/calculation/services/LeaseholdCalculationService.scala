/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package calculation.services

import calculation.data.SignificantAmounts._
import calculation.data.{Dates, SlabRatesTables, SliceRatesTables}
import calculation.exceptions.RequiredValueNotDefinedException
import calculation.factories.LeaseholdResultFactory
import calculation.models._
import calculation.models.calculationtables.SlabResult
import calculation.validators.internal.ModelValidation
import javax.inject.{Inject, Singleton}

@Singleton
class LeaseholdCalculationService @Inject()(val baseCalculationService: BaseCalculationService,
                                            val refundEntitlementService: RefundEntitlementService) {

  def checkIfShared(propertyDetails: Option[PropertyDetails]) : Boolean = {
   propertyDetails.exists(propDetails => propDetails.sharedOwnership.getOrElse(false))
  }

  def leaseholdResidentialJuly20OnwardsFTB(request: Request): Result = {
    val sliceRateTable = if(checkIfShared(request.propertyDetails)) SliceRatesTables.leaseholdResidentialJuly20FTBSharedLeaseRates.slices else SliceRatesTables.leaseholdResidentialJuly20FTBLeaseRates.slices
    val npv = getNPV("leaseholdResidentialJuly20Onwards", request.leaseDetails)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, sliceRateTable)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(request.premium, SliceRatesTables.leaseholdResidentialJuly20OnwardsFTBPremiumRates.slices)
    LeaseholdResultFactory.leaseholdResidentialJuly20OnwardsFTBResult(leaseResult, premiumResult, npv)
  }

  def leaseholdResidentialNov17OnwardsFTB(request: Request): Result = {
    val sliceRateTable = if(checkIfShared(request.propertyDetails)) SliceRatesTables.leaseholdResidentialNov17FTBSharedLeaseRates.slices else SliceRatesTables.leaseholdResidentialNov17FTBLeaseRates.slices
    val npv = getNPV("leaseholdResidentialNov17Onwards", request.leaseDetails)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, sliceRateTable)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(request.premium, SliceRatesTables.leaseholdResidentialNov17OnwardsFTBPremiumRates.slices)
    val effectiveDateAfter31March2020: Boolean = request.effectiveDate.isAfter(Dates.MAR2021_RESIDENTIAL_DATE)

    LeaseholdResultFactory.leaseholdResidentialNov17OnwardsFTBResult(leaseResult, premiumResult, npv, effectiveDateAfter31March2020)
  }

  def leaseholdResidentialAddPropJuly20Onwards(request: Request): Seq[Result] = {
    val npv = getNPV("leaseholdResidentialAddPropJuly20Onwards", request.leaseDetails)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, SliceRatesTables.leaseholdResidentialJuly20OnwardsLeaseRates.slices)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      if(request.premium < 40000) 0 else request.premium,
      SliceRatesTables.leaseholdResidentialAppPropJuly20OnwardsPremiumRates.slices
    )
    val prevResult = leaseholdResidentialJuly20Onwards(request, asPreviousResult = true, Some(npv))
    val prevPrem = prevResult.taxCalcs.lift(1).map(_.taxDue).getOrElse({
      throw new RequiredValueNotDefinedException("[LeaseholdCalculationService] [leaseholdResidentialAddPropJuly20Onwards] - " +
        "Premium result not defined in previous calculation")
    })

    val refundEntitlement = refundEntitlementService.calculateRefundEntitlement(premiumResult.taxDue, prevPrem, request.propertyDetails)
    val currResult = LeaseholdResultFactory.leaseholdResidentialAddPropJuly20Onwards(leaseResult, premiumResult, npv, refundEntitlement)
    val individual: Boolean = request.propertyDetails.exists(_.individual)

    if(individual) {
      Seq(currResult, prevResult)
    } else {
      Seq(currResult)
    }
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

    val effectiveDateAfter31March2020: Boolean = request.effectiveDate.isAfter(Dates.MAR2021_RESIDENTIAL_DATE)
    val refundEntitlement = refundEntitlementService.calculateRefundEntitlement(premiumResult.taxDue, prevPrem, request.propertyDetails)
    val currResult = LeaseholdResultFactory.leaseholdResidentialAddPropApr16Onwards(leaseResult, premiumResult, npv, refundEntitlement, effectiveDateAfter31March2020)
    val individual: Boolean = request.propertyDetails.exists(_.individual)

    (individual, effectiveDateAfter31March2020) match {
      case (true, _) => Seq(currResult, prevResult)
      case (false, true) => Seq(currResult)
      case (false, false) => Seq(currResult, prevResult)
    }
  }

  def leaseholdResidentialJuly20Onwards(request: Request, asPreviousResult: Boolean = false, preCalculatedNPV: Option[BigDecimal] = None): Result = {
    val npv = preCalculatedNPV.getOrElse(getNPV("leaseholdResidentialJuly20Onwards", request.leaseDetails))
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, SliceRatesTables.leaseholdResidentialJuly20OnwardsLeaseRates.slices)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(request.premium, SliceRatesTables.leaseholdResidentialJuly20nwardsPremiumRates.slices)

    LeaseholdResultFactory.leaseholdResidentialJuly20OnwardsResult(leaseResult, premiumResult, npv, asPreviousResult)
  }

  def leaseholdResidentialDec14Onwards(request: Request, asPreviousResult: Boolean = false, preCalculatedNPV: Option[BigDecimal] = None): Result = {
    val npv = preCalculatedNPV.getOrElse(getNPV("leaseholdResidentialDec14Onwards", request.leaseDetails))
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, SliceRatesTables.leaseholdResidentialDec14OnwardsLeaseRates.slices)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(request.premium, SliceRatesTables.leaseholdResidentialDec14OnwardsPremiumRates.slices)
    val effectiveDateAfter31March2020: Boolean = request.effectiveDate.isAfter(Dates.MAR2021_RESIDENTIAL_DATE)

    LeaseholdResultFactory.leaseholdResidentialDec14OnwardsResult(leaseResult, premiumResult, npv, asPreviousResult, effectiveDateAfter31March2020)
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
