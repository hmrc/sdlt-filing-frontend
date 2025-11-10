/*
 * Copyright 2025 HM Revenue & Customs
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
import models.prelimQuestions.PrelimReturn
import models.{CreateReturnResult, FullReturn, GetReturnByRefRequest, VendorReturn}
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import play.api.mvc.Request
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, StringContextOps, UpstreamErrorResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StampDutyLandTaxConnector @Inject()(val http: HttpClientV2,
                                          val config: FrontendAppConfig)
                                         (implicit ec: ExecutionContext) {

  private lazy val logger: Logger = LoggerFactory.getLogger(getClass)

  private lazy val sdltStubBase: String = config.baseUrl("stamp-duty-land-tax-stub") + "/stamp-duty-land-tax-stub"
  private lazy val sdltBackendBase: String = config.baseUrl("stamp-duty-land-tax") + "/stamp-duty-land-tax"

  def sdltStubUrl: String = sdltStubBase
  def backendUrl: String = sdltBackendBase

  private def activeBase: String =
    if (config.stubBool) sdltStubBase else sdltBackendBase
  
  def getFullReturn(getReturnByRefRequest: GetReturnByRefRequest)(implicit hc: HeaderCarrier,
                                               request: Request[_]): Future[FullReturn] = {
    http.post(url"$activeBase/filing/receive/full-return")
      .withBody(Json.toJson(getReturnByRefRequest))
      .execute[Either[UpstreamErrorResponse, FullReturn]]
      .flatMap {
        case Right(resp) =>
          Future.successful(
            resp)
        case Left(error) =>
          Future.failed(error)
      }
      .recover {
        case e => throw logResponse(e, "[StampDutyLandTaxConnector][getFullReturn]")
      }
  }

  def createReturn(prelimReturn: PrelimReturn)(implicit hc: HeaderCarrier,
                         request: Request[_]): Future[CreateReturnResult] = {
    http.post(url"$activeBase/filing/create/return")
      .withBody(Json.toJson(prelimReturn))
      .execute[Either[UpstreamErrorResponse, CreateReturnResult]]
      .flatMap {
        case Right(resp) =>
          Future.successful(
            resp)
        case Left(error) =>
          Future.failed(error)
      }
      .recover{
        case e => throw logResponse(e, "[StampDutyLandTaxConnector][createReturn]")
      }
  }

  private def logResponse(e: Throwable, method: String): Throwable = {
    logger.error(s"[$method] Error occurred: ${e.getMessage}", e)
    e
  }
}