package calculation.validators.api

import java.time.LocalDate

import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.util.{Failure, Success, Try}

object RequestValidators {

  def multiFieldDateReads(prefix: String): Reads[LocalDate] = new Reads[LocalDate] {
    override def reads(json: JsValue): JsResult[LocalDate] = {

      json.validate[(Int, Int, Int)](readDateParts(prefix)).flatMap {
        case (d, m, y) => Try(LocalDate.of(y, m, d)) match {
          case Success(date) => JsSuccess(date)
          case Failure(err) => JsError(s"'$prefix' date could not be parsed. Error: ${err.getMessage}")
        }
      }
    }
  }

  private def readDateParts(prefix: String):Reads[(Int, Int, Int)] = (
    (__ \ s"${prefix}Day").read[Int] and
    (__ \ s"${prefix}Month").read[Int] and
    (__ \ s"${prefix}Year").read[Int]
  )((day: Int, month: Int, year: Int) => (day, month, year))

}
