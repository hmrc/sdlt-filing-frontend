/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package calculation.models.calculationtables

case class SlabTable(slabs: Seq[Slab])

case class Slab(threshold: BigDecimal, rate: BigDecimal)

case class SlabResult(rate: BigDecimal, taxDue: BigDecimal)
