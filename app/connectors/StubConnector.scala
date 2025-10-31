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
import models.{PrelimReturn, ReturnId}
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import play.api.mvc.Request
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, StringContextOps, UpstreamErrorResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StubConnector @Inject()(val http: HttpClientV2,
                              val config: FrontendAppConfig)
                             (implicit ec: ExecutionContext) {

  lazy val logger: Logger = LoggerFactory.getLogger(getClass)

  lazy val sdltStubUrl: String = config.baseUrl("stamp-duty-land-tax-stub")

  def stubPremlimQuestions(returnId: String)(implicit hc: HeaderCarrier,
                                             request: Request[_]): Future[PrelimReturn] = {
    http.get(url"$sdltStubUrl/stamp-duty-land-tax-stub/prelim/returns?returnId=$returnId")
      .execute[PrelimReturn]
      .recover{
        case e => throw logResponse(e, "stubPrelimQuestions")
      }
  }

  def stubPrelimReturnId(prelimReturn: PrelimReturn)(implicit hc: HeaderCarrier,
                         request: Request[_]): Future[ReturnId] = {
    http.post(url"$sdltStubUrl/stamp-duty-land-tax-stub/prelim/returns")
      .withBody(Json.toJson(prelimReturn))
      .execute[Either[UpstreamErrorResponse, ReturnId]]
      .flatMap {
        case Right(resp) =>
          Future.successful(
            resp)
        case Left(error) =>
          Future.failed(error)
      }
      .recover{
        case e => throw logResponse(e, "stubPrelimReturnId")
      }
  }

  private def logResponse(e: Throwable, method: String): Throwable = {
    logger.error(s"[$method] Error occurred: ${e.getMessage}", e)
    e
  }
}