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

package models.vendor

import models.UserAnswers
import play.api.libs.json.{Json, OFormat}

import scala.concurrent.Future

case class CreateVendorRequest(
                       stornId: String,
                       returnResourceRef: String,
                       title: Option[String] = None,
                       forename1: Option[String] = None,
                       forename2: Option[String] = None,
                       name: String,
                       houseNumber: Option[String] = None,
                       addressLine1: String,
                       addressLine2: Option[String] = None,
                       addressLine3: Option[String] = None,
                       addressLine4: Option[String] = None,
                       postcode: Option[String] = None,
                       isRepresentedByAgent: String
                       )

object CreateVendorRequest {
  implicit val format: OFormat[CreateVendorRequest] = Json.format[CreateVendorRequest]
}
//mite not need this just have it so that it returns a 201 from the backend?

case class CreateVendorReturn(
                                vendorResourceRef: String,
                                vendorId: String
                              )

object CreateVendorReturn {
  implicit val format: OFormat[CreateVendorReturn] = Json.format[CreateVendorReturn]
}


case class UpdateVendorRequest(
                                stornId: String,
                                returnResourceRef: String,
                                title: Option[String] = None,
                                forename1: Option[String] = None,
                                forename2: Option[String] = None,
                                name: String,
                                houseNumber: Option[String] = None,
                                addressLine1: String,
                                addressLine2: Option[String] = None,
                                addressLine3: Option[String] = None,
                                addressLine4: Option[String] = None,
                                postcode: Option[String] = None,
                                isRepresentedByAgent: String,
                                vendorResourceRef: String,
                                nextVendorId: Option[String] = None
                              )

object UpdateVendorRequest {
  implicit val format: OFormat[UpdateVendorRequest] = Json.format[UpdateVendorRequest]
}

case class UpdateVendorReturn(
                                updated: Boolean
                              )

object UpdateVendorReturn {
  implicit val format: OFormat[UpdateVendorReturn] = Json.format[UpdateVendorReturn]
}



case class DeleteVendorRequest(
                               storn: String,
                               vendorResourceRef: String,
                               returnResourceRef: String
                             )

object DeleteVendorRequest {
  implicit val format: OFormat[DeleteVendorRequest] = Json.format[DeleteVendorRequest]

  def from(userAnswers: UserAnswers, vendorResourceRef: String): Future[DeleteVendorRequest] = {
    userAnswers.fullReturn match {
      case Some(fullReturn) =>
        fullReturn.vendor
          .flatMap(_.find(_.vendorResourceRef.contains(vendorResourceRef))) match {
          case Some(_) => Future.successful(DeleteVendorRequest(
            storn = fullReturn.stornId,
            vendorResourceRef = vendorResourceRef,
            returnResourceRef = fullReturn.returnResourceRef
          ))
          case None =>
            Future.failed(new NoSuchElementException("Vendor not found"))
        }
      case None =>
        Future.failed(new NoSuchElementException("Full return not found"))
    }
  }
}

case class DeleteVendorReturn(
                               deleted: Boolean
                             )

object DeleteVendorReturn {
  implicit val format: OFormat[DeleteVendorReturn] = Json.format[DeleteVendorReturn]
}
