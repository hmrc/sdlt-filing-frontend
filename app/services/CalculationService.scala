/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package services

import data.Dates
import data.Dates._
import enums.HoldingTypes._
import enums.PropertyTypes._
import enums.sdltRebuild.TaxReliefCode.{selfAssessedFreeHoldReliefCodes, selfAssessedLeaseHoldReliefCodes, standardZeroRateFreeholdReliefCodes, standardZeroRateLeaseholdReliefCodes}
import enums.sdltRebuild._
import enums.{HoldingTypes, PropertyTypes}
import exceptions.{InvalidDateException, RequiredValueNotDefinedException}
import models.sdltRebuild.EffectivePropertyType._
import models.sdltRebuild.{Mixed, NonResidential, Residential, ResidentialAdditionalProperty}
import models.{CalculationResponse, LeaseDetails, PropertyDetails, Request}
import utils.CalculationUtils.{averageRentIsBelowThreshold, duringNRB250HolidayPeriod, duringNRB500HolidayPeriod, freeholdNRSDLTOutOfScope, isAfter22Mar2012AndBefore25Mar2012, isAfterApr2013AndBeforeDec2014, isAfterMar2008AndBeforeMar2016, isAfterMar2010AndBeforeMar2012, isAfterMar2012AndBeforeDec2014, isAfterNov2017AndBeforeJul20, isAfterOct2024AndBeforeApril2025, isAfterSep2022AndBeforeOct24, isAfterSept2022AndBeforeApril2025, leaseholdNRSDLTOutOfScope, maximumThreshold, premiumIsGreaterThan500K}
import utils.DateUtil
import utils.LoggerUtil._
import data.Premium.MAX_PREMIUM_FTB

import javax.inject.{Inject, Singleton}

@Singleton
class CalculationService @Inject()(val leaseCalculationService: LeaseholdCalculationService,
                                   val freeCalculationService: FreeholdCalculationService,
                                   val additionalPropertyService: AdditionalPropertyService) extends DateUtil {

  def calculateTax(request: Request): CalculationResponse = {
    if(isComplexCalculation(request)){
      calculateTaxRelief(request)
    } else {
      calculateBaseTax(request)
    }
  }

  private def calculateBaseTax(request: Request): CalculationResponse = {
    (request.holdingType, request.propertyType) match {
      case (HoldingTypes.leasehold, PropertyTypes.residential) => calculateLeaseholdResidentialTax(request)
      case (HoldingTypes.leasehold,                         _) => calculateLeaseholdNonResidentialTax(request)
      case (HoldingTypes.freehold,  PropertyTypes.residential) => calculateFreeholdResidentialTax(request)
      case (HoldingTypes.freehold,                          _) => calculateFreeholdNonResidentialTax(request)
      case _ => throw new RequiredValueNotDefinedException("Value not defined")
    }
  }

  def calculateFreeholdResidentialTax(request: Request): CalculationResponse = {

    val premium = request.premium
    request.effectiveDate match {
      case date if nonUKResident(request.nonUKResident) && date.isAfter(Dates.MAR2021_RESIDENTIAL_DATE) =>
        calculateFreeholdNonUKResidentTax(request)
      case date if isAfterSept2022AndBeforeApril2025(date) &&
        checkFTBHigherThreshold(request.propertyDetails, request.firstTimeBuyer, premium) =>
        CalculationResponse(Seq(freeCalculationService.freeholdResidentialSept22OnwardsFTB(request)))
      case date if date.onOrAfter(Dates.NOV2017_RESIDENTIAL_DATE) &&
        checkFTB(request.propertyDetails, request.firstTimeBuyer, premium) =>
        CalculationResponse(Seq(freeCalculationService.freeholdResidentialNov17OnwardsFTB(request)))
      case date if duringNRB500HolidayPeriod(date) &&
        additionalPropertyService.additionalPropertyRatesApply(
          request.premium, request.propertyDetails, extractLeaseTerm(request.leaseDetails)) =>
        CalculationResponse(freeCalculationService.freeholdResidentialAddPropJuly20Onwards(request))
      case date if isAfterOct2024AndBeforeApril2025(date) &&
        additionalPropertyService.additionalPropertyRatesApply(
          request.premium, request.propertyDetails, extractLeaseTerm(request.leaseDetails)) =>
        CalculationResponse(freeCalculationService.freeholdResidentialAddPropOct24BeforeApril25(request))
      case date if date.onOrAfter(Dates.APRIL2025_RESIDENTIAL_DATE) &&
        additionalPropertyService.additionalPropertyRatesApply(
          request.premium, request.propertyDetails, extractLeaseTerm(request.leaseDetails)) =>
        CalculationResponse(freeCalculationService.freeholdResidentialAddPropApril25Onwards(request))
      case date if (duringNRB250HolidayPeriod(date) || isAfterSep2022AndBeforeOct24(date)) &&
        additionalPropertyService.additionalPropertyRatesApply(
          request.premium, request.propertyDetails, extractLeaseTerm(request.leaseDetails)) =>
        CalculationResponse(freeCalculationService.freeholdResidentialAddPropJuly21Onwards(request))
      case date if date.onOrAfter(Dates.APRIL2016_RESIDENTIAL_DATE) &&
        additionalPropertyService.additionalPropertyRatesApply(
          request.premium, request.propertyDetails, extractLeaseTerm(request.leaseDetails)) =>
        CalculationResponse(freeCalculationService.freeholdResidentialAddPropApr16Onwards(request))
      case date if duringNRB500HolidayPeriod(date) =>
        CalculationResponse(Seq(freeCalculationService.freeholdResidentialJuly20Onwards(request)))
      case date if duringNRB250HolidayPeriod(date) || isAfterSept2022AndBeforeApril2025(date) =>
        CalculationResponse(Seq(freeCalculationService.freeholdResidentialJuly21Onwards(request)))
      case date if date.onOrAfter(Dates.DECEMBER2014_RESIDENTIAL_DATE) =>
        CalculationResponse(Seq(freeCalculationService.freeholdResidentialDec14Onwards(request)))
      case date if date.onOrAfter(Dates.MIN_RESIDENTIAL_DATE) =>
        CalculationResponse(Seq(freeCalculationService.freeholdResidentialMar12toDec14(request)))
      case _ =>
        throw new InvalidDateException(s"Date of ${request.effectiveDate} is invalid or before 22/3/2012")
    }
  }

  def calculateFreeholdNonUKResidentTax(request: Request): CalculationResponse = {

    if (duringNRB500HolidayPeriod(request.effectiveDate)) {
      if (additionalPropertyService.additionalPropertyRatesApply(
          request.premium, request.propertyDetails, extractLeaseTerm(request.leaseDetails))) {
        if (!freeholdNRSDLTOutOfScope(request.premium)) {
          CalculationResponse(freeCalculationService.freeholdResidentialAddPropNonUKResApril21Onwards(request))
        } else {
          CalculationResponse(freeCalculationService.freeholdResidentialAddPropJuly20Onwards(request))
        }
      } else {
        if (!freeholdNRSDLTOutOfScope(request.premium)) {
          CalculationResponse(freeCalculationService.freeholdResidentialApril21OnwardsNonUKRes(request))
        } else {
          CalculationResponse(Seq(freeCalculationService.freeholdResidentialJuly20Onwards(request)))
        }
      }
    } else if (duringNRB250HolidayPeriod(request.effectiveDate)) {
      if (checkFTB(request.propertyDetails, request.firstTimeBuyer, request.premium)) {
        if (!freeholdNRSDLTOutOfScope(request.premium)) {
          CalculationResponse(freeCalculationService.freeholdResidentialJuly21OnwardsFTBNonUKRes(request))
        } else {
          CalculationResponse(Seq(freeCalculationService.freeholdResidentialNov17OnwardsFTB(request)))
        }
      } else if (additionalPropertyService.additionalPropertyRatesApply(
          request.premium, request.propertyDetails, extractLeaseTerm(request.leaseDetails))) {
        if (!freeholdNRSDLTOutOfScope(request.premium)) {
          CalculationResponse(freeCalculationService.freeholdResidentialAddPropNonUKResJuly21Onwards(request))
        } else {
          CalculationResponse(freeCalculationService.freeholdResidentialAddPropJuly21Onwards(request))
        }
      } else {
        if (!freeholdNRSDLTOutOfScope(request.premium)) {
          CalculationResponse(freeCalculationService.freeholdResidentialJuly21OnwardsNonUKRes(request))
        } else {
          CalculationResponse(Seq(freeCalculationService.freeholdResidentialJuly21Onwards(request)))
        }
      }
    } else if (isAfterSept2022AndBeforeApril2025(request.effectiveDate)) {
      if (checkFTBHigherThreshold(request.propertyDetails, request.firstTimeBuyer, request.premium)) {
        if (!freeholdNRSDLTOutOfScope(request.premium)) {
          CalculationResponse(freeCalculationService.freeholdResidentialSept22OnwardsFTBNonUKRes(request))
        } else {
          CalculationResponse(Seq(freeCalculationService.freeholdResidentialSept22OnwardsFTB(request)))
        }
      } else if (additionalPropertyService.additionalPropertyRatesApply(
        request.premium, request.propertyDetails, extractLeaseTerm(request.leaseDetails))) {
        if (!freeholdNRSDLTOutOfScope(request.premium)) {
          if (isAfterOct2024AndBeforeApril2025(request.effectiveDate)) {
            CalculationResponse(freeCalculationService.freeholdResidentialAddPropNonUKResOct24BeforeApril25(request))
          } else {
            CalculationResponse(freeCalculationService.freeholdResidentialAddPropNonUKResJuly21Onwards(request))
          }
        } else {
          CalculationResponse(freeCalculationService.freeholdResidentialAddPropJuly21Onwards(request))
        }
      } else {
        if (!freeholdNRSDLTOutOfScope(request.premium)) {
          CalculationResponse(freeCalculationService.freeholdResidentialJuly21OnwardsNonUKRes(request))
        } else {
          CalculationResponse(Seq(freeCalculationService.freeholdResidentialJuly21Onwards(request)))
        }
      }
    } else {
      if (checkFTB(request.propertyDetails, request.firstTimeBuyer, request.premium)) {
        if (!freeholdNRSDLTOutOfScope(request.premium)) {
          CalculationResponse(freeCalculationService.freeholdResidentialOct21OnwardsFTBNonUKRes(request))
        } else {
          CalculationResponse(Seq(freeCalculationService.freeholdResidentialNov17OnwardsFTB(request)))
        }
      } else if (additionalPropertyService.additionalPropertyRatesApply(
          request.premium, request.propertyDetails, extractLeaseTerm(request.leaseDetails))) {
        if (!freeholdNRSDLTOutOfScope(request.premium)) {
          if (request.effectiveDate.onOrAfter(Dates.APRIL2025_RESIDENTIAL_DATE)) {
            CalculationResponse(freeCalculationService.freeholdResidentialAddPropNonUKResApril25Onwards(request))
          } else {
            CalculationResponse(freeCalculationService.freeholdResidentialAddPropNonUKResOct21Onwards(request))
          }
        } else {
          CalculationResponse(freeCalculationService.freeholdResidentialAddPropApr16Onwards(request))
        }
      } else {
        if (!freeholdNRSDLTOutOfScope(request.premium)) {
          CalculationResponse(freeCalculationService.freeholdResidentialOct21OnwardsNonUKRes(request))
        } else {
          CalculationResponse(Seq(freeCalculationService.freeholdResidentialDec14Onwards(request)))
        }
      }
    }
  }

  def calculateFreeholdNonResidentialTax(request: Request): CalculationResponse = {
    if(request.effectiveDate.onOrAfter(Dates.MARCH2016_NON_RESIDENTIAL_DATE)){
      CalculationResponse(freeCalculationService.freeholdNonResidentialMar16Onwards(request))
    }else{
      CalculationResponse(Seq(freeCalculationService.freeholdNonResidentialMar12toMar16(request)))
    }
  }

  def calculateLeaseholdResidentialTax(request: Request): CalculationResponse = {
    val premium = request.premium

    request.effectiveDate match {
      case date if nonUKResident(request.nonUKResident) && date.isAfter(Dates.MAR2021_RESIDENTIAL_DATE) =>
        calculateLeaseholdNonUKResidentTax(request)
      case date if isAfterSept2022AndBeforeApril2025(date) &&
        checkFTBHigherThreshold(request.propertyDetails, request.firstTimeBuyer, premium) =>
        CalculationResponse(Seq(leaseCalculationService.leaseholdResidentialSept22OnwardsFTB(request)))
      case date if duringNRB250HolidayPeriod(date) &&
        checkFTB(request.propertyDetails, request.firstTimeBuyer, premium) =>
        CalculationResponse(Seq(leaseCalculationService.leaseholdResidentialJuly21OnwardsFTB(request)))
      case date if date.onOrAfter(Dates.NOV2017_RESIDENTIAL_DATE) &&
        checkFTB(request.propertyDetails, request.firstTimeBuyer, premium) =>
          CalculationResponse(Seq(leaseCalculationService.leaseholdResidentialNov17OnwardsFTB(request)))
      case date if isAfterOct2024AndBeforeApril2025(date) &&
        additionalPropertyService.additionalPropertyRatesApply(
          request.premium, request.propertyDetails, extractLeaseTerm(request.leaseDetails)) =>
        CalculationResponse(leaseCalculationService.leaseholdResidentialAddPropOct24BeforeApril25(request))
      case date if date.onOrAfter(Dates.APRIL2025_RESIDENTIAL_DATE) &&
        additionalPropertyService.additionalPropertyRatesApply(
          request.premium, request.propertyDetails, extractLeaseTerm(request.leaseDetails)) =>
        CalculationResponse(leaseCalculationService.leaseholdResidentialAddPropApril25Onwards(request))
      case date if (duringNRB250HolidayPeriod(date) || isAfterSep2022AndBeforeOct24(date)) &&
        additionalPropertyService.additionalPropertyRatesApply(
          request.premium, request.propertyDetails, extractLeaseTerm(request.leaseDetails)) =>
        CalculationResponse(leaseCalculationService.leaseholdResidentialAddPropJuly21Onwards(request))
      case date if duringNRB500HolidayPeriod(date) &&
        additionalPropertyService.additionalPropertyRatesApply(
          request.premium, request.propertyDetails, extractLeaseTerm(request.leaseDetails)) =>
          CalculationResponse(leaseCalculationService.leaseholdResidentialAddPropJuly20Onwards(request))
      case date if date.onOrAfter(Dates.APRIL2016_RESIDENTIAL_DATE) &&
        additionalPropertyService.additionalPropertyRatesApply(
          request.premium, request.propertyDetails, extractLeaseTerm(request.leaseDetails)) =>
        CalculationResponse(leaseCalculationService.leaseholdResidentialAddPropApr16Onwards(request))
      case date if duringNRB500HolidayPeriod(date) =>
        CalculationResponse(Seq(leaseCalculationService.leaseholdResidentialJuly20Onwards(request)))
      case date if duringNRB250HolidayPeriod(date) || isAfterSept2022AndBeforeApril2025(date) =>
        CalculationResponse(Seq(leaseCalculationService.leaseholdResidentialJuly21Onwards(request)))
      case date if date.onOrAfter(Dates.DECEMBER2014_RESIDENTIAL_DATE) =>
        CalculationResponse(Seq(leaseCalculationService.leaseholdResidentialDec14Onwards(request)))
      case date if date.onOrAfter(Dates.MIN_RESIDENTIAL_DATE) =>
        CalculationResponse(Seq(leaseCalculationService.leaseholdResidentialMar12toDec14(request)))
      case _ => throw new InvalidDateException(s"Date of ${request.effectiveDate} is invalid or before 22/3/2012")
    }
  }

  def calculateLeaseholdNonUKResidentTax(request: Request): CalculationResponse = {
    val nonUKResSDLTOutOfScope: Boolean = leaseholdNRSDLTOutOfScope(
      request.premium, request.leaseDetails.get.leaseTerm.years, request.highestRent,
      request.firstTimeBuyer.getOrElse(false), request.propertyDetails.get.sharedOwnership.getOrElse(false)
    )

    if (duringNRB250HolidayPeriod(request.effectiveDate)) {
      if (additionalPropertyService.additionalPropertyRatesApply(
          request.premium, request.propertyDetails, extractLeaseTerm(request.leaseDetails))) {
        if (!nonUKResSDLTOutOfScope) {
          CalculationResponse(leaseCalculationService.leaseholdResidentialJuly21OnwardsNonUKResAddProp(request))
        } else {
          CalculationResponse(leaseCalculationService.leaseholdResidentialAddPropJuly21Onwards(request))
        }
      } else if (checkFTB(request.propertyDetails, request.firstTimeBuyer, request.premium)) {
        if (!nonUKResSDLTOutOfScope) {
          CalculationResponse(leaseCalculationService.leaseholdResidentialJuly21OnwardsFTBNonUKRes(request))
        } else {
          CalculationResponse(Seq(leaseCalculationService.leaseholdResidentialJuly21OnwardsFTB(request)))
        }
      } else {
        if(!nonUKResSDLTOutOfScope) {
          CalculationResponse(leaseCalculationService.leaseholdResidentialJuly21OnwardsNonUKRes(request))
        } else {
          CalculationResponse(Seq(leaseCalculationService.leaseholdResidentialJuly21Onwards(request)))
        }
      }
    } else if (duringNRB500HolidayPeriod(request.effectiveDate)) {
      if (additionalPropertyService.additionalPropertyRatesApply(
        request.premium, request.propertyDetails, extractLeaseTerm(request.leaseDetails))) {
        if (!nonUKResSDLTOutOfScope) {
          CalculationResponse(leaseCalculationService.leaseholdResidentialApr21OnwardsNonUKResAddProp(request))
        } else {
          CalculationResponse(leaseCalculationService.leaseholdResidentialAddPropJuly20Onwards(request))
        }
      } else {
        if (!nonUKResSDLTOutOfScope) {
          CalculationResponse(leaseCalculationService.leaseholdResidentialApr21OnwardsNonUKRes(request))
        } else {
          CalculationResponse(Seq(leaseCalculationService.leaseholdResidentialJuly20Onwards(request)))
        }
      }
    } else if (isAfterSept2022AndBeforeApril2025(request.effectiveDate)) {
        if (additionalPropertyService.additionalPropertyRatesApply(
          request.premium, request.propertyDetails, extractLeaseTerm(request.leaseDetails))) {
          if (!nonUKResSDLTOutOfScope) {
            if(isAfterOct2024AndBeforeApril2025(request.effectiveDate)){
              CalculationResponse(leaseCalculationService.leaseholdResidentialOct24BeforeApr25NonUKResAddProp(request))
            } else {
              CalculationResponse(leaseCalculationService.leaseholdResidentialJuly21OnwardsNonUKResAddProp(request))
            }
          } else {
            CalculationResponse(leaseCalculationService.leaseholdResidentialAddPropJuly21Onwards(request))
          }
        } else if (checkFTBHigherThreshold(request.propertyDetails, request.firstTimeBuyer, request.premium)) {
          if (!nonUKResSDLTOutOfScope) {
            CalculationResponse(leaseCalculationService.leaseholdResidentialSept22OnwardsFTBNonUKRes(request))
          } else {
            CalculationResponse(Seq(leaseCalculationService.leaseholdResidentialSept22OnwardsFTB(request)))
          }
        } else {
          if(!nonUKResSDLTOutOfScope) {
            CalculationResponse(leaseCalculationService.leaseholdResidentialJuly21OnwardsNonUKRes(request))
          } else {
            CalculationResponse(Seq(leaseCalculationService.leaseholdResidentialJuly21Onwards(request)))
          }
        }
      } else {
      if (checkFTB(request.propertyDetails, request.firstTimeBuyer, request.premium)) {
        if (!nonUKResSDLTOutOfScope) {
          CalculationResponse(leaseCalculationService.leaseholdResidentialOct21OnwardsFTBNonUKRes(request))
        } else {
          CalculationResponse(Seq(leaseCalculationService.leaseholdResidentialNov17OnwardsFTB(request)))
        }
      } else if (additionalPropertyService.additionalPropertyRatesApply(
          request.premium, request.propertyDetails, extractLeaseTerm(request.leaseDetails))) {
        if (!nonUKResSDLTOutOfScope) {
          if(request.effectiveDate.onOrAfter(Dates.APRIL2025_RESIDENTIAL_DATE)){
            CalculationResponse(leaseCalculationService.leaseholdResidentialApr25OnwardsNonUKResAddProp(request))
          }else {
            CalculationResponse(leaseCalculationService.leaseholdResidentialOct21OnwardsNonUKResAddProp(request))
          }
        } else {
          CalculationResponse(leaseCalculationService.leaseholdResidentialAddPropApr16Onwards(request))
        }
      } else {
        if(!nonUKResSDLTOutOfScope) {
          CalculationResponse(leaseCalculationService.leaseholdResidentialOct21OnwardsNonUKRes(request))
        } else {
          CalculationResponse(Seq(leaseCalculationService.leaseholdResidentialDec14Onwards(request)))
        }
      }
    }
  }

  def calculateLeaseholdNonResidentialTax (request: Request): CalculationResponse = {
    if(request.effectiveDate.onOrAfter(Dates.MARCH2016_NON_RESIDENTIAL_DATE)){
      CalculationResponse(leaseCalculationService.leaseholdNonResidentialMar16Onwards(request))
    }else{
      CalculationResponse(Seq(leaseCalculationService.leaseholdNonResidentialMar12toMar16(request)))
    }
  }

  def checkFTB(propertyDetails: Option[PropertyDetails], firstTimeBuyer: Option[Boolean], premium: BigDecimal): Boolean = {
    val MAX_PREMIUM_FTB = 500000

    propertyDetails.exists(propDetails =>
      propDetails.individual && propDetails.twoOrMoreProperties.contains(false) && firstTimeBuyer.contains(true) && premium <= MAX_PREMIUM_FTB
    )
  }

  def checkFTBHigherThreshold(propertyDetails: Option[PropertyDetails], firstTimeBuyer: Option[Boolean], premium: BigDecimal): Boolean = {
    val MAX_PREMIUM_FTB = 625000

    propertyDetails.exists(propDetails =>
      propDetails.individual && propDetails.twoOrMoreProperties.contains(false) && firstTimeBuyer.contains(true) && premium <= MAX_PREMIUM_FTB
    )
  }

  def nonUKResident(nonUKResident: Option[Boolean]): Boolean = nonUKResident.getOrElse(false)

  def extractLeaseTerm(leaseDetails: Option[LeaseDetails]): Option[Int] = {
    leaseDetails.fold[Option[Int]](None)(leaseDets => Some(leaseDets.leaseTerm.years))
  }

  def calculateTaxRelief(request: Request): CalculationResponse = {

    val date = request.effectiveDate

    request.taxReliefDetails match {
      case _ if request.interestTransferred.contains("OT") => calculateTaxForOtherInterestTransferred(request)
      case None => calculateTaxNoRelief(request)
      case Some(taxReliefDetails) =>
        (request.holdingType, effectivePropertyType(request), taxReliefDetails.taxReliefCode, request.isLinked) match {
          /* ------------- FreeHoldCases--------------------------- */
          case (`freehold`, _, MultipleDwellingRelief, _) =>
            CalculationResponse(Seq(
              freeCalculationService.freeholdMultipleDwellingRelief
            ))
          case (`freehold`, _, CollectiveEnfranchisementByLeaseholders, _)
            if date.onOrAfter(APRIL2009_EFFECTIVE_DATE) =>
            CalculationResponse(Seq(
              freeCalculationService.freeholdCollectiveEnfranchisementByLeaseholdersReliefAfterApr09
            ))
          case (`freehold`, _, FreeportsTaxSiteRelief | InvestmentZonesTaxSiteRelief, Some(false))
            if taxReliefDetails.isPartialRelief.contains(true) =>
            CalculationResponse(Seq(
              freeCalculationService.freeholdFreeportPartialRelief
            ))
          case (`freehold`, _, FreeportsTaxSiteRelief | InvestmentZonesTaxSiteRelief, Some(false))
            if taxReliefDetails.isPartialRelief.contains(false) =>
            CalculationResponse(Seq(
              freeCalculationService.freeholdFreeportRelief
            ))
          case (`freehold`, Mixed | NonResidential, RightToBuy, Some(false))
            if date.isBefore(MARCH2016_NON_RESIDENTIAL_DATE) =>
            CalculationResponse(Seq(
              freeCalculationService.freeholdRightToBuyBeforeMarch2016(request)
            ))
          case (`freehold`, _, taxReliefCode, Some(false))
            if standardZeroRateFreeholdReliefCodes.contains(taxReliefCode) =>
            CalculationResponse(Seq(
              freeCalculationService.freeholdStandardZeroRateTaxRelief
            ))
          case (`freehold`, _, AcquisitionRelief, Some(false)) =>
            CalculationResponse(Seq(
              freeCalculationService.freeholdAcquisitionTaxRelief(request)
            ))
          case (`freehold`, _, AcquisitionRelief, Some(true))
            if date.isBefore(DECEMBER2014_RESIDENTIAL_DATE) =>
            CalculationResponse(Seq(
              freeCalculationService.freeholdAcquisitionReliefBeforeDec2014
            ))
          case (`freehold`, Residential, FirstTimeBuyersRelief, Some(true))
            if isAfterNov2017AndBeforeJul20(date) && request.isMultipleLand.contains(false) && maximumThreshold(request.premium, MAX_PREMIUM_FTB) =>
            CalculationResponse(Seq(
              freeCalculationService.freeholdResidentialReliefFirstTimeBuyersReliefAfterNov2017AndBeforeJul2020
            ))
          case (`freehold`, Residential, FirstTimeBuyersRelief, Some(true))
            if isAfter22Mar2012AndBefore25Mar2012(date) =>
            CalculationResponse(Seq(
              freeCalculationService.freeholdResidentialFTB22Mar12Before25Mar12
            ))
          case (`freehold`, _, AcquisitionRelief, Some(true))
            if date.onOrAfter(DECEMBER2014_RESIDENTIAL_DATE) =>
            CalculationResponse(Seq(
              freeCalculationService.freeholdAcquisitionReliefDec14Onwards
            ))
          case (`freehold`, ResidentialAdditionalProperty, PreCompletionTransaction, Some(false))
            if date.onOrAfter(APRIL2016_RESIDENTIAL_DATE) =>
            CalculationResponse(Seq(
              freeCalculationService.freeholdResidentialAddPropPreCompletionTransactionReliefApr16Onwards
            ))
          case (`freehold`, Residential | NonResidential | Mixed, PreCompletionTransaction, Some(false))
            if date.onOrAfter(APRIL2013_TAX_YEAR_START_DATE) =>
            CalculationResponse(Seq(
              freeCalculationService.freeholdPreCompletionTransactionReliefApr13Onwards
            ))
          case (`freehold`, Residential, RightToBuy, Some(true))
            if date.onOrAfter(DECEMBER2014_RESIDENTIAL_DATE) =>
            CalculationResponse(Seq(
              freeCalculationService.freeholdResidentialRightToBuyDec14Onwards
            ))
          case (`freehold`, NonResidential | Mixed, RightToBuy, Some(true))
            if date.isBefore(MARCH2016_NON_RESIDENTIAL_DATE) =>
            CalculationResponse(Seq(
              freeCalculationService.freeholdMixedNonResRightToBuyReliefBeforeMar16
            ))
          case (`freehold`, NonResidential | Mixed, RightToBuy, Some(true))
            if date.onOrAfter(MARCH2016_NON_RESIDENTIAL_DATE) =>
            CalculationResponse(Seq(
              freeCalculationService.freeholdMixedNonResRightToBuyReliefMar16Onwards
            ))
          case (`freehold`, ResidentialAdditionalProperty, RightToBuy, Some(true))
            if date.onOrAfter(APRIL2016_RESIDENTIAL_DATE) =>
            CalculationResponse(Seq(
              freeCalculationService.freeholdResAddPropRightToBuyReliefApr16Onwards
            ))
          case (`freehold`, Residential, RightToBuy, Some(true))
            if isAfterMar2012AndBeforeDec2014(date) =>
            CalculationResponse(Seq(
              freeCalculationService.freeholdResidentialRightToBuyMar12ToDec14
            ))
          case (`freehold`, Residential, ReliefFrom15PercentRate, Some(true))
            if isAfterApr2013AndBeforeDec2014(date) =>
            CalculationResponse(Seq(
              freeCalculationService.freeholdResidentialReliefFrom15PercentRateApr13BeforeDec14
            ))
          case (`freehold`, Residential, ReliefFrom15PercentRate, Some(true))
            if date.onOrAfter(DECEMBER2014_RESIDENTIAL_DATE) =>
            CalculationResponse(Seq(
              freeCalculationService.freeholdResidentialReliefFrom15PercentRateDec14Onwards
            ))
          case (`freehold`, ResidentialAdditionalProperty, ReliefFrom15PercentRate, Some(true))
            if date.onOrAfter(APRIL2016_RESIDENTIAL_DATE) =>
            CalculationResponse(Seq(
              freeCalculationService.freeholdResAddPropReliefFrom15PercentRateApr16Onwards
            ))
          case (`freehold`, Mixed | NonResidential, ReliefFrom15PercentRate, Some(true))
            if date.isBefore(MARCH2016_NON_RESIDENTIAL_DATE) =>
            CalculationResponse(Seq(
              freeCalculationService.freeHoldReliefFrom15PercentRateBefore17March2016
            ))
          case (`freehold`, Mixed | NonResidential, ReliefFrom15PercentRate, Some(false))
            if date.isBefore(MARCH2016_NON_RESIDENTIAL_DATE) =>
            calculateBaseTax(request)
          case (`freehold`, _, taxReliefCode, Some(true))
            if selfAssessedFreeHoldReliefCodes.contains(taxReliefCode) && date.isBefore(DECEMBER2014_RESIDENTIAL_DATE) =>
            CalculationResponse(Seq(
              freeCalculationService.freeholdStandardSelfAssessedReliefBeforeDec14
            ))
          case (`freehold`, _, taxReliefCode, Some(true))
            if selfAssessedFreeHoldReliefCodes.contains(taxReliefCode) && date.onOrAfter(DECEMBER2014_ANY_PROP_DATE) =>
            CalculationResponse(Seq(
              freeCalculationService.freeholdSelfAssessedOnOrAfterDecember2014
            ))
          case (`freehold`, Mixed | NonResidential | Residential, PreCompletionTransaction, Some(true))
            if date.onOrAfter(APRIL2013_TAX_YEAR_START_DATE) =>
              CalculationResponse(Seq(
                freeCalculationService.freeholdSelfAssessedOnOrAfterApril2013
              ))
          case (`freehold`, Residential, FirstTimeBuyersRelief, Some(true))
            if date.onOrAfter(JULY2020_RESIDENTIAL_DATE) =>
            CalculationResponse(Seq(
              freeCalculationService.freeholdResidentialFirstTimeBuyer500kMax
            ))
          case (`freehold`, Residential, FirstTimeBuyersRelief, Some(true))
            if !premiumIsGreaterThan500K(request.premium) && isAfterNov2017AndBeforeJul20(date) && request.isMultipleLand.contains(true)=>
              CalculationResponse(Seq(
                freeCalculationService.freeholdSelfAssessedResidentialFirstTimeBuyer500kMax
              ))

          /* ------------- LeaseHoldCases--------------------------- */
          case (`leasehold`, _, MultipleDwellingRelief, _) =>
            CalculationResponse(Seq(
              leaseCalculationService.leaseholdMultipleDwellingRelief
            ))
          case (`leasehold`, _, CollectiveEnfranchisementByLeaseholders, _)
            if date.onOrAfter(APRIL2009_EFFECTIVE_DATE) =>
            CalculationResponse(Seq(
              leaseCalculationService.leaseholdCollectiveEnfranchisementByLeaseholdersApr09Onwards
            ))
          case (`leasehold`, _, FreeportsTaxSiteRelief | InvestmentZonesTaxSiteRelief, Some(false))
            if taxReliefDetails.isPartialRelief.contains(true) =>
            CalculationResponse(Seq(
              leaseCalculationService.leaseholdFreeportPartialRelief
            ))
          case (`leasehold`, _, FreeportsTaxSiteRelief | InvestmentZonesTaxSiteRelief, Some(false))
            if taxReliefDetails.isPartialRelief.contains(false) =>
            CalculationResponse(Seq(
              leaseCalculationService.leaseholdFreeportRelief(request.leaseDetails)
            ))
          case (`leasehold`, Residential, FirstTimeBuyersRelief, Some(true))
            if isAfterNov2017AndBeforeJul20(date) && request.isMultipleLand.contains(true) =>
            CalculationResponse(Seq(
              leaseCalculationService.leaseholdResidentialFTBWithMultipleLands
            ))
          case (`leasehold`, Residential, FirstTimeBuyersRelief, _)
            if isAfterMar2010AndBeforeMar2012(date) =>
            CalculationResponse(Seq(
              leaseCalculationService.leaseholdResFTBReliefMar10BeforeMar12
            ))
          case (`leasehold`, Residential, FirstTimeBuyersRelief, Some(true))
            if date.onOrAfter(JULY2020_RESIDENTIAL_DATE) =>
            CalculationResponse(Seq(
              leaseCalculationService.leaseholdResidentialFTBOnOrAfterJul2020
            ))
          case (`leasehold`, _, AcquisitionRelief, Some(false)) =>
            CalculationResponse(Seq(
              leaseCalculationService.leaseholdAcquisitionTaxReliefRes(request)
            ))
          case (`leasehold`, ResidentialAdditionalProperty, PreCompletionTransaction, Some(false))
            if date.onOrAfter(APRIL2016_RESIDENTIAL_DATE) =>
            CalculationResponse(Seq(
              leaseCalculationService.leaseholdResAddPropPreCompletionTransactionApr2016Onwards(request.leaseDetails)
            ))
          case (`leasehold`, Residential | NonResidential | Mixed, PreCompletionTransaction, Some(false))
            if date.onOrAfter(APRIL2013_TAX_YEAR_START_DATE) =>
            CalculationResponse(Seq(
              leaseCalculationService.leaseholdPreCompletionTransactionApr2013Onwards(request.leaseDetails)
            ))
          case (`leasehold`, Mixed | NonResidential, _, Some(false))
            if standardZeroRateLeaseholdReliefCodes.contains(taxReliefDetails.taxReliefCode) =>
            CalculationResponse(Seq(
              leaseCalculationService.leaseholdMixedNonResPropStandardZeroRelief(request.leaseDetails)
            ))
          case (`leasehold`, _, taxReliefCode, Some(true))
            if selfAssessedLeaseHoldReliefCodes.contains(taxReliefCode) && date.onOrAfter(NOV2017_RESIDENTIAL_DATE) =>
            CalculationResponse(Seq(
              leaseCalculationService.leaseholdSelfAssessedOnOrAfterNov2017
            ))
          case (`leasehold`, Residential, FirstTimeBuyersRelief, Some(true))
            if !premiumIsGreaterThan500K(request.premium) && request.isMultipleLand.contains(false) && isAfterNov2017AndBeforeJul20(date) =>
            CalculationResponse(Seq(
              leaseCalculationService.leaseholdResFTBAfterNov17AndBeforeJul20upTo500k
            ))
          case (`leasehold`, Residential, FirstTimeBuyersRelief, Some(true))
            if premiumIsGreaterThan500K(request.premium) && request.isMultipleLand.contains(false) && isAfterNov2017AndBeforeJul20(date) =>
            CalculationResponse(Seq(
              leaseCalculationService.leaseholdResFTBAfterNov17AndBeforeJul20greaterThan500k
            ))
          case (holdingType, _, taxReliefCode, isLinked) =>
            logWarn(s"Falling back to Non-Tax Relief cases as TaxRelief logic not yet implemented for " +
              s"$taxReliefCode, holdingType: $holdingType, propertyType: ${effectivePropertyType(request)}, isLinked: $isLinked")
            calculateTaxNoRelief(request)
        }
    }
  }

  def calculateTaxNoRelief(request: Request): CalculationResponse = {
    val date = request.effectiveDate

    (request.holdingType, effectivePropertyType(request), request.isLinked) match {
      /* ------------- FreeHoldCases--------------------------- */
      case (`freehold`, Mixed | NonResidential, Some(true))
        if date.onOrAfter(MARCH2016_NON_RESIDENTIAL_DATE) =>
        CalculationResponse(Seq(
          freeCalculationService.freeholdMixedNonResMar16Onwards
        ))
      case (`freehold`, Mixed | NonResidential, Some(true))
        if date.isBefore(MARCH2016_NON_RESIDENTIAL_DATE) =>
        CalculationResponse(Seq(
          freeCalculationService.freeholdMixedNonResBeforeMar16
        ))
      case (`freehold`, Residential, Some(true))
        if isAfterMar2012AndBeforeDec2014(date) =>
        CalculationResponse(Seq(
          freeCalculationService.freeholdResidentialMar12BeforeDec14
        ))
      case (`freehold`, Residential, Some(true))
        if date.onOrAfter(DECEMBER2014_RESIDENTIAL_DATE) =>
        CalculationResponse(Seq(
          freeCalculationService.freeholdResidentialAfterDec14
        ))
      case (`freehold`, ResidentialAdditionalProperty, Some(true))
        if date.onOrAfter(APRIL2016_RESIDENTIAL_DATE) =>
        CalculationResponse(Seq(
          freeCalculationService.freeholdResidentialAddPropAprOnwards
        ))
      case (`freehold`, Mixed | NonResidential, Some(false))
        if date.isBefore(MARCH2016_NON_RESIDENTIAL_DATE) =>
        calculateBaseTax(request)
      /* ------------- LeaseHoldCases--------------------------- */
      case (`leasehold`, _, Some(true))
        if date.onOrAfter(NOV2017_EFFECTIVE_DATE) =>
        CalculationResponse(Seq(
          leaseCalculationService.leaseholdNov17Onwards
        ))
      case (`leasehold`, _, Some(true))
        if date.onOrAfter(NOV2017_RESIDENTIAL_DATE) =>
        CalculationResponse(Seq(
          leaseCalculationService.leaseholdNov17Onwards
        ))

      case (`leasehold`, Mixed | NonResidential, Some(false))
        if request.effectiveDate.isBefore(Dates.MARCH_17_2016_DATE) && request.relevantRentDetails.exists(averageRentIsBelowThreshold) =>
          CalculationResponse(Seq(
            leaseCalculationService.leaseholdMixedNonResBeforeMarch2016(request)
          ))

      case (`leasehold`, Mixed, _)
        if isAfterMar2008AndBeforeMar2016(date) =>
        CalculationResponse(Seq(
          leaseCalculationService.leaseholdMixedPropMar08BeforeMar16
        ))

      case _ =>
        logWarn(s"Falling back to Standard tax cases as Non-TaxRelief logic not yet implemented for " +
          s"holdingType: ${request.holdingType}, propertyType: ${effectivePropertyType(request)}, isLinked: ${request.isLinked}")
        calculateBaseTax(request)
    }
  }

  private def calculateTaxForOtherInterestTransferred(request: Request): CalculationResponse =
    CalculationResponse(Seq(
      if (request.holdingType == freehold) freeCalculationService.freeholdOtherInterestTransferred
      else                                 leaseCalculationService.leaseholdOtherInterestTransferred
    ))

  private def isComplexCalculation(request: Request): Boolean =
    request.taxReliefDetails.nonEmpty ||
      request.isLinked.nonEmpty ||
      request.interestTransferred.nonEmpty ||
      request.propertyType == mixed ||
      request.isMultipleLand.nonEmpty
}
