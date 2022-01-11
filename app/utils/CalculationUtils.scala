/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package utils

import data.Dates

import java.time.LocalDate

object CalculationUtils extends DateUtil {
  def freeholdNRSDLTOutOfScope(premium: BigDecimal): Boolean = {
    premium < 40000
  }

  private def nrsdltOutOfScopeForRent(premium: BigDecimal, leaseTermYears: Int, highestRent: BigDecimal,
                                                  firstTimeBuyer: Boolean, sharedOwnership: Boolean): Boolean = {
    (firstTimeBuyer && sharedOwnership) || leaseTermYears <= 7 || (premium < 40000 && highestRent < 1000)
  }

  private def nrsdltOutOfScopeForPremium(premium: BigDecimal, leaseTermYears: Int): Boolean = {
    leaseTermYears <= 7 || premium < 40000
  }

  def leaseholdNRSDLTInScopeForLeaseOrPremium(premium: BigDecimal, leaseTermYears: Int, highestRent: BigDecimal,
                                              firstTimeBuyer: Boolean, sharedOwnership: Boolean): Boolean = {
    !nrsdltOutOfScopeForRent(premium, leaseTermYears, highestRent, firstTimeBuyer, sharedOwnership) ||
      !nrsdltOutOfScopeForPremium(premium, leaseTermYears)
  }

  def leaseholdNRSDLTOutOfScope(premium: BigDecimal, leaseTermYears: Int, highestRent: BigDecimal,
                                              firstTimeBuyer: Boolean, sharedOwnership: Boolean): Boolean = {
    nrsdltOutOfScopeForRent(premium, leaseTermYears, highestRent, firstTimeBuyer, sharedOwnership) &&
      nrsdltOutOfScopeForPremium(premium, leaseTermYears)
  }

  def duringNRB500HolidayPeriod(date: LocalDate): Boolean = {
    date.onOrAfter(Dates.JULY2020_RESIDENTIAL_DATE) && date.onOrBefore(Dates.JUNE2021_RESIDENTIAL_DATE)
  }

  def duringNRB250HolidayPeriod(date: LocalDate): Boolean = {
    date.onOrAfter(Dates.JULY2021_RESIDENTIAL_DATE) && date.onOrBefore(Dates.SEPT2021_RESIDENTIAL_DATE)
  }
}
