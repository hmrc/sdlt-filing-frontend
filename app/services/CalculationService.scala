/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package services

import javax.inject.{Inject, Singleton}
import enums.{HoldingTypes, PropertyTypes}
import exceptions.InvalidDateException
import models.{CalculationResponse, PropertyDetails, Request}
import data.Dates
import utils.DateUtil
import utils.CalculationUtils.{freeholdNRSDLTOutOfScope, leaseholdNRSDLTOutOfScopeForLeaseAndPremium}

@Singleton
class CalculationService @Inject()(val leaseCalculationService: LeaseholdCalculationService,
                                   val freeCalculationService: FreeholdCalculationService,
                                   val additionalPropertyService: AdditionalPropertyService) extends DateUtil {

  val MAX_PREMIUM_FTB = 500000

  def CalculateTax(request: Request): CalculationResponse = {
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
    request.effectiveDate match {
      case date if checkNonUKResident(request.nonUKResident) && date.isAfter(Dates.MAR2021_RESIDENTIAL_DATE) =>
        calculateFreeholdNonUKResidentTax(request)
      case date if date.onOrAfter(Dates.JULY2020_RESIDENTIAL_DATE)
        && date.onOrBefore(Dates.MAR2021_RESIDENTIAL_DATE)
        && checkPropDetailsFTB(request.propertyDetails, request.firstTimeBuyer)
        && request.premium <= MAX_PREMIUM_FTB => CalculationResponse(Seq(freeCalculationService.freeholdResidentialJuly20OnwardsFTB(request)))
      case date if date.onOrAfter(Dates.NOV2017_RESIDENTIAL_DATE)
        && checkPropDetailsFTB(request.propertyDetails, request.firstTimeBuyer)
        && request.premium <= MAX_PREMIUM_FTB => CalculationResponse(Seq(freeCalculationService.freeholdResidentialNov17OnwardsFTB(request)))
      case date if date.onOrAfter(Dates.JULY2020_RESIDENTIAL_DATE)
        && date.onOrBefore(Dates.MAR2021_RESIDENTIAL_DATE)
        && additionalPropertyService.additionalPropertyRatesApply(request.propertyDetails) => CalculationResponse(freeCalculationService.freeholdResidentialAddPropJuly20Onwards(request))
      case date if date.onOrAfter(Dates.APRIL2016_RESIDENTIAL_DATE) && additionalPropertyService.additionalPropertyRatesApply(request.propertyDetails) => CalculationResponse(freeCalculationService.freeholdResidentialAddPropApr16Onwards(request))
      case date if date.onOrAfter(Dates.JULY2020_RESIDENTIAL_DATE) && date.onOrBefore(Dates.MAR2021_RESIDENTIAL_DATE) => CalculationResponse(Seq(freeCalculationService.freeholdResidentialJuly20Onwards(request)))
      case date if date.onOrAfter(Dates.DECEMBER2014_RESIDENTIAL_DATE) => CalculationResponse(Seq(freeCalculationService.freeholdResidentialDec14Onwards(request)))
      case date if date.onOrAfter(Dates.MIN_RESIDENTIAL_DATE) => CalculationResponse(Seq(freeCalculationService.freeholdResidentialMar12toDec14(request)))
      case _ => throw new InvalidDateException(s"Date of ${request.effectiveDate} is invalid or before 22/3/2012")
    }
  }

  def calculateFreeholdNonUKResidentTax(request: Request): CalculationResponse = {
    val nonUKResSDLTOutOfScope: Boolean = freeholdNRSDLTOutOfScope(request.premium)
    if (checkPropDetailsFTB(request.propertyDetails, request.firstTimeBuyer) && request.premium <= MAX_PREMIUM_FTB) {
      if(nonUKResSDLTOutOfScope) {
        CalculationResponse(Seq(freeCalculationService.freeholdResidentialNov17OnwardsFTB(request)))
      } else {
        CalculationResponse(freeCalculationService.freeholdResidentialApr21OnwardsFTBNonUKRes(request))
      }
    } else if (additionalPropertyService.additionalPropertyRatesApply(request.propertyDetails)) {
        CalculationResponse(freeCalculationService.freeholdResidentialAddPropNonUKResApr21Onwards(request))
    } else {
      CalculationResponse(freeCalculationService.freeholdResidentialApr21OnwardsNonUKRes(request))
    }
  }

  def calculateFreeholdNonResidentialTax (request: Request): CalculationResponse = {
    if(request.effectiveDate.onOrAfter(Dates.MARCH2016_NON_RESIDENTIAL_DATE)){
      CalculationResponse(freeCalculationService.freeholdNonResidentialMar16Onwards(request))
    }else{
      CalculationResponse(Seq(freeCalculationService.freeholdNonResidentialMar12toMar16(request)))
    }
  }

  def calculateLeaseholdResidentialTax (request: Request): CalculationResponse = {
    request.effectiveDate match {
      case date if checkNonUKResident(request.nonUKResident) && date.isAfter(Dates.MAR2021_RESIDENTIAL_DATE) =>
        calculateLeaseholdNonUKResidentTax(request)
      case date if date.onOrAfter(Dates.JULY2020_RESIDENTIAL_DATE)
        && date.onOrBefore(Dates.MAR2021_RESIDENTIAL_DATE)
        && checkPropDetailsFTB(request.propertyDetails, request.firstTimeBuyer)
        && request.premium <= MAX_PREMIUM_FTB => CalculationResponse(Seq(leaseCalculationService.leaseholdResidentialJuly20OnwardsFTB(request)))
      case date if date.onOrAfter(Dates.NOV2017_RESIDENTIAL_DATE)
        && checkPropDetailsFTB(request.propertyDetails, request.firstTimeBuyer)
        && request.premium <= MAX_PREMIUM_FTB => CalculationResponse(Seq(leaseCalculationService.leaseholdResidentialNov17OnwardsFTB(request)))
      case date if date.onOrAfter(Dates.JULY2020_RESIDENTIAL_DATE)
        && date.onOrBefore(Dates.MAR2021_RESIDENTIAL_DATE)
        && additionalPropertyService.additionalPropertyRatesApply(request.propertyDetails) => CalculationResponse(leaseCalculationService.leaseholdResidentialAddPropJuly20Onwards(request))
      case date if date.onOrAfter(Dates.APRIL2016_RESIDENTIAL_DATE) && additionalPropertyService.additionalPropertyRatesApply(request.propertyDetails) => CalculationResponse(leaseCalculationService.leaseholdResidentialAddPropApr16Onwards(request))
      case date if date.onOrAfter(Dates.JULY2020_RESIDENTIAL_DATE) && date.onOrBefore(Dates.MAR2021_RESIDENTIAL_DATE) => CalculationResponse(Seq(leaseCalculationService.leaseholdResidentialJuly20Onwards(request)))
      case date if date.onOrAfter(Dates.DECEMBER2014_RESIDENTIAL_DATE) =>
        CalculationResponse(Seq(leaseCalculationService.leaseholdResidentialDec14Onwards(request)))
      case date if date.onOrAfter(Dates.MIN_RESIDENTIAL_DATE) => CalculationResponse(Seq(leaseCalculationService.leaseholdResidentialMar12toDec14(request)))
      case _ => throw new InvalidDateException(s"Date of ${request.effectiveDate} is invalid or before 22/3/2012")
    }
  }

  def calculateLeaseholdNonUKResidentTax(request: Request): CalculationResponse = {
    val nonUKResSDLTOutOfScope: Boolean =
      leaseholdNRSDLTOutOfScopeForLeaseAndPremium(request.premium, request.leaseDetails.get.leaseTerm.years, request.highestRent,
        request.firstTimeBuyer.getOrElse(false), request.propertyDetails.get.sharedOwnership.getOrElse(false))

    if (checkPropDetailsFTB(request.propertyDetails, request.firstTimeBuyer) && request.premium <= MAX_PREMIUM_FTB) {
      if(nonUKResSDLTOutOfScope) {
        CalculationResponse(Seq(leaseCalculationService.leaseholdResidentialNov17OnwardsFTB(request)))
      } else {
        CalculationResponse(leaseCalculationService.leaseholdResidentialApr21OnwardsFTBNonUKRes(request))
      }
    } else if (additionalPropertyService.additionalPropertyRatesApply(request.propertyDetails)) {
        CalculationResponse(leaseCalculationService.leaseholdResidentialAddPropApr21OnwardsNonUKRes(request))
    } else {
      CalculationResponse(leaseCalculationService.leaseholdResidentialApr21OnwardsNonUKRes(request))
    }
  }

  def calculateLeaseholdNonResidentialTax (request: Request): CalculationResponse = {
    if(request.effectiveDate.onOrAfter(Dates.MARCH2016_NON_RESIDENTIAL_DATE)){
      CalculationResponse(leaseCalculationService.leaseholdNonResidentialMar16Onwards(request))
    }else{
      CalculationResponse(Seq(leaseCalculationService.leaseholdNonResidentialMar12toMar16(request)))
    }
  }

  def checkPropDetailsFTB(propertyDetails: Option[PropertyDetails], firstTimeBuyer: Option[Boolean]): Boolean = {
    propertyDetails.exists(propDetails =>
      if (propDetails.individual && propDetails.twoOrMoreProperties.contains(false) && firstTimeBuyer.contains(true)) true
      else false
    )
  }

  def checkNonUKResident(nonUKResident: Option[Boolean]): Boolean = nonUKResident.getOrElse(false)
}
