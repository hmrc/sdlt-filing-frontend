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

package models.purchaser

import models.{Purchaser, UserAnswers}
import play.api.libs.json.{Json, OFormat}

import scala.concurrent.Future

case class CreatePurchaserRequest(
                                   stornId: String,
                                   returnResourceRef: String,
                                   isCompany: String,
                                   isTrustee: String,
                                   isConnectedToVendor: String,
                                   isRepresentedByAgent: String,
                                   title: Option[String] = None,
                                   surname: Option[String] = None,
                                   forename1: Option[String] = None,
                                   forename2: Option[String] = None,
                                   companyName: Option[String] = None,
                                   houseNumber: Option[String] = None,
                                   address1: String,
                                   address2: Option[String] = None,
                                   address3: Option[String] = None,
                                   address4: Option[String] = None,
                                   postcode: Option[String] = None,
                                   phone: Option[String] = None,
                                   nino: Option[String] = None,
                                   isUkCompany: Option[String] = None,
                                   hasNino: Option[String] = None,
                                   dateOfBirth: Option[String] = None,
                                   registrationNumber: Option[String] = None,
                                   placeOfRegistration: Option[String] = None
                                 )

object CreatePurchaserRequest {
  implicit val format: OFormat[CreatePurchaserRequest] = Json.format[CreatePurchaserRequest]

  def from(userAnswers: UserAnswers, purchaser: Purchaser): Future[CreatePurchaserRequest] = {
    userAnswers.fullReturn match {
      case Some(fullReturn) =>
        (purchaser.isCompany, purchaser.isTrustee, purchaser.isConnectedToVendor, purchaser.isRepresentedByAgent, purchaser.address1) match {
          case (Some(isCompany), Some(isTrustee), Some(isConnectedToVendor), Some(isRepresentedByAgent), Some(address1)) =>
            Future.successful(CreatePurchaserRequest(
              stornId = fullReturn.stornId,
              returnResourceRef = fullReturn.returnResourceRef,
              isCompany = isCompany,
              isTrustee = isTrustee,
              isConnectedToVendor = isConnectedToVendor,
              isRepresentedByAgent = isRepresentedByAgent,
              title = purchaser.title,
              surname = purchaser.surname,
              forename1 = purchaser.forename1,
              forename2 = purchaser.forename2,
              companyName = purchaser.companyName,
              houseNumber = purchaser.houseNumber,
              address1 = address1,
              address2 = purchaser.address2,
              address3 = purchaser.address3,
              address4 = purchaser.address4,
              postcode = purchaser.postcode,
              phone = purchaser.phone,
              nino = purchaser.nino,
              isUkCompany = purchaser.isUkCompany,
              hasNino = purchaser.hasNino,
              dateOfBirth = purchaser.dateOfBirth,
              registrationNumber = purchaser.registrationNumber,
              placeOfRegistration = purchaser.placeOfRegistration
            ))
          case _ => Future.failed(new NoSuchElementException(" Purchaser mandatory Resources not found"))
        }
      case None =>
        Future.failed(new NoSuchElementException("Full return not found"))
    }
  }
  
}

case class CreatePurchaserReturn(
                                  purchaserResourceRef: String,
                                  purchaserId: String
                                )

object CreatePurchaserReturn {
  implicit val format: OFormat[CreatePurchaserReturn] = Json.format[CreatePurchaserReturn]
}

case class UpdatePurchaserRequest(
                                   stornId: String,
                                   returnResourceRef: String,
                                   purchaserResourceRef: String,
                                   isCompany: String,
                                   isTrustee: String,
                                   isConnectedToVendor: String,
                                   isRepresentedByAgent: String,
                                   title: Option[String] = None,
                                   surname: Option[String] = None,
                                   forename1: Option[String] = None,
                                   forename2: Option[String] = None,
                                   companyName: Option[String] = None,
                                   houseNumber: Option[String] = None,
                                   address1: String,
                                   address2: Option[String] = None,
                                   address3: Option[String] = None,
                                   address4: Option[String] = None,
                                   postcode: Option[String] = None,
                                   phone: Option[String] = None,
                                   nino: Option[String] = None,
                                   nextPurchaserId: Option[String] = None,
                                   isUkCompany: Option[String] = None,
                                   hasNino: Option[String] = None,
                                   dateOfBirth: Option[String] = None,
                                   registrationNumber: Option[String] = None,
                                   placeOfRegistration: Option[String] = None
                                 )

object UpdatePurchaserRequest {
  implicit val format: OFormat[UpdatePurchaserRequest] = Json.format[UpdatePurchaserRequest]

  def from(userAnswers: UserAnswers, purchaser: Purchaser): Future[UpdatePurchaserRequest] = {
    userAnswers.fullReturn match {
      case Some(fullReturn) =>
        (purchaser.purchaserResourceRef, purchaser.isCompany, purchaser.isTrustee, purchaser.isConnectedToVendor, purchaser.address1) match {
          case (Some(ref), Some(isCompany), Some(isTrustee), Some(isConnectedToVendor), Some(address1)) =>
            Future.successful(UpdatePurchaserRequest(
              stornId = fullReturn.stornId,
              purchaserResourceRef = ref,
              returnResourceRef = fullReturn.returnResourceRef,
              isCompany = isCompany,
              isTrustee = isTrustee,
              isConnectedToVendor = isConnectedToVendor,
              isRepresentedByAgent = purchaser.isRepresentedByAgent.getOrElse("NO"),
              title = purchaser.title,
              surname = purchaser.surname,
              forename1 = purchaser.forename1,
              forename2 = purchaser.forename2,
              companyName = purchaser.companyName,
              houseNumber = purchaser.houseNumber,
              address1 = address1,
              address2 = purchaser.address2,
              address3 = purchaser.address3,
              address4 = purchaser.address4,
              postcode = purchaser.postcode,
              phone = purchaser.phone,
              nino = purchaser.nino,
              nextPurchaserId = purchaser.nextPurchaserID,
              isUkCompany = purchaser.isUkCompany,
              hasNino = purchaser.hasNino,
              dateOfBirth = purchaser.dateOfBirth,
              registrationNumber = purchaser.registrationNumber,
              placeOfRegistration = purchaser.placeOfRegistration
            ))
          case _ => Future.failed(new NoSuchElementException(" Purchaser mandatory Resources not found"))
        }
      case None =>
        Future.failed(new NoSuchElementException("Full return not found"))
    }
  }
}

case class UpdatePurchaserReturn(
                                  updated: Boolean
                                )

object UpdatePurchaserReturn {
  implicit val format: OFormat[UpdatePurchaserReturn] = Json.format[UpdatePurchaserReturn]
}

case class  DeletePurchaserRequest(
                                   storn: String,
                                   purchaserResourceRef: String,
                                   returnResourceRef: String
                                 )

object DeletePurchaserRequest {
  implicit val format: OFormat[DeletePurchaserRequest] = Json.format[DeletePurchaserRequest]

  def from(userAnswers: UserAnswers, purchaserID: String): Future[DeletePurchaserRequest] = {
    userAnswers.fullReturn match {
      case Some(fullReturn) =>
        fullReturn.purchaser
          .flatMap(_.find(_.purchaserID.contains(purchaserID))) match {
          case Some(purchaser) if purchaser.purchaserResourceRef.isDefined => Future.successful(DeletePurchaserRequest(
            storn = fullReturn.stornId,
            purchaserResourceRef = purchaser.purchaserResourceRef.get,
            returnResourceRef = fullReturn.returnResourceRef,
          ))
          case _ =>
            Future.failed(new NoSuchElementException("Purchaser not found"))
        }
      case None =>
        Future.failed(new NoSuchElementException("Full return not found"))
    }
  }
}

case class DeletePurchaserReturn(
                                  deleted: Boolean
                                )

object DeletePurchaserReturn {
  implicit val format: OFormat[DeletePurchaserReturn] = Json.format[DeletePurchaserReturn]
}

case class CreateCompanyDetailsRequest(
                                        stornId: String,
                                        returnResourceRef: String,
                                        purchaserResourceRef: String,
                                        utr: Option[String] = None,
                                        vatReference: Option[String] = None,
                                        compTypeBank: Option[String] = None,
                                        compTypeBuilder: Option[String] = None,
                                        compTypeBuildsoc: Option[String] = None,
                                        compTypeCentgov: Option[String] = None,
                                        compTypeIndividual: Option[String] = None,
                                        compTypeInsurance: Option[String] = None,
                                        compTypeLocalauth: Option[String] = None,
                                        compTypeOcharity: Option[String] = None,
                                        compTypeOcompany: Option[String] = None,
                                        compTypeOfinancial: Option[String] = None,
                                        compTypePartship: Option[String] = None,
                                        compTypeProperty: Option[String] = None,
                                        compTypePubliccorp: Option[String] = None,
                                        compTypeSoletrader: Option[String] = None,
                                        compTypePenfund: Option[String] = None
                                      )

object CreateCompanyDetailsRequest {
  implicit val format: OFormat[CreateCompanyDetailsRequest] = Json.format[CreateCompanyDetailsRequest]
}

case class CreateCompanyDetailsReturn(
                                       companyDetailsId: String
                                     )

object CreateCompanyDetailsReturn {
  implicit val format: OFormat[CreateCompanyDetailsReturn] = Json.format[CreateCompanyDetailsReturn]
}

case class UpdateCompanyDetailsRequest(
                                        stornId: String,
                                        returnResourceRef: String,
                                        purchaserResourceRef: String,
                                        utr: Option[String] = None,
                                        vatReference: Option[String] = None,
                                        compTypeBank: Option[String] = None,
                                        compTypeBuilder: Option[String] = None,
                                        compTypeBuildsoc: Option[String] = None,
                                        compTypeCentgov: Option[String] = None,
                                        compTypeIndividual: Option[String] = None,
                                        compTypeInsurance: Option[String] = None,
                                        compTypeLocalauth: Option[String] = None,
                                        compTypeOcharity: Option[String] = None,
                                        compTypeOcompany: Option[String] = None,
                                        compTypeOfinancial: Option[String] = None,
                                        compTypePartship: Option[String] = None,
                                        compTypeProperty: Option[String] = None,
                                        compTypePubliccorp: Option[String] = None,
                                        compTypeSoletrader: Option[String] = None,
                                        compTypePenfund: Option[String] = None
                                      )

object UpdateCompanyDetailsRequest {
  implicit val format: OFormat[UpdateCompanyDetailsRequest] = Json.format[UpdateCompanyDetailsRequest]
}

case class UpdateCompanyDetailsReturn(
                                       updated: Boolean
                                     )

object UpdateCompanyDetailsReturn {
  implicit val format: OFormat[UpdateCompanyDetailsReturn] = Json.format[UpdateCompanyDetailsReturn]
}

case class DeleteCompanyDetailsRequest(
                                        storn: String,
                                        returnResourceRef: String
                                      )

object DeleteCompanyDetailsRequest {
  implicit val format: OFormat[DeleteCompanyDetailsRequest] = Json.format[DeleteCompanyDetailsRequest]

  def from(userAnswers: UserAnswers, companyDetailsId: String): Future[DeleteCompanyDetailsRequest] = {
    userAnswers.fullReturn match {
      case Some(fullReturn) =>
        fullReturn.companyDetails match {
          case Some(details) if details.companyDetailsID.contains(companyDetailsId) =>
            Future.successful(DeleteCompanyDetailsRequest(
            storn = fullReturn.stornId,
            returnResourceRef = fullReturn.returnResourceRef
          ))
          case _ =>
            Future.failed(new NoSuchElementException("Company Details" +
              "not found"))
        }
      case None =>
        Future.failed(new NoSuchElementException("Full return not found"))
    }
  }
}

case class DeleteCompanyDetailsReturn(
                                       deleted: Boolean
                                     )

object DeleteCompanyDetailsReturn {
  implicit val format: OFormat[DeleteCompanyDetailsReturn] = Json.format[DeleteCompanyDetailsReturn]
}
