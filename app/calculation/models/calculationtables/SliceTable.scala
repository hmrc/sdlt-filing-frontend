package calculation.models.calculationtables

case class SliceTable(slices: Seq[Slice])

case class Slice(from: BigDecimal, to: BigDecimal, rate: BigDecimal)
