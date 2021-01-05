/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package calculation.enums

import play.api.libs.json._

object TaxTypes extends Enumeration {
  val rent = Value
  val premium = Value

  implicit val writes = Writes.enumNameWrites
}

object CalcTypes extends Enumeration {
  val slice = Value
  val slab = Value

  implicit val writes = Writes.enumNameWrites
}
