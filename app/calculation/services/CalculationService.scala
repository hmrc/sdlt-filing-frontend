package calculation.services

import javax.inject.{Inject, Singleton}

import calculation.enums.{HoldingTypes, PropertyTypes}
import calculation.exceptions.InvalidDateException
import calculation.models.{CalculationResponse, PropertyDetails, Request}
import calculation.data.Dates
import calculation.utils.DateUtil

@Singleton
class CalculationService @Inject()(
                                    val leaseCalculationService: LeaseholdCalculationService,
                                    val freeCalculationService: FreeholdCalculationService,
                                    val additionalPropertyService: AdditionalPropertyService
                                  ) extends CalculationSrv

trait CalculationSrv extends DateUtil{

  val leaseCalculationService: LeaseholdCalculationSrv
  val freeCalculationService: FreeholdCalculationSrv
  val additionalPropertyService: AdditionalPropertySrv

  def CalculateTax(request: Request): CalculationResponse ={
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

  def calculateFreeholdResidentialTax (request: Request): CalculationResponse = {
    request.effectiveDate match {
      case date if date.isBetween(Dates.NOV2017_RESIDENTIAL_DATE, Dates.NOV2019_RESIDENTIAL_DATE) && checkPropDetailsFTB(request.propertyDetails, request.firstTimeBuyer) =>  CalculationResponse(Seq(freeCalculationService.freeholdResidentialNov17OnwardsFTB(request)))
      case date if date.onOrAfter(Dates.APRIL2016_RESIDENTIAL_DATE) && additionalPropertyService.additionalPropertyRatesApply(request.propertyDetails) =>  CalculationResponse(freeCalculationService.freeholdResidentialAddPropApr16Onwards(request))
      case date if date.onOrAfter(Dates.DECEMBER2014_RESIDENTIAL_DATE) =>  CalculationResponse(Seq(freeCalculationService.freeholdResidentialDec14Onwards(request)))
      case date if date.onOrAfter(Dates.MIN_RESIDENTIAL_DATE) => CalculationResponse(Seq(freeCalculationService.freeholdResidentialMar12toDec14(request)))
      case _ => throw new InvalidDateException(s"Date of ${request.effectiveDate} is invalid or before 22/3/2012")
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
      case date if date.isBetween(Dates.NOV2017_RESIDENTIAL_DATE, Dates.NOV2019_RESIDENTIAL_DATE)  && checkPropDetailsFTB(request.propertyDetails, request.firstTimeBuyer) =>  CalculationResponse(Seq(leaseCalculationService.leaseholdResidentialNov17OnwardsFTB(request)))
      case date if date.onOrAfter(Dates.APRIL2016_RESIDENTIAL_DATE) && additionalPropertyService.additionalPropertyRatesApply(request.propertyDetails) =>  CalculationResponse(leaseCalculationService.leaseholdResidentialAddPropApr16Onwards(request))
      case date if date.onOrAfter(Dates.DECEMBER2014_RESIDENTIAL_DATE) =>  CalculationResponse(Seq(leaseCalculationService.leaseholdResidentialDec14Onwards(request)))
      case date if date.onOrAfter(Dates.MIN_RESIDENTIAL_DATE) => CalculationResponse(Seq(leaseCalculationService.leaseholdResidentialMar12toDec14(request)))
      case _ => throw new InvalidDateException(s"Date of ${request.effectiveDate} is invalid or before 22/3/2012")
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
}
