/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package services

import javax.inject.{Inject, Singleton}
import enums.{HoldingTypes, PropertyTypes}
import exceptions.InvalidDateException
import models.{CalculationResponse, LeaseDetails, PropertyDetails, Request}
import data.Dates
import utils.DateUtil
import utils.CalculationUtils.{duringNRB250HolidayPeriod, duringNRB500HolidayPeriod, freeholdNRSDLTOutOfScope, leaseholdNRSDLTOutOfScope}

import java.time.LocalDate

@Singleton
class CalculationService @Inject()(val leaseCalculationService: LeaseholdCalculationService,
                                   val freeCalculationService: FreeholdCalculationService,
                                   val additionalPropertyService: AdditionalPropertyService) extends DateUtil {

  def calculateTax(request: Request): CalculationResponse = {
    request.holdingType match {
      case HoldingTypes.freehold  =>
        request.propertyType match {
          case PropertyTypes.residential => calculateFreeholdResidentialTax(request)
          case PropertyTypes.nonResidential => calculateFreeholdNonResidentialTax(request)
        }

      case HoldingTypes.leasehold =>
        request.propertyType match {
          case PropertyTypes.residential => calculateLeaseholdResidentialTax(request)
          case PropertyTypes.nonResidential => calculateLeaseholdNonResidentialTax(request)
        }
    }
  }

  def calculateFreeholdResidentialTax(request: Request): CalculationResponse = {

    val premium = request.premium

    request.effectiveDate match {
      case date if nonUKResident(request.nonUKResident) && date.isAfter(Dates.MAR2021_RESIDENTIAL_DATE) =>
        calculateFreeholdNonUKResidentTax(request)
      case date if isAfterSept2022AndBeforeApil2025(date) &&
        checkFTBHigherThreshold(request.propertyDetails, request.firstTimeBuyer, premium) =>
        CalculationResponse(Seq(freeCalculationService.freeholdResidentialSept22OnwardsFTB(request)))
      case date if date.onOrAfter(Dates.NOV2017_RESIDENTIAL_DATE) &&
        checkFTB(request.propertyDetails, request.firstTimeBuyer, premium) =>
        CalculationResponse(Seq(freeCalculationService.freeholdResidentialNov17OnwardsFTB(request)))
      case date if duringNRB500HolidayPeriod(date) &&
        additionalPropertyService.additionalPropertyRatesApply(
          request.premium, request.propertyDetails, extractLeaseTerm(request.leaseDetails)) =>
        CalculationResponse(freeCalculationService.freeholdResidentialAddPropJuly20Onwards(request))
      case date if (duringNRB250HolidayPeriod(date) || isAfterSept2022AndBeforeApil2025(date)) &&
        additionalPropertyService.additionalPropertyRatesApply(
          request.premium, request.propertyDetails, extractLeaseTerm(request.leaseDetails)) =>
        CalculationResponse(freeCalculationService.freeholdResidentialAddPropJuly21Onwards(request))
      case date if date.onOrAfter(Dates.APRIL2016_RESIDENTIAL_DATE) &&
        additionalPropertyService.additionalPropertyRatesApply(
          request.premium, request.propertyDetails, extractLeaseTerm(request.leaseDetails)) =>
        CalculationResponse(freeCalculationService.freeholdResidentialAddPropApr16Onwards(request))
      case date if duringNRB500HolidayPeriod(date) =>
        CalculationResponse(Seq(freeCalculationService.freeholdResidentialJuly20Onwards(request)))
      case date if duringNRB250HolidayPeriod(date) || isAfterSept2022AndBeforeApil2025(date) =>
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
    } else if (isAfterSept2022AndBeforeApil2025(request.effectiveDate)) {
      if (checkFTBHigherThreshold(request.propertyDetails, request.firstTimeBuyer, request.premium)) {
        if (!freeholdNRSDLTOutOfScope(request.premium)) {
          CalculationResponse(freeCalculationService.freeholdResidentialSept22OnwardsFTBNonUKRes(request))
        } else {
          CalculationResponse(Seq(freeCalculationService.freeholdResidentialSept22OnwardsFTB(request)))
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
          CalculationResponse(freeCalculationService.freeholdResidentialAddPropNonUKResOct21Onwards(request))
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
      case date if isAfterSept2022AndBeforeApil2025(date) &&
        checkFTBHigherThreshold(request.propertyDetails, request.firstTimeBuyer, premium) =>
        CalculationResponse(Seq(leaseCalculationService.leaseholdResidentialSept22OnwardsFTB(request)))
      case date if duringNRB250HolidayPeriod(date) &&
        checkFTB(request.propertyDetails, request.firstTimeBuyer, premium) =>
        CalculationResponse(Seq(leaseCalculationService.leaseholdResidentialJuly21OnwardsFTB(request)))
      case date if date.onOrAfter(Dates.NOV2017_RESIDENTIAL_DATE) &&
        checkFTB(request.propertyDetails, request.firstTimeBuyer, premium) =>
          CalculationResponse(Seq(leaseCalculationService.leaseholdResidentialNov17OnwardsFTB(request)))
      case date if (duringNRB250HolidayPeriod(date) || isAfterSept2022AndBeforeApil2025(date)) &&
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
      case date if duringNRB250HolidayPeriod(date) || isAfterSept2022AndBeforeApil2025(date) =>
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
    } else if (isAfterSept2022AndBeforeApil2025(request.effectiveDate)) {
        if (additionalPropertyService.additionalPropertyRatesApply(
          request.premium, request.propertyDetails, extractLeaseTerm(request.leaseDetails))) {
          if (!nonUKResSDLTOutOfScope) {
            CalculationResponse(leaseCalculationService.leaseholdResidentialJuly21OnwardsNonUKResAddProp(request))
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
          CalculationResponse(leaseCalculationService.leaseholdResidentialOct21OnwardsNonUKResAddProp(request))
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

  def isAfterSept2022AndBeforeApil2025(date: LocalDate) = {
    date.onOrAfter(Dates.SEPT2022_RESIDENTIAL_DATE) && date.isBefore(Dates.APRIL2025_RESIDENTIAL_DATE)
  }

}
