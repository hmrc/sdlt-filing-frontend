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

import com.google.inject.Inject
import connectors.StampDutyLandTaxConnector
import models.purchaser.{DeleteCompanyDetailsRequest, UpdatePurchaserRequest}
import models.requests.DataRequest
import models.{GetReturnByRefRequest, NormalMode, ReturnInfo, ReturnInfoRequest, ReturnVersionUpdateRequest, UserAnswers}
import navigation.Navigator
import pages.purchaser.{ChangePurchaserOnePage, ConfirmChangeOfMainPurchaserPage}
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Result}
import repositories.SessionRepository
import services.FullReturnService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class PurchaserUpdateMainPurchaserService @Inject()(
                                                     backendConnector: StampDutyLandTaxConnector,
                                                     purchaserService: PurchaserService,
                                                     populatePurchaserService: PopulatePurchaserService,
                                                     sessionRepository: SessionRepository,
                                                     fullReturnService: FullReturnService,
                                                     navigator: Navigator
                                                   ) extends Logging {

  private def updateNewVersion(
                                userAnswers: UserAnswers,
                                version: Option[Int] = None)
                              (implicit hc: HeaderCarrier, ec: ExecutionContext, request: DataRequest[AnyContent]): Future[Int] = {

    logger.info(s"[PurchaserUpdateMainPurchaserService][updateNewVersion] version passed in: $version")

    for {
      updateReq <- ReturnVersionUpdateRequest.from(userAnswers, version)
      versionResponse <- backendConnector.updateReturnVersion(updateReq)
      newVersion <- versionResponse.newVersion match {
        case Some(v) =>
          logger.info(s"[PurchaserUpdateMainPurchaserService][updateNewVersion] update return version response version: $v")
          Future.successful(v)

        case None => Future.failed(new IllegalStateException("Return version was not updated (newVersion missing)"))
      }
    } yield newVersion
  }

  private def updateReturnInfo(
                                userAnswers: UserAnswers,
                                returnInfo: ReturnInfo,
                              )(implicit request: DataRequest[AnyContent], hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] =
    for {
      req <- ReturnInfoRequest.from(userAnswers = userAnswers, returnInfo = returnInfo)
      returnInfoReturn <- backendConnector.updateReturnInfo(req)
    } yield {
      if returnInfoReturn.updated then
        logger.info(s"[PurchaserUpdateMainPurchaserService][updateReturnInfo] ReturnInfo has been updated with : ${returnInfo.mainPurchaserID}")
    }


  private def updateOldMainPurchaserDetails(
                                             userAnswers: UserAnswers,
                                             oldMainPurchaserId: Option[String]
                                           )(implicit hc: HeaderCarrier, ec: ExecutionContext, request: DataRequest[AnyContent]): Future[Unit] = {

    oldMainPurchaserId.fold {
      Future.failed(new NoSuchElementException("Old main purchaser id is missing"))
    } { purchaserId =>
      val purchasers = userAnswers.fullReturn.flatMap(_.purchaser).getOrElse(Seq.empty)
      purchaserService.findById(purchasers, purchaserId) match {
        case Some(purchaserToBeUpdated) =>
          for {
            updateRequest <- UpdatePurchaserRequest.fromMainToEssential(userAnswers, purchaserToBeUpdated)
            updateResponse <- backendConnector.updatePurchaser(updateRequest)
            _ <- (
              if (purchaserService.isMainPurchaserCompany(purchaserToBeUpdated)) {
                for {
                  deleteCompanyDetailsRequest <- DeleteCompanyDetailsRequest.from(userAnswers)
                  deleteCompanyDetailsReturn <- backendConnector.deleteCompanyDetails(deleteCompanyDetailsRequest)
                } yield {
                  if deleteCompanyDetailsReturn.deleted then
                    logger.info(s"[PurchaserUpdateMainPurchaserService][updateOldMainPurchaserDetails] successfully deleted company details")
                }
              } else {
                Future.unit
              }
              )
          } yield {
            if updateResponse.updated then
            logger.info(s"[PurchaserUpdateMainPurchaserService][updateOldMainPurchaserDetails] successfully updated main purchaser details with " +
              s"UpdatePurchaserRequest: $updateRequest")
          }

        case None =>
          Future.failed(new NoSuchElementException(s"Purchaser with id $purchaserId not found"))
      }
    }
  }

  private def populatePurchaser(userAnswers: UserAnswers, purchaserId: String)(implicit ec: ExecutionContext, hc: HeaderCarrier, request: DataRequest[AnyContent]): Future[Result] = {
    val maybePurchaser = userAnswers.fullReturn
      .flatMap(_.purchaser)
      .flatMap(_.find(_.purchaserID.contains(purchaserId)))

    val maybeReturnId:Option[String] = userAnswers.returnId

    (maybePurchaser, maybeReturnId) match {
      case (Some(purchaser), Some(id)) =>
        for {
          fullReturn <- fullReturnService.getFullReturn(GetReturnByRefRequest(returnResourceRef = id, storn = userAnswers.storn))
          updatedUserAnswers = UserAnswers(id = request.userId, returnId = Some(id), fullReturn = Some(fullReturn), storn = userAnswers.storn)
          updatedAnswers <- Future.fromTry(
            populatePurchaserService.populatePurchaserInSession(purchaser,
              purchaserId, updatedUserAnswers))
          _ <- sessionRepository.set(updatedAnswers)
        } yield Redirect(navigator.nextPage(ConfirmChangeOfMainPurchaserPage, NormalMode, updatedAnswers))

      case _ =>
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }
  }

  def updateMainPurchaser(userAnswers: UserAnswers)(implicit request: DataRequest[AnyContent], hc: HeaderCarrier, ec: ExecutionContext): Future[Result] = {

    val newMainPurchaserId: Option[String] = userAnswers.get(ChangePurchaserOnePage)
      userAnswers.fullReturn
        .flatMap(_.returnInfo)
        .fold {
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        } { returnInfo =>
          newMainPurchaserId match {
            case Some(purchId) =>
              for {
                _ <- updateNewVersion(userAnswers)
                _ <- updateReturnInfo(userAnswers, returnInfo.copy(mainPurchaserID = Some(purchId)))
                _ <- updateOldMainPurchaserDetails(userAnswers, oldMainPurchaserId = returnInfo.mainPurchaserID)
                result <- populatePurchaser(userAnswers, purchId)
              } yield {
                logger.info(s"[PurchaserUpdateMainPurchaserService][updateMainPurchaser] successfully updated main purchaser " +
                  s"from ${returnInfo.mainPurchaserID} to $purchId.")
                result
              }
            case None =>
              Future.successful(Redirect(controllers.purchaser.routes.ChangePurchaserOneController.onPageLoad()))
          }
        }.recover {
          case e: Throwable =>
            logger.info(s"[PurchaserUpdateMainPurchaserService][updateMainPurchaser] failed to update main purchaser: ${e.getMessage}. Redirecting to journey recovery")
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
  }
}
