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
import utils.DateTimeFormats.*

import java.time.*
import java.time.temporal.ChronoUnit
import scala.util.Try

object TaxCalcRequestValidator {

  private val APR2021_RESIDENTIAL_DATE           = LocalDate.of(2021, 4, 1)
  private val RIGHT_TO_BUY_RELIEF                = "22"
  private val FIRST_TIME_BUYER_RELIEF            = "32"
  private val RELIEF_FROM_17_PERCENT_RATE_RELIEF = "35"
  private val GRANT_OF_LEASE                     = "L"
  private val CONVEYANCE_WITH_LEASE_INVOLVEMENT  = "A"
  private val YES                                = "yes"
  private val ANNUAL_RENT_THRESHOLD              = 1000
  private val PREMIUM_THRESHOLD                  = 40000
  private val LEASE_TERM_THRESHOLD               = 7
  private val NON_EXEMPT_RELIEF_CODES            = Set(RIGHT_TO_BUY_RELIEF, FIRST_TIME_BUYER_RELIEF, RELIEF_FROM_17_PERCENT_RATE_RELIEF)

  private case class ValidLeaseDates(
                                      startDate: LocalDate,
                                      endDate: LocalDate
                                    )

  def buildRequest(userAnswers: UserAnswers): Either[BuildRequestError, SdltCalculationRequest] =
    for {
      fullReturn       <- userAnswers.fullReturn.toRight(MissingFullReturnError)
      mainLandId       <- fullReturn.returnInfo.flatMap(_.mainLandID).toRight(MissingMainLandIdError)
      land             <- fullReturn.land.flatMap(_.find(_.landID.contains(mainLandId))).toRight(MissingAboutTheLandError)
      transaction      <- fullReturn.transaction.toRight(MissingAboutTheTransactionError)
      interestCode     <- land.interestCreatedTransferred.toRight(MissingLandAnswerError("interestCreatedTransferred"))
      propertyCode     <- land.propertyType.toRight(MissingLandAnswerError("propertyType"))
      propertyType     <- PropertyTypes.fromCode(propertyCode).toRight(UnknownPropertyTypeError(propertyCode))
      effectiveDate    <- transaction.effectiveDate.toRight(MissingTransactionAnswerError("effectiveDate"))
      parsedDate       <- parseDate(effectiveDate).left.map(_ => InvalidDateError(effectiveDate))
      transDesc        <- transaction.transactionDescription.toRight(MissingTransactionAnswerError("transactionDescription"))
      holdingType      <- HoldingTypes.fromCode(transDesc).toRight(UnknownHoldingTypeError(transDesc))
      premium          <- premiumFor(holdingType, transaction, fullReturn.lease)
      isLinkedRaw      <- transaction.isLinked.toRight(MissingTransactionAnswerError("isLinked"))
      leaseDetails     <- fullReturn.lease.fold(Right(None))(buildLeaseDetails(_, transaction, parsedDate))
      taxReliefDetails <- getTaxReliefDetails(transaction)
    } yield SdltCalculationRequest(
      holdingType         = holdingType,
      propertyType        = propertyType,
      effectiveDateDay    = parsedDate.getDayOfMonth,
      effectiveDateMonth  = parsedDate.getMonthValue,
      effectiveDateYear   = parsedDate.getYear,
      nonUKResident       = handleNonUkResident(fullReturn, parsedDate, propertyType),
      premium             = parseAmount(premium),
      highestRent         = fullReturn.lease.flatMap(_.startingRent).flatMap(v => Try(parseAmount(v)).toOption).getOrElse(BigDecimal(0)),
      propertyDetails     = buildPropertyDetails(propertyCode),
      leaseDetails        = leaseDetails,
      relevantRentDetails = fullReturn.lease.map(buildRelevantRentDetails),
      firstTimeBuyer      = Some(if (transaction.reliefReason.contains(FIRST_TIME_BUYER_RELIEF)) "Yes" else "No"),
      isLinked            = Some(isLinkedRaw.toLowerCase == YES),
      interestTransferred = Some(interestCode),
      taxReliefDetails    = taxReliefDetails,
      isMultipleLand      = fullReturn.land.map(_.size > 1),
      declaredNpv         = fullReturn.lease.flatMap(_.netPresentValue.flatMap(v => Try(parseAmount(v)).toOption))
    )

  private def parseAmount(value: String): BigDecimal =
    BigDecimal(value.replace(",", ""))

  private val LEADING_AMOUNT = """-?[\d,]*(\.\d+)?""".r

  private def amountOrZero(value: Option[String]): BigDecimal =
    value
      .flatMap(v => LEADING_AMOUNT.findPrefixOf(v.trim))
      .flatMap(v => Try(parseAmount(v)).toOption)
      .getOrElse(BigDecimal(0))

  private def premiumFor(holdingType: HoldingTypes.Value, transaction: Transaction, lease: Option[Lease]): Either[BuildRequestError, String] =
    holdingType match {
      case HoldingTypes.leasehold => lease.flatMap(_.totalPremiumPayable).toRight(MissingLeaseAnswerError("totalPremiumPayable"))
      case _                      => transaction.totalConsideration.toRight(MissingTransactionAnswerError("totalConsideration"))
    }

  private def getTaxReliefDetails(transaction: Transaction): Either[BuildRequestError, Option[TaxReliefDetails]] =
    transaction.claimingRelief.map(_.toLowerCase).toRight(MissingTransactionAnswerError("claimingRelief")).flatMap {
      case YES =>
        for {
          reliefReason <- transaction.reliefReason.toRight(MissingTransactionAnswerError("reliefReason"))
          reliefCode   <- Try(reliefReason.toInt).toOption.toRight(InvalidReliefReasonError(reliefReason))
        } yield Some(TaxReliefDetails(
          taxReliefCode   = reliefCode,
          isPartialRelief = Some(transaction.reliefAmount.isDefined)
        ))
      case _   => Right(None)
    }

  private def handleNonUkResident(
                                   fullReturn: FullReturn,
                                   effectiveDate: LocalDate,
                                   propertyType: PropertyTypes.Value
                                 ): Option[String] =
    if (propertyType != PropertyTypes.residential || effectiveDate.isBefore(APR2021_RESIDENTIAL_DATE)) None
    else fullReturn.residency.map { r =>
      if (isLiable(r) && !isExemptFromNRSDLT(fullReturn.transaction, fullReturn.lease, r)) "Yes" else "No"
    }

  private def isLiable(residency: Residency): Boolean =
    isYes(residency.isCloseCompany) ||
      (isYes(residency.isNonUkResidents) && !isYes(residency.isCrownRelief))

  private def isExemptFromNRSDLT(transaction: Option[Transaction], lease: Option[Lease], residency: Residency): Boolean = {
    val reliefCode      = transaction.filter(t => isYes(t.claimingRelief)).flatMap(_.reliefReason)
    val transactionType = transaction.flatMap(_.transactionDescription)
    val leaseType       = lease.flatMap(_.leaseType)
    val leaseTerm       = lease.flatMap { l =>
        getValidLeaseDates(l).toOption.map { dates =>
          calculateLeaseTerm(dates.startDate, dates.endDate)
        }
      }

    val shortLease    =
      leaseTerm.exists(_.years < LEASE_TERM_THRESHOLD)

    val lowValueLease =
      amountOrZero(lease.flatMap(_.totalPremiumPayable)) < PREMIUM_THRESHOLD &&
        amountOrZero(lease.flatMap(_.startingRent)) < ANNUAL_RENT_THRESHOLD

    (leaseType, transactionType) match {
      case _ if reliefCode.exists(code => !NON_EXEMPT_RELIEF_CODES(code))                            => true
      case (Some("R"), Some(CONVEYANCE_WITH_LEASE_INVOLVEMENT)) if isYes(residency.isNonUkResidents) => shortLease
      case (Some("R"), _)                                                                            => shortLease || lowValueLease
      case _                                                                                         => false
    }
  }

  private def isYes(value: Option[String]): Boolean =
    value.exists(_.equalsIgnoreCase("yes"))

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
      case LandTypeOfProperty.Residential.toString =>
        Some(PropertyDetails(
          individual           = "Yes",
          twoOrMoreProperties  = Some("No"),
          replaceMainResidence = None,
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
      validDates            <- getValidLeaseDates(lease)
      calculationStartDate  = if (effectiveDate.isAfter(validDates.startDate)) effectiveDate else validDates.startDate
      leaseTerm             = calculateLeaseTerm(calculationStartDate, validDates.endDate)
      rentYears             = if (leaseTerm.years < 5 && leaseTerm.daysInPartialYear > 0) Math.min(leaseTerm.years + 1, 5) else Math.min(leaseTerm.years, 5)
      adjustedEndDate       = if(leaseTerm.years == 8 && getLeaseYears(calculationStartDate, validDates.endDate) == 7) calculationStartDate.plusYears(8).plusDays(leaseTerm.days - 1) else validDates.endDate
    } yield Option.when(transaction.transactionDescription.contains(GRANT_OF_LEASE))(
      LeaseDetails(
        startDateDay    = validDates.startDate.getDayOfMonth,
        startDateMonth  = validDates.startDate.getMonthValue,
        startDateYear   = validDates.startDate.getYear,
        endDateDay      = adjustedEndDate.getDayOfMonth,
        endDateMonth    = adjustedEndDate.getMonthValue,
        endDateYear     = adjustedEndDate.getYear,
        leaseTerm       = leaseTerm,
        year1Rent = 0,
        year2Rent = Option.when(rentYears >= 2)(0),
        year3Rent = Option.when(rentYears >= 3)(0),
        year4Rent = Option.when(rentYears >= 4)(0),
        year5Rent = Option.when(rentYears >= 5)(0)
      )
    )

  private def getValidLeaseDates(lease: Lease): Either[BuildRequestError, ValidLeaseDates] =
    for {
      contractStartDate <- lease.contractStartDate.toRight(MissingLeaseAnswerError("contractStartDate"))
      contractEndDate <- lease.contractEndDate.toRight(MissingLeaseAnswerError("contractEndDate"))
      startDate <- parseDate(contractStartDate).left.map(_ => InvalidDateError(contractStartDate))
      endDate <- parseDate(contractEndDate).left.map(_ => InvalidDateError(contractEndDate))
    } yield ValidLeaseDates(startDate, endDate)

  private def calculateLeaseTerm(
                                  validStartDate: LocalDate,
                                  validEndDate: LocalDate,
                                ): LeaseTerm = {
    val years = getLeaseYears(validStartDate, validEndDate)
    val partialStart = validStartDate.plusYears(years)
    val days = ChronoUnit.DAYS.between(partialStart, validEndDate.plusDays(1)).toInt
    val daysInPartialYear = if (years < 5 && days > 0) days else 0
    val yearsForNrsdlt = if (years == 7 && days > 0) 8 else years

    LeaseTerm(
      years = yearsForNrsdlt,
      days = days,
      daysInPartialYear = daysInPartialYear
    )
  }

  private def getLeaseYears(validStartDate: LocalDate, validEndDate: LocalDate): Int =
    Period.between(validStartDate, validEndDate.plusDays(1)).getYears

  private def buildRelevantRentDetails(lease: Lease): RelevantRentDetails =
    RelevantRentDetails(
      contractPre201603        = Some("Yes"),
      contractVariedPost201603 = Some("No"),
      relevantRent             =
        if (lease.isAnnualRentOver1000.map(_.toLowerCase).contains(YES)) Some(ANNUAL_RENT_THRESHOLD)
        else Some(BigDecimal(0))
    )
}
