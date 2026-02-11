/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package models.scalabuild

case class DetailTableRow(
    rowHeading: String,
    rate: BigDecimal,
    taxDue: Int
)
