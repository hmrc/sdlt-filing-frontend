package calculation.models.calculationtables

case class SlabTable(slabs: Seq[Slab])

case class Slab(threshold: BigDecimal, rate: BigDecimal)
