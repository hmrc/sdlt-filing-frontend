package calculation

import calculation.models.calculationtables.{Slab, SlabResult}
import play.api.Logger


object CalculationLogic {
  def calculateTaxDueSlab(amount: BigDecimal, slabs: Seq[Slab]): SlabResult = {
    slabs.find(amount > _.threshold).map { firstSlabAboveAmount =>
      SlabResult(firstSlabAboveAmount.rate, calcTax(amount, firstSlabAboveAmount.rate))
    }.getOrElse{
      Logger.warn("[CalculationLogic.calculateTaxDueSlab]: Amount["+amount+"] was less than minimum threshold["+slabs.last.threshold+"]")
      SlabResult(rate = 0, taxDue = 0)}
  }

  def calcTax(amount: BigDecimal, rate: BigDecimal): BigDecimal = {
    Math.floor(Math.floor(amount.toDouble) * rate.toDouble / 100)
  }


}
