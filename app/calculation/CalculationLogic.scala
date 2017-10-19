package calculation

import calculation.models.SliceDetails
import calculation.models.calculationtables.{Slab, SlabResult, Slice}
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

  //  def calculateTaxDueSlice(amount: BigDecimal, slices: Seq[Slice]): SliceResult ={
  //
  //
  //  }

  def convertSliceToSliceDetails(amount: BigDecimal, slice: Slice): SliceDetails = {
    if(slice.to.isDefined) {
      if (amount > slice.to.get) {
        SliceDetails(from = slice.from.toInt, to = Some(slice.to.get.toInt), rate = slice.rate.toInt, taxDue = calcTax(slice.to.get - slice.from, slice.rate).toInt)
      }
      else if (amount <= slice.from) {
        SliceDetails(from = slice.from.toInt, to = Some(slice.to.get.toInt), rate = slice.rate.toInt, taxDue = 0)
      }
      else {
        SliceDetails(from = slice.from.toInt, to = Some(slice.to.get.toInt), rate = slice.rate.toInt, taxDue = calcTax(amount - slice.from, slice.rate).toInt)
      }
    }else if (amount > slice.from){
      SliceDetails(from = slice.from.toInt, to = None, rate = slice.rate.toInt, taxDue = calcTax(amount - slice.from, slice.rate).toInt)
    }
    else {
      SliceDetails(from = slice.from.toInt, to = None, rate = slice.rate.toInt, taxDue = 0)
    }
  }



}
