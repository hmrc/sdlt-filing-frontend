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
import models.{FullReturn, Transaction, UserAnswers}
import models.taxCalculation._
import org.slf4j.{Logger, LoggerFactory}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SdltCalculationService @Inject()(connector: SdltCalculationConnector)(implicit ec: ExecutionContext) {

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
      fullReturn    <- userAnswers.fullReturn.toRight("FullReturn not found in UserAnswers")
      land          <- fullReturn.land.flatMap(_.headOption).toRight("Land not found in FullReturn")
      transaction   <- fullReturn.transaction.toRight("Transaction not found in FullReturn")
      interestCode  <- land.interestCreatedTransferred.toRight("interestCreatedTransferred not found in Land")
      propertyCode  <- land.propertyType.toRight("propertyType not found in Land")
      effectiveDate <- transaction.effectiveDate.toRight("effectiveDate not found in Transaction")
      premium       <- transaction.totalConsideration.toRight("totalConsideration not found in Transaction")
      (day, month, year) = parseEffectiveDate(effectiveDate)
    } yield SdltCalculationRequest(
      holdingType         = toHoldingType(interestCode),
      propertyType        = toPropertyType(propertyCode),
      effectiveDateDay    = day,
      effectiveDateMonth  = month,
      effectiveDateYear   = year,
      nonUKResident       = fullReturn.residency.flatMap(_.isNonUkResidents),
      premium             = premium,
      highestRent         = highestRentFrom(fullReturn),
      propertyDetails     = None,
      leaseDetails        = None,
      relevantRentDetails = None,
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

  private def parseEffectiveDate(date: String): (Int, Int, Int) = {
    val d = LocalDate.parse(date)
    (d.getDayOfMonth, d.getMonthValue, d.getYear)
  }

  private def highestRentFrom(fullReturn: FullReturn): BigDecimal =
    fullReturn.lease
      .flatMap(_.startingRent)
      .map(BigDecimal(_))
      .getOrElse(BigDecimal(0))

  private def toIsLinked(transaction: Transaction): Option[Boolean] =
    transaction.isLinked.map(_.toUpperCase == "YES")
}