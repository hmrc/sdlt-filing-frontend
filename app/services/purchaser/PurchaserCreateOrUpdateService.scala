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

package services.purchaser

import connectors.StampDutyLandTaxConnector
import models.purchaser.{CreatePurchaserRequest, PurchaserSessionQuestions, UpdatePurchaserRequest}
import models.{CompanyDetails, Purchaser, ReturnVersionUpdateRequest, UserAnswers}
import play.api.mvc.Results.Redirect
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.{ExecutionContext, Future}

class PurchaserCreateOrUpdateService {

  val logger: Logger = LoggerFactory.getLogger(getClass)

  private def purchaserOptDetails(userAnswers: UserAnswers,
                                  sessionData: PurchaserSessionQuestions): Option[(String, Option[String])] =
    for {
      fullReturn    <- userAnswers.fullReturn
      purchaserList <- fullReturn.purchaser
      purchaserID   <- sessionData.purchaserCurrent.purchaserAndCompanyId.map(_.purchaserID)
      purchaser     <- purchaserList.find(_.purchaserID.contains(purchaserID))
      resourceRef   <- purchaser.purchaserResourceRef
    } yield (resourceRef, purchaser.nextPurchaserID)

  private def purchaserResourceRef(userAnswers: UserAnswers, sessionData: PurchaserSessionQuestions): String =
    purchaserOptDetails(userAnswers, sessionData)
      .map(_._1)
      .getOrElse("purchaser not found in full return")

  private def requireReturnId(userAnswers: UserAnswers): String =
    userAnswers.returnId.getOrElse(throw new NotFoundException("Return ID is required"))

  def result(userAnswers: UserAnswers,
             sessionData: PurchaserSessionQuestions,
             backendConnector: StampDutyLandTaxConnector,
             purchaserRequestService: PurchaserRequestService,
             purchaserService: PurchaserService)
            (implicit ec: ExecutionContext, hc: HeaderCarrier, request: Request[_]): Future[Result] = {

    val purchaserCount = userAnswers.fullReturn
      .flatMap(_.purchaser)
      .map(_.length)
      .getOrElse(0)

    val hasPurchaserId = sessionData.purchaserCurrent.purchaserAndCompanyId.map(_.purchaserID).isDefined

    logger.info(s"[PurchaserCreateOrUpdateService][result] hasPurchaserId=$hasPurchaserId, purchaserCount=$purchaserCount")

    val resultFuture = (hasPurchaserId, purchaserCount < 99) match {
      case (true, _) =>
        logger.info("[PurchaserCreateOrUpdateService][result] Routing to UPDATE purchaser")
        callUpdatePurchaser(backendConnector, purchaserRequestService, purchaserService, userAnswers, sessionData)
      case (false, true) =>
        logger.info("[PurchaserCreateOrUpdateService][result] Routing to CREATE purchaser")
        callCreatePurchaser(backendConnector, purchaserRequestService, purchaserService, userAnswers, sessionData)
      case _ =>
        logger.warn("[PurchaserCreateOrUpdateService][result] Purchaser count >= 99, skipping create/update")
        Future.successful(Redirect(controllers.purchaser.routes.PurchaserOverviewController.onPageLoad()))
    }

    resultFuture.recover {
      case ex =>
        logger.error(s"[PurchaserCreateOrUpdateService][result] Failed to create/update purchaser: ${ex.getMessage}", ex)
        Redirect(controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad())
    }
  }

  def callUpdatePurchaser(backendConnector: StampDutyLandTaxConnector,
                          purchaserRequestService: PurchaserRequestService,
                          purchaserService: PurchaserService,
                          userAnswers: UserAnswers,
                          sessionData: PurchaserSessionQuestions)
                         (implicit ec: ExecutionContext, hc: HeaderCarrier, request: Request[_]): Future[Result] = {

    for {
      updateReturnVersionRequest <- ReturnVersionUpdateRequest.from(userAnswers)
      returnVersion              <- backendConnector.updateReturnVersion(updateReturnVersionRequest)
      _                          <- if (returnVersion.newVersion.isEmpty)
        Future.failed(new IllegalStateException("Return version update did not produce a new version"))
      else Future.unit
      companyDetails             <- CompanyDetails.from(userAnswers)
      purchaser                  <- Purchaser.from(Some(userAnswers), logger)
      returnId                    = requireReturnId(userAnswers)
      updateRequest              <- UpdatePurchaserRequest.from(userAnswers, purchaser)
      updateResponse             <- backendConnector.updatePurchaser(updateRequest)
      _                          <- if (!updateResponse.updated)
        Future.failed(new IllegalStateException("Purchaser update failed - backend returned updated = false"))
      else {
        logger.info(s"[callUpdatePurchaser] purchaser being updated: $purchaser")
        logger.info(s"[callUpdatePurchaser] update request: $updateRequest")
        logger.info(s"[callUpdatePurchaser] session data: $sessionData")
        Future.unit
      }
      mainPurchaserID             = userAnswers.fullReturn.flatMap(_.returnInfo.flatMap(_.mainPurchaserID))
      updatingPurchaserId         = sessionData.purchaserCurrent.purchaserAndCompanyId.map(_.purchaserID)
      isMainPurchaser             = mainPurchaserID == updatingPurchaserId
      isCompany                   = purchaser.isCompany.contains("YES")
      companyDetailsExist         = userAnswers.fullReturn.flatMap(_.companyDetails).isDefined
      _                          <- (isMainPurchaser, isCompany, companyDetailsExist) match {
        case (true, true, false) =>
          backendConnector.createCompanyDetails(
            purchaserRequestService.convertToCreateCompanyDetailsRequest(
              companyDetails, userAnswers.storn, returnId, purchaserResourceRef(userAnswers, sessionData)
            )
          )
        case (true, true, true) =>
          backendConnector.updateCompanyDetails(
            purchaserRequestService.convertToUpdateCompanyDetailsRequest(
              companyDetails, userAnswers.storn, returnId, purchaserResourceRef(userAnswers, sessionData)
            )
          )
        case _ => Future.unit
      }
    } yield Redirect(controllers.purchaser.routes.PurchaserOverviewController.onPageLoad())
      .flashing("purchaserUpdated" -> purchaserService.createPurchaserName(purchaser).map(_.fullName).getOrElse(""))
  }

  def callCreatePurchaser(backendConnector: StampDutyLandTaxConnector,
                          purchaserRequestService: PurchaserRequestService,
                          purchaserService: PurchaserService,
                          userAnswers: UserAnswers,
                          sessionData: PurchaserSessionQuestions)
                         (implicit ec: ExecutionContext, hc: HeaderCarrier, request: Request[_]): Future[Result] = {

    for {
      companyDetails        <- CompanyDetails.from(userAnswers)
      purchaser             <- Purchaser.from(Some(userAnswers), logger)
      returnId               = requireReturnId(userAnswers)
      createRequest         <- CreatePurchaserRequest.from(userAnswers, purchaser)
      createPurchaserReturn <- backendConnector.createPurchaser(createRequest)
      doesMainPurchaserExist = userAnswers.fullReturn.flatMap(_.returnInfo.flatMap(_.mainPurchaserID)).isDefined
      _                     <- (purchaser.isCompany, doesMainPurchaserExist) match {
        case (Some("YES"), false) =>
          backendConnector.createCompanyDetails(
            purchaserRequestService.convertToCreateCompanyDetailsRequest(
              companyDetails, userAnswers.storn, returnId, createPurchaserReturn.purchaserResourceRef
            )
          )
        case _ => Future.unit
      }
    } yield Redirect(controllers.purchaser.routes.PurchaserOverviewController.onPageLoad())
      .flashing("purchaserCreated" -> purchaserService.createPurchaserName(purchaser).map(_.fullName).getOrElse(""))
  }
}