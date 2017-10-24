package calculation.services

import calculation.models.{LeaseDetails, SliceDetails}
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

  def calculateNPV(leaseDetails: LeaseDetails): Double ={
    val rentsList = Seq(Some(leaseDetails.year1Rent),
                             leaseDetails.year2Rent,
                             leaseDetails.year3Rent,
                             leaseDetails.year4Rent,
                             leaseDetails.year5Rent).flatten

    val fullYears = leaseDetails.leaseTerm.years
    val partialDays = leaseDetails.leaseTerm.days
    val daysInYear = leaseDetails.leaseTerm.daysInPartialYear

    val (baseDivisor,baseNPV) = rentsList.foldLeft(BigDecimal(1),0.0){(sum,element) =>
      val divisor = calcDivisorRate(sum._1)
      (divisor , sum._2 + (Math.floor(element.toDouble * 1000 / divisor.toDouble) / 1000))
    }

     Math.floor(if((fullYears >= 5) && (partialDays > 0)) {
      val rentPartialYear = rentsList.max * partialDays / daysInYear
      val (divisor, totalNPV) = if(fullYears == 5) (baseDivisor,baseNPV) else calcNPVAbove5Years(baseNPV, 6,  fullYears, rentsList.max)
       totalNPV + Math.floor(rentPartialYear.toDouble / calcDivisorRate(divisor).toDouble)
    }
    else if(fullYears > 5) {
       calcNPVAbove5Years(baseNPV, 6,  fullYears, rentsList.max)._2}
    else baseNPV)
  }

  private def calcDivisorRateScaling(incrementAmount: Int): BigDecimal ={
    val test = List.fill(incrementAmount)(1.035)
    test.foldLeft(1.0)((x,y) => x * y)
  }

  private def calcDivisorRate(currentDivisor: BigDecimal): BigDecimal ={
    currentDivisor * 1.035
  }

  def calcNPVAbove5Years(totalNPV: Double, currYear: Int, yearMax: Int, highestRent: BigDecimal): (BigDecimal, Double) ={
    val currNPV = totalNPV + (Math.floor(highestRent.toDouble * 1000 / calcDivisorRateScaling(currYear).toDouble) / 1000)
    if(currYear >= yearMax) (calcDivisorRateScaling(currYear).toDouble,currNPV)  else calcNPVAbove5Years(currNPV, currYear+1, yearMax,highestRent)
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
