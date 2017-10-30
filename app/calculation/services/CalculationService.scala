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
          case PropertyTypes.residential => freeholdResidential(request)
          case PropertyTypes.nonResidential => freeholdNonResidential(request)
        }

      case HoldingTypes.leasehold =>
        request.propertyType match {
          case PropertyTypes.residential => leaseholdResidential(request)
          case PropertyTypes.nonResidential => leaseholdNonResidential(request)
        }
    }
  }

  def freeholdResidential (request: Request): CalculationResponse = {
    request.effectiveDate match {
      case date if date.isAfter(Dates.APRIL2016_RESIDENTIAL_DATE) =>  CalculationResponse(freeCalculationService.freeholdResidentialAddPropApr16Onwards(request))
      case date if date.isAfter(Dates.DECEMBER2014_RESIDENTIAL_DATE) =>  CalculationResponse(Seq(freeCalculationService.freeholdResidentialDec14Onwards(request)))
      case date if date.isAfter(Dates.MIN_RESIDENTIAL_DATE) => CalculationResponse(Seq(freeCalculationService.freeholdResidentialMar12toDec14(request)))
      case _ => throw new RequiredValueNotDefinedException(s"Date of ${request.effectiveDate} is invalid or before 22/3/2012")
    }
  }

  def freeholdNonResidential (request: Request): CalculationResponse = {
    if(request.effectiveDate.isAfter(LocalDate.of(2016, 3, 17))){
      CalculationResponse(freeCalculationService.freeholdNonResidentialMar16Onwards(request))
    }else{
      CalculationResponse(Seq(freeCalculationService.freeholdNonResidentialMar12toMar16(request)))
    }
  }

  def leaseholdResidential (request: Request): CalculationResponse = ???
  def leaseholdNonResidential (request: Request): CalculationResponse= ???
}
