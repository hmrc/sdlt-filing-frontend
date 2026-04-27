/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package services.taxCalculation

import models.*
import models.land.LandTypeOfProperty
import models.taxCalculation.*

import java.time.*
import java.time.temporal.ChronoUnit
import scala.util.Try

object TaxCalcRequestValidator {

  private val APR2021_RESIDENTIAL_DATE = LocalDate.of(2021, 4, 1)
  private val FIRST_TIME_BUYER_RELIEF  = "32"
  private val GRANT_OF_LEASE           = "L"
  private val YES                      = "YES"
  private val ANNUAL_RENT_THRESHOLD    = 1000

  def buildRequest(userAnswers: UserAnswers): Either[BuildRequestError, SdltCalculationRequest] =
    for {
      fullReturn       <- userAnswers.fullReturn.toRight(MissingFullReturnError)
      land             <- fullReturn.land.flatMap(_.headOption).toRight(MissingAboutTheLandError)
      transaction      <- fullReturn.transaction.toRight(MissingAboutTheTransactionError)
      interestCode     <- land.interestCreatedTransferred.toRight(MissingLandAnswerError("interestCreatedTransferred"))
      propertyCode     <- land.propertyType.toRight(MissingLandAnswerError("propertyType"))
      propertyType     <- PropertyTypes.fromCode(propertyCode).toRight(UnknownPropertyTypeError(propertyCode))
      effectiveDate    <- transaction.effectiveDate.toRight(MissingTransactionAnswerError("effectiveDate"))
      parsedDate       <- Try(LocalDate.parse(effectiveDate)).toOption.toRight(InvalidDateError(effectiveDate))
      premium          <- transaction.totalConsideration.toRight(MissingTransactionAnswerError("totalConsideration"))
      transDesc        <- transaction.transactionDescription.toRight(MissingTransactionAnswerError("transactionDescription"))
      holdingType      <- HoldingTypes.fromCode(transDesc).toRight(UnknownHoldingTypeError(transDesc))
      leaseDetails     <- fullReturn.lease.fold(Right(None))(buildLeaseDetails(_, transaction, parsedDate))
      taxReliefDetails <- getTaxReliefDetails(transaction)
    } yield SdltCalculationRequest(
      holdingType         = holdingType,
      propertyType        = propertyType,
      effectiveDateDay    = parsedDate.getDayOfMonth,
      effectiveDateMonth  = parsedDate.getMonthValue,
      effectiveDateYear   = parsedDate.getYear,
      nonUKResident       = handleNonUkResident(fullReturn.residency, parsedDate, propertyType),
      premium             = premium,
      highestRent         = fullReturn.lease.flatMap(_.startingRent).flatMap(v => Try(BigDecimal(v)).toOption).getOrElse(BigDecimal(0)),
      propertyDetails     = buildPropertyDetails(propertyCode),
      leaseDetails        = leaseDetails,
      relevantRentDetails = fullReturn.lease.map(buildRelevantRentDetails),
      firstTimeBuyer      = Some(if (transaction.reliefReason.contains(FIRST_TIME_BUYER_RELIEF)) "Yes" else "No"),
      isLinked            = transaction.isLinked.map(_.toUpperCase == YES),
      interestTransferred = Some(interestCode),
      taxReliefDetails    = taxReliefDetails,
      isMultipleLand      = fullReturn.land.map(_.size > 1),
      declaredNpv         = fullReturn.lease.flatMap(_.netPresentValue.flatMap(v => Try(BigDecimal(v)).toOption))
    )

  private def getTaxReliefDetails(transaction: Transaction): Either[BuildRequestError, Option[TaxReliefDetails]] =
    transaction.claimingRelief.map(_.toUpperCase) match {
      case Some(YES) =>
        for {
          reliefReason <- transaction.reliefReason.toRight(MissingTransactionAnswerError("reliefReason"))
          reliefCode   <- Try(reliefReason.toInt).toOption.toRight(InvalidReliefReasonError(reliefReason))
        } yield Some(TaxReliefDetails(
          taxReliefCode   = reliefCode,
          isPartialRelief = Some(transaction.reliefAmount.isDefined)
        ))
      case _ => Right(None)
    }

  private def handleNonUkResident(residency: Option[Residency], effectiveDate: LocalDate, propertyType: PropertyTypes.Value): Option[String] =
    if (effectiveDate.isBefore(APR2021_RESIDENTIAL_DATE) && propertyType == PropertyTypes.residential) None
    else residency.flatMap(_.isNonUkResidents).map(_.toLowerCase.capitalize)

  private def buildPropertyDetails(propertyCode: String): Option[PropertyDetails] =
    propertyCode match {
      case LandTypeOfProperty.Additional.toString =>
        Some(PropertyDetails(
          individual           = "Yes",
          twoOrMoreProperties  = Some("Yes"),
          replaceMainResidence = Some("No"),
          sharedOwnership      = None,
          currentValue         = None
        ))
      case _ =>
        Some(PropertyDetails(
          individual           = "No",
          twoOrMoreProperties  = None,
          replaceMainResidence = None,
          sharedOwnership      = None,
          currentValue         = None
        ))
    }

  private def buildLeaseDetails(lease: Lease, transaction: Transaction, effectiveDate: LocalDate): Either[BuildRequestError, Option[LeaseDetails]] =
    for {
      contractStartDate    <- lease.contractStartDate.toRight(MissingLeaseAnswerError("contractStartDate"))
      contractEndDate      <- lease.contractEndDate.toRight(MissingLeaseAnswerError("contractEndDate"))
      validStartDate       <- Try(LocalDate.parse(contractStartDate)).toOption.toRight(InvalidDateError(contractStartDate))
      validEndDate         <- Try(LocalDate.parse(contractEndDate)).toOption.toRight(InvalidDateError(contractEndDate))
      calculationStartDate  = if (effectiveDate.isAfter(validStartDate)) effectiveDate else validStartDate
      years                 = Period.between(calculationStartDate, validEndDate.plusDays(1)).getYears
      partialStart          = calculationStartDate.plusYears(years)
      days                  = ChronoUnit.DAYS.between(partialStart, validEndDate.plusDays(1)).toInt
      daysInPartialYear     = if (years < 5 && days > 0) days else 0
      rentYears             = if (years < 5 && daysInPartialYear > 0) Math.min(years + 1, 5) else Math.min(years, 5)
    } yield Option.when(transaction.transactionDescription.contains(GRANT_OF_LEASE))(
      LeaseDetails(
        startDateDay    = validStartDate.getDayOfMonth,
        startDateMonth  = validStartDate.getMonthValue,
        startDateYear   = validStartDate.getYear,
        endDateDay      = validEndDate.getDayOfMonth,
        endDateMonth    = validEndDate.getMonthValue,
        endDateYear     = validEndDate.getYear,
        leaseTerm       = LeaseTerm(
          years = years,
          days = days,
          daysInPartialYear = daysInPartialYear
        ),
        year1Rent = 0,
        year2Rent = Option.when(rentYears >= 2)(0),
        year3Rent = Option.when(rentYears >= 3)(0),
        year4Rent = Option.when(rentYears >= 4)(0),
        year5Rent = Option.when(rentYears >= 5)(0)
      )
    )

  private def buildRelevantRentDetails(lease: Lease): RelevantRentDetails =
    RelevantRentDetails(
      contractPre201603        = Some("Yes"),
      contractVariedPost201603 = Some("No"),
      relevantRent             =
        if (lease.isAnnualRentOver1000.map(_.toUpperCase).contains(YES)) Some(ANNUAL_RENT_THRESHOLD)
        else Some(BigDecimal(0))
    )
}
