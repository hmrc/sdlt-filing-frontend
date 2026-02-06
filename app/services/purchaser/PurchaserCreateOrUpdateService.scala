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
import models.purchaser.{PurchaserSessionQuestions}
import models.{CompanyDetails, Purchaser, ReturnVersionUpdateRequest, UserAnswers}

import scala.language.postfixOps
// import play.api.libs.json.Json
import play.api.mvc.Results.Redirect
import play.api.mvc.{Request, Result}
// import play.api.mvc.Results.{Created, Ok, Redirect}
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}

import scala.concurrent.{ExecutionContext, Future}

class PurchaserCreateOrUpdateService {

  private def purchaserOptDetails(userAnswers: UserAnswers,
                               sessionData: PurchaserSessionQuestions): Option[(String, Option[String])] = {
    for {
      fullReturn <- userAnswers.fullReturn
      purchaserList <- fullReturn.purchaser
      purchaserID <- sessionData.purchaserCurrent.purchaserAndCompanyId.map(_.purchaserID)
      purchaser <- purchaserList.find(_.purchaserID.contains(purchaserID))
      purchaserReturnRef <- purchaser.purchaserResourceRef
    } yield (purchaserReturnRef, purchaser.nextPurchaserID)

  }

  def result(userAnswers: UserAnswers,
             sessionData: PurchaserSessionQuestions,
             backendConnector: StampDutyLandTaxConnector,
             purchaserRequestService: PurchaserRequestService)(implicit ec: ExecutionContext, hc: HeaderCarrier, request: Request[_]): Future[Result] = {

    val (vendorList, purchaserList) = userAnswers.fullReturn match {
      case Some(fr) => (fr.purchaser.getOrElse(Seq.empty), fr.purchaser.getOrElse(Seq.empty))
      case None => (Seq.empty, Seq.empty)
    }

    val errorCalc: Boolean = (vendorList.length + purchaserList.length) < 99

    for {
      _ <- (sessionData.purchaserCurrent.purchaserAndCompanyId.map(_.purchaserID).isDefined, errorCalc) match {
        case (true, _) =>
          callUpdatePurchaser(backendConnector, purchaserRequestService, userAnswers, sessionData)
        case (false, true) =>
          callCreatePurchaser(backendConnector, purchaserRequestService, userAnswers, sessionData)
        case (false, false) => //TODO: Redirect to above 99 purchasers error page
         Future.unit
      }
    } yield {
      Redirect(controllers.purchaser.routes.PurchaserOverviewController.onPageLoad())
    }
  }

  def callUpdatePurchaser(
                           backendConnector: StampDutyLandTaxConnector,
                           purchaserRequestService: PurchaserRequestService,
                           userAnswers: UserAnswers,
                           sessionData: PurchaserSessionQuestions)(implicit ec: ExecutionContext, hc: HeaderCarrier, request: Request[_]): Future[Result] = {

    for {
      updateReturnVersionRequest <- ReturnVersionUpdateRequest.from(userAnswers)

      returnVersion <- backendConnector.updateReturnVersion(updateReturnVersionRequest)

      companyDetails <- CompanyDetails.from(userAnswers)

      purchaser <- Purchaser.from(Some(userAnswers))

      returnId = userAnswers.returnId.getOrElse(
        throw new NotFoundException("Return ID is required")
      )
      _ <- if (returnVersion.newVersion.isDefined) {

        for {
          _ <- backendConnector.updatePurchaser(purchaserRequestService.convertToUpdatePurchaserRequest(
            purchaser,
            userAnswers.storn,
            returnId,
            purchaserOptDetails(userAnswers, sessionData).map(_._1).getOrElse("purchaser not found in full return"),
            purchaserOptDetails(userAnswers, sessionData).map(_._2).getOrElse(throw new IllegalStateException("purchaser not found in full return")
            )))

          _ <- {
            val mainPurchaserId = userAnswers.fullReturn.flatMap(_.returnInfo.flatMap(_.mainPurchaserID))
            val updatingPurchaserId = sessionData.purchaserCurrent.purchaserAndCompanyId.map(_.purchaserID)
            val toUpdateOrCreateEligible = mainPurchaserId == updatingPurchaserId
            val companyDetailsExist = userAnswers.fullReturn.flatMap(_.companyDetails).isDefined
            val isCompany = purchaser.isCompany.exists(_.equalsIgnoreCase("YES"))


            (toUpdateOrCreateEligible, isCompany, companyDetailsExist) match {
              case (true, true, false) => backendConnector.createCompanyDetails(
                purchaserRequestService.convertToCreateCompanyDetailsRequest(
                  companyDetails,
                  userAnswers.storn,
                  returnId,
                  purchaserOptDetails(userAnswers, sessionData).map(_._1).getOrElse(
                    "purchaser not found in full return")
                )
              )
              case (true, true, true) => backendConnector.updateCompanyDetails(
                purchaserRequestService.convertToUpdateCompanyDetailsRequest(
                  companyDetails,
                  userAnswers.storn,
                  returnId,
                  purchaserOptDetails(userAnswers, sessionData).map(_._1).getOrElse(
                    "purchaser not found in full return")
                )
              )
              case _ =>
                Future.unit
            }
          }
        } yield ()
      } else {
        Future.failed(new IllegalStateException("Return version update did not produce a new version"))
      }
    } yield Redirect(controllers.purchaser.routes.PurchaserOverviewController.onPageLoad())
  }

  def callCreatePurchaser(
                                   backendConnector: StampDutyLandTaxConnector,
                                   purchaserRequestService: PurchaserRequestService,
                                   userAnswers: UserAnswers,
                                   sessionData: PurchaserSessionQuestions)
                                 (implicit ec: ExecutionContext, hc: HeaderCarrier, request: Request[_]): Future[Result]  = {

    for  {
      companyDetails <- CompanyDetails.from(userAnswers)

      purchaser <- Purchaser.from(Some(userAnswers))

      returnId = userAnswers.returnId.getOrElse(
        throw new NotFoundException("Return ID is required")
      )

      createPurchaserReturn <- backendConnector.createPurchaser(
        purchaserRequestService.convertToCreatePurchaserRequest(
          purchaser,
          userAnswers.storn,
          returnId
        )
      )

      doesMainPurchaserExist = userAnswers.fullReturn.flatMap(_.returnInfo.flatMap(_.mainPurchaserID)).isDefined
      isCompanyNormalized = purchaser.isCompany.map(_.toUpperCase)

      _ <- (isCompanyNormalized, doesMainPurchaserExist) match {
        case (Some("YES"), false) =>
          backendConnector.createCompanyDetails(
            purchaserRequestService.convertToCreateCompanyDetailsRequest(
              companyDetails,
              userAnswers.storn,
              returnId,
              createPurchaserReturn.purchaserResourceRef
            )
          )
        case (_ , _) =>
          Future.unit
      }
    } yield Redirect(controllers.purchaser.routes.PurchaserOverviewController.onPageLoad())
  }

}
