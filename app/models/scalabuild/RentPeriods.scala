/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package models.scalabuild

import models.LeaseTerm
import play.api.libs.json.{Json, OFormat}

import scala.collection.immutable.List

case class RentPeriods(
    year1Rent: BigDecimal,
    year2Rent: Option[BigDecimal] = None,
    year3Rent: Option[BigDecimal] = None,
    year4Rent: Option[BigDecimal] = None,
    year5Rent: Option[BigDecimal] = None
) {
  val rents: List[BigDecimal] = {
    List(year1Rent) ++
      year2Rent.toList ++
      year3Rent.toList ++
      year4Rent.toList ++
      year5Rent
  }
}

object RentPeriods {
  implicit val formats: OFormat[RentPeriods] = Json.format[RentPeriods]
}

case class LeaseContext(periodCount: Int, term: LeaseTerm)
object LeaseContext {
  implicit val formats: OFormat[LeaseContext] = Json.format[LeaseContext]
}
