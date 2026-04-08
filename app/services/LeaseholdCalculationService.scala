/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package services

import data.LeaseholdSliceRatesTables._
import data.SignificantAmounts._
import data.SlabRatesTables._
import data.{Dates, LeaseholdSliceRatesTables, SlabRatesTables}
import exceptions.RequiredValueNotDefinedException
import factories.LeaseholdResultFactory
import models._
import models.calculationtables.{SlabResult, Slice}
import utils.CalculationUtils.{duringNRB250HolidayPeriod, duringNRB500HolidayPeriod, isAfterSept2022AndBeforeApril2025, leaseholdNRSDLTInScopeForLeaseOrPremium}
import utils.DateUtil
import validators.internal.ModelValidation

import java.time.LocalDate
import javax.inject.{Inject, Singleton}

@Singleton
class LeaseholdCalculationService @Inject()(val baseCalculationService: BaseCalculationService,
                                            val refundEntitlementService: RefundEntitlementService) extends DateUtil{

  //STANDARD RESIDENTIAL

  def leaseholdResidentialMar12toDec14(request: Request): Result = {
    val npv = getNPV("leaseholdResidentialMar12toDec14", request)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseholdResidentialMar12toDec14LeaseRates.slices)
    val premiumResult = baseCalculationService.calculateTaxDueSlab(request.premium, SlabRatesTables.leaseholdResidentialMar12toDec14PremiumRates.slabs)

    LeaseholdResultFactory.leaseholdResidentialMar12toDec14Result(leaseResult, premiumResult, npv)
  }

  def leaseholdResidentialDec14Onwards(request: Request, asPreviousResult: Boolean = false, preCalculatedNPV: Option[BigDecimal] = None, nonUKRes: Boolean = false): Result = {
    val npv = preCalculatedNPV.getOrElse(getNPV("leaseholdResidentialDec14Onwards", request))
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseholdResidentialDec14OnwardsLeaseRates.slices)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(request.premium, leaseholdResidentialDec14OnwardsPremiumRates.slices)
    val effectiveDateAfter31March2020: Boolean = request.effectiveDate.isAfter(Dates.MAR2021_RESIDENTIAL_DATE)
    val individual: Boolean = request.propertyDetails.exists(_.individual)

    LeaseholdResultFactory.leaseholdResidentialDec14OnwardsResult(
      leaseResult, premiumResult, npv, asPreviousResult, effectiveDateAfter31March2020, nonUKRes, individual)
  }

  def leaseholdResidentialJuly20Onwards(request: Request, asPreviousResult: Boolean = false, preCalculatedNPV: Option[BigDecimal] = None): Result = {
    val npv = preCalculatedNPV.getOrElse(getNPV("leaseholdResidentialJuly20Onwards", request))
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseholdResidentialJuly20OnwardsLeaseRates.slices)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(request.premium, leaseholdResidentialJuly20OnwardsPremiumRates.slices)

    LeaseholdResultFactory.leaseholdResidentialJuly20OnwardsResult(leaseResult, premiumResult, npv, asPreviousResult)
  }

  def leaseholdResidentialJuly21Onwards(request: Request, asPreviousResult: Boolean = false, preCalculatedNPV: Option[BigDecimal] = None): Result = {
    val npv = preCalculatedNPV.getOrElse(getNPV("leaseholdResidentialJuly21Onwards", request))
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseholdResidentialJuly21OnwardsLeaseRates.slices)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(request.premium, leaseholdResidentialJuly21OnwardsPremiumRates.slices)

    LeaseholdResultFactory.leaseholdResidentialJuly20OnwardsResult(leaseResult, premiumResult, npv, asPreviousResult)
  }

  //NON-RESIDENTIAL

  def leaseholdNonResidentialMar12toMar16(request: Request, asPrevResult: Boolean = false, preCalculatedNPV: Option[BigDecimal] = None): Result = {
    val npv = preCalculatedNPV.getOrElse(getNPV("leaseholdNonResidentialMar12toMar16", request))

    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseholdNonResidentialMar12toMar16LeaseRates.slices)
    val premiumResult = if(eligibleForZeroRate(request)) {
      SlabResult(rate = 0, taxDue = 0)
    } else {
      baseCalculationService.calculateTaxDueSlab(request.premium, SlabRatesTables.leaseholdNonResidentialMar12toMar16PremiumRates.slabs)
    }

    LeaseholdResultFactory.leaseholdNonResidentialMar12toMar16Result(leaseResult, premiumResult, npv, asPrevResult)
  }

  def leaseholdNonResidentialMar16Onwards(request: Request): Seq[Result] = {
    val npv = getNPV("leaseholdNonResidentialMar16Onwards", request)
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

  def leaseholdMixedNonResBeforeMarch2016(request: Request): Result = {
    val npv = getNPV("leaseholdMixedNonResidentialBeforeMarch2016", request)

    val premiumResult = baseCalculationService.calculateTaxDueSlab(
      request.premium,
      SlabRatesTables.leaseholdMixedNonResidentialBeforeMarch172016Rates.slabs
    )

    val leasedResult = baseCalculationService.calculateTaxDueSlice(
      npv,
      leaseholdMixedAfterMarch172016.slices
    )

    LeaseholdResultFactory.leaseholdAddMixedLogic(
      premiumResult = premiumResult,
      leaseResult = leasedResult,
      npv)
  }

  def leaseholdMixedNonResApr2013toMar2016(request: Request): Result = {
    val npv = getNPV("leaseholdMixedNonResApr2013toMar2016", request)

    val premiumResult = baseCalculationService.calculateTaxDueSlab(
      request.premium,
      leaseholdMixedNonResApr2013toMar2016Above1kRentRates.slabs
    )

    val leasedResult = baseCalculationService.calculateTaxDueSlice(
      npv,
      leaseholdMixedNonResApr2013toMar2016Above1kRentNPVRates.slices
    )

    LeaseholdResultFactory.leaseholdReliefFrom15PercentRateAbove1kRentRes(
      premiumResult = premiumResult, leaseResult = leasedResult, npv
    )
  }

  def leaseholdMixedNonResMar2008toMar2016(request: Request): Result = {
    val npv = getNPV("leaseholdMixedNonResMar2008toMar2016", request)

    val premiumResult = baseCalculationService.calculateTaxDueSlab(
      request.premium,
      leaseholdMixedNonResMar2008toMar2016Below1kRentRates.slabs
    )

    val leasedResult = baseCalculationService.calculateTaxDueSlice(
      npv,
      leaseholdMixedNonResMar2008toMar2016Below1kRentNPVRates.slices
    )

    LeaseholdResultFactory.leaseholdRightToBuyBelow1kRentRes(
      premiumResult = premiumResult, leaseResult = leasedResult, npv
    )
  }

  //HRAD

  def leaseholdResidentialAddPropApr16Onwards(request: Request): Seq[Result] = {
    val npv = getNPV("leaseholdResidentialAddPropApr16Onwards", request)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseholdResidentialAddPropApr16OnwardsLeaseRates.slices)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      request.premium,
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

  def leaseholdResidentialAddPropJuly20Onwards(request: Request): Seq[Result] = {
    val npv = getNPV("leaseholdResidentialAddPropJuly20Onwards", request)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseholdResidentialJuly20OnwardsLeaseRates.slices)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      request.premium,
      leaseholdResidentialAddPropJuly20OnwardsPremiumRates.slices
    )
    val prevResult = leaseholdResidentialJuly20Onwards(request, asPreviousResult = true, Some(npv))
    val prevPrem = prevResult.taxCalcs.lift(1).map(_.taxDue).get

    val refundEntitlement = refundEntitlementService.calculateRefundEntitlement(premiumResult.taxDue, prevPrem, request.propertyDetails)
    val currResult = LeaseholdResultFactory.leaseholdResidentialAddPropJuly20Onwards(leaseResult, premiumResult, npv, refundEntitlement)
    val individual: Boolean = request.propertyDetails.exists(_.individual)

    if(individual) {
      Seq(currResult, prevResult)
    } else {
      Seq(currResult)
    }
  }

  def leaseholdResidentialAddPropJuly21Onwards(request: Request): Seq[Result] = {
    val npv = getNPV("leaseholdResidentialAddPropJuly21Onwards", request)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseholdResidentialJuly21OnwardsLeaseRates.slices)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      request.premium,
      leaseholdResidentialAddPropJuly21OnwardsPremiumRates.slices
    )
    val prevResult = leaseholdResidentialJuly21Onwards(request, asPreviousResult = true, Some(npv))
    val prevPrem = prevResult.taxCalcs.lift(1).map(_.taxDue).get

    val refundEntitlement = refundEntitlementService.calculateRefundEntitlement(premiumResult.taxDue, prevPrem, request.propertyDetails)
    val currResult = LeaseholdResultFactory.leaseholdResidentialAddPropJuly20Onwards(leaseResult, premiumResult, npv, refundEntitlement)
    val individual: Boolean = request.propertyDetails.exists(_.individual)

    if(individual) {
      Seq(currResult, prevResult)
    } else {
      Seq(currResult)
    }
  }

  // Additional property changes from Oct24 before Apr25
  def leaseholdResidentialAddPropOct24BeforeApril25(request: Request): Seq[Result] = {
    val npv = getNPV("leaseholdResidentialAddPropOct24Onwards", request)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseholdResidentialJuly21OnwardsLeaseRates.slices)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      request.premium,
      leaseholdResidentialAddPropOct24BeforeApr25PremiumRates.slices
    )
    val prevResult = leaseholdResidentialJuly21Onwards(request, asPreviousResult = true, Some(npv))
    val prevPrem = prevResult.taxCalcs.lift(1).map(_.taxDue).get

    val refundEntitlement = refundEntitlementService.calculateRefundEntitlement(premiumResult.taxDue, prevPrem, request.propertyDetails)
    val currResult = LeaseholdResultFactory.leaseholdResidentialAddPropJuly20Onwards(leaseResult, premiumResult, npv, refundEntitlement)
    val individual: Boolean = request.propertyDetails.exists(_.individual)

    if(individual) {
      Seq(currResult, prevResult)
    } else {
      Seq(currResult)
    }
  }

  // Additional property changes Apr25 onwards
  def leaseholdResidentialAddPropApril25Onwards(request: Request): Seq[Result] = {
    val npv = getNPV("leaseholdResidentialAddPropApril25Onwards", request)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseholdResidentialAddPropApr16OnwardsLeaseRates.slices)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      request.premium,
      leaseholdResidentialAddPropApr25OnwardsPremiumRates.slices
    )
    val prevResult = leaseholdResidentialAddPropApr25OnwardsPrevResult(request, Some(npv))
    val prevPrem = prevResult.taxCalcs.lift(1).map(_.taxDue).get

    val refundEntitlement = refundEntitlementService.calculateRefundEntitlement(premiumResult.taxDue, prevPrem, request.propertyDetails)
    val currResult = LeaseholdResultFactory.leaseholdResidentialAddPropJuly20Onwards(leaseResult, premiumResult, npv, refundEntitlement)
    val individual: Boolean = request.propertyDetails.exists(_.individual)

    if(individual) {
      Seq(currResult, prevResult)
    } else {
      Seq(currResult)
    }
  }

   def leaseholdResidentialAddPropApr25OnwardsPrevResult(request: Request, preCalculatedNPV: Option[BigDecimal] = None): Result = {
    val npv = preCalculatedNPV.getOrElse(getNPV("leaseholdResidentialJuly21Onwards", request))
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseholdResidentialOct21OnwardsLeaseRates.slices)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(request.premium, leaseholdResidentialOct21OnwardsPremiumRates.slices)

    LeaseholdResultFactory.leaseholdResidentialJuly20OnwardsResult(leaseResult, premiumResult, npv, asPrevResult = true)
  }

  //FTB

  def leaseholdResidentialNov17OnwardsFTB(request: Request): Result = {
    val sliceRateTable = if(checkIfShared(request.propertyDetails)) leaseholdResidentialNov17FTBSharedLeaseRates.slices else leaseholdResidentialNov17FTBLeaseRates.slices
    val npv = getNPV("leaseholdResidentialNov17Onwards", request)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, sliceRateTable)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(request.premium, leaseholdResidentialNov17OnwardsFTBPremiumRates.slices)

    LeaseholdResultFactory.leaseholdResidentialNov17OnwardsFTBResult(leaseResult, premiumResult, npv)
  }

  def leaseholdResidentialJuly21OnwardsFTB(request: Request): Result = {
    val sliceRateTable = if(checkIfShared(request.propertyDetails)) leaseholdResidentialJuly21FTBSharedLeaseRates.slices else leaseholdResidentialJuly21FTBLeaseRates.slices
    val npv = getNPV("leaseholdResidentialJuly21OnwardsFTB", request)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, sliceRateTable)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(request.premium, leaseholdResidentialNov17OnwardsFTBPremiumRates.slices)

    LeaseholdResultFactory.leaseholdResidentialNov17OnwardsFTBResult(leaseResult, premiumResult, npv)
  }

  def leaseholdMixedOrNonResidentialMar16Onwards(request: Request): Result = {

    val npv = getNPV("leaseholdMixedOrNonResidentialMar16Onwards", request)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseholdMixedOrNonResidentialMar16OnwardsLeaseRates.slices)
    val premiumResult = baseCalculationService.calculateTaxDueSlab(request.premium, SlabRatesTables.leaseholdMixedOrNonResidentialMar16OnwardsPremiumRates.slabs)

    LeaseholdResultFactory.leaseholdMixedOrNonResidentialMar16OnwardsResult(leaseResult, premiumResult, npv)
  }

  def leaseholdResidentialSept22OnwardsFTB(request: Request): Result = {
    val sliceRateTable = if(checkIfShared(request.propertyDetails)) leaseholdResidentialJuly21FTBSharedLeaseRates.slices else leaseholdResidentialJuly21FTBLeaseRates.slices
    val npv = getNPV("leaseholdResidentialJuly21OnwardsFTB", request)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, sliceRateTable)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(request.premium, leaseholdResidentialSep22OnwardsFTBRates.slices)

    LeaseholdResultFactory.leaseholdResidentialNov17OnwardsFTBResult(leaseResult, premiumResult, npv)
  }

  //NRSDLT

  def leaseholdResidentialApr21OnwardsNonUKRes(request: Request, preCalculatedNPV: Option[BigDecimal] = None): Seq[Result] = {
    val npv = preCalculatedNPV.getOrElse(getNPV("leaseholdResidentialApril21OnwardsNonUKRes", request))

    val prevLeaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseholdResidentialJuly20OnwardsLeaseRates.slices)
    val prevPremiumResult = baseCalculationService.calculateTaxDueSlice(request.premium, leaseholdResidentialJuly20OnwardsPremiumRates.slices)
    val individual: Boolean = request.propertyDetails.exists(_.individual)

    val prevResult = LeaseholdResultFactory.leaseholdResidentialDec14OnwardsResult(prevLeaseResult, prevPremiumResult, npv, asPrevResult = true,
      afterMarch2021 = true, nonUKRes = true, individual)

    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseholdResidentialApr21OnwardsNonUKResLeaseRates.slices)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(request.premium, leaseholdResidentialApr21OnwardsNonUKResPremiumRates.slices)

    val nrsdltInScope = leaseholdNRSDLTInScopeForLeaseOrPremium(
      request.premium, request.leaseDetails.get.leaseTerm.years, request.highestRent,
      request.propertyDetails.get.sharedOwnership.getOrElse(false), request.firstTimeBuyer.getOrElse(false)
    )

    val res = Seq(LeaseholdResultFactory.leaseholdResidentialApr21OnwardsResultNonUKRes(
      leaseResult, premiumResult, npv, individual = individual, nrsdltInScope = nrsdltInScope, additionalProp = false))

    if(individual) res :+ prevResult else res
  }

  def leaseholdResidentialJuly21OnwardsNonUKRes(request: Request, preCalculatedNPV: Option[BigDecimal] = None): Seq[Result] = {
    val npv = preCalculatedNPV.getOrElse(getNPV("leaseholdResidentialJuly21OnwardsNonUKRes", request))

    val prevLeaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseholdResidentialJuly21OnwardsLeaseRates.slices)
    val prevPremiumResult = baseCalculationService.calculateTaxDueSlice(request.premium, leaseholdResidentialJuly21OnwardsPremiumRates.slices)
    val individual: Boolean = request.propertyDetails.exists(_.individual)

    val prevResult = LeaseholdResultFactory.leaseholdResidentialDec14OnwardsResult(prevLeaseResult, prevPremiumResult, npv, asPrevResult = true,
      afterMarch2021 = true, nonUKRes = true, individual)

    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseholdResidentialJuly21OnwardsNonUKResLeaseRates.slices)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(request.premium, leaseholdResidentialJuly21OnwardsNonUKResPremiumRates.slices)

    val nrsdltInScope = leaseholdNRSDLTInScopeForLeaseOrPremium(
      request.premium, request.leaseDetails.get.leaseTerm.years, request.highestRent,
      request.propertyDetails.get.sharedOwnership.getOrElse(false), request.firstTimeBuyer.getOrElse(false)
    )

    val res = Seq(LeaseholdResultFactory.leaseholdResidentialApr21OnwardsResultNonUKRes(
      leaseResult, premiumResult, npv, individual = individual, nrsdltInScope = nrsdltInScope, additionalProp = false))

    if(individual) res :+ prevResult else res
  }

  def leaseholdResidentialOct21OnwardsNonUKRes(request: Request, preCalculatedNPV: Option[BigDecimal] = None): Seq[Result] = {
    val npv = preCalculatedNPV.getOrElse(getNPV("leaseholdResidentialOct21OnwardsNonUKRes", request))

    val prevLeaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseholdResidentialOct21OnwardsLeaseRates.slices)
    val prevPremiumResult = baseCalculationService.calculateTaxDueSlice(request.premium, leaseholdResidentialOct21OnwardsPremiumRates.slices)
    val individual: Boolean = request.propertyDetails.exists(_.individual)

    val prevResult = LeaseholdResultFactory.leaseholdResidentialDec14OnwardsResult(prevLeaseResult, prevPremiumResult, npv, asPrevResult = true,
      afterMarch2021 = true, nonUKRes = true, individual)

    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseholdResidentialOct21OnwardsNonUKResLeaseRates.slices)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(request.premium, leaseholdResidentialOct21OnwardsNonUKResPremiumRates.slices)

    val nrsdltInScope = leaseholdNRSDLTInScopeForLeaseOrPremium(
      request.premium, request.leaseDetails.get.leaseTerm.years, request.highestRent,
      request.propertyDetails.get.sharedOwnership.getOrElse(false), request.firstTimeBuyer.getOrElse(false)
    )

    val res = Seq(LeaseholdResultFactory.leaseholdResidentialApr21OnwardsResultNonUKRes(
      leaseResult, premiumResult, npv, individual = individual, nrsdltInScope = nrsdltInScope, additionalProp = false))

    if(individual) res :+ prevResult else res
  }

  //FTB + NRSDLT

  def leaseholdResidentialJuly21OnwardsFTBNonUKRes(request: Request): Seq[Result] = {

    val sharedOwnership = checkIfShared(request.propertyDetails)

    val leaseSlices = if(sharedOwnership) {
      leaseholdResidentialJuly21FTBSharedLeaseRates.slices
    } else {
      leaseholdResidentialJuly21FTBNonUKResLeaseRates.slices
    }

    val npv = getNPV("leaseholdResidentialJuly21OnwardsFTBNonUKRes", request)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseSlices)

    val premiumSlices: Seq[Slice] = leaseholdResidentialJuly21OnwardsFTBNonUKResPremiumRates.slices

    val premiumResult = baseCalculationService.calculateTaxDueSlice(request.premium, premiumSlices)

    val prevResult = leaseholdResidentialJuly21OnwardsNonUKResFTBPrevRes(request, npv)

    val individual: Boolean = request.propertyDetails.exists(_.individual)

    val nrsdltInScope = leaseholdNRSDLTInScopeForLeaseOrPremium(
      request.premium, request.leaseDetails.get.leaseTerm.years, request.highestRent,
      sharedOwnership, request.firstTimeBuyer.getOrElse(false)
    )

    val currResult = LeaseholdResultFactory.leaseholdResidentialApr21OnwardsResultNonUKRes(
      leaseResult, premiumResult, npv, individual = individual, nrsdltInScope = nrsdltInScope, additionalProp = false)

    if(individual)
      Seq(currResult, prevResult)
    else
      Seq(currResult)

  }

  def leaseholdResidentialSept22OnwardsFTBNonUKRes(request: Request): Seq[Result] = {

    val sharedOwnership = checkIfShared(request.propertyDetails)

    val leaseSlices = if(sharedOwnership) {
      leaseholdResidentialJuly21FTBSharedLeaseRates.slices
    } else {
      leaseholdResidentialJuly21FTBNonUKResLeaseRates.slices
    }

    val npv = getNPV("leaseholdResidentialJuly21OnwardsFTBNonUKRes", request)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseSlices)

    val premiumSlices: Seq[Slice] = leaseholdResidentialSep22OnwardsFTBNonUKResRates.slices

    val premiumResult = baseCalculationService.calculateTaxDueSlice(request.premium, premiumSlices)

    val prevResult = leaseholdResidentialSept22OnwardsNonUKResFTBPrevRes(request, npv)

    val individual: Boolean = request.propertyDetails.exists(_.individual)

    val nrsdltInScope = leaseholdNRSDLTInScopeForLeaseOrPremium(
      request.premium, request.leaseDetails.get.leaseTerm.years, request.highestRent,
      sharedOwnership, request.firstTimeBuyer.getOrElse(false)
    )

    val currResult = LeaseholdResultFactory.leaseholdResidentialApr21OnwardsResultNonUKRes(
      leaseResult, premiumResult, npv, individual = individual, nrsdltInScope = nrsdltInScope, additionalProp = false)

    if(individual)
      Seq(currResult, prevResult)
    else
      Seq(currResult)

  }

  def leaseholdResidentialJuly21OnwardsNonUKResFTBPrevRes(request: Request, npv: BigDecimal): Result = {

    val sharedOwnership = checkIfShared(request.propertyDetails)

    val leaseSlices = if(sharedOwnership) {
      leaseholdResidentialJuly21FTBSharedLeaseRates.slices
    } else {
      leaseholdResidentialJuly21FTBLeaseRates.slices
    }

    val premiumSlices = leaseholdResidentialJuly21OnwardsFTBPremiumRates.slices

    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseSlices)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(request.premium, premiumSlices)
    val individual: Boolean = request.propertyDetails.exists(_.individual)

    val nrsdltInScope = leaseholdNRSDLTInScopeForLeaseOrPremium(
      request.premium, request.leaseDetails.get.leaseTerm.years, request.highestRent,
      sharedOwnership, request.firstTimeBuyer.getOrElse(false)
    )

    LeaseholdResultFactory.leaseholdResidentialApr21OnwardsResultNonUKRes(leaseResult, premiumResult, npv, asPrevResult = true,
      individual = individual, nrsdltInScope = nrsdltInScope, additionalProp = false)
  }

  def leaseholdResidentialSept22OnwardsNonUKResFTBPrevRes(request: Request, npv: BigDecimal): Result = {

    val sharedOwnership = checkIfShared(request.propertyDetails)

    val leaseSlices = if(sharedOwnership) {
      leaseholdResidentialJuly21FTBSharedLeaseRates.slices
    } else {
      leaseholdResidentialJuly21FTBLeaseRates.slices
    }

    val premiumSlices = leaseholdResidentialSep22OnwardsFTBRates.slices

    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseSlices)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(request.premium, premiumSlices)
    val individual: Boolean = request.propertyDetails.exists(_.individual)

    val nrsdltInScope = leaseholdNRSDLTInScopeForLeaseOrPremium(
      request.premium, request.leaseDetails.get.leaseTerm.years, request.highestRent,
      sharedOwnership, request.firstTimeBuyer.getOrElse(false)
    )

    LeaseholdResultFactory.leaseholdResidentialApr21OnwardsResultNonUKRes(leaseResult, premiumResult, npv, asPrevResult = true,
      individual = individual, nrsdltInScope = nrsdltInScope, additionalProp = false)
  }

  def leaseholdResidentialOct21OnwardsFTBNonUKRes(request: Request): Seq[Result] = {

    val sharedOwnership = checkIfShared(request.propertyDetails)

    val leaseSlices = if(sharedOwnership) {
      leaseholdResidentialNov17FTBSharedLeaseRates.slices
    } else {
      leaseholdResidentialOct21FTBNonUKResLeaseRates.slices
    }

    val npv = getNPV("leaseholdResidentialOct21OnwardsFTBNonUKRes", request)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseSlices)

    val premiumSlices: Seq[Slice] = leaseholdResidentialOct21OnwardsFTBNonUKResPremiumRates.slices

    val premiumResult = baseCalculationService.calculateTaxDueSlice(request.premium, premiumSlices)

    val prevResult = leaseholdResidentialOct21OnwardsNonUKResFTBPrevRes(request, npv)

    val individual: Boolean = request.propertyDetails.exists(_.individual)

    val nrsdltInScope = leaseholdNRSDLTInScopeForLeaseOrPremium(
      request.premium, request.leaseDetails.get.leaseTerm.years, request.highestRent,
      sharedOwnership, request.firstTimeBuyer.getOrElse(false)
    )

    val currResult = LeaseholdResultFactory.leaseholdResidentialApr21OnwardsResultNonUKRes(
      leaseResult, premiumResult, npv, individual = individual, nrsdltInScope = nrsdltInScope, additionalProp = false)

    if(individual)
      Seq(currResult, prevResult)
    else
      Seq(currResult)

  }

  def leaseholdResidentialOct21OnwardsNonUKResFTBPrevRes(request: Request, npv: BigDecimal): Result = {

    val sliceRateTable = if(checkIfShared(request.propertyDetails)) {
      leaseholdResidentialNov17FTBSharedLeaseRates.slices
    } else {
      leaseholdResidentialNov17FTBLeaseRates.slices
    }

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

  //NRSDLT + HRAD

  def leaseholdResidentialApr21OnwardsNonUKResAddProp(request: Request): Seq[Result] = {
    val npv = getNPV("leaseholdResidentialAddPropApril21OnwardsNonUKRes", request)
    val leaseRates = leaseRatesToUse(request.premium, request.effectiveDate, request.leaseDetails.get.leaseTerm.years, request.highestRent)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseRates)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      request.premium,
      leaseholdResidentialAddPropNonUKResApr21OnwardsPremiumRates.slices
    )
    val prevResult = leaseholdResidentialApr21OnwardsNonUKResAddPropResPrevRes(request, npv)
    val prevPrem = prevResult.taxCalcs.lift(1).map(_.taxDue).get

    val refundEntitlement = refundEntitlementService.calculateRefundEntitlement(premiumResult.taxDue, prevPrem, request.propertyDetails)
    val currResult = LeaseholdResultFactory.leaseholdResidentialAddPropApr21OnwardsNonUKRes(leaseResult, premiumResult, npv, refundEntitlement)
    val individual: Boolean = request.propertyDetails.exists(_.individual)

    if(individual) {
      Seq(currResult, prevResult)
    } else {
      Seq(currResult)
    }
  }

  def leaseholdResidentialApr21OnwardsNonUKResAddPropResPrevRes(request: Request, npv: BigDecimal): Result = {

    val leaseRates = leaseRatesToUse(request.premium, request.effectiveDate, request.leaseDetails.get.leaseTerm.years, request.highestRent)

    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseRates)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      request.premium, leaseholdResidentialApr21OnwardsNonUKResPremiumRates.slices)
    val individual: Boolean = request.propertyDetails.exists(_.individual)

    val nrsdltInScope = leaseholdNRSDLTInScopeForLeaseOrPremium(
      request.premium, request.leaseDetails.get.leaseTerm.years, request.highestRent,
      request.propertyDetails.get.sharedOwnership.getOrElse(false), request.firstTimeBuyer.getOrElse(false)
    )

    LeaseholdResultFactory.leaseholdResidentialApr21OnwardsResultNonUKRes(
      leaseResult, premiumResult, npv, asPrevResult = true, additionalProp = true, individual = individual, nrsdltInScope = nrsdltInScope)
  }

  def leaseholdResidentialJuly21OnwardsNonUKResAddProp(request: Request): Seq[Result] = {
    val npv = getNPV("leaseholdResidentialAddPropJuly21OnwardsNonUKRes", request)
    val leaseRates = leaseRatesToUse(request.premium, request.effectiveDate, request.leaseDetails.get.leaseTerm.years, request.highestRent)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseRates)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      request.premium,
      leaseholdResidentialAddPropNonUKResJuly21OnwardsPremiumRates.slices
    )
    val prevResult = leaseholdResidentialJuly21OnwardsNonUKResAddPropResPrevRes(request, npv)
    val prevPrem = prevResult.taxCalcs.lift(1).map(_.taxDue).get

    val refundEntitlement = refundEntitlementService.calculateRefundEntitlement(premiumResult.taxDue, prevPrem, request.propertyDetails)
    val currResult = LeaseholdResultFactory.leaseholdResidentialAddPropApr21OnwardsNonUKRes(leaseResult, premiumResult, npv, refundEntitlement)
    val individual: Boolean = request.propertyDetails.exists(_.individual)

    if(individual) {
      Seq(currResult, prevResult)
    } else {
      Seq(currResult)
    }
  }

  def leaseholdResidentialJuly21OnwardsNonUKResAddPropResPrevRes(request: Request, npv: BigDecimal): Result = {

    val leaseRates = leaseRatesToUse(request.premium, request.effectiveDate, request.leaseDetails.get.leaseTerm.years, request.highestRent)

    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseRates)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      request.premium, leaseholdResidentialJuly21OnwardsNonUKResPremiumRates.slices)
    val individual: Boolean = request.propertyDetails.exists(_.individual)

    val nrsdltInScope = leaseholdNRSDLTInScopeForLeaseOrPremium(
      request.premium, request.leaseDetails.get.leaseTerm.years, request.highestRent,
      request.propertyDetails.get.sharedOwnership.getOrElse(false), request.firstTimeBuyer.getOrElse(false)
    )

    LeaseholdResultFactory.leaseholdResidentialApr21OnwardsResultNonUKRes(
      leaseResult, premiumResult, npv, asPrevResult = true, additionalProp = true, individual = individual, nrsdltInScope = nrsdltInScope)
  }

  // non UK resident additional property changes from Oct24 before Apr25
  def leaseholdResidentialOct24BeforeApr25NonUKResAddProp(request: Request): Seq[Result] = {
    val npv = getNPV("leaseholdResidentialAddPropOct24BeforeApr25NonUKRes", request)
    val leaseRates = leaseRatesToUse(request.premium, request.effectiveDate, request.leaseDetails.get.leaseTerm.years, request.highestRent)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseRates)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      request.premium,
      leaseholdResidentialAddPropNonUKResOct24BeforeApr25PremiumRates.slices
    )
    val prevResult = leaseholdResidentialOct24BeforeApr25NonUKResAddPropResPrevRes(request, npv)
    val prevPrem = prevResult.taxCalcs.lift(1).map(_.taxDue).get

    val refundEntitlement = refundEntitlementService.calculateRefundEntitlement(premiumResult.taxDue, prevPrem, request.propertyDetails)
    val currResult = LeaseholdResultFactory.leaseholdResidentialAddPropApr21OnwardsNonUKRes(leaseResult, premiumResult, npv, refundEntitlement)
    val individual: Boolean = request.propertyDetails.exists(_.individual)

    if(individual) {
      Seq(currResult, prevResult)
    } else {
      Seq(currResult)
    }
  }

  private def leaseholdResidentialOct24BeforeApr25NonUKResAddPropResPrevRes(request: Request, npv: BigDecimal): Result = {

    val leaseRates = leaseRatesToUse(request.premium, request.effectiveDate, request.leaseDetails.get.leaseTerm.years, request.highestRent)

    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseRates)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      request.premium, leaseholdResidentialJuly21OnwardsNonUKResPremiumRates.slices)
    val individual: Boolean = request.propertyDetails.exists(_.individual)

    val nrsdltInScope = leaseholdNRSDLTInScopeForLeaseOrPremium(
      request.premium, request.leaseDetails.get.leaseTerm.years, request.highestRent,
      request.propertyDetails.get.sharedOwnership.getOrElse(false), request.firstTimeBuyer.getOrElse(false)
    )

    LeaseholdResultFactory.leaseholdResidentialApr21OnwardsResultNonUKRes(
      leaseResult, premiumResult, npv, asPrevResult = true, additionalProp = true, individual = individual, nrsdltInScope = nrsdltInScope)
  }

  // non UK resident additional property changes after Apr25
  def leaseholdResidentialApr25OnwardsNonUKResAddProp(request: Request): Seq[Result] = {
    val npv = getNPV("leaseholdResidentialAddPropOct24BeforeApr25NonUKRes", request)
    val leaseRates = leaseRatesToUse(request.premium, request.effectiveDate, request.leaseDetails.get.leaseTerm.years, request.highestRent)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseRates)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      request.premium,
      leaseholdResidentialAddPropNonUKResApr25OnwardsPremiumRates.slices
    )
    val prevResult = leaseholdResidentialAfterApr25NonUKResAddPropResPrevRes(request, npv)
    val prevPrem = prevResult.taxCalcs.lift(1).map(_.taxDue).get

    val refundEntitlement = refundEntitlementService.calculateRefundEntitlement(premiumResult.taxDue, prevPrem, request.propertyDetails)
    val currResult = LeaseholdResultFactory.leaseholdResidentialAddPropApr21OnwardsNonUKRes(leaseResult, premiumResult, npv, refundEntitlement)
    val individual: Boolean = request.propertyDetails.exists(_.individual)

    if(individual) {
      Seq(currResult, prevResult)
    } else {
      Seq(currResult)
    }
  }

  private def leaseholdResidentialAfterApr25NonUKResAddPropResPrevRes(request: Request, npv: BigDecimal): Result = {
    val leaseRates = leaseRatesToUse(request.premium, request.effectiveDate, request.leaseDetails.get.leaseTerm.years, request.highestRent)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseRates)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      request.premium, leaseholdResidentialOct21OnwardsNonUKResPremiumRates.slices)
    val individual: Boolean = request.propertyDetails.exists(_.individual)
    val nrsdltInScope = leaseholdNRSDLTInScopeForLeaseOrPremium(
      request.premium, request.leaseDetails.get.leaseTerm.years, request.highestRent,
      request.propertyDetails.get.sharedOwnership.getOrElse(false), request.firstTimeBuyer.getOrElse(false)
    )
    LeaseholdResultFactory.leaseholdResidentialApr21OnwardsResultNonUKRes(
      leaseResult, premiumResult, npv, asPrevResult = true, additionalProp = true, individual = individual, nrsdltInScope = nrsdltInScope)
  }


  def leaseholdResidentialOct21OnwardsNonUKResAddProp(request: Request): Seq[Result] = {
    val npv = getNPV("leaseholdResidentialAddPropOct21OnwardsNonUKRes", request)
    val leaseRates = leaseRatesToUse(request.premium, request.effectiveDate, request.leaseDetails.get.leaseTerm.years, request.highestRent)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseRates)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      request.premium,
      leaseholdResidentialAddPropNonUKResOct21OnwardsPremiumRates.slices
    )
    val prevResult = leaseholdResidentialOct21OnwardsNonUKResAddPropResPrevRes(request, npv)
    val prevPrem = prevResult.taxCalcs.lift(1).map(_.taxDue).get

    val refundEntitlement = refundEntitlementService.calculateRefundEntitlement(premiumResult.taxDue, prevPrem, request.propertyDetails)
    val currResult = LeaseholdResultFactory.leaseholdResidentialAddPropApr21OnwardsNonUKRes(leaseResult, premiumResult, npv, refundEntitlement)
    val individual: Boolean = request.propertyDetails.exists(_.individual)

    if(individual) {
      Seq(currResult, prevResult)
    } else {
      Seq(currResult)
    }
  }

  def leaseholdResidentialOct21OnwardsNonUKResAddPropResPrevRes(request: Request, npv: BigDecimal): Result = {

    val leaseRates = leaseRatesToUse(request.premium, request.effectiveDate, request.leaseDetails.get.leaseTerm.years, request.highestRent)

    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseRates)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(
      request.premium, leaseholdResidentialOct21OnwardsNonUKResPremiumRates.slices)
    val individual: Boolean = request.propertyDetails.exists(_.individual)

    val nrsdltInScope = leaseholdNRSDLTInScopeForLeaseOrPremium(
      request.premium, request.leaseDetails.get.leaseTerm.years, request.highestRent,
      request.propertyDetails.get.sharedOwnership.getOrElse(false), request.firstTimeBuyer.getOrElse(false)
    )

    LeaseholdResultFactory.leaseholdResidentialApr21OnwardsResultNonUKRes(
      leaseResult, premiumResult, npv, asPrevResult = true, additionalProp = true, individual = individual, nrsdltInScope = nrsdltInScope)
  }

  //Shared functions

  def leaseRatesToUse(premium: BigDecimal, effectiveDate: LocalDate, leaseTermYears: Int, highestRent: BigDecimal): Seq[Slice] = {
    if (duringNRB250HolidayPeriod(effectiveDate) || isAfterSept2022AndBeforeApril2025(effectiveDate)) {
      if ((leaseTermYears < 7 || highestRent < 1000) && premium < 40000) {
        leaseholdResidentialJuly21OnwardsLeaseRates.slices
      } else {
        leaseholdResidentialJuly21OnwardsNonUKResLeaseRates.slices
      }
    } else if (duringNRB500HolidayPeriod(effectiveDate)) {
      if ((leaseTermYears < 7 || highestRent < 1000) && premium < 40000) {
        leaseholdResidentialJuly20OnwardsLeaseRates.slices
      } else {
        leaseholdResidentialApr21OnwardsNonUKResLeaseRates.slices
      }
    } else {
      if ((leaseTermYears < 7 || highestRent < 1000) && premium < 40000) {
        leaseholdResidentialDec14OnwardsLeaseRates.slices
      } else {
        leaseholdResidentialOct21OnwardsNonUKResLeaseRates.slices
      }
    }
  }

  def checkIfShared(propertyDetails: Option[PropertyDetails]): Boolean = {
    propertyDetails.exists(propDetails => propDetails.sharedOwnership.getOrElse(false))
  }

  private[services] def getNPV(func: String, request: Request): BigDecimal = {
    request.declaredNpv.getOrElse(baseCalculationService.calculateNPV(
      request.leaseDetails.getOrElse{throw new RequiredValueNotDefinedException(
        s"[LeaseholdCalculationService] [$func] Lease details not defined when required"
      )}
    ))
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

  def leaseholdZeroRateTaxReliefRes(request: Request): Result = {
    val calculatedNpv = Some(getNPV("leaseHoldZeroRateTaxRelief", request).toInt)
    LeaseholdResultFactory.leaseHoldZeroRateTaxRelief(calculatedNpv)
  }

  def leaseholdAcquisitionTaxReliefRes(request: Request): Result ={

    val npv = getNPV("leaseholdAcquisitionTaxRelief", request)

    val premiumResult = baseCalculationService.calculateTaxDueSlab(
      request.premium,
      SlabRatesTables.acquisitionTaxReliefRate.slabs
    )

    val leasedResult = baseCalculationService.calculateTaxDueSlab(
      npv,
      SlabRatesTables.acquisitionTaxReliefRate.slabs
    )

    LeaseholdResultFactory.leaseholdAcquisitionTaxReliefRes(premiumResult, leasedResult, npv)
  }

  def leaseholdMixedNonResidentialRightToBuyBeforeMarch08(request: Request): Result = {

    val npv = getNPV("leaseholdMixedNonResidentialRightToBuyBeforeMarch08", request)

    val premiumResult = baseCalculationService.calculateTaxDueSlab(
      request.premium,
      leaseholdMixedNonResidentialRightToBuyBeforeMarch08Rates.slabs
    )

    val leaseResult = baseCalculationService.calculateTaxDueSlice(
      npv,
      leaseholdMixedNonResidentialRightToBuyBeforeMarch08RentRates.slices
    )

    LeaseholdResultFactory.leaseholdMixedNonResidentialRightToBuyBeforeMarch08Result(leaseResult, premiumResult, npv)
  }

  def leaseholdMixedNonResidentialRightToBuyBeforeMarch16(request: Request): Result ={

    val npv = getNPV("leaseholdMixedNonResidentialRightToBuyBeforeMarch16", request)

    val premiumResult = baseCalculationService.calculateTaxDueSlab(
      request.premium,
      SlabRatesTables.leaseholdMixedNonResidentialRightToBuyBeforeMarch2016Rates.slabs
    )

    val leaseResult = baseCalculationService.calculateTaxDueSlice(
      npv,
      LeaseholdSliceRatesTables.leaseholdMixedNonResidentialRightToBuyBeforeMarch2016Rates.slices
    )

    LeaseholdResultFactory.leaseholdMixedNonResidentialRightToBuyBeforeMarch16Result(leaseResult, premiumResult, npv)

  }


  val leaseholdCollectiveEnfranchisementByLeaseholdersApr09Onwards: Result =
    LeaseholdResultFactory
      .leaseholdSelfAssessedResult

  val leaseholdFreeportPartialRelief: Result =
    LeaseholdResultFactory
      .leaseholdSelfAssessedResult

  val leaseholdResFTBReliefMar10BeforeMar12: Result =
    LeaseholdResultFactory
      .leaseholdSelfAssessedResult

  val leaseholdOtherInterestTransferred: Result =
    LeaseholdResultFactory
      .leaseholdSelfAssessedResult

  val leaseholdNov17Onwards: Result =
    LeaseholdResultFactory
      .leaseholdSelfAssessedResult

  val leaseholdMixedPropMar08BeforeMar16: Result =
    LeaseholdResultFactory
      .leaseholdSelfAssessedResult

  val leaseholdResidentialFTBWithMultipleLands: Result =
    LeaseholdResultFactory
      .leaseholdSelfAssessedResult

  val leaseholdMultipleDwellingRelief: Result =
    LeaseholdResultFactory
      .leaseholdSelfAssessedResult

  val leaseholdSelfAssessedOnOrAfterNov2017: Result = {
    LeaseholdResultFactory
      .leaseholdSelfAssessedResult
  }

  val leaseholdResidentialFTBOnOrAfterJul2020: Result =
    LeaseholdResultFactory
      .leaseholdSelfAssessedResult

  val leaseholdResFTBAfterNov17AndBeforeJul20greaterThan500k: Result = {
    LeaseholdResultFactory
      .leaseholdSelfAssessedResult
  }

  val leaseholdResFTBAfterNov17AndBeforeJul20upTo500k: Result = {
    LeaseholdResultFactory
      .leaseholdSelfAssessedResult
  }


  def leaseholdFreeportRelief(request: Request): Result = {
    val calculatedNpv = Some(getNPV("leaseholdFreeportRelief", request).toInt)
    LeaseholdResultFactory.leaseHoldZeroRateTaxRelief(calculatedNpv)
  }

  def leaseholdResAddPropPreCompletionTransactionApr2016Onwards(request: Request): Result = {
    val calculatedNpv = Some(getNPV("leaseholdResAddPropPreCompletionTransactionAfterApr2016", request).toInt)
    LeaseholdResultFactory.leaseHoldZeroRateTaxRelief(calculatedNpv)
  }

  def leaseholdPreCompletionTransactionApr2013Onwards(request: Request): Result = {
    val calculatedNpv = Some(getNPV("leaseholdPreCompletionTransactionAfterApr2013", request).toInt)
    LeaseholdResultFactory.leaseHoldZeroRateTaxRelief(calculatedNpv)
  }

  def leaseholdMixedNonResPropStandardZeroRelief(request: Request): Result = {
    val calculatedNpv = Some(getNPV("leaseholdMixedNonResPropStandardZeroRelief", request).toInt)
    LeaseholdResultFactory.leaseHoldZeroRateTaxRelief(calculatedNpv)
  }

  def leaseholdMixedNonResBeforeMar08(request: Request): Result = {
    val npv = getNPV("leaseholdMixedNonResBeforeMar08", request)

    val premiumResult = baseCalculationService.calculateTaxDueSlab(
      request.premium,
      leaseholdMixedNonResBeforeMar08Rates.slabs
    )

    val leaseResult = baseCalculationService.calculateTaxDueSlice(
      npv,
      leaseholdMixedNonResBeforeMar08RentRates.slices
    )

    LeaseholdResultFactory.leaseholdMixedNonResBeforeMar08Result(leaseResult, premiumResult, npv)
  }
  def leaseholdReliefFrom15PercentRateMixedAndNonResAfterApril2013AndBeforeMarch2016(request: Request): Result ={

    val npv = getNPV("leaseholdReliefFrom15PercentRateMixedAndNonResAfterApril2013AndBeforeMarch2016", request)

    val premiumResult = baseCalculationService.calculateTaxDueSlab(
      request.premium,
      leaseHoldMixedNonResAfterApril2013AndBeforeMarch2016LeaseRates.slabs
    )

    val leasedResult = baseCalculationService.calculateTaxDueSlice(
      npv,
      leaseHoldMixedNonResidentialReliefFrom15PercentAfterApril2013AndBeforeMarch2016.slices
    )

    LeaseholdResultFactory.leaseholdReliefFrom15PercentRateMixedAndNonResAfterApril2013AndBeforeMarch2016Res(premiumResult, leasedResult, npv)
  }

  def leaseholdReliefFrom15PercentRateRightToBuyMixedOnOrAfterMarch2016(request: Request): Result = {
    val npv = getNPV("leaseholdNonResidentialMar16Onwards", request)
    val leaseResult = baseCalculationService.calculateTaxDueSlice(npv, leaseholdReliefFrom15PercentRateRightToBuyMixedOnOrAfterMarch2016NPVRates.slices)
    val premiumResult = baseCalculationService.calculateTaxDueSlice(request.premium, leaseholdReliefFrom15PercentRateRightToBuyMixedOnOrAfterMarch2016PremiumRates.slices)
    LeaseholdResultFactory.leaseholdReliefFrom15PercentRateRightToBuyMixedOnOrAfterMarch2016Res(leaseResult, premiumResult, npv)
  }

}
