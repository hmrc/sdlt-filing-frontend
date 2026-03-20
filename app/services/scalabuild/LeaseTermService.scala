/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package services.scalabuild

import models.LeaseTerm

import java.time.LocalDate
import scala.annotation.tailrec

class LeaseTermService {
  def calculateTermOfLease(
                            effectiveDate: LocalDate,
                            leaseStart: LocalDate,
                            leaseEnd: LocalDate
                          ): LeaseTerm = {

    val start =
      if (effectiveDate.isAfter(leaseStart)) effectiveDate else leaseStart

    def is29Feb(d: LocalDate): Boolean =
      d.getMonthValue == 2 && d.getDayOfMonth == 29

    // Find number of full years
    @tailrec
    def fullYears(n: Int): Int = {
      val comparisonDate = start.plusYears(n).minusDays(1)
      if (comparisonDate.isAfter(leaseEnd)) n - 1
      else fullYears(n + 1)
    }

    val numYears = fullYears(1)

    // Count remaining days after full years
    def remainingDays(current: LocalDate, count: Int): Int = {
      if (current.isAfter(leaseEnd)) count - 1
      else remainingDays(current.plusDays(1), count + 1)
    }

    val startOfPartial =
      start.plusYears(numYears).minusDays(1).plusDays(1)

    val numDays =
      remainingDays(startOfPartial, 1)

    val numDaysInPartialYear =
      if (numDays > 0) {
        val partialYearEndDate =
          start.plusYears(numYears + 1).minusDays(1)

        def daysInYear(current: LocalDate, count: Int): Int = {
          if (current.isAfter(partialYearEndDate)) count - 1
          else daysInYear(current.plusDays(1), count + 1)
        }

        val calculated =
          daysInYear(start.plusYears(numYears), 1)

        if (is29Feb(start)) 365 else calculated
      } else 0

    LeaseTerm(
      years = numYears,
      days = numDays,
      daysInPartialYear = numDaysInPartialYear
    )
  }
}