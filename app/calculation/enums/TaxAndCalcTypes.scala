package calculation.enums

import play.api.libs.json.{Format, Reads, Writes}

object TaxTypes extends Enumeration {
  val rent = Value
  val premium = Value

  implicit val format = Format(Reads.enumNameReads(TaxTypes), Writes.enumNameWrites)
}

object CalcTypes extends Enumeration {
  val slice = Value
  val slab = Value

  implicit val format = Format(Reads.enumNameReads(CalcTypes), Writes.enumNameWrites)
}
