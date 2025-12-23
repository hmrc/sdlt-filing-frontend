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

package services.vendor

import connectors.StampDutyLandTaxConnector
import models.vendor.VendorSessionQuestions
import models.{ReturnVersionUpdateRequest, UserAnswers, Vendor}
import play.api.mvc.Results.Redirect
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}

import scala.concurrent.{ExecutionContext, Future}
class VendorCreateOrUpdateService {

  private def vendorOptDetails(userAnswers: UserAnswers,
                               sessionData: VendorSessionQuestions): Option[(String, Option[String])] = {
    for {
      fullReturn <- userAnswers.fullReturn
      vendorList <- fullReturn.vendor
      vendorID <- sessionData.vendorCurrent.vendorID
      vendor <- vendorList.find(_.vendorID.contains(vendorID))
      vendorReturnRef <- vendor.vendorResourceRef
    } yield (vendorReturnRef, vendor.nextVendorID)

  }

  def result(userAnswers: UserAnswers,
             sessionData: VendorSessionQuestions,
             backendConnector: StampDutyLandTaxConnector,
             vendorRequestService: VendorRequestService)(implicit ec: ExecutionContext, hc: HeaderCarrier, request: Request[_]): Future[Result] = {

    val (vendorList, purchaserList) = userAnswers.fullReturn match {
      case Some(fr) => (fr.vendor.getOrElse(Seq.empty), fr.purchaser.getOrElse(Seq.empty))
      case None => (Seq.empty, Seq.empty)
    }
    val errorCalc: Boolean = (vendorList.length + purchaserList.length) < 99

    for {
      vendor <- Vendor.from(Some(userAnswers))
      returnId = userAnswers.returnId.getOrElse(
        throw new NotFoundException("Return ID is required")
      )
      _ <- (sessionData.vendorCurrent.vendorID.isDefined, errorCalc) match {
        case (true, _) =>
          ReturnVersionUpdateRequest.from(userAnswers).flatMap { updateReturnVersionRequest =>
            backendConnector.updateReturnVersion(updateReturnVersionRequest).flatMap { returnVersion =>
              if (returnVersion.newVersion.isDefined) {
                backendConnector.updateVendor(
                  vendorRequestService.convertToUpdateVendorRequest(
                    vendor, userAnswers.storn, returnId,
                    vendorOptDetails(userAnswers, sessionData).map(_._1).getOrElse(
                      throw new IllegalStateException("Vendor not found in full return")
                    ),
                    vendorOptDetails(userAnswers, sessionData).map(_._2).getOrElse(
                      throw new IllegalStateException("Vendor not found in full return")
                    )
                  )
                )
              } else {
                Future.failed(new IllegalStateException("Return version update did not produce a new version"))
              }
            }
          }
        case (false, true) =>
          backendConnector.createVendor(
            vendorRequestService.convertToVendorRequest(
              vendor,
              userAnswers.storn,
              returnId
            )
          )
        case (false, false) => //TODO: Redirect to above 99 vendors error page
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    } yield {
      Redirect(controllers.vendor.routes.VendorOverviewController.onPageLoad())
    }
  }
}