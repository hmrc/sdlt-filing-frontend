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
import models.*
import models.prelimQuestions.PrelimReturn
import models.vendor.*
import models.purchaser.*
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

  def createVendor(vendorRequest: CreateVendorRequest)(implicit hc: HeaderCarrier,
                                               request: Request[_]): Future[CreateVendorReturn] = {
    http.post(url"$activeBase/filing/create/vendor")
      .withBody(Json.toJson(vendorRequest))
      .execute[Either[UpstreamErrorResponse, CreateVendorReturn]]
      .flatMap {
        case Right(resp) =>
          Future.successful(
            resp)
        case Left(error) =>
          Future.failed(error)
      }
      .recover {
        case e => throw logResponse(e, "[StampDutyLandTaxConnector][createVendor]")
      }
  }

  def updateVendor(updateVendorRequest: UpdateVendorRequest)(implicit hc: HeaderCarrier,
                                                       request: Request[_]): Future[UpdateVendorReturn] = {
    http.post(url"$activeBase/filing/update/vendor")
      .withBody(Json.toJson(updateVendorRequest))
      .execute[Either[UpstreamErrorResponse, UpdateVendorReturn]]
      .flatMap {
        case Right(resp) =>
          Future.successful(
            resp)
        case Left(error) =>
          Future.failed(error)
      }
      .recover {
        case e => throw logResponse(e, "[StampDutyLandTaxConnector][updateVendor]")
      }
  }

  def deleteVendor(deleteVendorRequest: DeleteVendorRequest)(implicit hc: HeaderCarrier,
                                                             request: Request[_]): Future[DeleteVendorReturn] = {
    http.post(url"$activeBase/filing/delete/vendor")
      .withBody(Json.toJson(deleteVendorRequest))
      .execute[Either[UpstreamErrorResponse, DeleteVendorReturn]]
      .flatMap {
        case Right(resp) =>
          Future.successful(
            resp)
        case Left(error) =>
          Future.failed(error)
      }
      .recover {
        case e => throw logResponse(e, "[StampDutyLandTaxConnector][deleteVendor]")
      }
  }

  def createReturnAgent(createReturnAgentRequest: CreateReturnAgentRequest)(implicit hc: HeaderCarrier,
                                                       request: Request[_]): Future[CreateReturnAgentReturn] = {
    http.post(url"$activeBase/filing/create/return-agent")
      .withBody(Json.toJson(createReturnAgentRequest))
      .execute[Either[UpstreamErrorResponse, CreateReturnAgentReturn]]
      .flatMap {
        case Right(resp) =>
          Future.successful(
            resp)
        case Left(error) =>
          Future.failed(error)
      }
      .recover {
        case e => throw logResponse(e, "[StampDutyLandTaxConnector][createReturnAgent]")
      }
  }

  def updateReturnAgent(updateReturnAgentRequest: UpdateReturnAgentRequest)(implicit hc: HeaderCarrier,
                                                             request: Request[_]): Future[UpdateReturnAgentReturn] = {
    http.post(url"$activeBase/filing/update/return-agent")
      .withBody(Json.toJson(updateReturnAgentRequest))
      .execute[Either[UpstreamErrorResponse, UpdateReturnAgentReturn]]
      .flatMap {
        case Right(resp) =>
          Future.successful(
            resp)
        case Left(error) =>
          Future.failed(error)
      }
      .recover {
        case e => throw logResponse(e, "[StampDutyLandTaxConnector][updateReturnAgent]")
      }
  }

  def deleteReturnAgent(deleteReturnAgentRequest: DeleteReturnAgentRequest)(implicit hc: HeaderCarrier,
                                                             request: Request[_]): Future[DeleteReturnAgentReturn] = {
    http.post(url"$activeBase/filing/delete/return-agent")
      .withBody(Json.toJson(deleteReturnAgentRequest))
      .execute[Either[UpstreamErrorResponse, DeleteReturnAgentReturn]]
      .flatMap {
        case Right(resp) =>
          Future.successful(
            resp)
        case Left(error) =>
          Future.failed(error)
      }
      .recover {
        case e => throw logResponse(e, "[StampDutyLandTaxConnector][deleteReturnAgent]")
      }
  }

  def updateReturnVersion(updateReturnVersionRequest: ReturnVersionUpdateRequest)(implicit hc: HeaderCarrier,
                                                                            request: Request[_]): Future[ReturnVersionUpdateReturn] = {
    http.post(url"$activeBase/filing/update/return-version")
      .withBody(Json.toJson(updateReturnVersionRequest))
      .execute[Either[UpstreamErrorResponse, ReturnVersionUpdateReturn]]
      .flatMap {
        case Right(resp) =>
          Future.successful(
            resp)
        case Left(error) =>
          Future.failed(error)
      }
      .recover {
        case e => throw logResponse(e, "[StampDutyLandTaxConnector][updateReturnVersion]")
      }
  }

  def createPurchaser(purchaserRequest: CreatePurchaserRequest)(implicit hc: HeaderCarrier,
                                                       request: Request[_]): Future[CreatePurchaserReturn] = {
    http.post(url"$activeBase/filing/create/purchaser")
      .withBody(Json.toJson(purchaserRequest))
      .execute[Either[UpstreamErrorResponse, CreatePurchaserReturn]]
      .flatMap {
        case Right(resp) =>
          Future.successful(
            resp)
        case Left(error) =>
          Future.failed(error)
      }
      .recover {
        case e => throw logResponse(e, "[StampDutyLandTaxConnector][createPurchaser]")
      }
  }

  def updatePurchaser(updatePurchaserRequest: UpdatePurchaserRequest)(implicit hc: HeaderCarrier,
                                                             request: Request[_]): Future[UpdatePurchaserReturn] = {
    http.post(url"$activeBase/filing/update/purchaser")
      .withBody(Json.toJson(updatePurchaserRequest))
      .execute[Either[UpstreamErrorResponse, UpdatePurchaserReturn]]
      .flatMap {
        case Right(resp) =>
          Future.successful(
            resp)
        case Left(error) =>
          Future.failed(error)
      }
      .recover {
        case e => throw logResponse(e, "[StampDutyLandTaxConnector][updatePurchaser]")
      }
  }

  def deletePurchaser(deletePurchaserRequest: DeletePurchaserRequest)(implicit hc: HeaderCarrier,
                                                             request: Request[_]): Future[DeletePurchaserReturn] = {
    http.post(url"$activeBase/filing/delete/purchaser")
      .withBody(Json.toJson(deletePurchaserRequest))
      .execute[Either[UpstreamErrorResponse, DeletePurchaserReturn]]
      .flatMap {
        case Right(resp) =>
          Future.successful(
            resp)
        case Left(error) =>
          Future.failed(error)
      }
      .recover {
        case e => throw logResponse(e, "[StampDutyLandTaxConnector][deletePurchaser]")
      }
  }

  def createCompanyDetails(companyDetailsRequest: CreateCompanyDetailsRequest)(implicit hc: HeaderCarrier,
                                                                request: Request[_]): Future[CreateCompanyDetailsReturn] = {
    http.post(url"$activeBase/filing/create/company-details")
      .withBody(Json.toJson(companyDetailsRequest))
      .execute[Either[UpstreamErrorResponse, CreateCompanyDetailsReturn]]
      .flatMap {
        case Right(resp) =>
          Future.successful(
            resp)
        case Left(error) =>
          Future.failed(error)
      }
      .recover {
        case e => throw logResponse(e, "[StampDutyLandTaxConnector][createCompanyDetails]")
      }
  }

  def updateCompanyDetails(updateCompanyDetailsRequest: UpdateCompanyDetailsRequest)(implicit hc: HeaderCarrier,
                                                                      request: Request[_]): Future[UpdateCompanyDetailsReturn] = {
    http.post(url"$activeBase/filing/update/company-details")
      .withBody(Json.toJson(updateCompanyDetailsRequest))
      .execute[Either[UpstreamErrorResponse, UpdateCompanyDetailsReturn]]
      .flatMap {
        case Right(resp) =>
          Future.successful(
            resp)
        case Left(error) =>
          Future.failed(error)
      }
      .recover {
        case e => throw logResponse(e, "[StampDutyLandTaxConnector][updateCompanyDetails]")
      }
  }

  def deleteCompanyDetails(deleteCompanyDetailsRequest: DeleteCompanyDetailsRequest)(implicit hc: HeaderCarrier,
                                                                      request: Request[_]): Future[DeleteCompanyDetailsReturn] = {
    http.post(url"$activeBase/filing/delete/company-details")
      .withBody(Json.toJson(deleteCompanyDetailsRequest))
      .execute[Either[UpstreamErrorResponse, DeleteCompanyDetailsReturn]]
      .flatMap {
        case Right(resp) =>
          Future.successful(
            resp)
        case Left(error) =>
          Future.failed(error)
      }
      .recover {
        case e => throw logResponse(e, "[StampDutyLandTaxConnector][deleteCompanyDetails]")
      }
  }



  private def logResponse(e: Throwable, method: String): Throwable = {
    logger.error(s"[$method] Error occurred: ${e.getMessage}", e)
    e
  }
}