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

package services

import connectors.SdltCalculationConnector
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
      dateTuple          <- parseEffectiveDate(effectiveDate).toRight("Failed to parse effectiveDate")
      (day, month, year)  = dateTuple
      parsedDate          = LocalDate.of(year, month, day)
      premium            <- transaction.totalConsideration.toRight("totalConsideration not found in Transaction")
    } yield SdltCalculationRequest(
      holdingType         = toHoldingType(interestCode),
      propertyType        = toPropertyType(propertyCode),
      effectiveDateDay    = day,
      effectiveDateMonth  = month,
      effectiveDateYear   = year,
      nonUKResident       = fullReturn.residency.flatMap(_.isNonUkResidents),
      premium             = premium,
      highestRent         = fullReturn.lease.flatMap(_.startingRent).map(BigDecimal(_)).getOrElse(BigDecimal(0)),
      propertyDetails     = buildPropertyDetails(propertyCode, fullReturn),
      leaseDetails        = buildLeaseDetails(interestCode, fullReturn, parsedDate),
      relevantRentDetails = buildRelevantRentDetails(propertyCode, interestCode, transaction, fullReturn, parsedDate),
      firstTimeBuyer      = None,
      isLinked            = toIsLinked(transaction),
      interestTransferred = Some(interestCode),
      taxReliefDetails    = None,
      isMultipleLand      = fullReturn.land.map(_.size > 1)
    )

  private def toHoldingType(interestCode: String): HoldingTypes.Value =
    if (interestCode.startsWith("F")) HoldingTypes.freehold else HoldingTypes.leasehold

  private def toPropertyType(propertyCode: String): PropertyTypes.Value = propertyCode match {
    case "01" | "04" => PropertyTypes.residential
    case "02"        => PropertyTypes.mixed
    case "03"        => PropertyTypes.nonResidential
  }

  private def buildPropertyDetails(propertyCode: String, fullReturn: FullReturn): Option[PropertyDetails] =
    propertyCode match {
      case "04" =>
        Some(PropertyDetails(
          individual           = "Yes",
          twoOrMoreProperties  = Some("Yes"),
          replaceMainResidence = Some("No"),
          sharedOwnership      = None,
          currentValue         = None
        ))
      case "01" =>
        val isIndividual = fullReturn.purchaser
          .flatMap(_.headOption)
          .flatMap(_.isCompany)
          .exists(_.toUpperCase == "NO")
        Some(PropertyDetails(
          individual           = if (isIndividual) "Yes" else "No",
          twoOrMoreProperties  = None,
          replaceMainResidence = None,
          sharedOwnership      = None,
          currentValue         = None
        ))
      case _ => None
    }

  private def parseEffectiveDate(date: String): Option[(Int, Int, Int)] =
    Try(LocalDate.parse(date))
      .toOption
      .map(d => (d.getDayOfMonth, d.getMonthValue, d.getYear))

  private def toIsLinked(transaction: Transaction): Option[Boolean] =
    transaction.isLinked.map(_.toUpperCase == "YES")

  private def buildLeaseDetails(interestCode: String, fullReturn: FullReturn, effectiveDate: LocalDate): Option[LeaseDetails] =
    fullReturn.lease
      .filter(_ => toHoldingType(interestCode) == HoldingTypes.leasehold)
      .flatMap(toLeaseDetails(_, effectiveDate))

  private def buildRelevantRentDetails(
    propertyCode:  String,
    interestCode:  String,
    transaction:   Transaction,
    fullReturn:    FullReturn,
    effectiveDate: LocalDate
  ): Option[RelevantRentDetails] = {
    val isApplicableLeasehold =
      (toPropertyType(propertyCode) == PropertyTypes.nonResidential || toPropertyType(propertyCode) == PropertyTypes.mixed) &&
      toHoldingType(interestCode) == HoldingTypes.leasehold

    if (!isApplicableLeasehold) { None }
    else {

      val rent = fullReturn.lease.flatMap(_.startingRent).flatMap(r => Try(BigDecimal(r)).toOption)

      val annualRentIs1000OrMore = fullReturn.lease.exists(_.isAnnualRentOver1000.contains(true))

      if (effectiveDate.isBefore(march2016NonResidentialDate)) {
        // Before 17 March 2016: only relevantRent is required
        rent.map(r => RelevantRentDetails(
          contractPre201603        = None,
          contractVariedPost201603 = None,
          relevantRent             = Some(if (annualRentIs1000OrMore) 1000 else 0)
        ))
      } else {
        // On/after 17 March 2016: contractPre201603 is required
        transaction.contractDate
          .flatMap(d => Try(LocalDate.parse(d)).toOption)
          .map { contractDate =>
            val contractBefore = contractDate.isBefore(march2016NonResidentialDate)
            RelevantRentDetails(
              contractPre201603        = Some(if (contractBefore) "Yes" else "No"),
              contractVariedPost201603 = None, // not captured in current data model; only valid when contractBefore = false
              relevantRent             = Some(if (annualRentIs1000OrMore) 1000 else 0)
            )
          }
      }
    }
  }

  private def toLeaseDetails(lease: Lease, effectiveDate: LocalDate): Option[LeaseDetails] =
    for {
      startStr  <- lease.contractStartDate
      endStr    <- lease.contractEndDate
      rentStr   <- lease.startingRent
      startDate <- Try(LocalDate.parse(startStr)).toOption
      endDate   <- Try(LocalDate.parse(endStr)).toOption
      rent      <- Try(BigDecimal(rentStr)).toOption
    } yield {
      val selectDate        = if (effectiveDate.isAfter(startDate)) effectiveDate else startDate
      val years             = Period.between(selectDate, endDate.plusDays(1)).getYears
      val partialStart      = selectDate.plusYears(years)
      val daysInPartialYear = ChronoUnit.DAYS.between(partialStart, endDate.plusDays(1)).toInt
      val yearsRequired     = if (years < 5 && daysInPartialYear > 0) years + 1 else years

      LeaseDetails(
        startDateDay    = startDate.getDayOfMonth,
        startDateMonth  = startDate.getMonthValue,
        startDateYear   = startDate.getYear,
        endDateDay      = endDate.getDayOfMonth,
        endDateMonth    = endDate.getMonthValue,
        endDateYear     = endDate.getYear,
        leaseTerm       = LeaseTerm(years = years, days = daysInPartialYear, daysInPartialYear = daysInPartialYear),
        year1Rent       = rent,
        year2Rent       = Option.when(yearsRequired >= 2)(rent),
        year3Rent       = Option.when(yearsRequired >= 3)(rent),
        year4Rent       = Option.when(yearsRequired >= 4)(rent),
        year5Rent       = Option.when(yearsRequired >= 5)(rent)
      )
    }
}