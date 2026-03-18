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

case class ReturnVersionUpdateRequest(
                                     storn: String,
                                     returnResourceRef: String,
                                     currentVersion: String
                                   )

object ReturnVersionUpdateRequest {
  implicit val format: OFormat[ReturnVersionUpdateRequest] = Json.format[ReturnVersionUpdateRequest]

  def from(userAnswers: UserAnswers, version: Option[Long] = None): Future[ReturnVersionUpdateRequest] = {
    userAnswers.fullReturn match {
      case Some(fullReturn) =>
        if(version.isDefined) {
          Future.successful(ReturnVersionUpdateRequest(
            storn = userAnswers.storn,
            returnResourceRef = fullReturn.returnResourceRef,
            currentVersion = version.get.toString
          ))
        } else {
          fullReturn.returnInfo.flatMap(_.version) match {
            case Some(version) => Future.successful(ReturnVersionUpdateRequest(
              storn = userAnswers.storn,
              returnResourceRef = fullReturn.returnResourceRef,
              currentVersion = version
            ))
            case None =>
              Future.failed(new NoSuchElementException("Return version not found"))
          }
        }
      case None => Future.failed(new NoSuchElementException("Full return not found"))
    }
  }
}

case class ReturnVersionUpdateReturn(
                                       newVersion: Option[Int]
                                     )

object ReturnVersionUpdateReturn {
  implicit val format: OFormat[ReturnVersionUpdateReturn] = Json.format[ReturnVersionUpdateReturn]
}
