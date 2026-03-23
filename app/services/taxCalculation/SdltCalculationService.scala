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
import models.{FullReturn, UserAnswers}
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
      relevantRentDetails = buildRelevantRentDetails(propertyCode, interestCode, fullReturn, parsedDate),
      firstTimeBuyer      = None,
      isLinked            = transaction.isLinked.map(_.toUpperCase == "YES"),
      interestTransferred = Some(interestCode),
      taxReliefDetails    = None,
      isMultipleLand      = fullReturn.land.map(_.size > 1)
    )

  private def toHoldingType(interestCode: String): HoldingTypes.Value = interestCode match {
    case LandInterestTransferredOrCreated.FG.toString => HoldingTypes.freehold
    case LandInterestTransferredOrCreated.FP.toString => HoldingTypes.freehold
    case LandInterestTransferredOrCreated.FT.toString => HoldingTypes.freehold
    case LandInterestTransferredOrCreated.LG.toString => HoldingTypes.leasehold
    case LandInterestTransferredOrCreated.LP.toString => HoldingTypes.leasehold
    case LandInterestTransferredOrCreated.LT.toString => HoldingTypes.leasehold
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
      case LandTypeOfProperty.Residential.toString =>
        Some(PropertyDetails(
          individual           = if (isIndividual(fullReturn)) "Yes" else "No",
          twoOrMoreProperties  = None,
          replaceMainResidence = None,
          sharedOwnership      = None,
          currentValue         = None
        ))
      case _ => None
    }

  private def buildLeaseDetails(interestCode: String, fullReturn: FullReturn, effectiveDate: LocalDate): Option[LeaseDetails] =
    for {
      lease             <- fullReturn.lease.filter(_ => toHoldingType(interestCode) == HoldingTypes.leasehold)
      start             <- lease.contractStartDate
      end               <- lease.contractEndDate
      rent              <- lease.startingRent
      startDate         <- Try(LocalDate.parse(start)).toOption
      endDate           <- Try(LocalDate.parse(end)).toOption
      rent              <- Try(BigDecimal(rent)).toOption
      selectDate        = if (effectiveDate.isAfter(startDate)) effectiveDate else startDate
      years             = Period.between(selectDate, endDate.plusDays(1)).getYears
      partialStart      = selectDate.plusYears(years)
      days              = ChronoUnit.DAYS.between(partialStart, endDate.plusDays(1)).toInt
      daysInPartialYear = if (days > 0) ChronoUnit.DAYS.between(partialStart, partialStart.plusYears(1)).toInt else 0
      yearsRequired     = if (years < 5 && daysInPartialYear > 0) years + 1 else years
    } yield LeaseDetails(
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