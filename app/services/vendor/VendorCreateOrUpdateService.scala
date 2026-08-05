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

import com.google.inject.Inject
import connectors.StampDutyLandTaxConnector
import models.vendor.{CreateVendorRequest, UpdateVendorRequest}
import models.{AgentType, DeleteReturnAgentRequest, ReturnVersionUpdateRequest, UserAnswers, Vendor}
import play.api.mvc.Results.Redirect
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.http.HeaderCarrier
import utils.FullName

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class VendorCreateOrUpdateService @Inject()(backendConnector: StampDutyLandTaxConnector)(implicit ex: ExecutionContext){

  def createVendor(userAnswers: UserAnswers)(implicit hc: HeaderCarrier, request: Request[_]): Future[Result] = {
    for {
      vendor <- Vendor.from(userAnswers)
      createVendorRequest <- CreateVendorRequest.from(userAnswers, vendor)
      createVendorReturn <- backendConnector.createVendor(createVendorRequest)
    } yield {
      if (createVendorReturn.vendorId.nonEmpty) {
        Redirect(controllers.vendor.routes.VendorOverviewController.onPageLoad())
          .flashing("vendorCreated" -> FullName.fullName(createVendorRequest.forename1, createVendorRequest.forename2, createVendorRequest.name))
      } else {
        Redirect(controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad())
      }
    }
  }

  def updateVendor(userAnswers: UserAnswers)(implicit hc: HeaderCarrier, request: Request[_]): Future[Result] = {
    for {
      vendor <- Vendor.from(userAnswers)
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

        case Right(updateReturnVersionReturn) if updateReturnVersionReturn.newVersion.isDefined =>
          for {
            updateVendorRequest <- UpdateVendorRequest.from(userAnswers, vendor)
            updateVendorReturn  <- backendConnector.updateVendor(updateVendorRequest)
          } yield
            if (updateVendorReturn.updated)
              Redirect(controllers.vendor.routes.VendorOverviewController.onPageLoad())
                .flashing("vendorUpdated" -> FullName.fullName(updateVendorRequest.forename1, updateVendorRequest.forename2, updateVendorRequest.name))
            else
              Redirect(controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad())

        case Right(_) =>
          Future.successful(
            Redirect(controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad())
          )
      }
    } yield result
  }

  def updateIsRepresentedByAgent(value: Boolean, userAnswers: UserAnswers)(implicit hc: HeaderCarrier, request: Request[_]): Future[Either[Result, Boolean]] = {

    val hasVendorAgentDetails = userAnswers.fullReturn.exists(_.returnAgent.exists(_.exists(_.agentType.contains(AgentType.Vendor.toString))))

    for {
      mainVendor <- Vendor.mainVendorFrom(userAnswers)
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
            updateVendorRequest <- UpdateVendorRequest.from(userAnswers, mainVendor.copy(isRepresentedByAgent = if value then Some("yes") else Some("no")))
            updateVendorReturn  <- backendConnector.updateVendor(updateVendorRequest)
            _                   <- deleteVendorAgentIfRequired(value, hasVendorAgentDetails, userAnswers)
          } yield Right(updateVendorReturn.updated)

        case Right(_) =>
          Future.successful(Right(false))
      }
    } yield result
  }

  private def deleteVendorAgentIfRequired(value: Boolean, hasVendorAgentDetails: Boolean, userAnswers: UserAnswers)(implicit hc: HeaderCarrier, request: Request[_]): Future[Unit] = {
    if (!value && hasVendorAgentDetails) {
      for {
        deleteVendorAgentRequest <- DeleteReturnAgentRequest.from(userAnswers, agentType = AgentType.Vendor)
        _ <- backendConnector.deleteReturnAgent(deleteVendorAgentRequest)
      } yield ()
    } else {
      Future.unit
    }
  }

  def isVendorPurchaserCountBelowMaximum(userAnswers: UserAnswers): Boolean = {
    val (vendorList, purchaserList) = userAnswers.fullReturn match {
      case Some(fr) => (fr.vendor.getOrElse(Seq.empty), fr.purchaser.getOrElse(Seq.empty))
      case None => (Seq.empty, Seq.empty)
    }
    (vendorList.length + purchaserList.length) < 99
  }
}