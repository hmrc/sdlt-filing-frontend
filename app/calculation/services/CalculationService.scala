package calculation.services

import java.time.LocalDate
import javax.inject.{Inject, Singleton}

import calculation.enums.{HoldingTypes, PropertyTypes}
import calculation.exceptions.RequiredValueNotDefinedException
import calculation.models.{CalculationResponse, Request}
import calculation.data.Dates

@Singleton
class CalculationService @Inject()(
                                    val leaseCalculationService: LeaseholdCalculationService,
                                    val freeCalculationService: FreeholdCalculationService
                                  ) extends CalculationSrv

trait CalculationSrv{

  val leaseCalculationService: LeaseholdCalculationSrv
  val freeCalculationService: FreeholdCalculationSrv

  def selectCalculationFunction(request: Request): CalculationResponse ={
    request.holdingType match {
      case HoldingTypes.freehold  =>
        request.propertyType match {
          case PropertyTypes.residential => freeholdResidentialSelector(request)
          case PropertyTypes.nonResidential => freeholdNonResidentialSelector(request)
        }

      case HoldingTypes.leasehold =>
        request.propertyType match {
          case PropertyTypes.residential => leaseholdResidentialSelector(request)
          case PropertyTypes.nonResidential => leaseholdNonResidentialSelector(request)
        }
    }
  }

  def freeholdResidentialSelector (request: Request): CalculationResponse = {
    request.effectiveDate match {
      case date if compareDates(date, Dates.APRIL2016_RESIDENTIAL_DATE) =>  CalculationResponse(freeCalculationService.freeholdResidentialAddPropApr16Onwards(request))
      case date if compareDates(date, Dates.DECEMBER2014_RESIDENTIAL_DATE) =>  CalculationResponse(Seq(freeCalculationService.freeholdResidentialDec14Onwards(request)))
      case date if compareDates(date, Dates.MIN_RESIDENTIAL_DATE) => CalculationResponse(Seq(freeCalculationService.freeholdResidentialMar12toDec14(request)))
      case _ => throw new RequiredValueNotDefinedException(s"Date of ${request.effectiveDate} is invalid or before 22/3/2012")
    }
  }

  def freeholdNonResidentialSelector (request: Request): CalculationResponse = {
    if(compareDates(request.effectiveDate, Dates.MARCH2016_NON_RESIDENTIAL_DATE)){
      CalculationResponse(freeCalculationService.freeholdNonResidentialMar16Onwards(request))
    }else{
      CalculationResponse(Seq(freeCalculationService.freeholdNonResidentialMar12toMar16(request)))
    }
  }

  def leaseholdResidentialSelector (request: Request): CalculationResponse = {
    request.effectiveDate match {
      case date if compareDates(date, Dates.APRIL2016_RESIDENTIAL_DATE) =>  CalculationResponse(leaseCalculationService.leaseholdResidentialAddPropApr16Onwards(request))
      case date if compareDates(date, Dates.DECEMBER2014_RESIDENTIAL_DATE) =>  CalculationResponse(Seq(leaseCalculationService.leaseholdResidentialDec14Onwards(request)))
      case date if compareDates(date, Dates.MIN_RESIDENTIAL_DATE) => CalculationResponse(Seq(leaseCalculationService.leaseholdResidentialMar12toDec14(request)))
      case _ => throw new RequiredValueNotDefinedException(s"Date of ${request.effectiveDate} is invalid or before 22/3/2012")
    }
  }
  def leaseholdNonResidentialSelector (request: Request): CalculationResponse = {
    if(compareDates(request.effectiveDate, Dates.MARCH2016_NON_RESIDENTIAL_DATE)){
      CalculationResponse(leaseCalculationService.leaseholdNonResidentialMar16Onwards(request))
    }else{
      CalculationResponse(Seq(leaseCalculationService.leaseholdNonResidentialMar12toMar16(request)))
    }
  }

  def compareDates(effectiveDate: LocalDate, lawDate: LocalDate): Boolean ={
    if(effectiveDate.isAfter(lawDate) || effectiveDate.isEqual(lawDate)) true else false
  }
}
