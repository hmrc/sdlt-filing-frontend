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

package services.submission

import connectors.StampDutyLandTaxConnector
import models.{FullReturn, Purchaser, UserAnswers, Vendor}
import models.submission.{SubmissionResponse, SubmitRequest}
import models.ukResidency.DeleteResidencyRequest
import pages.submission.{EmailConfirmationPage, SubmissionFailedPage}
import play.api.Logging
import play.api.mvc.Request
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.PropertyTypeHelper.isResidentialProperty

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class ChrisSubmissionService @Inject()(connector: StampDutyLandTaxConnector,
                                       sessionRepository: SessionRepository,
                                       backendConnector: StampDutyLandTaxConnector)
                                      (implicit ec: ExecutionContext) extends Logging {

  def submit(userAnswers: UserAnswers)(implicit hc: HeaderCarrier, request: Request[_]): Future[SubmissionResponse] =
    userAnswers.fullReturn match {
      case None =>
        logger.error("[ChrisSubmissionService][submit] no fullReturn in userAnswers")
        Future.failed(new NoSuchElementException("No fullReturn present for submission"))

      case Some(fullReturn) =>
        val residentialPropertyCheck: Boolean = isResidentialProperty(fullReturn)
        val residencyCheck: Boolean = fullReturn.residency.isDefined
        val resetResidencyCheck: Boolean = !residentialPropertyCheck && residencyCheck

        val deleteResidencyFuture = if (resetResidencyCheck) {
          for {
            req <- DeleteResidencyRequest.from(userAnswers, fullReturn.returnResourceRef)
            _ <- backendConnector.deleteResidency(req)
          } yield ()
        } else {
          Future.unit
        }

        val emailFullReturn = userAnswers.get(EmailConfirmationPage)
        val submitRequest = if (resetResidencyCheck) {
          SubmitRequest(email = emailFullReturn, fullReturn = fullReturn.copy(residency = None))
        } else {
          SubmitRequest(email = emailFullReturn, fullReturn = fullReturn)
        }

        for {
          _ <- deleteResidencyFuture
          result <- connector.submit(submitRequest)
        } yield result
    }

  def submitInBackground(userAnswers: UserAnswers)(implicit hc: HeaderCarrier, request: Request[_]): Unit =
    submit(userAnswers).onComplete {
      case Success(response) =>
        logger.info(s"[ChrisSubmissionService][submitInBackground] completed: $response")
        response match {
          case _: SubmissionResponse.Submitted | _: SubmissionResponse.Acknowledged =>
            ()
          case _: SubmissionResponse.Rejected | _: SubmissionResponse.Failed | _: SubmissionResponse.Retryable =>
            flagSubmissionFailed(userAnswers)
        }

      case Failure(e) =>
        logger.error("[ChrisSubmissionService][submitInBackground] submit failed", e)
        flagSubmissionFailed(userAnswers)
    }

  private def flagSubmissionFailed(userAnswers: UserAnswers): Unit =
    userAnswers.set(SubmissionFailedPage, true).fold(
      errs => logger.error(s"[ChrisSubmissionService] could not set SubmissionFailedPage: $errs"),
      ua   => sessionRepository.set(ua).recover {
        case re => logger.error("[ChrisSubmissionService] failed to persist SubmissionFailedPage", re)
      }
    )
}