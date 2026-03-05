/*
 * Copyright 2023 HM Revenue & Customs
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

package models.taxCalculation.validators.api

import play.api.libs.functional.syntax.*
import play.api.libs.json.*

import java.time.LocalDate
import scala.util.{Failure, Success, Try}

object RequestValidators {

  def multiFieldDateReads(prefix: String): Reads[LocalDate] = (json: JsValue) => {

    json.validate[(Int, Int, Int)](readDateParts(prefix)).flatMap {
      case (d, m, y) => Try(LocalDate.of(y, m, d)) match {
        case Success(date) => JsSuccess(date)
        case Failure(err) => JsError(s"'$prefix' date could not be parsed. Error: ${err.getMessage}")
      }
    }
  }

  private def readDateParts(prefix: String):Reads[(Int, Int, Int)] = (
    (__ \ s"${prefix}Day").read[Int] and
      (__ \ s"${prefix}Month").read[Int] and
      (__ \ s"${prefix}Year").read[Int]
    )((day: Int, month: Int, year: Int) => (day, month, year))

  val yesNoToBooleanReads: Reads[Boolean] = (json: JsValue) => json.validate[String] match {
    case JsSuccess("Yes", JsPath(_)) => JsSuccess(true)
    case JsSuccess("No", JsPath(_)) => JsSuccess(false)
    case JsSuccess(str, JsPath(_)) => JsError(s"'$str' could not be converted to Boolean")
    case err@JsError(_) => err
  }
}