package calculation.services

import calculation.models.SliceDetails
import calculation.models.calculationtables.{Slab, SlabResult, Slice, SliceResult}


object BaseCalculationService {

  def calculateTaxDueSlab(amount: BigDecimal, slabs: Seq[Slab]): SlabResult = {
    slabs.find(amount > _.threshold).map { firstSlabAboveAmount =>
      SlabResult(firstSlabAboveAmount.rate, calcTax(amount, firstSlabAboveAmount.rate))
    }.getOrElse(
      SlabResult(rate = 0, taxDue = 0)
    )
  }

  def calculateTaxDueSlice(amount: BigDecimal, slices: Seq[Slice]): SliceResult = {
    val sliceDetails = slices.map(slice => convertSliceToSliceDetails(amount, slice))
    val totalTaxDue = sliceDetails.foldLeft(0)(_ + _.taxDue)
    SliceResult(taxDue = totalTaxDue, slices = sliceDetails)
  }

  private def convertSliceToSliceDetails(amount: BigDecimal, slice: Slice): SliceDetails = {
    val taxDue = if(amount > slice.from) {
      calcTaxWithMax(amount - slice.from, slice.rate, slice.to.map(_ - slice.from)).toInt
    } else 0

    SliceDetails(
      from = slice.from.toInt,
      to = slice.to.map(_.toInt),
      rate = slice.rate.toInt,
      taxDue = taxDue
    )
  }

  private def calcTax(amount: BigDecimal, rate: BigDecimal): BigDecimal = {
    Math.floor(Math.floor(amount.toDouble) * rate.toDouble / 100)
  }

  private def calcTaxWithMax(amount: BigDecimal, rate: BigDecimal, max: Option[BigDecimal]): BigDecimal = {
    calcTax(optionalMax(amount, max), rate)
  }

  private def optionalMax(amount: BigDecimal, oMax: Option[BigDecimal]): BigDecimal = {
    oMax.map { max =>
      if(amount > max) max else amount
    }.getOrElse(amount)
  }
}
