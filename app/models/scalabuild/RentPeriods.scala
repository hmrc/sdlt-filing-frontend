/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package models.scalabuild

import play.api.libs.json.{Json, OFormat}

case class RentPeriods(rents: List[BigDecimal])

object RentPeriods {
  implicit val format: OFormat[RentPeriods] = Json.format[RentPeriods]
}

case class LeaseContext(periodCount: Int, term: LeaseTerm)


