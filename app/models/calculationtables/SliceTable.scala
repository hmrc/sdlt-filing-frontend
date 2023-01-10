/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models.calculationtables

import models.SliceDetails

case class SliceTable(slices: Seq[Slice])

case class Slice(from: BigDecimal, to: Option[BigDecimal], rate: BigDecimal)

case class SliceResult( taxDue: BigDecimal, slices: Seq[SliceDetails])
