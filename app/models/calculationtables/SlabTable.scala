/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models.calculationtables

case class SlabTable(slabs: Seq[Slab])

case class Slab(threshold: BigDecimal, rate: BigDecimal)

case class SlabResult(rate: BigDecimal, taxDue: BigDecimal)
