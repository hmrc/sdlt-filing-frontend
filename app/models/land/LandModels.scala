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

package models.land

import models.{Land, UserAnswers}
import play.api.libs.json.{Json, OFormat}

import scala.concurrent.Future

case class CreateLandRequest(
                              stornId: String,
                              returnResourceRef: String,
                              propertyType: String,
                              interestTransferredCreated: String,
                              houseNumber: Option[String] = None,
                              addressLine1: String,
                              addressLine2: Option[String] = None,
                              addressLine3: Option[String] = None,
                              addressLine4: Option[String] = None,
                              postcode: Option[String] = None,
                              landArea: Option[String] = None,
                              areaUnit: Option[String] = None,
                              localAuthorityNumber: Option[String] = None,
                              mineralRights: Option[String] = None,
                              nlpgUprn: Option[String] = None,
                              willSendPlansByPost: Option[String] = None,
                              titleNumber: Option[String] = None
                            )

object CreateLandRequest {
  implicit val format: OFormat[CreateLandRequest] = Json.format[CreateLandRequest]

  def from(userAnswers: UserAnswers, land: Land): Future[CreateLandRequest] =
    userAnswers.fullReturn match {
      case Some(fullReturn) =>
        (land.propertyType, land.interestCreatedTransferred, land.address1) match {
          case (Some(propertyType), Some(interestTransferred), Some(address1)) =>
            Future.successful(CreateLandRequest(
              stornId = fullReturn.stornId,
              returnResourceRef = fullReturn.returnResourceRef,
              propertyType = propertyType,
              interestTransferredCreated = interestTransferred,
              houseNumber = land.houseNumber,
              addressLine1 = address1,
              addressLine2 = land.address2,
              addressLine3 = land.address3,
              addressLine4 = land.address4,
              postcode = land.postcode,
              landArea = land.landArea,
              areaUnit = land.areaUnit,
              localAuthorityNumber = land.localAuthorityNumber,
              mineralRights = land.mineralRights,
              nlpgUprn = land.NLPGUPRN,
              willSendPlansByPost = land.willSendPlanByPost,
              titleNumber = land.titleNumber
            ))
          case _ => Future.failed(new NoSuchElementException("Land mandatory fields not found"))
        }
      case None => Future.failed(new NoSuchElementException("Full return not found"))
    }
}

case class CreateLandReturn(
                             landResourceRef: String,
                             landId: String
                           )

object CreateLandReturn {
  implicit val format: OFormat[CreateLandReturn] = Json.format[CreateLandReturn]
}

case class UpdateLandRequest(
                              stornId: String,
                              returnResourceRef: String,
                              landResourceRef: String,
                              propertyType: String,
                              interestTransferredCreated: String,
                              houseNumber: Option[String] = None,
                              addressLine1: String,
                              addressLine2: Option[String] = None,
                              addressLine3: Option[String] = None,
                              addressLine4: Option[String] = None,
                              postcode: Option[String] = None,
                              landArea: Option[String] = None,
                              areaUnit: Option[String] = None,
                              localAuthorityNumber: Option[String] = None,
                              mineralRights: Option[String] = None,
                              nlpgUprn: Option[String] = None,
                              willSendPlansByPost: Option[String] = None,
                              titleNumber: Option[String] = None,
                              nextLandId: Option[String] = None
                            )

object UpdateLandRequest {
  implicit val format: OFormat[UpdateLandRequest] = Json.format[UpdateLandRequest]

  def from(userAnswers: UserAnswers, land: Land): Future[UpdateLandRequest] =
    userAnswers.fullReturn match {
      case Some(fullReturn) =>
        (land.landResourceRef, land.propertyType, land.interestCreatedTransferred, land.address1) match {
          case (Some(landRef), Some(propertyType), Some(interestCreatedTransferred), Some(address1)) =>
            Future.successful(UpdateLandRequest(
              stornId = userAnswers.storn,
              returnResourceRef = fullReturn.returnResourceRef,
              landResourceRef = landRef,
              propertyType = propertyType,
              interestTransferredCreated = interestCreatedTransferred,
              houseNumber = land.houseNumber,
              addressLine1 = address1,
              addressLine2 = land.address2,
              addressLine3 = land.address3,
              addressLine4 = land.address4,
              postcode = land.postcode,
              landArea = land.landArea,
              areaUnit = land.areaUnit,
              localAuthorityNumber = land.localAuthorityNumber,
              mineralRights = land.mineralRights,
              nlpgUprn = land.NLPGUPRN,
              willSendPlansByPost = land.willSendPlanByPost,
              titleNumber = land.titleNumber,
              nextLandId = land.nextLandID
        ))
          case _ => Future.failed(new NoSuchElementException("Land mandatory fields not found"))
        }
      case None => Future.failed(new NoSuchElementException("Full return not found"))
    }
}

case class UpdateLandReturn(
                             updated: Boolean
                           )

object UpdateLandReturn {
  implicit val format: OFormat[UpdateLandReturn] = Json.format[UpdateLandReturn]
}

case class DeleteLandRequest(
                              storn: String,
                              returnResourceRef: String,
                              landResourceRef: String
                            )

object DeleteLandRequest {
  implicit val format: OFormat[DeleteLandRequest] = Json.format[DeleteLandRequest]
  def from(userAnswers: UserAnswers, landResourceRef: String): Future[DeleteLandRequest] = {
    userAnswers.fullReturn match {
      case Some(fullReturn) =>
        fullReturn.land.flatMap(_.find(_.landResourceRef.contains(landResourceRef))) match {
          case Some(_) =>
            Future.successful(DeleteLandRequest(
              storn = fullReturn.stornId,
              returnResourceRef = fullReturn.returnResourceRef,
              landResourceRef = landResourceRef
            ))
          case None =>
            Future.failed(new NoSuchElementException("Land not found"))
        }

      case None =>
        Future.failed(new NoSuchElementException("Full return not found"))
    }
  }
}

case class DeleteLandReturn(
                             deleted: Boolean
                           )

object DeleteLandReturn {
  implicit val format: OFormat[DeleteLandReturn] = Json.format[DeleteLandReturn]
}
