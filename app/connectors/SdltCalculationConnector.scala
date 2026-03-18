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

package connectors

import config.FrontendAppConfig
import models.taxCalculation.{CalculationResponse, SdltCalculationRequest}
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.HttpReads.Implicits.*

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SdltCalculationConnector @Inject()(val http: HttpClientV2,
                                         val config: FrontendAppConfig
                                       )(implicit ec: ExecutionContext) {

  def calculateStampDutyLandTax(request: SdltCalculationRequest)
                               (implicit hc: HeaderCarrier): Future[CalculationResponse] =
    http
      .post(url"${config.sdltCalculationUrl}")
      .withBody(Json.toJson(request))
      .execute[CalculationResponse]
      .map { resp =>
        logger.info(s"[SdltCalculationConnector][calculateStampDutyLandTax] response: $resp")
        resp
      }
      .recoverWith {
        case e: UpstreamErrorResponse =>
          logger.error(s"[SdltCalculationConnector][calculateStampDutyLandTax] upstream error: ${e.statusCode}", e)
          Future.failed(e)
        case e =>
          logger.error("[SdltCalculationConnector][calculateStampDutyLandTax] unexpected error", e)
          Future.failed(e)
      }
}
