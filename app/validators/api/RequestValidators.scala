/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package validators.api

import java.time.LocalDate

import play.api.libs.json._
import play.api.libs.functional.syntax._

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