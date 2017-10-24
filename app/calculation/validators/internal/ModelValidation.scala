package calculation.validators.internal

import calculation.enums.{HoldingTypes, PropertyTypes}
import calculation.models.{LeaseDetails, PropertyDetails, Request}
import calculation.data.Dates._

sealed trait ValidationResult
case object  ValidationSuccess              extends ValidationResult
case class   ValidationFailure(err: String) extends ValidationResult

object ModelValidation {

  def listValidationErrors(request: Request): Seq[ValidationResult] = {
    Seq(
      validLeaseDetails(request),
      validEffectiveDate(request),
      validPropertyDetails(request),
      validLeaseTerm(request)
    ).filterNot(_ == ValidationSuccess)
  }

  private def validLeaseDetails(request: Request): ValidationResult = {
    request.holdingType match {
      case HoldingTypes.freehold  => ValidationSuccess
      case HoldingTypes.leasehold =>
        request.leaseDetails.map {
            case LeaseDetails(_, _, _, _, Some(_), Some(_), Some(_), _) => ValidationSuccess
            case LeaseDetails(_, _, _, _, Some(_), Some(_), None, None) => ValidationSuccess
            case LeaseDetails(_, _, _, _, Some(_), None, None, None) => ValidationSuccess
            case LeaseDetails(_, _, _, _, None, None, None, None) => ValidationSuccess
            case _ => ValidationFailure("Lease details have been input incorrectly")

      }.getOrElse(ValidationFailure("No lease details provided for leasehold property"))
    }
  }

  private def validLeaseTerm(request: Request): ValidationResult = {
   request.leaseDetails.map { lease =>
     val rentsList = Seq(Some(lease.year1Rent),
       lease.year2Rent,
       lease.year3Rent,
       lease.year4Rent,
       lease.year5Rent).flatten

     if(lease.leaseTerm.years == rentsList.size || lease.leaseTerm.years > 5 && rentsList.size == 5){
       ValidationSuccess
     }else{
       ValidationFailure(s"Lease term: ${lease.leaseTerm.years} does not match amount of lease year rents: ${rentsList.size}")
     }
   }.getOrElse(ValidationSuccess)
  }

  private def validEffectiveDate(request: Request): ValidationResult = {
    request.propertyType match {
      case PropertyTypes.nonResidential => ValidationSuccess
      case PropertyTypes.residential =>
        if(request.effectiveDate.isBefore(MIN_RESIDENTIAL_DATE)) {
          ValidationFailure(s"Effective date of '${request.effectiveDate}' is before 22 March, 2012")
        } else {
          ValidationSuccess
        }
    }
  }

  private def validPropertyDetails(request: Request): ValidationResult = {
    request.propertyType match {
      case PropertyTypes.nonResidential => ValidationSuccess
      case PropertyTypes.residential =>
        if(request.effectiveDate.isAfter(END_OF_MARCH_2016)) {
          request.propertyDetails.map(validPropertyDetailsStructure).getOrElse(
            ValidationFailure(
              s"No property details for '${request.holdingType}' residential property " +
                s"with effective date of '${request.effectiveDate}'"
            )
          )
        } else {
          ValidationSuccess
        }
    }
  }

  private [validators] def validPropertyDetailsStructure(propertyDetails: PropertyDetails): ValidationResult = {
    propertyDetails match {
      case PropertyDetails(false, _, _) => ValidationSuccess
      case PropertyDetails(true, Some(_), Some(_)) => ValidationSuccess
      case PropertyDetails(true, twoOrMoreProperties, replaceMainResidence) =>
        ValidationFailure(
          s"Property details failed validation with 'individual': true, " +
            s"'twoOrMoreProperties': $twoOrMoreProperties, " +
            s"'replaceMainResidence': $replaceMainResidence")
    }
  }
}
