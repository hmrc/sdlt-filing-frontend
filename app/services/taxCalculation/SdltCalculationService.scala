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

import connectors.SdltCalculationConnector
import models.land.LandTypeOfProperty.*
import models.land.LandTypeOfProperty
import models.prelimQuestions.TransactionType
import models.prelimQuestions.TransactionType.*
import models.*
import models.taxCalculation.*
import org.slf4j.*
import uk.gov.hmrc.http.HeaderCarrier

import java.time.*
import java.time.temporal.ChronoUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.util.Try

@Singleton
class SdltCalculationService @Inject()(connector: SdltCalculationConnector) {

  // TODO: DTR-2815: Must Implement Self-Assessed response for Residential before 2012-03-22
  
  private val logger: Logger = LoggerFactory.getLogger(getClass)

  private val APR2021_RESIDENTIAL_DATE = LocalDate.of(2021, 4, 1)
  private val FIRST_TIME_BUYER_RELIEF  = "32"
  private val GRANT_OF_LEASE           = "L"
  private val YES                      = "YES"
  private val ANNUAL_RENT_THRESHOLD    = 1000
  

  def calculateStampDutyLandTax(userAnswers: UserAnswers)(implicit hc: HeaderCarrier, ec: scala.concurrent.ExecutionContext): Future[Result] =
    buildRequest(userAnswers) match {
      case Right(request) =>
        logger.info(s"[SdltCalculationService][calculateStampDutyLandTax] sending calculation request")
        connector.calculateStampDutyLandTax(request).map(_.result.headOption.getOrElse(
          throw new IllegalStateException("Calculation response contained no results")
        ))
      case Left(error) =>
        logger.error(s"[SdltCalculationService][calculateStampDutyLandTax] failed to build request: ${error.message}")
        Future.failed(new IllegalStateException(error.message))
    }

  private def buildRequest(userAnswers: UserAnswers): Either[BuildRequestError, SdltCalculationRequest] =
    for {
      fullReturn    <- userAnswers.fullReturn.toRight(MissingFullReturn)
      land          <- fullReturn.land.flatMap(_.headOption).toRight(MissingAboutTheLand)
      transaction   <- fullReturn.transaction.toRight(MissingAboutTheTransaction)
      interestCode  <- land.interestCreatedTransferred.toRight(MissingLandAnswer("interestCreatedTransferred"))
      propertyCode  <- land.propertyType.toRight(MissingLandAnswer("propertyType"))
      propertyType  <- getPropertyType(propertyCode).toRight(UnknownPropertyType(propertyCode))
      effectiveDate <- transaction.effectiveDate.toRight(MissingTransactionAnswer("effectiveDate"))
      parsedDate    <- validateDate(effectiveDate).toRight(InvalidDate(effectiveDate))
      premium       <- transaction.totalConsideration.toRight(MissingTransactionAnswer("totalConsideration"))
      transDesc     <- transaction.transactionDescription.toRight(MissingTransactionAnswer("transactionDescription"))
      holdingType   <- getHoldingType(transDesc).toRight(UnknownHoldingType(transDesc))
      leaseDetails  <- fullReturn.lease.fold(Right(None))(buildLeaseDetails(_, transaction, parsedDate))
    } yield SdltCalculationRequest(
      holdingType         = holdingType,
      propertyType        = propertyType,
      effectiveDateDay    = parsedDate.getDayOfMonth,
      effectiveDateMonth  = parsedDate.getMonthValue,
      effectiveDateYear   = parsedDate.getYear,
      nonUKResident       = handleNonUkResident(fullReturn.residency, parsedDate, propertyType),
      premium             = premium,
      highestRent         = BigDecimal(0),
      propertyDetails     = buildPropertyDetails(propertyCode),
      leaseDetails        = leaseDetails,
      relevantRentDetails = fullReturn.lease.map(buildRelevantRentDetails),
      firstTimeBuyer      = Some(if (transaction.reliefReason.contains(FIRST_TIME_BUYER_RELIEF)) "Yes" else "No"),
      isLinked            = transaction.isLinked.map(_.toUpperCase == YES),
      interestTransferred = Some(interestCode),
      taxReliefDetails    = getTaxReliefDetails(transaction),
      isMultipleLand      = fullReturn.land.map(_.size > 1),
      declaredNpv         = fullReturn.lease.flatMap(_.netPresentValue.flatMap(toBigDecimal))
    )

  private def getHoldingType(transactionDesc: String): Option[HoldingTypes.Value] =
    TransactionType.parse(Some(transactionDesc)).map {
      case GrantOfLease            => HoldingTypes.leasehold
      case ConveyanceTransfer      => HoldingTypes.freehold
      case ConveyanceTransferLease => HoldingTypes.freehold
      case OtherTransaction        => HoldingTypes.freehold
    }

  private def getPropertyType(propertyCode: String): Option[PropertyTypes.Value] = propertyCode match {
    case Residential.toString     => Some(PropertyTypes.residential)
    case Additional.toString      => Some(PropertyTypes.residential)
    case Mixed.toString           => Some(PropertyTypes.mixed)
    case NonResidential.toString  => Some(PropertyTypes.nonResidential)
    case _                        => None
  }

  private def getTaxReliefDetails(transaction: Transaction): Option[TaxReliefDetails] =
    for {
      claimingRelief <- transaction.claimingRelief
      if claimingRelief.toUpperCase == YES
      reliefReason   <- transaction.reliefReason
      reliefCode     <- Try(reliefReason.toInt).toOption
    } yield TaxReliefDetails(
      taxReliefCode   = reliefCode,
      isPartialRelief = Some(transaction.reliefAmount.isDefined)       // TRANSACTION.RELIEF_AMOUNT - (1.9) ... amount remaining chargeable (whole pounds only)
    )

  private def handleNonUkResident(residency: Option[Residency], effectiveDate: LocalDate, propertyType: PropertyTypes.Value): Option[String] =
    if (effectiveDate.isBefore(APR2021_RESIDENTIAL_DATE) && propertyType == PropertyTypes.residential) { None }
    else { residency.flatMap(_.isNonUkResidents).map(_.capitalize) }
  
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
      contractStartDate   <- lease.contractStartDate.toRight(MissingLeaseAnswer("contractStartDate"))
      contractEndDate     <- lease.contractEndDate.toRight(MissingLeaseAnswer("contractEndDate"))
      validStartDate      <- validateDate(contractStartDate).toRight(InvalidDate(contractStartDate))
      validEndDate        <- validateDate(contractEndDate).toRight(InvalidDate(contractEndDate))
      calculationStartDate = if (effectiveDate.isAfter(validStartDate)) effectiveDate else validStartDate
      years                = Period.between(calculationStartDate, validEndDate.plusDays(1)).getYears
      partialStart         = calculationStartDate.plusYears(years)
      days                 = ChronoUnit.DAYS.between(partialStart, validEndDate.plusDays(1)).toInt
      daysInPartialYear    = if (years < 5 && days > 0) days else 0
      rentYears            = if (years < 5 && daysInPartialYear > 0) Math.min(years + 1, 5) else Math.min(years, 5)
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
        if (lease.isAnnualRentOver1000.map(_.toUpperCase).contains(YES)) {
          Some(ANNUAL_RENT_THRESHOLD)
        } else {
          Some(BigDecimal(0))
        }
    )

  private def toBigDecimal(number: String): Option[BigDecimal] =
    Try(BigDecimal(number)).toOption

  private def validateDate(date: String): Option[LocalDate] =
    Try(LocalDate.parse(date)).toOption
  
  private sealed trait BuildRequestError {
    def message: String
  }

  private case object MissingFullReturn extends BuildRequestError {
    val message = "FullReturn not found in User answers"
  }

  private case object MissingAboutTheLand extends BuildRequestError {
    val message = "Could not find the first land in User answers"
  }

  private case object MissingAboutTheTransaction extends BuildRequestError {
    val message = "Could not extract 'About the Transaction' journey answers from Full return"
  }

  private case class MissingLandAnswer(value: String) extends BuildRequestError {
    val message = s"Could not find user answer from 'About the Land' journey: $value"
  }
  
  private case class MissingTransactionAnswer(value: String) extends BuildRequestError {
    val message = s"Could not find user answer from 'About the Transaction' journey: $value"
  }

  private case class InvalidDate(value: String) extends BuildRequestError {
    val message = s"Invalid date: $value"
  }
  
  private case class UnknownHoldingType(value: String) extends BuildRequestError {
    val message = s"Unknown Holding Type found: $value"
  }

  private case class UnknownPropertyType(value: String) extends BuildRequestError {
    val message = s"Unknown Property Type found: $value"
  }

  private case class MissingLeaseAnswer(value: String) extends BuildRequestError {
    val message = s"Could not find user answer from 'About the Lease': $value"
  }
}
