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
import models.land.{LandInterestTransferredOrCreated, LandTypeOfProperty}
import models.prelimQuestions.TransactionType
import models.prelimQuestions.TransactionType.{ConveyanceTransfer, ConveyanceTransferLease, GrantOfLease, OtherTransaction}
import models.{FullReturn, Lease, Transaction, UserAnswers}
import models.taxCalculation.*
import org.slf4j.{Logger, LoggerFactory}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{LocalDate, Period}
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import scala.concurrent.Future
import scala.util.Try

class SdltCalculationService @Inject()(connector: SdltCalculationConnector) {

  private val march2016NonResidentialDate = LocalDate.of(2016, 3, 17)

  val logger: Logger = LoggerFactory.getLogger(getClass)

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
      fullReturn         <- userAnswers.fullReturn.toRight("FullReturn not found in UserAnswers")
      land               <- fullReturn.land.flatMap(_.headOption).toRight("Land not found in FullReturn")
      transaction        <- fullReturn.transaction.toRight("Transaction not found in FullReturn")
      interestCode       <- land.interestCreatedTransferred.toRight("interestCreatedTransferred not found in Land")
      propertyCode       <- land.propertyType.toRight("propertyType not found in Land")
      effectiveDate      <- transaction.effectiveDate.toRight("effectiveDate not found in Transaction")
      parsedDate         <- Try(LocalDate.parse(effectiveDate)).toOption.toRight("Failed to parse effectiveDate")
      (day, month, year)  = (parsedDate.getDayOfMonth, parsedDate.getMonthValue, parsedDate.getYear)
      premium            <- transaction.totalConsideration.toRight("totalConsideration not found in Transaction")
      holdingType        <- transaction.transactionDescription.flatMap(toHoldingType).toRight("Unknown transaction type")
    } yield SdltCalculationRequest(
      holdingType         = holdingType,
      propertyType        = toPropertyType(propertyCode),
      effectiveDateDay    = day,
      effectiveDateMonth  = month,
      effectiveDateYear   = year,
      nonUKResident       = fullReturn.residency.flatMap(_.isNonUkResidents),
      premium             = premium,
      highestRent         = BigDecimal(0),
      propertyDetails     = buildPropertyDetails(propertyCode, fullReturn),
      leaseDetails        = fullReturn.lease.map(l => buildLeaseDetails(l, parsedDate)),
      relevantRentDetails = Some(buildRelevantRentDetails(leaseDetails)),
      firstTimeBuyer      = None,                                             // TODO: TO BE IMPLEMENTED - journey not built
      isLinked            = transaction.isLinked.map(_.toUpperCase == "YES"),
      interestTransferred = Some(interestCode),
      taxReliefDetails    = None,                                             // TODO: TO BE IMPLEMENTED - journey not built
      isMultipleLand      = fullReturn.land.map(_.size > 1),
      declaredNpv         = Some(1000) // TODO: TO BE IMPLEMENTED - journey not built
    )

  private def toHoldingType(transactionDesc: String): Option[HoldingTypes.Value] =
    TransactionType.parse(Some(transactionDesc)).map {
      case GrantOfLease            => HoldingTypes.leasehold
      case ConveyanceTransfer      => HoldingTypes.freehold
      case ConveyanceTransferLease => HoldingTypes.freehold
      case OtherTransaction        => HoldingTypes.freehold
    }

  private def toPropertyType(propertyCode: String): PropertyTypes.Value = propertyCode match {
    case LandTypeOfProperty.Residential.toString | LandTypeOfProperty.Additional.toString => PropertyTypes.residential
    case LandTypeOfProperty.Mixed.toString                                                => PropertyTypes.mixed
    case LandTypeOfProperty.NonResidential.toString                                       => PropertyTypes.nonResidential
  }

  private def buildPropertyDetails(propertyCode: String, fullReturn: FullReturn): Option[PropertyDetails] =
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
          individual           = "Yes",
          twoOrMoreProperties  = Some("No"),
          replaceMainResidence = None,
          sharedOwnership      = None,
          currentValue         = None
        ))
    }

  private def buildLeaseDetails(lease: Lease, transaction: Transaction, effectiveDate: LocalDate): Either[String, Option[LeaseDetails]] =
    for {
      contractStartDate   <- lease.contractStartDate.toRight("contractStartDate not found in Lease")
      contractEndDate     <- lease.contractEndDate.toRight("contractEndDate not found in Lease")
      startDate           <- Try(LocalDate.parse(contractStartDate)).toOption.toRight("Failed to parse contractStartDate")
      endDate             <- Try(LocalDate.parse(contractEndDate)).toOption.toRight("Failed to parse contractEndDate")
      calculationStartDate = if (effectiveDate.isAfter(startDate)) effectiveDate else startDate
      years                = Period.between(calculationStartDate, endDate.plusDays(1)).getYears
      partialStart         = calculationStartDate.plusYears(years)
      days                 = ChronoUnit.DAYS.between(partialStart, endDate.plusDays(1)).toInt
      yearsRequired        = if (years < 5 && days > 0) years + 1 else years
      if transaction.transactionDescription.contains("L")
    } yield Some(LeaseDetails(
      startDateDay    = startDate.getDayOfMonth,
      startDateMonth  = startDate.getMonthValue,
      startDateYear   = startDate.getYear,
      endDateDay      = endDate.getDayOfMonth,
      endDateMonth    = endDate.getMonthValue,
      endDateYear     = endDate.getYear,
      leaseTerm       = LeaseTerm(
        years = years,
        days = days,
        daysInPartialYear = 0
      ),
      year1Rent = 0,
      year2Rent = Option.when(yearsRequired >= 2)(0),
      year3Rent = Option.when(yearsRequired >= 3)(0),
      year4Rent = Option.when(yearsRequired >= 4)(0),
      year5Rent = Option.when(yearsRequired >= 5)(0)
    ))

  private def buildRelevantRentDetails(lease: Lease): RelevantRentDetails =
    RelevantRentDetails(
      contractPre201603        = Some("Yes"),
      contractVariedPost201603 = Some("No"),
      relevantRent             =
        if (lease.isAnnualRentOver1000.equals("true")) {
          Some(ANNUAL_RENT_THRESHOLD)
        } else {
          Some(BigDecimal(0))
        }
      )
      
  private val ANNUAL_RENT_THRESHOLD: BigDecimal = 1000
}
