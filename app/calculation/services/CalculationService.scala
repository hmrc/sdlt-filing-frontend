package calculation.services

import java.time.LocalDate
import javax.inject.{Inject, Singleton}

import calculation.enums.{HoldingTypes, PropertyTypes}
import calculation.exceptions.InvalidDateException
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

  implicit class DateHelper(dt: LocalDate) {
    def onOrAfter(compDate: LocalDate): Boolean = {
      dt.isAfter(compDate) || dt.isEqual(compDate)
    }
  }

  def calculateFreeholdResidentialTax (request: Request): CalculationResponse = {
    request.effectiveDate match {
      case date if date.onOrAfter(Dates.APRIL2016_RESIDENTIAL_DATE) =>  CalculationResponse(freeCalculationService.freeholdResidentialAddPropApr16Onwards(request))
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
      case date if date.onOrAfter(Dates.APRIL2016_RESIDENTIAL_DATE) =>  CalculationResponse(leaseCalculationService.leaseholdResidentialAddPropApr16Onwards(request))
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
}
