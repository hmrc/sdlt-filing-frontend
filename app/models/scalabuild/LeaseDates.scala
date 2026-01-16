/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package models.scalabuild

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class LeaseDates(startDate: LocalDate, endDate: LocalDate)

object LeaseDates {
  implicit val format: OFormat[LeaseDates] = Json.format[LeaseDates]
}
