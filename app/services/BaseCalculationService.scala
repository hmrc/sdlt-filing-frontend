/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package services

import javax.inject.Singleton

import models.{LeaseDetails, SliceDetails}
import models.calculationtables.{Slab, SlabResult, Slice, SliceResult}

import scala.math.BigDecimal.RoundingMode

@Singleton
class BaseCalculationService {

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

  def calculateNPV(leaseDetails: LeaseDetails): BigDecimal ={
    val rentsList = Seq(Some(leaseDetails.year1Rent),
                             leaseDetails.year2Rent,
                             leaseDetails.year3Rent,
                             leaseDetails.year4Rent,
                             leaseDetails.year5Rent).flatten


    val fullYears = leaseDetails.leaseTerm.years
    val partialDays = leaseDetails.leaseTerm.days
    val daysInYear = leaseDetails.leaseTerm.daysInPartialYear
    val highestRent = rentsList.max
    val fullNPVYears = if(fullYears < 5 && partialDays > 0) fullYears + 1 else fullYears

    val initialNPV = BigDecimal(0)
    val initialDivisor = BigDecimal(1)
    
    val (fullYearsNPV, lastFullYearDivisor) = (0 until fullNPVYears).foldLeft(Tuple2(initialNPV, initialDivisor)) {
      case ((npv, divisor), year) =>
        val updatedDivisor = calcDivisorRate(divisor)
        val updatedNPV = rentsList.lift(year).map {
          rent => npv + npvIncrease(rent, updatedDivisor)
        }.getOrElse {
          npv + npvIncrease(highestRent, updatedDivisor)
        }
        (updatedNPV, updatedDivisor)
    }

    val partialYearNPVIncrease = if(fullYears >= 5 && partialDays > 0) {
      val rentPartialYear = highestRent * partialDays / daysInYear
      (rentPartialYear / calcDivisorRate(lastFullYearDivisor)).setScale(0, RoundingMode.FLOOR)
    } else BigDecimal(0)

    val unroundedNPV = fullYearsNPV + partialYearNPVIncrease
    unroundedNPV.setScale(0, RoundingMode.FLOOR)
  }

  private def npvIncrease(annualRent: BigDecimal, divisor: BigDecimal): BigDecimal = {
    (annualRent / divisor).setScale(3, RoundingMode.FLOOR)
  }

  private def calcDivisorRate(currentDivisor: BigDecimal): BigDecimal ={
    currentDivisor * 1.035
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
