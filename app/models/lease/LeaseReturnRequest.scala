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

package models.lease

import models.{Lease, UserAnswers}
import play.api.libs.json.{Json, OFormat}

import scala.concurrent.Future

case class LeasePayload(
                         isAnnualRentOver1000: Option[String],
                         contractEndDate: Option[String],
                         contractStartDate: Option[String],
                         leaseType: Option[String],
                         netPresentValue: Option[String],
                         totalPremiumPayable: Option[String],
                         rentFreePeriod: Option[String],
                         startingRent: Option[String],
                         startingRentEndDate: Option[String],
                         laterRentKnown: Option[String],
                         vatAmount: Option[String]
                       )

object LeasePayload {
  implicit val format: OFormat[LeasePayload] = Json.format[LeasePayload]
}

case class CreateLeaseRequest(
                               stornId: String,
                               returnResourceRef: String,
                               lease: LeasePayload
                             )

object CreateLeaseRequest {
  implicit val format: OFormat[CreateLeaseRequest] = Json.format[CreateLeaseRequest]

  def from(userAnswers: UserAnswers, lease: Lease): Future[CreateLeaseRequest] =
    userAnswers.fullReturn match {
      case Some(fullReturn) =>
        Future.successful(CreateLeaseRequest(
          stornId           = fullReturn.stornId,
          returnResourceRef = fullReturn.returnResourceRef,
          lease = LeasePayload(
            isAnnualRentOver1000 = lease.isAnnualRentOver1000,
            contractEndDate = lease.contractEndDate,
            contractStartDate = lease.contractStartDate,
            leaseType = lease.leaseType,
            netPresentValue = lease.netPresentValue,
            totalPremiumPayable = lease.totalPremiumPayable,
            rentFreePeriod = lease.rentFreePeriod,
            startingRent = lease.startingRent,
            startingRentEndDate = lease.startingRentEndDate,
            laterRentKnown = lease.laterRentKnown,
            vatAmount = lease.VATAmount
          )
        ))
      case None => Future.failed(new NoSuchElementException("Full return not found"))
    }
}

case class CreateLeaseReturn(created: Boolean)

object CreateLeaseReturn {
  implicit val format: OFormat[CreateLeaseReturn] = Json.format[CreateLeaseReturn]
}

case class UpdateLeaseRequest(
                               stornId: String,
                               returnResourceRef: String,
                               lease: LeasePayload
                             )

object UpdateLeaseRequest {
  implicit val format: OFormat[UpdateLeaseRequest] = Json.format[UpdateLeaseRequest]

  def from(userAnswers: UserAnswers, lease: Lease): Future[UpdateLeaseRequest] =
    userAnswers.fullReturn match {
      case Some(fullReturn) =>
        Future.successful(UpdateLeaseRequest(
          stornId = userAnswers.storn,
          returnResourceRef = fullReturn.returnResourceRef,
          LeasePayload(
            isAnnualRentOver1000 = lease.isAnnualRentOver1000,
            contractEndDate = lease.contractEndDate,
            contractStartDate = lease.contractStartDate,
            leaseType = lease.leaseType,
            netPresentValue = lease.netPresentValue,
            totalPremiumPayable = lease.totalPremiumPayable,
            rentFreePeriod = lease.rentFreePeriod,
            startingRent = lease.startingRent,
            startingRentEndDate = lease.startingRentEndDate,
            laterRentKnown = lease.laterRentKnown,
            vatAmount = lease.VATAmount
          )
        ))
      case None => Future.failed(new NoSuchElementException("Full return not found"))
    }
}

case class UpdateLeaseReturn(updated: Boolean)

object UpdateLeaseReturn {
  implicit val format: OFormat[UpdateLeaseReturn] = Json.format[UpdateLeaseReturn]
}

case class DeleteLeaseRequest(
                               storn: String,
                               returnResourceRef: String
                             )

object DeleteLeaseRequest {
  implicit val format: OFormat[DeleteLeaseRequest] = Json.format[DeleteLeaseRequest]

  def from(userAnswers: UserAnswers, leaseId: String): Future[DeleteLeaseRequest] = {
    userAnswers.fullReturn match {
      
      case Some(fullReturn) =>
        
        fullReturn.lease match {
          
          case Some(lease) if lease.leaseID.contains(leaseId) => Future.successful(DeleteLeaseRequest(
            storn = fullReturn.stornId,
            returnResourceRef = fullReturn.returnResourceRef,
          ))
          case _ =>
            Future.failed(new NoSuchElementException("Lease not found"))
        }
      case None =>
        Future.failed(new NoSuchElementException("Full return not found"))
    }
  }
}

case class DeleteLeaseReturn(deleted: Boolean)

object DeleteLeaseReturn {
  implicit val format: OFormat[DeleteLeaseReturn] = Json.format[DeleteLeaseReturn]
}
