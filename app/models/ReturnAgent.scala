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

package models

import models.AgentType.{Purchaser, Vendor}
import models.purchaserAgent.PurchaserAgentSessionQuestions
import models.vendorAgent.VendorAgentSessionQuestions
import play.api.libs.json.{Json, OFormat}

import scala.concurrent.Future

case class CreateReturnAgentRequest(
                                     stornId: String,
                                     returnResourceRef: String,
                                     agentType: String,
                                     name: String,
                                     houseNumber: Option[Int] = None,
                                     addressLine1: String,
                                     addressLine2: Option[String] = None,
                                     addressLine3: Option[String] = None,
                                     addressLine4: Option[String] = None,
                                     postcode: String,
                                     phoneNumber: Option[String] = None,
                                     email: Option[String] = None,
                                     agentReference: Option[String] = None,
                                     isAuthorised: Option[String] = None
                                   )

object CreateReturnAgentRequest {
  implicit val format: OFormat[CreateReturnAgentRequest] = Json.format[CreateReturnAgentRequest]

  def from(userAnswers: UserAnswers, agentType: AgentType): Future[CreateReturnAgentRequest] = {
    userAnswers.fullReturn match {
      case Some(fullReturn) =>
        agentType match {
          case Purchaser =>
            val paSessionQuestions: PurchaserAgentSessionQuestions = (userAnswers.data \ "purchaserAgentCurrent").as[PurchaserAgentSessionQuestions]
            Future.successful(CreateReturnAgentRequest(
              stornId = fullReturn.stornId,
              returnResourceRef = fullReturn.returnResourceRef,
              agentType = agentType.toString,
              name = paSessionQuestions.purchaserAgentName,
              houseNumber = paSessionQuestions.purchaserAgentAddress.houseNumber,
              addressLine1 = paSessionQuestions.purchaserAgentAddress.line1,
              addressLine2 = paSessionQuestions.purchaserAgentAddress.line2,
              addressLine3 = paSessionQuestions.purchaserAgentAddress.line3,
              addressLine4 = paSessionQuestions.purchaserAgentAddress.line4,
              postcode = paSessionQuestions.purchaserAgentAddress.postcode,
              phoneNumber = paSessionQuestions.purchaserAgentContactDetails.flatMap(_.phoneNumber),
              email = paSessionQuestions.purchaserAgentContactDetails.flatMap(_.emailAddress),
              agentReference = paSessionQuestions.purchaserAgentReference,
              isAuthorised = Some(paSessionQuestions.purchaserAgentAuthorised.toUpperCase)
            ))
          case Vendor =>
            val vaSessionQuestions: VendorAgentSessionQuestions = (userAnswers.data \ "vendorAgentCurrent").as[VendorAgentSessionQuestions]
            Future.successful(CreateReturnAgentRequest(
              stornId = fullReturn.stornId,
              returnResourceRef = fullReturn.returnResourceRef,
              agentType = agentType.toString,
              name = vaSessionQuestions.vendorAgentName,
              houseNumber = vaSessionQuestions.vendorAgentAddress.houseNumber,
              addressLine1 = vaSessionQuestions.vendorAgentAddress.line1,
              addressLine2 = vaSessionQuestions.vendorAgentAddress.line2,
              addressLine3 = vaSessionQuestions.vendorAgentAddress.line3,
              addressLine4 = vaSessionQuestions.vendorAgentAddress.line4,
              postcode = vaSessionQuestions.vendorAgentAddress.postcode,
              phoneNumber = vaSessionQuestions.vendorAgentContactDetails.flatMap(_.phoneNumber),
              email = vaSessionQuestions.vendorAgentContactDetails.flatMap(_.emailAddress),
              agentReference = vaSessionQuestions.vendorAgentReference
            ))
        }
      case None =>
        Future.failed(new NoSuchElementException("[CreateReturnAgentRequest] Full return not found"))
    }
  }
}

case class CreateReturnAgentReturn(
                                    returnAgentId: String
                                  )

object CreateReturnAgentReturn {
  implicit val format: OFormat[CreateReturnAgentReturn] = Json.format[CreateReturnAgentReturn]
}


case class UpdateReturnAgentRequest(
                                     stornId: String,
                                     returnResourceRef: String,
                                     agentType: String,
                                     name: String,
                                     houseNumber: Option[Int] = None,
                                     addressLine1: String,
                                     addressLine2: Option[String] = None,
                                     addressLine3: Option[String] = None,
                                     addressLine4: Option[String] = None,
                                     postcode: String,
                                     phoneNumber: Option[String] = None,
                                     email: Option[String] = None,
                                     agentReference: Option[String] = None,
                                     isAuthorised: Option[String] = None
                                   )

object UpdateReturnAgentRequest {
  implicit val format: OFormat[UpdateReturnAgentRequest] = Json.format[UpdateReturnAgentRequest]

  def from(userAnswers: UserAnswers, agentType: AgentType): Future[UpdateReturnAgentRequest] = {
    userAnswers.fullReturn match {
      case Some(fullReturn) =>
        agentType match {
          case Purchaser =>
            val paSessionQuestions: PurchaserAgentSessionQuestions = (userAnswers.data \ "purchaserAgentCurrent").as[PurchaserAgentSessionQuestions]
            Future.successful(UpdateReturnAgentRequest(
              stornId = fullReturn.stornId,
              returnResourceRef = fullReturn.returnResourceRef,
              agentType = agentType.toString,
              name = paSessionQuestions.purchaserAgentName,
              houseNumber = paSessionQuestions.purchaserAgentAddress.houseNumber,
              addressLine1 = paSessionQuestions.purchaserAgentAddress.line1,
              addressLine2 = paSessionQuestions.purchaserAgentAddress.line2,
              addressLine3 = paSessionQuestions.purchaserAgentAddress.line3,
              addressLine4 = paSessionQuestions.purchaserAgentAddress.line4,
              postcode = paSessionQuestions.purchaserAgentAddress.postcode,
              phoneNumber = paSessionQuestions.purchaserAgentContactDetails.flatMap(_.phoneNumber),
              email = paSessionQuestions.purchaserAgentContactDetails.flatMap(_.emailAddress),
              agentReference = paSessionQuestions.purchaserAgentReference,
              isAuthorised = Some(paSessionQuestions.purchaserAgentAuthorised.toUpperCase)
            ))
          case Vendor =>
            val vaSessionQuestions: VendorAgentSessionQuestions = (userAnswers.data \ "vendorAgentCurrent").as[VendorAgentSessionQuestions]
            Future.successful(UpdateReturnAgentRequest(
              stornId = fullReturn.stornId,
              returnResourceRef = fullReturn.returnResourceRef,
              agentType = agentType.toString,
              name = vaSessionQuestions.vendorAgentName,
              houseNumber = vaSessionQuestions.vendorAgentAddress.houseNumber,
              addressLine1 = vaSessionQuestions.vendorAgentAddress.line1,
              addressLine2 = vaSessionQuestions.vendorAgentAddress.line2,
              addressLine3 = vaSessionQuestions.vendorAgentAddress.line3,
              addressLine4 = vaSessionQuestions.vendorAgentAddress.line4,
              postcode = vaSessionQuestions.vendorAgentAddress.postcode,
              phoneNumber = vaSessionQuestions.vendorAgentContactDetails.flatMap(_.phoneNumber),
              email = vaSessionQuestions.vendorAgentContactDetails.flatMap(_.emailAddress),
              agentReference = vaSessionQuestions.vendorAgentReference
            ))
        }
      case None =>
        Future.failed(new NoSuchElementException("[UpdateReturnAgentRequest] Full return not found"))
    }
  }
}

case class UpdateReturnAgentReturn(
                                    updated: Boolean
                                  )

object UpdateReturnAgentReturn {
  implicit val format: OFormat[UpdateReturnAgentReturn] = Json.format[UpdateReturnAgentReturn]
}

case class DeleteReturnAgentRequest(
                                     storn: String,
                                     returnResourceRef: String,
                                     agentType: String
                                   )

object DeleteReturnAgentRequest {
  implicit val format: OFormat[DeleteReturnAgentRequest] = Json.format[DeleteReturnAgentRequest]

  def from(userAnswers: UserAnswers, agentType: AgentType): Future[DeleteReturnAgentRequest] = {
    userAnswers.fullReturn match {
      case Some(fullReturn) =>
        Future.successful(DeleteReturnAgentRequest(
          storn = fullReturn.stornId,
          returnResourceRef = fullReturn.returnResourceRef,
          agentType = agentType.toString
        ))
      case None =>
        Future.failed(new NoSuchElementException("Full return not found"))
    }
  }
}

case class DeleteReturnAgentReturn(
                                    deleted: Boolean
                                  )

object DeleteReturnAgentReturn {
  implicit val format: OFormat[DeleteReturnAgentReturn] = Json.format[DeleteReturnAgentReturn]
}