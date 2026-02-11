/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package services.scalabuild

import models.LeaseTerm

import java.time.LocalDate

class LeaseTermService {
  def calculateTermOfLease(
                            effectiveDate: LocalDate,
                            leaseStart: LocalDate,
                            leaseEnd: LocalDate
                          ): LeaseTerm = {

    val start = if (effectiveDate.isAfter(leaseStart)) effectiveDate else leaseStart

    def is29Feb(d: LocalDate): Boolean =
      d.getMonthValue == 2 && d.getDayOfMonth == 29

    var numYears = 1
    var comparisonDate = start.plusYears(numYears).minusDays(1)
    while (!comparisonDate.isAfter(leaseEnd)) {
      numYears += 1
      comparisonDate = start.plusYears(numYears).minusDays(1)
    }
    // we went past the end date so need to go back 1 year
    numYears -= 1

    // count the number of partial days, i.e. keep adding 1 day till we get past the end date
    var numDays = 1
    comparisonDate = start.plusYears(numYears).minusDays(1)

    comparisonDate = comparisonDate.plusDays(1)

    while (!comparisonDate.isAfter(leaseEnd)) {
      numDays += 1
      comparisonDate = comparisonDate.plusDays(1)
    }
    // we went past the end date so need to go back 1 day
    numDays -= 1

    var numDaysInPartialYear = 0
    // need to calculate number of days in partial year (is it 365 or 366)
    if (numDays > 0) {
      val partialYearEndDate = start.plusYears(numYears + 1).minusDays(1)
      // set comparison date to end date of last full year in term
      var comparisonDate =  start.plusYears(numYears)
      numDaysInPartialYear = 1

      while (!comparisonDate.isAfter(partialYearEndDate)) {
        numDaysInPartialYear += 1
        comparisonDate = comparisonDate.plusDays(1)
      }
      numDaysInPartialYear -= 1
      if(is29Feb(start)){
        numDaysInPartialYear =365
      }
    }

    LeaseTerm(
      years = numYears,
      days = numDays,
      daysInPartialYear = numDaysInPartialYear
    )
  }
}