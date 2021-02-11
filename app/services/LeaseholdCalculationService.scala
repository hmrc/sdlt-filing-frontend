/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package services

import data.LeaseholdSliceRatesTables._
import data.SignificantAmounts._
import data.{Dates, SlabRatesTables}
import exceptions.RequiredValueNotDefinedException
import factories.LeaseholdResultFactory
import models._
import models.calculationtables.{SlabResult, Slice}
import utils.CalculationUtils.leaseholdNRSDLTInScopeForLeaseOrPremium
import validators.internal.ModelValidation

import javax.inject.{Inject, Singleton}

@Singleton
class LeaseholdCalculationService @Inject()(val baseCalculationService: BaseCalculationService,
                                            val refundEntitlementService: RefundEntitlementService) {

  //March 12 requests

  def leaseholdResidentialMar12toDec14(request: Request): Result = {
    val npv = getNPV("leaseholdResidentialMar12toDec14", request.leaseDetails)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseholdResidentialMar12toDec14LeaseRates.slices)
    val premiumResult = baseCalculationService.calculateTaxDueSlab(request.premium, SlabRatesTables.leaseholdResidentialMar12toDec14PremiumRates.slabs)

    LeaseholdResultFactory.leaseholdResidentialMar12toDec14Result(leaseResult, premiumResult, npv)
  }

  def leaseholdNonResidentialMar12toMar16(request: Request, asPrevResult: Boolean = false, preCalculatedNPV: Option[BigDecimal] = None): Result = {
    val npv = preCalculatedNPV.getOrElse(getNPV("leaseholdNonResidentialMar12toMar16", request.leaseDetails))

    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseholdNonResidentialMar12toMar16LeaseRates.slices)
    val premiumResult = if(eligibleForZeroRate(request)) {
      SlabResult(rate = 0, taxDue = 0)
    } else {
      baseCalculationService.calculateTaxDueSlab(request.premium, SlabRatesTables.leaseholdNonResidentialMar12toMar16PremiumRates.slabs)
    }

    LeaseholdResultFactory.leaseholdNonResidentialMar12toMar16Result(leaseResult, premiumResult, npv, asPrevResult)
  }

  //March 16 requests

  def leaseholdNonResidentialMar16Onwards(request: Request): Seq[Result] = {
    val npv = getNPV("leaseholdNonResidentialMar16Onwards", request.leaseDetails)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseholdNonResidentialMar16OnwardsLeaseRates.slices)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(request.premium, leaseholdNonResidentialMar16OnwardsPremiumRates.slices)

    if(nonResPrevCalcRequired(request))
      Seq(
        LeaseholdResultFactory.leaseholdNonResidentialMar16OnwardsResult(leaseResult, premiumResult, npv),
        leaseholdNonResidentialMar12toMar16(request, asPrevResult = true, preCalculatedNPV = Some(npv))
      )
    else
      Seq(LeaseholdResultFactory.leaseholdNonResidentialMar16OnwardsResult(leaseResult, premiumResult, npv))
  }

  //December 14 requests

  def leaseholdResidentialDec14Onwards(request: Request, asPreviousResult: Boolean = false, preCalculatedNPV: Option[BigDecimal] = None, nonUKRes: Boolean = false): Result = {
    val npv = preCalculatedNPV.getOrElse(getNPV("leaseholdResidentialDec14Onwards", request.leaseDetails))
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseholdResidentialDec14OnwardsLeaseRates.slices)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(request.premium, leaseholdResidentialDec14OnwardsPremiumRates.slices)
    val effectiveDateAfter31March2020: Boolean = request.effectiveDate.isAfter(Dates.MAR2021_RESIDENTIAL_DATE)
    val individual: Boolean = request.propertyDetails.exists(_.individual)

    LeaseholdResultFactory.leaseholdResidentialDec14OnwardsResult(
      leaseResult, premiumResult, npv, asPreviousResult, effectiveDateAfter31March2020, nonUKRes, individual)
  }

  //April 16 requests

  def leaseholdResidentialAddPropApr16Onwards(request: Request): Seq[Result] = {
    val npv = getNPV("leaseholdResidentialAddPropApr16Onwards", request.leaseDetails)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseholdResidentialAddPropApr16OnwardsLeaseRates.slices)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      if(request.premium < 40000) 0 else request.premium,
      leaseholdResidentialAddPropApr16OnwardsPremiumRates.slices
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

  //Nov 17 requests

  def leaseholdResidentialNov17OnwardsFTB(request: Request): Result = {
    val sliceRateTable = if(checkIfShared(request.propertyDetails)) leaseholdResidentialNov17FTBSharedLeaseRates.slices else leaseholdResidentialNov17FTBLeaseRates.slices
    val npv = getNPV("leaseholdResidentialNov17Onwards", request.leaseDetails)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, sliceRateTable)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(request.premium, leaseholdResidentialNov17OnwardsFTBPremiumRates.slices)
    val effectiveDateAfter31March2020: Boolean = request.effectiveDate.isAfter(Dates.MAR2021_RESIDENTIAL_DATE)

    LeaseholdResultFactory.leaseholdResidentialNov17OnwardsFTBResult(leaseResult, premiumResult, npv, effectiveDateAfter31March2020)
  }

  //July 20 requests

  def leaseholdResidentialJuly20Onwards(request: Request, asPreviousResult: Boolean = false, preCalculatedNPV: Option[BigDecimal] = None): Result = {
    val npv = preCalculatedNPV.getOrElse(getNPV("leaseholdResidentialJuly20Onwards", request.leaseDetails))
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseholdResidentialJuly20OnwardsLeaseRates.slices)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(request.premium, leaseholdResidentialJuly20nwardsPremiumRates.slices)

    LeaseholdResultFactory.leaseholdResidentialJuly20OnwardsResult(leaseResult, premiumResult, npv, asPreviousResult)
  }

  def leaseholdResidentialJuly20OnwardsFTB(request: Request): Result = {
    val sliceRateTable = if(checkIfShared(request.propertyDetails)) leaseholdResidentialJuly20FTBSharedLeaseRates.slices else leaseholdResidentialJuly20FTBLeaseRates.slices
    val npv = getNPV("leaseholdResidentialJuly20Onwards", request.leaseDetails)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, sliceRateTable)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(request.premium, leaseholdResidentialJuly20OnwardsFTBPremiumRates.slices)
    LeaseholdResultFactory.leaseholdResidentialJuly20OnwardsFTBResult(leaseResult, premiumResult, npv)
  }

  def leaseholdResidentialAddPropJuly20Onwards(request: Request): Seq[Result] = {
    val npv = getNPV("leaseholdResidentialAddPropJuly20Onwards", request.leaseDetails)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseholdResidentialJuly20OnwardsLeaseRates.slices)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      if(request.premium < 40000) 0 else request.premium,
      leaseholdResidentialAddPropJuly20OnwardsPremiumRates.slices
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

  //April 21 requests

  def leaseholdResidentialApr21OnwardsNonUKRes(request: Request, preCalculatedNPV: Option[BigDecimal] = None): Seq[Result] = {
    val npv = preCalculatedNPV.getOrElse(getNPV("leaseholdResidentialApril21OnwardsNonUKRes", request.leaseDetails))

    val prevLeaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseholdResidentialNov17FTBLeaseRates.slices)
    val prevPremiumResult = baseCalculationService.calculateTaxDueSlice(request.premium, leaseholdResidentialDec14OnwardsPremiumRates.slices)
    val individual: Boolean = request.propertyDetails.exists(_.individual)

    val prevResult = LeaseholdResultFactory.leaseholdResidentialDec14OnwardsResult(prevLeaseResult, prevPremiumResult, npv, asPrevResult = true,
      afterMarch2021 = true, nonUKRes = true, individual)

    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseholdResidentialApr21OnwardsNonUKResLeaseRates.slices)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(request.premium, leaseholdResidentialApr21OnwardsNonUKResPremiumRates.slices)

    val nrsdltInScope = leaseholdNRSDLTInScopeForLeaseOrPremium(
      request.premium, request.leaseDetails.get.leaseTerm.years, request.highestRent,
      request.propertyDetails.get.sharedOwnership.getOrElse(false), request.firstTimeBuyer.getOrElse(false)
    )

    Seq(LeaseholdResultFactory.leaseholdResidentialApr21OnwardsResultNonUKRes(
      leaseResult, premiumResult, npv, individual = individual, nrsdltInScope = nrsdltInScope, additionalProp = false), prevResult)
  }

  def leaseholdResidentialApr21OnwardsNonUKResFTBPrevRes(request: Request, preCalculatedNPV: Option[BigDecimal] = None): Result = {
    val sliceRateTable = if(checkIfShared(request.propertyDetails)) leaseholdResidentialNov17FTBSharedLeaseRates.slices else leaseholdResidentialNov17FTBLeaseRates.slices
    val npv = preCalculatedNPV.getOrElse(getNPV("leaseholdResidentialApr21OnwardsNonUKResFTBPrevRes", request.leaseDetails))

    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, sliceRateTable)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(request.premium, leaseholdResidentialNov17OnwardsFTBPremiumRates.slices)
    val individual: Boolean = request.propertyDetails.exists(_.individual)

    val nrsdltInScope = leaseholdNRSDLTInScopeForLeaseOrPremium(
      request.premium, request.leaseDetails.get.leaseTerm.years, request.highestRent,
      request.propertyDetails.get.sharedOwnership.getOrElse(false), request.firstTimeBuyer.getOrElse(false)
    )

    LeaseholdResultFactory.leaseholdResidentialApr21OnwardsResultNonUKRes(leaseResult, premiumResult, npv, asPrevResult = true,
      individual = individual, nrsdltInScope = nrsdltInScope, additionalProp = false)
  }

  def leaseholdResidentialApr21OnwardsNonUKResAddPropResPrevRes(request: Request, preCalculatedNPV: Option[BigDecimal] = None): Result = {
    val npv = preCalculatedNPV.getOrElse(getNPV("leaseholdResidentialApr21OnwardsNonUKResPrevRes", request.leaseDetails))

    val leaseRates = ratesToUse(request.leaseDetails.get.leaseTerm.years, request.highestRent)

    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseRates)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      if(request.premium < 40000) 0 else request.premium, leaseholdResidentialApr21OnwardsNonUKResPremiumRates.slices)
    val individual: Boolean = request.propertyDetails.exists(_.individual)

    val nrsdltInScope = leaseholdNRSDLTInScopeForLeaseOrPremium(
      request.premium, request.leaseDetails.get.leaseTerm.years, request.highestRent,
      request.propertyDetails.get.sharedOwnership.getOrElse(false), request.firstTimeBuyer.getOrElse(false)
    )

    LeaseholdResultFactory.leaseholdResidentialApr21OnwardsResultNonUKRes(
      leaseResult, premiumResult, npv, asPrevResult = true, additionalProp = true, individual = individual, nrsdltInScope = nrsdltInScope)
  }

  def leaseholdResidentialAddPropApr21OnwardsNonUKRes(request: Request): Seq[Result] = {
    val npv = getNPV("leaseholdResidentialAddPropApril21OnwardsNonUKRes", request.leaseDetails)
    val leaseRates = ratesToUse(request.leaseDetails.get.leaseTerm.years, request.highestRent)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseRates)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      if(request.premium < 40000) 0 else request.premium,
      leaseholdResidentialAddPropNonUKResApr21OnwardsPremiumRates.slices
    )
    val prevResult = leaseholdResidentialApr21OnwardsNonUKResAddPropResPrevRes(request, Some(npv))
    val prevPrem = prevResult.taxCalcs.lift(1).map(_.taxDue).getOrElse({
      throw new RequiredValueNotDefinedException("[LeaseholdCalculationService] [leaseholdResidentialAddPropApr21OnwardsNonUKRes] - " +
        "Premium result not defined in previous calculation")
    })

    val refundEntitlement = refundEntitlementService.calculateRefundEntitlement(premiumResult.taxDue, prevPrem, request.propertyDetails)
    val currResult = LeaseholdResultFactory.leaseholdResidentialAddPropApr21OnwardsNonUKRes(leaseResult, premiumResult, npv, refundEntitlement)
    val individual: Boolean = request.propertyDetails.exists(_.individual)

    if(individual) {
      Seq(currResult, prevResult)
    } else {
      Seq(currResult)
    }
  }

  def leaseholdResidentialApr21OnwardsFTBNonUKRes(request: Request): Seq[Result] = {

    val sharedOwnership = checkIfShared(request.propertyDetails)

    val sliceRateTable = if(sharedOwnership) {
      leaseholdResidentialApr21FTBSharedNonUKResLeaseRates.slices
    } else {
      leaseholdResidentialApr21FTBNonUKResLeaseRates.slices
    }

    val npv = getNPV("leaseholdResidentialApril21Onwards", request.leaseDetails)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, sliceRateTable)

    val premiumRatesToUse: Seq[Slice] = if(request.premium < 40000){
      leaseholdResidentialNov17OnwardsFTBPremiumRates.slices
    } else {
      leaseholdResidentialApr21OnwardsFTBNonUKResPremiumRates.slices
    }

    val premiumResult = baseCalculationService.calculateTaxDueSlice(if(request.premium < 40000) 0 else request.premium, premiumRatesToUse)

    val prevResult = leaseholdResidentialApr21OnwardsNonUKResFTBPrevRes(request, Some(npv))

    val individual: Boolean = request.propertyDetails.exists(_.individual)

    val nrsdltInScope = leaseholdNRSDLTInScopeForLeaseOrPremium(
      request.premium, request.leaseDetails.get.leaseTerm.years, request.highestRent,
      sharedOwnership, request.firstTimeBuyer.getOrElse(false)
    )

    val currResult = LeaseholdResultFactory.leaseholdResidentialApr21OnwardsResultNonUKRes(
      leaseResult, premiumResult, npv, individual = individual, nrsdltInScope = nrsdltInScope, additionalProp = false)

    if (individual && nrsdltInScope) {
      Seq(currResult, prevResult)
    } else {
      Seq(currResult)
    }
  }

  //Shared functions

  def ratesToUse(leaseTermYears: Int, highestRent: BigDecimal): Seq[Slice] = {
    if(leaseTermYears < 7 || highestRent < 1000){
      leaseholdResidentialDec14OnwardsLeaseRates.slices
    } else {
      leaseholdResidentialApr21OnwardsNonUKResLeaseRates.slices
    }
  }

  def checkIfShared(propertyDetails: Option[PropertyDetails]): Boolean = {
    propertyDetails.exists(propDetails => propDetails.sharedOwnership.getOrElse(false))
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
