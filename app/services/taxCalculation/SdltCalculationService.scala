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
import controllers.routes.ReturnTaskListController
import models.requests.DataRequest
import models.UserAnswers
import models.taxCalculation.{CalculationOutcome, MissingDataError, TaxCalculationFlow}
import models.taxCalculation.CalculationOutcome.{Calculated, PreMarch2012, SelfAssessed}
import pages.taxCalculation.TaxCalculationFlowPage
import play.api.Logging
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import queries.Settable
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.SelfAssessedHelper

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SdltCalculationService @Inject()(
                                        connector: SdltCalculationConnector,
                                        sessionRepository: SessionRepository,
                                      )(implicit ec: ExecutionContext) extends Logging  {

  private val selfAssessedHeading: String = "Self-assessed"
  
  def whenInFlow(expected: TaxCalculationFlow)(onAllowed: => Result)(implicit request: DataRequest[?]): Result =
    if (request.userAnswers.get(TaxCalculationFlowPage).contains(expected)) onAllowed
    else Redirect(ReturnTaskListController.onPageLoad())

  def whenInFlowAsync(expected: TaxCalculationFlow)
                     (onAllowed: => Future[Result])
                     (implicit request: DataRequest[?]): Future[Result] =
    if (request.userAnswers.get(TaxCalculationFlowPage).contains(expected)) onAllowed
    else Future.successful(Redirect(ReturnTaskListController.onPageLoad()))

  def calculateStampDutyLandTax(userAnswers: UserAnswers)
                               (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[MissingDataError, CalculationOutcome]] =
    TaxCalcRequestValidator.buildRequest(userAnswers) match {
      case Right(_) if SelfAssessedHelper.isResidentialBeforeMarch2012Date(userAnswers) =>
        logger.info(s"[SdltCalculationService][calculateStampDutyLandTax] effective date is before 22/03/2012")
        Future.successful(Right(PreMarch2012))
      case Right(request) =>
        logger.info(s"[SdltCalculationService][calculateStampDutyLandTax] sending calculation request")
        connector.calculateStampDutyLandTax(request).flatMap(_.result.headOption match {
          case Some(result) if result.resultHeading.contains(selfAssessedHeading) =>
            Future.successful(Right(SelfAssessed))
          case Some(result) =>
            Future.successful(Right(Calculated(result)))
          case None =>
            Future.failed(new IllegalStateException("Calculation response contained no results"))
        })
      case Left(error: MissingDataError) =>
        logger.error(s"[SdltCalculationService][calculateStampDutyLandTax] missing session data: ${error.message}")
        Future.successful(Left(error))
      case Left(error) =>
        logger.error(s"[SdltCalculationService][calculateStampDutyLandTax] failed to build request: ${error.message}")
        Future.failed(new IllegalStateException(error.message))
    }

  def savePenaltiesAndInterestYesNoAnswer(key: Settable[Boolean],
                                          value: Boolean)
                                         (implicit request: DataRequest[?]): Future[Boolean] = {
    for {
      updatedAnswers <- Future.fromTry {
        request.userAnswers.set(key, value)
      }
      persistenceResult <- sessionRepository.set(updatedAnswers)
    } yield
      persistenceResult
  }

}
