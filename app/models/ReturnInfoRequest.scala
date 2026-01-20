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

import play.api.libs.json.{Json, OFormat}

import scala.concurrent.Future

case class ReturnInfoRequest(
                              returnResourceRef: String,
                              storn: String,
                              mainPurchaserID: Option[String] = None,
                              mainVendorID: Option[String] = None,
                              mainLandID: Option[String] = None,
                              IRMarkGenerated: Option[String] = None,
                              landCertForEachProp: Option[String] = None,
                              declaration: Option[String] = None)

object ReturnInfoRequest {
  implicit val format: OFormat[ReturnInfoRequest] = Json.format[ReturnInfoRequest]
  
  def from(userAnswers: UserAnswers, returnInfo: ReturnInfo): Future[ReturnInfoRequest] = {
    userAnswers.fullReturn match {
      case Some(fullReturn) =>
        Future.successful(ReturnInfoRequest(
          returnResourceRef = fullReturn.returnResourceRef,
          storn = fullReturn.stornId,
          mainPurchaserID = returnInfo.mainPurchaserID,
          mainVendorID = returnInfo.mainVendorID,
          mainLandID = returnInfo.mainLandID,
          IRMarkGenerated = returnInfo.IRMarkGenerated,
          landCertForEachProp = returnInfo.landCertForEachProp,
          declaration = returnInfo.declaration
        ))
      case None =>
        Future.failed(new NoSuchElementException("[ReturnInfoRequest] Full return not found"))
    }
  }
  
}


case class ReturnInfoReturn(updated: Boolean)

object ReturnInfoReturn {
  implicit val format: OFormat[ReturnInfoReturn] = Json.format[ReturnInfoReturn]
}
