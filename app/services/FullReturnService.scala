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

import connectors.StampDutyLandTaxConnector
import models.land.LandInterestTransferredOrCreated
import models.{FullReturn, GetReturnByRefRequest, Land}
import org.slf4j.{Logger, LoggerFactory}
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FullReturnService @Inject()(backendConnector: StampDutyLandTaxConnector)(implicit ec: ExecutionContext) {

  val logger: Logger = LoggerFactory.getLogger(getClass)

  private val validLandInterests: Set[String] =
    LandInterestTransferredOrCreated.values.map(_.toString).toSet

  private def stripInvalidLandInterest(fullReturn: FullReturn): FullReturn = {
    val cleanedLands: Option[Seq[Land]] =
      fullReturn.land.map(_.map { land =>
        land.interestCreatedTransferred match {
          case Some(value) if !validLandInterests.contains(value) =>
            logger.info(
              s"[FullReturnService][stripInvalidLandInterest] Removing invalid " +
                s"interestCreatedTransferred '$value' for landID: ${land.landID}"
            )
            land.copy(interestCreatedTransferred = None)
          case _ => land
        }
      })

    fullReturn.copy(land = cleanedLands)
  }

  def getFullReturn(getReturnByRefRequest: GetReturnByRefRequest)
                   (implicit hc: HeaderCarrier, request: Request[_]): Future[FullReturn] = {
    logger.info("[getFullReturnBE] Getting Full Return")
    backendConnector.getFullReturn(getReturnByRefRequest).map(stripInvalidLandInterest)
  }
}