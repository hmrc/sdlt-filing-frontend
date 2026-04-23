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
import models.UserAnswers
import models.taxCalculation.{MissingDataError, TaxCalculationResult}
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SdltCalculationService @Inject()(
                                        connector: SdltCalculationConnector
                                      ) extends Logging {

  // TODO: DTR-2815: Must Implement Self-Assessed response for Residential before 2012-03-22

  def calculateStampDutyLandTax(userAnswers: UserAnswers)
                               (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[MissingDataError, TaxCalculationResult]] =
    TaxCalcRequestValidator.buildRequest(userAnswers) match {
      case Right(request) =>
        logger.info(s"[SdltCalculationService][calculateStampDutyLandTax] sending calculation request")
        connector.calculateStampDutyLandTax(request).flatMap(_.result.headOption match {
          case Some(result) => Future.successful(Right(result))
          case None         => Future.failed(new IllegalStateException("Calculation response contained no results"))
        })
      case Left(error: MissingDataError) =>
        logger.warn(s"[SdltCalculationService][calculateStampDutyLandTax] missing session data: ${error.message}")
        Future.successful(Left(error))
      case Left(error) =>
        logger.error(s"[SdltCalculationService][calculateStampDutyLandTax] failed to build request: ${error.message}")
        Future.failed(new IllegalStateException(error.message))
    }
}
