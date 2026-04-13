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

  // TODO: REMOVE EXPLANATORY COMMENTS
  // TODO: DTR-2815: Must Implement Self-Assessed response for Residential before 2012-03-22
  
  val logger: Logger = LoggerFactory.getLogger(getClass)

  val APR2021_RESIDENTIAL_DATE = LocalDate.of(2021, 4, 1)

  def calculateStampDutyLandTax(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[CalculationResponse] =
    buildRequest(userAnswers) match {
      case Right(request) =>
        logger.info(s"[SdltCalculationService][calculateStampDutyLandTax] sending calculation request")
        connector.calculateStampDutyLandTax(request)
      case Left(error) =>
        logger.error(s"[SdltCalculationService][calculateStampDutyLandTax] failed to build request: $error")
        Future.failed(new IllegalStateException(error))
    }

  private def buildRequest(userAnswers: UserAnswers): Either[String, SdltCalculationRequest] =
    for {
      fullReturn    <- userAnswers.fullReturn.toRight("FullReturn not found in UserAnswers")                                  // RETURN
      land          <- fullReturn.land.flatMap(_.headOption).toRight("Land not found in FullReturn")                          // LAND
      transaction   <- fullReturn.transaction.toRight("Transaction not found in FullReturn")                                  // TRANSACTION
      interestCode  <- land.interestCreatedTransferred.toRight("interestCreatedTransferred not found in Land")                // LAND.INTEREST_TRANSFERRED_CREATED - (1.3) Interest transferred or created
      propertyCode  <- land.propertyType.toRight("propertyType not found in Land")                                            // LAND.PROPERTY_TYPE - (1.1) Type of property
      effectiveDate <- transaction.effectiveDate.toRight("effectiveDate not found in Transaction")                            // TRANSACTION.EFFECTIVE_DATE - (1.4) Effective date of transaction
      parsedDate    <- validateDate(effectiveDate).toRight(s"Failed to parse effectiveDate: $effectiveDate")
      premium       <- transaction.totalConsideration.toRight("totalConsideration not found in Transaction")                  // TRANSACTION.TOTAL_CONSIDERATION - (1.10) Total consideration
      transDesc     <- transaction.transactionDescription.toRight("transactionDescription not found in Transaction")          // TRANSACTION.TRANSACTION_DESCRIPTION - (1.2) Description of transaction
      holdingType   <- getHoldingType(transDesc)
      leaseDetails  <- fullReturn.lease.fold(Right(None))(buildLeaseDetails(_, transaction, parsedDate))                      // LEASE
      propertyType  <- getPropertyType(propertyCode)
    } yield SdltCalculationRequest(
      holdingType         = holdingType,
      propertyType        = propertyType,
      effectiveDateDay    = parsedDate.getDayOfMonth,
      effectiveDateMonth  = parsedDate.getMonthValue,
      effectiveDateYear   = parsedDate.getYear,
      nonUKResident       = fullReturn.residency.flatMap(_.isNonUkResidents.flatMap(handleNonUkResident(parsedDate, propertyType, _))),     // RESIDENCY.IS_NON_UK_RESIDENTS - Non-UK resident purchaser?
      premium             = premium,
      highestRent         = BigDecimal(0),                                                                                    // Rent values are bypassed - declaredNpv is used for leasehold calculation
      propertyDetails     = buildPropertyDetails(propertyCode),
      leaseDetails        = leaseDetails,
      relevantRentDetails = fullReturn.lease.map(buildRelevantRentDetails),
      firstTimeBuyer      = Some(if (transaction.reliefReason.contains("32")) "Yes" else "No"),                               // TRANSACTION.RELIEF_REASON
      isLinked            = transaction.isLinked.map(_.toUpperCase == "YES"),                                                 // TRANSACTION.IS_LINKED - (1.13) Is this transaction linked to any other?
      interestTransferred = Some(interestCode),                                                                               // LAND.INTEREST_TRANSFERRED_CREATED - (1.3) Interest transferred or created
      taxReliefDetails    = getTaxReliefDetails(transaction),
      isMultipleLand      = fullReturn.land.map(_.size > 1),
      declaredNpv         = fullReturn.lease.flatMap(_.netPresentValue.flatMap(toBigDecimal))                                 // LEASE.NET_PRESENT_VALUE - (1.23) Net present value upon which tax is calculated
    )

  private def getHoldingType(transactionDesc: String): Either[String, HoldingTypes.Value] =
    TransactionType.parse(Some(transactionDesc)).map {
      case GrantOfLease            => HoldingTypes.leasehold
      case ConveyanceTransfer      => HoldingTypes.freehold
      case ConveyanceTransferLease => HoldingTypes.freehold
      case OtherTransaction        => HoldingTypes.freehold
    }.toRight(s"Unknown transaction type: $transactionDesc")

  private def getPropertyType(propertyCode: String): Either[String, PropertyTypes.Value] = propertyCode match {
    case Residential.toString     => Right(PropertyTypes.residential)
    case Additional.toString      => Right(PropertyTypes.residential)
    case Mixed.toString           => Right(PropertyTypes.mixed)
    case NonResidential.toString  => Right(PropertyTypes.nonResidential)
    case _                        => Left(s"Unknown property type: $propertyCode")
  }

  private def getTaxReliefDetails(transaction: Transaction): Option[TaxReliefDetails] =
    for {
      claimingRelief <- transaction.claimingRelief                     // TRANSACTION.CLAIMING_RELIEF - (1.9) Are you claiming relief?
      if claimingRelief.toUpperCase == "YES"
      reliefReason   <- transaction.reliefReason                       // TRANSACTION.RELIEF_REASON - (1.9) If 'yes', please show the reason
      reliefCode     <- Try(reliefReason.toInt).toOption
    } yield TaxReliefDetails(
      taxReliefCode   = reliefCode,
      isPartialRelief = Some(transaction.reliefAmount.isDefined)       // TRANSACTION.RELIEF_AMOUNT - (1.9) ... amount remaining chargeable (whole pounds only)
    )

  private def handleNonUkResident(effectiveDate: LocalDate, propertyType: PropertyTypes.Value, isNonUkResident: String): Option[String] = {
    if (effectiveDate.isBefore(APR2021_RESIDENTIAL_DATE) && propertyType == PropertyTypes.residential) { None }
    else                                                  { Some(isNonUkResident.capitalize) }
  }
  
  private def buildPropertyDetails(propertyCode: String): Option[PropertyDetails] =
    propertyCode match {
      case LandTypeOfProperty.Additional.toString =>                                        // LAND.PROPERTY_TYPE - (1.1) Type of property
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

  private def buildLeaseDetails(lease: Lease, transaction: Transaction, effectiveDate: LocalDate): Either[String, Option[LeaseDetails]] =
    for {
      contractStartDate   <- lease.contractStartDate.toRight("contractStartDate not found in Lease")                            // LEASE.CONTRACT_START_DATE - (1.17) Start date as specified in lease
      contractEndDate     <- lease.contractEndDate.toRight("contractEndDate not found in Lease")                                // LEASE.CONTRACT_END_DATE - (1.18) End date as specified in lease
      validStartDate      <- validateDate(contractStartDate).toRight(s"Failed to parse contractStartDate: $contractStartDate")
      validEndDate        <- validateDate(contractEndDate).toRight(s"Failed to parse contractEndDate: $contractEndDate")
      calculationStartDate = if (effectiveDate.isAfter(validStartDate)) effectiveDate else validStartDate
      years                = Period.between(calculationStartDate, validEndDate.plusDays(1)).getYears
      partialStart         = calculationStartDate.plusYears(years)
      days                 = ChronoUnit.DAYS.between(partialStart, validEndDate.plusDays(1)).toInt
      daysInPartialYear    = if (years < 5 && days > 0) days else 0                                                              // only set for sub-5-year leases with partial days
      rentYears            = if (years < 5 && daysInPartialYear > 0) Math.min(years + 1, 5) else Math.min(years, 5)              // matches SDLTC yearsRequired logic
    } yield Option.when(transaction.transactionDescription.contains("L"))(
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
        year1Rent = 0,                                 // Rent values are bypassed - declaredNpv is used for leasehold calculation
        year2Rent = Option.when(rentYears >= 2)(0),    // Rent values are bypassed - declaredNpv is used for leasehold calculation
        year3Rent = Option.when(rentYears >= 3)(0),    // Rent values are bypassed - declaredNpv is used for leasehold calculation
        year4Rent = Option.when(rentYears >= 4)(0),    // Rent values are bypassed - declaredNpv is used for leasehold calculation
        year5Rent = Option.when(rentYears >= 5)(0)     // Rent values are bypassed - declaredNpv is used for leasehold calculation
      )
    )

  private def buildRelevantRentDetails(lease: Lease): RelevantRentDetails =
    RelevantRentDetails(
      contractPre201603        = Some("Yes"),
      contractVariedPost201603 = Some("No"),
      relevantRent             =
        if (lease.isAnnualRentOver1000.map(_.toUpperCase).contains("YES")) {    // LEASE.IS_ANNUAL_RENT_OVER_1000 - Is the annual rent £1000 or more?
          Some(ANNUAL_RENT_THRESHOLD)
        } else {
          Some(BigDecimal(0))
        }
    )

  private def toBigDecimal(number: String): Option[BigDecimal] =
    Try(BigDecimal(number)).toOption

  private def validateDate(date: String): Option[LocalDate] =
    Try(LocalDate.parse(date)).toOption
  
  private val ANNUAL_RENT_THRESHOLD: BigDecimal = 1000
}
