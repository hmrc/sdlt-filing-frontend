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
import models.purchaser.{CreateCompanyDetailsRequest, CreatePurchaserRequest, UpdateCompanyDetailsRequest, UpdatePurchaserRequest}
import models.{Purchaser, ReturnVersionUpdateRequest, UserAnswers}
import play.api.mvc.Results.Redirect
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.http.HeaderCarrier
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.{ExecutionContext, Future}

class PurchaserCreateOrUpdateService {

  val logger: Logger = LoggerFactory.getLogger(getClass)
  
  def updatePurchaser(backendConnector: StampDutyLandTaxConnector,
                          purchaserService: PurchaserService,
                          userAnswers: UserAnswers)
                         (implicit ec: ExecutionContext, hc: HeaderCarrier, request: Request[_]): Future[Result] = {

    for {
      updateReturnVersionRequest <- ReturnVersionUpdateRequest.from(userAnswers)
      returnVersion              <- backendConnector.updateReturnVersion(updateReturnVersionRequest)
      purchaser                  <- Purchaser.from(Some(userAnswers), logger)
      updateRequest              <- UpdatePurchaserRequest.from(userAnswers, purchaser)
      updateResponse             <- backendConnector.updatePurchaser(updateRequest)
      _                          <- updateOrCreateCompanyDetails(backendConnector, userAnswers, purchaser, updateRequest.purchaserResourceRef)
    } yield Redirect(controllers.purchaser.routes.PurchaserOverviewController.onPageLoad())
      .flashing("purchaserUpdated" -> purchaserService.createPurchaserName(purchaser).map(_.fullName).getOrElse(""))
  }

  def createPurchaser(backendConnector: StampDutyLandTaxConnector,
                          purchaserService: PurchaserService,
                          userAnswers: UserAnswers)
                         (implicit ec: ExecutionContext, hc: HeaderCarrier, request: Request[_]): Future[Result] = {

    for {
      purchaser             <- Purchaser.from(Some(userAnswers), logger)
      createRequest         <- CreatePurchaserRequest.from(userAnswers, purchaser)
      createPurchaserReturn <- backendConnector.createPurchaser(createRequest)
      _                     <- updateOrCreateCompanyDetails(backendConnector, userAnswers, purchaser, createPurchaserReturn.purchaserResourceRef)
    } yield Redirect(controllers.purchaser.routes.PurchaserOverviewController.onPageLoad())
      .flashing("purchaserCreated" -> purchaserService.createPurchaserName(purchaser).map(_.fullName).getOrElse(""))
  }
  
  private def updateOrCreateCompanyDetails(backendConnector: StampDutyLandTaxConnector,
                                           userAnswers: UserAnswers,
                                           purchaser: Purchaser,
                                           purchaserResourceRef: String)
                                          (implicit ec: ExecutionContext, hc: HeaderCarrier, request: Request[_]): Future[Unit] = {
    userAnswers.fullReturn.map { fullReturn =>
      val isCompany = purchaser.isCompany.exists(_.equalsIgnoreCase("YES"))
      val doesMainPurchaserExist = fullReturn.returnInfo.flatMap(_.mainPurchaserID).isDefined
      val isMainPurchaser = (fullReturn.returnInfo.flatMap(_.mainPurchaserID), purchaser.purchaserID) match {
        case (Some(mainId), Some(purchaserId)) => mainId.equals(purchaserId)
        case _ => false
      }
      val companyDetailsExists = fullReturn.companyDetails.isDefined
      if (isCompany && (isMainPurchaser || !doesMainPurchaserExist)) {
        if (companyDetailsExists) {
          for {
            updateCompanyDetailsRequest <- UpdateCompanyDetailsRequest.from(userAnswers, purchaserResourceRef)
            updateCompanyDetailsReturn <- backendConnector.updateCompanyDetails(updateCompanyDetailsRequest)
          } yield {
            if updateCompanyDetailsReturn.updated then
              logger.info(s"[updateOrCreateCompanyDetails] update company details request: $updateCompanyDetailsRequest")
          }
        } else {
          for {
            createCompanyDetailsRequest <- CreateCompanyDetailsRequest.from(userAnswers, purchaserResourceRef)
            createCompanyDetailsReturn <- backendConnector.createCompanyDetails(createCompanyDetailsRequest)
          } yield {
            if !createCompanyDetailsReturn.companyDetailsId.isBlank then
              logger.info(s"[updateOrCreateCompanyDetails] create company details request: $createCompanyDetailsRequest")
          }
        }
      } else {
        Future.unit
      }
    }.getOrElse(Future.failed(new NoSuchElementException("Full return not found")))
  }

  def isVendorPurchaserCountBelowMaximum(userAnswers: UserAnswers): Boolean = {
    val (vendorList, purchaserList) = userAnswers.fullReturn match {
      case Some(fr) => (fr.vendor.getOrElse(Seq.empty), fr.purchaser.getOrElse(Seq.empty))
      case None => (Seq.empty, Seq.empty)
    }
    (vendorList.length + purchaserList.length) < 99
  }
}