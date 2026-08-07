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

import com.google.inject.Inject
import connectors.StampDutyLandTaxConnector
import models.requests.DataRequest
import models.{ReturnInfoRequest, UserAnswers}
import play.api.Logging
import play.api.mvc.AnyContent
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class CertificateForEachService @Inject()(backendConnector: StampDutyLandTaxConnector) extends Logging {

  def store(userAnswers: UserAnswers, certificateForEach: Boolean)
           (implicit request: DataRequest[AnyContent], hc: HeaderCarrier, ec: ExecutionContext): Future[UserAnswers] = {

    val answersWithCert = withLandCertForEachProp(userAnswers, if (certificateForEach) "yes" else "no")

    answersWithCert.fullReturn.flatMap(_.returnInfo) match {
      case Some(returnInfo) =>
        for {
          req              <- ReturnInfoRequest.from(userAnswers = answersWithCert, returnInfo = returnInfo)
          returnInfoReturn <- backendConnector.updateReturnInfo(req)
        } yield {
          if returnInfoReturn.updated then
            logger.info(s"[CertificateForEachService][store] ReturnInfo has been updated with : ${returnInfo.landCertForEachProp}")
          else
            logger.warn("[CertificateForEachService][store] backend did not update ReturnInfo")
          answersWithCert
        }

      case None =>
        logger.warn("[CertificateForEachService][store] no ReturnInfo on the full return, nothing to update")
        Future.successful(answersWithCert)
    }
  }

  private def withLandCertForEachProp(userAnswers: UserAnswers, value: String): UserAnswers =
    userAnswers.copy(
      fullReturn = userAnswers.fullReturn.map { fullReturn =>
        fullReturn.copy(
          returnInfo = fullReturn.returnInfo.map(
            _.copy(landCertForEachProp = Some(value))
          )
        )
      }
    )
}
