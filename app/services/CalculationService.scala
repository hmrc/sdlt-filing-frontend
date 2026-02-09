/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package services

import data.Dates
import enums.HoldingTypes._
import enums.PropertyTypes._
import enums.sdltRebuild.TaxReliefCode.{selfAssessedFreeHoldReliefCodes, standardZeroRateFreeholdReliefCodes, standardZeroRateLeaseholdReliefCodes}
import enums.sdltRebuild._
import enums.{HoldingTypes, PropertyTypes}
import exceptions.{InvalidDateException, InvalidTaxReliefCombinationException, RequiredValueNotDefinedException}
import models.sdltRebuild.TaxReliefDetails
import models.{CalculationResponse, LeaseDetails, PropertyDetails, Request}
import utils.CalculationUtils.{duringNRB250HolidayPeriod, duringNRB500HolidayPeriod, freeholdNRSDLTOutOfScope, isAfterApr2013AndBeforeDec2014, isAfterMar2008AndBeforeMar2016, isAfterMar2010AndBeforeMar2012, isAfterOct2024AndBeforeApril2025, isAfterSep2022AndBeforeOct24, isAfterSept2022AndBeforeApril2025, leaseholdNRSDLTOutOfScope}
import utils.DateUtil
import utils.LoggerUtil._

import javax.inject.{Inject, Singleton}

@Singleton
class CalculationService @Inject()(val leaseCalculationService: LeaseholdCalculationService,
                                   val freeCalculationService: FreeholdCalculationService,
                                   val additionalPropertyService: AdditionalPropertyService) extends DateUtil {

  def calculateTax(request: Request): CalculationResponse = {
    (request.holdingType, request.propertyType, request.taxReliefDetails, request.isLinked) match {
      case (_, _, Some(taxReliefDetails), _) => calculateTaxRelief(request, taxReliefDetails)
      case (_, _, None, Some(_)) => calculateTaxNoRelief(request)
      case (`leasehold`, `mixed`, _, _) => calculateLeaseholdMixedPropertyTax(request)
      case _ =>
        (request.holdingType, request.propertyType) match {
          case (HoldingTypes.leasehold, PropertyTypes.residential) => calculateLeaseholdResidentialTax(request)
          case (HoldingTypes.leasehold, PropertyTypes.nonResidential) => calculateLeaseholdNonResidentialTax(request)
          case (HoldingTypes.freehold, PropertyTypes.residential) => calculateFreeholdResidentialTax(request)
          case (HoldingTypes.freehold, PropertyTypes.nonResidential) => calculateFreeholdNonResidentialTax(request)
          case _ => throw new RequiredValueNotDefinedException("Value not defined")
        }
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

  def calculateTaxRelief(request: Request,
                         taxReliefDetails: TaxReliefDetails): CalculationResponse = {

    // Comment (to be removed)
    // Property types from the AS-IS design are:
    // residential, nonResidential, mixed and residentialAdditionalProperty

    val isAdditionalProperty =
      request.propertyDetails.exists(_.twoOrMoreProperties.contains(true))

    (request.holdingType, request.propertyType, isAdditionalProperty, taxReliefDetails.taxReliefCode, request.isLinked) match {
      /* ------------- FreeHoldCases--------------------------- */
      case (`freehold`, _, _, CollectiveEnfranchisementByLeaseholders, _)
        if request.effectiveDate.onOrAfter(Dates.APRIL2009_EFFECTIVE_DATE) =>
        CalculationResponse(Seq(
          freeCalculationService.freeholdSelfAssessedRes
        ))
      case (`freehold`, _, _, FreeportsTaxSiteRelief | InvestmentZonesTaxSiteRelief, Some(false))
        if taxReliefDetails.isPartialRelief.contains(true) =>
          CalculationResponse(Seq(
            freeCalculationService.freeholdSelfAssessedRes
          ))
      case (`freehold`, _, _, FreeportsTaxSiteRelief | InvestmentZonesTaxSiteRelief, Some(false)) =>
        CalculationResponse(Seq(
          freeCalculationService.freeholdZeroRateTaxReliefRes
        ))

      case (`freehold`, `mixed` | `nonResidential`, _ , RightToBuy, Some(false)) if request.effectiveDate.isBefore(Dates.MARCH2016_NON_RESIDENTIAL_DATE) =>
        CalculationResponse(Seq(
          freeCalculationService.freeholdRightToBuyBeforeMarch2016(request)
        ))

      case (`freehold`, _, _, taxReliefCode, Some(false))
        if standardZeroRateFreeholdReliefCodes.contains(taxReliefCode) =>
        CalculationResponse(Seq(
            freeCalculationService.freeholdZeroRateTaxReliefRes
          ))
      case (`freehold`, _, _, AcquisitionRelief, Some(false)) =>
        CalculationResponse(Seq(
          freeCalculationService.freeholdAcquisitionTaxRelief(request)
        ))
      case (`freehold`, _, _, AcquisitionRelief, Some(true)) if
        request.effectiveDate.isBefore(Dates.DECEMBER2014_RESIDENTIAL_DATE) =>
        CalculationResponse(Seq(
          freeCalculationService.freeholdSelfAssessedRes
        ))
      case (`freehold`, `residential`, true, PreCompletionTransaction, Some(false))
        if request.effectiveDate.onOrAfter(Dates.APRIL2016_RESIDENTIAL_DATE) =>
          CalculationResponse(Seq(
            freeCalculationService.freeholdZeroRateTaxReliefRes
          ))
      case (`freehold`, `residential`|`mixed`|`nonResidential`, false, PreCompletionTransaction, Some(false))
        if request.effectiveDate.onOrAfter(Dates.APRIL2013_TAX_YEAR_START_DATE) =>
          CalculationResponse(Seq(
            freeCalculationService.freeholdZeroRateTaxReliefRes
          ))
      case (`freehold`, `residential`, true, RightToBuy, Some(true))
        if request.effectiveDate.onOrAfter(Dates.APRIL2016_RESIDENTIAL_DATE) =>
          CalculationResponse(Seq(
            freeCalculationService.freeholdSelfAssessedRes
          ))
      case (`freehold`, `residential`, false, ReliefFrom15PercentRate, Some(true))
        if isAfterApr2013AndBeforeDec2014(request.effectiveDate) =>
          CalculationResponse(Seq(
            freeCalculationService.freeholdSelfAssessedRes
          ))
      case (`freehold`, _, _, taxReliefCode, Some(true)) if selfAssessedFreeHoldReliefCodes.contains(taxReliefCode) && request.effectiveDate.isBefore(Dates.DECEMBER2014_RESIDENTIAL_DATE) =>
        CalculationResponse(Seq(freeCalculationService.freeholdSelfAssessedRes))

      case (`freehold`, `mixed` | `nonResidential`, false , RightToBuy, Some(true))
        if request.effectiveDate.isBefore(Dates.MARCH2016_NON_RESIDENTIAL_DATE) =>
        CalculationResponse(Seq(
          freeCalculationService.freeholdSelfAssessedRes
        ))

      /* ------------- LeaseHoldCases--------------------------- */
      case (`leasehold`, _, _, CollectiveEnfranchisementByLeaseholders, _)
        if request.effectiveDate.onOrAfter(Dates.APRIL2009_EFFECTIVE_DATE) =>
        CalculationResponse(Seq(
          leaseCalculationService.leaseholdSelfAssessedRes
        ))
      case (`leasehold`, _, _, FreeportsTaxSiteRelief | InvestmentZonesTaxSiteRelief, Some(false))
        if taxReliefDetails.isPartialRelief.contains(true) =>
          CalculationResponse(Seq(
            leaseCalculationService.leaseholdSelfAssessedRes
          ))
      case (`leasehold`, _, _, FreeportsTaxSiteRelief | InvestmentZonesTaxSiteRelief, Some(false)) =>
        CalculationResponse(Seq(
          leaseCalculationService.leaseholdZeroRateTaxReliefRes(request.leaseDetails)
        ))
      case (`leasehold`, `residential`, false, FirstTimeBuyersRelief, _)
        if isAfterMar2010AndBeforeMar2012(request.effectiveDate) =>
          CalculationResponse(Seq(
            leaseCalculationService.leaseholdSelfAssessedRes
          ))
      case (`leasehold`, _, _, AcquisitionRelief, Some(false)) =>
        CalculationResponse(Seq(
          leaseCalculationService.leaseholdAcquisitionTaxReliefRes(request)
        ))
      case(`leasehold`, `residential`, true, PreCompletionTransaction, Some(false))
        if request.effectiveDate.onOrAfter(Dates.APRIL2016_RESIDENTIAL_DATE) =>
          CalculationResponse(Seq(
            leaseCalculationService.leaseholdZeroRateTaxReliefRes(request.leaseDetails)
          ))
      case (`leasehold`, `residential`|`mixed`|`nonResidential`, false, PreCompletionTransaction, Some(false))
        if request.effectiveDate.onOrAfter(Dates.APRIL2013_TAX_YEAR_START_DATE) =>
          CalculationResponse(Seq(
            leaseCalculationService.leaseholdZeroRateTaxReliefRes(request.leaseDetails)
          ))
      case (`leasehold`, `mixed`|`nonResidential`, false, _, Some(false))
        if standardZeroRateLeaseholdReliefCodes.contains(taxReliefDetails.taxReliefCode) =>
          CalculationResponse(Seq(
            leaseCalculationService.leaseholdZeroRateTaxReliefRes(request.leaseDetails)
          ))
      case (_, `mixed`, _, _, _) =>
        throw new InvalidTaxReliefCombinationException(s"taxReliefCode: ${taxReliefDetails.taxReliefCode} does not apply to Mixed properties")
      case (holdingType, propertyType, isAdditionalProperty, taxReliefCode, isLinked) =>
        logWarn(s"TaxRelief logic not yet implemented for" +
          s"taxReliefCode: $taxReliefCode, holdingType: $holdingType, propertyType: $propertyType, " +
          s"isAdditionalProperty: $isAdditionalProperty, isLinked: $isLinked")
        calculateTax(request.copy(taxReliefDetails = None))
    }
  }

  def calculateTaxNoRelief(request: Request): CalculationResponse = {

    (request.holdingType, request.propertyType, request.isLinked) match {
      /* ------------- FreeHoldCases--------------------------- */
      case (`freehold`, `mixed` | `nonResidential`, Some(true))
      if request.effectiveDate.onOrAfter(Dates.MARCH2016_NON_RESIDENTIAL_DATE) =>
        CalculationResponse(Seq(
          freeCalculationService.freeholdSelfAssessedRes
        ))
      case (`freehold`, `mixed` | `nonResidential`, Some(true))
        if request.effectiveDate.isBefore(Dates.MAR2017_EFFECTIVE_DATE) =>
        CalculationResponse(Seq(
          freeCalculationService.freeholdSelfAssessedRes
        ))
      case (`freehold`, `mixed`, _) =>
        throw new InvalidDateException(
          s"Effective date: ${request.effectiveDate} currently does not apply to ${request.holdingType}, ${request.propertyType} properties with isLinked = ${request.isLinked}")

      /* ------------- LeaseHoldCases--------------------------- */
      case (`leasehold`, _, Some(true))
        if request.effectiveDate.onOrAfter(Dates.NOV2017_RESIDENTIAL_DATE) =>
        CalculationResponse(Seq(
          leaseCalculationService.leaseholdSelfAssessedRes
        ))
      case _ =>
        calculateTax(request.copy(isLinked = None, taxReliefDetails = None))
    }
  }

  def calculateLeaseholdMixedPropertyTax(request: Request): CalculationResponse = {
    if (isAfterMar2008AndBeforeMar2016(request.effectiveDate)) {
      CalculationResponse(Seq(
        leaseCalculationService.leaseholdSelfAssessedRes
      ))
    } else {
      throw new InvalidDateException(s"Date of ${request.effectiveDate} is outside of range ${Dates.MIN_MIXED_PROPERTY_DATE} - ${Dates.MARCH2016_NON_RESIDENTIAL_DATE}")
    }
  }
}
