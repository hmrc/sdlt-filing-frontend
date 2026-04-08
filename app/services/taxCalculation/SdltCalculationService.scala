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
import models.land.LandTypeOfProperty
import models.prelimQuestions.TransactionType
import models.{FullReturn, UserAnswers}
import models.taxCalculation.*
import org.slf4j.{Logger, LoggerFactory}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{LocalDate, Period}
import javax.inject.Inject
import scala.concurrent.Future
import scala.util.Try

class SdltCalculationService @Inject()(connector: SdltCalculationConnector) {

  private val march2016NonResidentialDate = LocalDate.of(2016, 3, 17)

  val logger: Logger = LoggerFactory.getLogger(getClass)

  def buildCalculationRequest(userAnswers: UserAnswers): Either[String, SdltCalculationRequest] =
    buildRequest(userAnswers)

  def calculateStampDutyLandTax(request: SdltCalculationRequest)(implicit hc: HeaderCarrier): Future[CalculationResponse] = {
    logger.info(s"[SdltCalculationService][calculateStampDutyLandTax] sending calculation request")
    connector.calculateStampDutyLandTax(request)
  }

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
      fullReturn          <- userAnswers.fullReturn.toRight("FullReturn not found in UserAnswers")
      land                <- fullReturn.land.flatMap(_.headOption).toRight("Land not found in FullReturn")  // TODO: HEADOPTION? 
      transaction         <- fullReturn.transaction.toRight("Transaction not found in FullReturn")
      maybeTransactionDesc = transaction.newTransactionDescription.orElse(transaction.transactionDescription)
      transactionDesc     <- maybeTransactionDesc.toRight("Transaction Description not found in FullReturn")
      holdingType         <- toHoldingType(transactionDesc)
      propertyCode        <- land.propertyType.toRight("propertyType not found in Land")    // TODO: DO WE SHOW ABOUT THE LEASE IF we have a freehold and leashold
      propertyType        <- toPropertyType(propertyCode)
      propertyDetails      = buildPropertyDetails(propertyCode, fullReturn)
      interestCode        <- land.interestCreatedTransferred.toRight("interestCreatedTransferred not found in Land")
      effectiveDate       <- transaction.effectiveDate.toRight("effectiveDate not found in Transaction")
      parsedDate          <- Try(LocalDate.parse(effectiveDate)).toOption.toRight("Failed to parse effectiveDate")
      leaseDetails        <- buildLeaseDetails(holdingType, fullReturn, parsedDate)
      relevantRentDetails  = buildRelevantRentDetails(propertyCode, interestCode, fullReturn, parsedDate)
      nonUKResident        = fullReturn.residency.flatMap(_.isNonUkResidents).map(v => if (v.toUpperCase == "YES") "Yes" else "No")
      highestRent         <- fullReturn.lease.flatMap(_.startingRent).toRight("startingRent not found in Lease")
      validHighestRent    <- Try(BigDecimal(highestRent)).toOption.toRight("Failed to parse highestRent")
      declaredNpv         <- fullReturn.lease.flatMap(_.netPresentValue).toRight("netPresentValue not found in Lease")
      validNpv            <- Try(BigDecimal(declaredNpv)).toOption.toRight("Failed to parse netPresentValue")
      (day, month, year)   = (parsedDate.getDayOfMonth, parsedDate.getMonthValue, parsedDate.getYear)
      premium             <- transaction.totalConsideration.toRight("totalConsideration not found in Transaction")
      interestCode        <- land.interestCreatedTransferred.toRight("interestCreatedTransferred not found in Land")
    } yield SdltCalculationRequest(
      holdingType         = holdingType,
      propertyType        = propertyType,
      effectiveDateDay    = day,
      effectiveDateMonth  = month,
      effectiveDateYear   = year,
      nonUKResident       = nonUKResident,
      premium             = premium,
      highestRent         = validHighestRent,
      propertyDetails     = propertyDetails,
      leaseDetails        = leaseDetails,
      relevantRentDetails = relevantRentDetails,
      firstTimeBuyer      = None,
      isLinked            = transaction.isLinked.map(_.toUpperCase == "YES"),
      interestTransferred = None,
      taxReliefDetails    = None,
      isMultipleLand      = fullReturn.land.map(_.size > 1),
      declaredNpv         = Some(validNpv)
    )

  private def toHoldingType(transactionType: String): Either[String, HoldingTypes.Value] = transactionType match {
    case TransactionType.ConveyanceTransfer.toString       => Right(HoldingTypes.freehold)
    case TransactionType.ConveyanceTransferLease.toString  => Right(HoldingTypes.freehold)
    case TransactionType.OtherTransaction.toString         => Right(HoldingTypes.freehold)
    case TransactionType.GrantOfLease.toString             => Right(HoldingTypes.leasehold)
    case _                                                 => Left(s"Unknown transaction type: $transactionType")
  }

  private def toPropertyType(propertyCode: String): Either[String, PropertyTypes.Value] = propertyCode match {
    case LandTypeOfProperty.Residential.toString | LandTypeOfProperty.Additional.toString => Right(PropertyTypes.residential)
    case LandTypeOfProperty.Mixed.toString                                                => Right(PropertyTypes.mixed)
    case LandTypeOfProperty.NonResidential.toString                                       => Right(PropertyTypes.nonResidential)
    case _                                                                                => Left(s"Unknown property code: $propertyCode")
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
          individual           = if (isIndividual(fullReturn)) "Yes" else "No",
          twoOrMoreProperties  = None,
          replaceMainResidence = None,
          sharedOwnership      = None,
          currentValue         = None
        ))
    }

  private def buildLeaseDetails(holdingType: HoldingTypes.Value, fullReturn: FullReturn, effectiveDate: LocalDate): Either[String, Option[LeaseDetails]] =
    if (holdingType != HoldingTypes.leasehold) { Right(None) }
    else {
      for {
        lease             <- fullReturn.lease.toRight(s"Lease not found for leasehold property")
        contractStartDate <- lease.contractStartDate.toRight(s"contractStartDate not found in Lease")
        contractEndDate   <- lease.contractEndDate.toRight(s"contractEndDate not found in Lease")
        startingRent      <- lease.startingRent.toRight(s"startingRent not found in Lease")
        startDate         <- Try(LocalDate.parse(contractStartDate)).toOption.toRight(s"Failed to parse contractStartDate")
        endDate           <- Try(LocalDate.parse(contractEndDate)).toOption.toRight(s"Failed to parse contractEndDate")
        selectStartDate    = if (effectiveDate.isAfter(startDate)) effectiveDate else startDate
        rent              <- Try(BigDecimal(startingRent)).toOption.toRight(s"Failed to parse startingRent")
      } yield Some(LeaseDetails(
        startDateDay    = startDate.getDayOfMonth,
        startDateMonth  = startDate.getMonthValue,
        startDateYear   = startDate.getYear,
        endDateDay      = endDate.getDayOfMonth,
        endDateMonth    = endDate.getMonthValue,
        endDateYear     = endDate.getYear,
        leaseTerm       = LeaseTerm(
          years = Period.between(selectStartDate, endDate.plusDays(1)).getYears,
          days = 0,
          daysInPartialYear = 0
        ),
        year1Rent       = 0,
        year2Rent       = Some(0),
        year3Rent       = Some(0),
        year4Rent       = Some(0),
        year5Rent       = Some(0)
      ))
    }

  private def buildRelevantRentDetails(propertyCode: String,
                                       interestCode: String,
                                       fullReturn: FullReturn,
                                       effectiveDate: LocalDate
                                      ): Option[RelevantRentDetails] =

    Option.when(isMixedOrNonResidentialLeasehold(propertyCode, interestCode))(
      RelevantRentDetails(
        contractPre201603        = Option.unless(effectiveDate.isBefore(march2016NonResidentialDate))("Yes"),
        contractVariedPost201603 = Option.unless(effectiveDate.isBefore(march2016NonResidentialDate))("No"),
        relevantRent             = Some(BigDecimal(
          if (fullReturn.lease.exists(_.isAnnualRentOver1000.contains("true"))) 1000
          else 0
        ))
      )
    )

  private val isMixedOrNonResidentialLeasehold: (String, String) => Boolean = (propertyCode, interestCode) =>
    (toPropertyType(propertyCode) == PropertyTypes.nonResidential ||
      toPropertyType(propertyCode) == PropertyTypes.mixed) &&
      toHoldingType(interestCode) == HoldingTypes.leasehold

  private val isIndividual: FullReturn => Boolean =
    _.purchaser
      .flatMap(_.headOption)
      .flatMap(_.isCompany)
      .exists(_.toUpperCase == "NO")
}
