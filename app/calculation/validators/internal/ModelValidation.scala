package calculation.validators.internal

import java.time.{LocalDate, Period}

import calculation.data.Dates
import calculation.enums.{HoldingTypes, PropertyTypes}
import calculation.models.{LeaseDetails, PropertyDetails, RelevantRentDetails, Request}
import calculation.data.Dates._
import calculation.data.SignificantAmounts.RELEVANT_RENT_PREMIUM_THRESHOLD
import calculation.utils.DateUtil

sealed trait ValidationResult
case object  ValidationSuccess              extends ValidationResult
case class   ValidationFailure(err: String) extends ValidationResult

object ModelValidation extends DateUtil{
  def listValidationErrors(request: Request): Seq[ValidationFailure] = {
    Seq(
      validLeaseDetails(request),
      validEffectiveDate(request),
      validPropertyDetails(request),
      validLeaseTerm(request),
      validRelevantRentDetails(request),
      validFirstTimeBuyer(request)
    ).flatMap {
      case err :ValidationFailure => Some(err)
      case _ => None
    }.distinct
  }

  private[validators] def validLeaseDetails(request: Request): ValidationResult = {
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

   private[validators] def validLeaseTerm(request: Request): ValidationResult = {
     request.leaseDetails.map { lease =>
      val rentsList = Seq(Some(lease.year1Rent),
       lease.year2Rent,
       lease.year3Rent,
       lease.year4Rent,
       lease.year5Rent).flatten

     val fullYears = lease.leaseTerm.years
     val yearsRequired = if(fullYears < 5 && lease.leaseTerm.daysInPartialYear > 0) fullYears + 1 else fullYears

     if(yearsRequired == rentsList.size || yearsRequired > 5 && rentsList.size == 5){
       validLeaseLength(request.effectiveDate, lease)
       }else{
       ValidationFailure(s"Lease term: ${lease.leaseTerm.years} does not match amount of lease year rents: ${rentsList.size} and ${lease.leaseTerm.daysInPartialYear} partial days")
     }
   }.getOrElse(ValidationSuccess)
  }

  private[validators] def validLeaseLength(effectiveDate: LocalDate, leaseDetails: LeaseDetails) : ValidationResult ={
    // ??? Necessary ??? val validStartDate = if(effectiveDate.isAfter(leaseDetails.startDate)) effectiveDate else leaseDetails.startDate
    val comparisonDate = effectiveDate.plus(Period.of(leaseDetails.leaseTerm.years, 0, leaseDetails.leaseTerm.days-1))
    if(comparisonDate.equals(leaseDetails.endDate))
      ValidationSuccess
    else
      ValidationFailure(s"Lease term year: ${leaseDetails.leaseTerm.years} or Lease term date: ${leaseDetails.leaseTerm.days} does not match the difference between $effectiveDate and ${leaseDetails.endDate}")
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
      case PropertyDetails(true, Some(false), _) => ValidationSuccess
      case PropertyDetails(true, Some(true), Some(_)) => ValidationSuccess
      case PropertyDetails(true, twoOrMoreProperties, replaceMainResidence) =>
        ValidationFailure(
          s"Property details failed validation with 'individual': true, " +
            s"'twoOrMoreProperties': $twoOrMoreProperties, " +
            s"'replaceMainResidence': $replaceMainResidence")
    }
  }

  private [validators] def validFirstTimeBuyer(request: Request): ValidationResult ={
    if(request.effectiveDate.isBetween(Dates.NOV2017_RESIDENTIAL_DATE,Dates.NOV2019_RESIDENTIAL_DATE) && request.propertyType.equals(PropertyTypes.residential)){
      request.propertyDetails.map{ propDetails =>
        if(propDetails.individual && propDetails.twoOrMoreProperties.contains(false)){
          request.firstTimeBuyer match{
            case Some(_) => ValidationSuccess
            case _ => ValidationFailure(s"First time buyer was not defined.")
          }
        } else ValidationSuccess
      }getOrElse ValidationFailure("No property details found for first time buyer.")
    }else ValidationSuccess
  }

  private [validators] def validRelevantRentDetails(request: Request): ValidationResult = {
    request.holdingType match {
      case HoldingTypes.freehold => ValidationSuccess
      case HoldingTypes.leasehold => request.propertyType match {
        case PropertyTypes.residential => ValidationSuccess
        case PropertyTypes.nonResidential =>
          if(request.premium >= RELEVANT_RENT_PREMIUM_THRESHOLD)
            ValidationSuccess
          else
            request.leaseDetails.map { leaseDetails =>
              if(allRentsBelow2000(leaseDetails))
                request.relevantRentDetails.map(validRelevantRentDetailsStructure(_, request.effectiveDate)).getOrElse(
                  ValidationFailure(s"Relevant rent details not provided when premium: ${request.premium}, " +
                    s"holding type: leasehold, property type: non-residential and all rents <£2000")
                )
              else
                ValidationSuccess
            }.getOrElse {
              ValidationFailure("No lease details provided for leasehold property")
            }
      }
    }
  }

  def allRentsBelow2000(leaseDetails: LeaseDetails): Boolean = {
    !Seq(
      Some(leaseDetails.year1Rent),
      leaseDetails.year2Rent,
      leaseDetails.year3Rent,
      leaseDetails.year4Rent,
      leaseDetails.year5Rent
    ).flatten.exists(_ >= 2000)
  }

  private[validators] def validRelevantRentDetailsStructure(relRentDetails: RelevantRentDetails, effectiveDate: LocalDate): ValidationResult = {
    if(effectiveDate.isBefore(MARCH2016_NON_RESIDENTIAL_DATE))
      relRentDetails.relevantRent.map(_ => ValidationSuccess)
        .getOrElse(ValidationFailure("No relevant rent amount provided"))
    else
      relRentDetails match {
          case RelevantRentDetails(Some(false), _, _)                => ValidationSuccess
          case RelevantRentDetails(Some(true), Some(true), _)        => ValidationSuccess
          case RelevantRentDetails(Some(true), Some(false), Some(_)) => ValidationSuccess
          case RelevantRentDetails(exchangedBefore, contractChanged, relRent) =>
            ValidationFailure(
              s"Relevant Rent details failed validation with " +
                s"'exchangedContractsBeforeMar16': $exchangedBefore, " +
                s"'contractChangedSinceMar16': $contractChanged, " +
                s"'relevantRent': $relRent")
      }
  }
}
