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
import models.{AgentType, DeleteReturnAgentRequest, Purchaser, ReturnVersionUpdateRequest, UserAnswers}
import play.api.mvc.Results.Redirect
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.http.HeaderCarrier
import utils.LoggingUtil

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class PurchaserCreateOrUpdateService extends LoggingUtil {
  
  def updatePurchaser(backendConnector: StampDutyLandTaxConnector,
                      purchaserService: PurchaserService,
                      userAnswers: UserAnswers)
                     (implicit ec: ExecutionContext, hc: HeaderCarrier, request: Request[_]): Future[Result] = {

    for {
      updateReturnVersionRequest <- ReturnVersionUpdateRequest.from(userAnswers)
      versionResult <-
        backendConnector
          .updateReturnVersion(updateReturnVersionRequest)
          .map(Right(_))
          .recover { case NonFatal(_) =>
            Left(Redirect(controllers.routes.UpdateReturnVersionErrorController.onPageLoad()))
          }
      result <- versionResult match {
        case Left(errorRedirect) =>
          Future.successful(errorRedirect)

        case Right(_) =>
          for {
            purchaser <- Purchaser.from(Some(userAnswers), logger)
            updateRequest <- UpdatePurchaserRequest.from(userAnswers, purchaser)
            updateResponse <- backendConnector.updatePurchaser(updateRequest)
            _ <- {
              logger.debug(s"[PurchaserCreateOrUpdateService][updatePurchaser] update purchaser request: $updateRequest")
              if updateResponse.updated then
                infoLog(s"[PurchaserCreateOrUpdateService][updatePurchaser] purchaser with reference: ${updateRequest.purchaserResourceRef} has been successfully updated." +
                  s" ReturnId=${updateRequest.returnResourceRef}")
              else
                warnLog(s"[PurchaserCreateOrUpdateService][updatePurchaser] purchaser with reference: ${updateRequest.purchaserResourceRef} has not been updated." +
                  s" ReturnId=${updateRequest.returnResourceRef}")
              Future.unit
            }
            _ <- updateOrCreateCompanyDetails(backendConnector, userAnswers, purchaser, updateRequest.purchaserResourceRef)
          } yield Redirect(controllers.purchaser.routes.PurchaserOverviewController.onPageLoad())
            .flashing("purchaserUpdated" -> purchaserService.createPurchaserName(purchaser).map(_.fullName).getOrElse(""))
      }
    } yield result
  }

  def updateIsRepresentedByAgent(backendConnector: StampDutyLandTaxConnector, value: Boolean, userAnswers: UserAnswers)
                                (implicit ec: ExecutionContext, hc: HeaderCarrier, request: Request[_]): Future[Either[Result, Boolean]] = {

    val hasPurchaserAgentDetails = userAnswers.fullReturn.exists(_.returnAgent.exists(_.exists(_.agentType.contains(AgentType.Purchaser.toString))))

    for {
      mainPurchaser              <- Purchaser.mainPurchaserFrom(userAnswers)
      updateReturnVersionRequest <- ReturnVersionUpdateRequest.from(userAnswers)
      versionResult <-
        backendConnector
          .updateReturnVersion(updateReturnVersionRequest)
          .map(Right(_))
          .recover { case NonFatal(_) =>
            Left(Redirect(controllers.routes.UpdateReturnVersionErrorController.onPageLoad()))
          }
      result <- versionResult match {
        case Left(errorRedirect) =>
          Future.successful(Left(errorRedirect))

        case Right(updateReturnVersionReturn) if updateReturnVersionReturn.newVersion.isDefined =>
          for {
            updatePurchaserRequest <- UpdatePurchaserRequest.from(userAnswers, mainPurchaser.copy(isRepresentedByAgent = if value then Some("yes") else Some("no")))
            updatePurchaserReturn  <- backendConnector.updatePurchaser(updatePurchaserRequest)
            _ <- {
              logger.debug(s"[PurchaserCreateOrUpdateService][updateIsRepresentedByAgent] update purchaser request: $updatePurchaserRequest")
              if updatePurchaserReturn.updated then
                infoLog(s"[PurchaserCreateOrUpdateService][updateIsRepresentedByAgent] purchaser has been successfully updated with isRepresentedByAgent. ReturnId=${updatePurchaserRequest.returnResourceRef}")
              else
                warnLog(s"[PurchaserCreateOrUpdateService][updateIsRepresentedByAgent] purchaser has not been updated. ReturnId=${updatePurchaserRequest.returnResourceRef}")
              Future.unit
            }
            _                      <- deletePurchaserAgentIfRequired(backendConnector, value, hasPurchaserAgentDetails, userAnswers)
          } yield Right(updatePurchaserReturn.updated)

        case Right(_) =>
          Future.successful(Right(false))
      }
    } yield result
  }

  private def deletePurchaserAgentIfRequired(backendConnector: StampDutyLandTaxConnector, value: Boolean, hasPurchaserAgentDetails: Boolean, userAnswers: UserAnswers)(implicit ec: ExecutionContext, hc: HeaderCarrier, request: Request[_]): Future[Unit] = {
    if (!value && hasPurchaserAgentDetails) {
      for {
        deletePurchaserAgentRequest <- DeleteReturnAgentRequest.from(userAnswers, agentType = AgentType.Purchaser)
        deleteReturnAgentReturn <- backendConnector.deleteReturnAgent(deletePurchaserAgentRequest)
      } yield {
        logger.debug(s"[PurchaserCreateOrUpdateService][deletePurchaserAgentIfRequired] delete purchaser agent request: $deletePurchaserAgentRequest")
        if deleteReturnAgentReturn.deleted then
          infoLog(s"[PurchaserCreateOrUpdateService][deletePurchaserAgentIfRequired] purchaser agent has been successfully deleted. ReturnId=${deletePurchaserAgentRequest.returnResourceRef}")
        else  
          warnLog(s"[PurchaserCreateOrUpdateService][deletePurchaserAgentIfRequired] purchaser agent has not been deleted. ReturnId=${deletePurchaserAgentRequest.returnResourceRef}")
      }
    } else {
      Future.unit
    }
  }

  def createPurchaser(backendConnector: StampDutyLandTaxConnector,
                      purchaserService: PurchaserService,
                      userAnswers: UserAnswers)
                     (implicit ec: ExecutionContext, hc: HeaderCarrier, request: Request[_]): Future[Result] = {

    for {
      purchaser             <- Purchaser.from(Some(userAnswers), logger)
      createRequest         <- CreatePurchaserRequest.from(userAnswers, purchaser)
      createPurchaserReturn <- backendConnector.createPurchaser(createRequest)
      _ = {
        logger.debug(s"[PurchaserCreateOrUpdateService][createPurchaser] create purchaser request: $createRequest")
        if createPurchaserReturn.purchaserId.nonEmpty then
          infoLog(s"[PurchaserCreateOrUpdateService][createPurchaser] purchaser has been successfully created." +
            s" PurchaserId=${createPurchaserReturn.purchaserId}. ReturnId=${createRequest.returnResourceRef}")
        else 
          warnLog(s"[PurchaserCreateOrUpdateService][createPurchaser] purchaser has not been created." +
            s" PurchaserId=${createPurchaserReturn.purchaserId}. ReturnId=${createRequest.returnResourceRef}")
      }
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
      val isCompany = purchaser.isCompany.exists(_.equalsIgnoreCase("yes"))
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
            logger.debug(s"[PurchaserCreateOrUpdateService][updateOrCreateCompanyDetails] update company details request: $updateCompanyDetailsRequest")
            if updateCompanyDetailsReturn.updated then
              infoLog(s"[PurchaserCreateOrUpdateService][updateOrCreateCompanyDetails] company details have been successfully updated. ReturnId=${updateCompanyDetailsRequest.returnResourceRef}")
            else
              warnLog(s"[PurchaserCreateOrUpdateService][updateOrCreateCompanyDetails] company details have not been updated. ReturnId=${updateCompanyDetailsRequest.returnResourceRef}")
          }
        } else {
          for {
            createCompanyDetailsRequest <- CreateCompanyDetailsRequest.from(userAnswers, purchaserResourceRef)
            createCompanyDetailsReturn <- backendConnector.createCompanyDetails(createCompanyDetailsRequest)
          } yield {
            logger.debug(s"[PurchaserCreateOrUpdateService][updateOrCreateCompanyDetails] create company details request: $createCompanyDetailsRequest")
            if !createCompanyDetailsReturn.companyDetailsId.isBlank then
              infoLog(s"[PurchaserCreateOrUpdateService][updateOrCreateCompanyDetails] company details have been successfully created." +
                s" CompanyDetailsId=${createCompanyDetailsReturn.companyDetailsId}. ReturnId=${createCompanyDetailsRequest.returnResourceRef}") 
            else  
              warnLog(s"[PurchaserCreateOrUpdateService][updateOrCreateCompanyDetails] company details have not been created." +
                s" CompanyDetailsId=${createCompanyDetailsReturn.companyDetailsId}. ReturnId=${createCompanyDetailsRequest.returnResourceRef}")
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