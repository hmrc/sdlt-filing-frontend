/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package calculation.models

import calculation.enums.{CalcTypes, TaxTypes}
import play.api.libs.json._
import play.api.libs.functional.syntax._

object SliceDetails {

  private val toWrites = new Writes[Option[Int]] {
    override def writes(o: Option[Int]): JsValue = {
      o.map(JsNumber(_)).getOrElse(JsNumber(-1))
    }
  }

  implicit val writes = (
    (__ \ "from").write[Int] and
      (__ \ "to").write[Option[Int]](toWrites) and
      (__ \ "rate").write[Int] and
      (__ \ "taxDue").write[Int]
    )(unlift(SliceDetails.unapply))
}

object CalculationDetails {
  implicit val writes = Json.writes[CalculationDetails]
}

object Result {
  implicit val writes = Json.writes[Result]
}

object CalculationResponse {
  implicit val writes = Json.writes[CalculationResponse]
}


case class CalculationResponse(
                 result: Seq[Result]
                 )

case class Result(
                   totalTax: Int,
                   resultHeading: Option[String] = None,
                   resultHint: Option[String] = None,
                   npv: Option[Int],
                   taxCalcs: Seq[CalculationDetails]
                 )

case class CalculationDetails(
                            taxType: TaxTypes.Value,
                            calcType: CalcTypes.Value,
                            taxDue: Int,
                            detailHeading: Option[String] = None,
                            bandHeading: Option[String] = None,
                            detailFooter: Option[String] = None,
                            rate: Option[Int] = None,
                            slices: Option[Seq[SliceDetails]] = None
                            )

case class SliceDetails(
                       from: Int,
                       to: Option[Int],
                       rate: Int,
                       taxDue: Int
                       )
