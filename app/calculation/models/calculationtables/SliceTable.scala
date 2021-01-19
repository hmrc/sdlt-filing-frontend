/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package calculation.models.calculationtables

import calculation.models.SliceDetails

case class SliceTable(slices: Seq[Slice])

case class Slice(from: BigDecimal, to: Option[BigDecimal], rate: BigDecimal)

case class SliceResult( taxDue: BigDecimal, slices: Seq[SliceDetails])
