/*
 * Copyright 2023 HM Revenue & Customs
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

  def isAfterSept2022AndBeforeApril2025(date: LocalDate) = {
    date.onOrAfter(Dates.SEPT2022_RESIDENTIAL_DATE) && date.isBefore(Dates.APRIL2025_RESIDENTIAL_DATE)
  }

  def isAfterOct2024AndBeforeApril2025(date: LocalDate): Boolean = {
    date.onOrAfter(Dates.OCT2024_RESIDENTIAL_DATE) && date.isBefore(Dates.APRIL2025_RESIDENTIAL_DATE)
  }

  def isAfterSep2022AndBeforeOct24(date: LocalDate): Boolean = {
    date.onOrAfter(Dates.SEPT2022_RESIDENTIAL_DATE) && date.isBefore(Dates.OCT2024_RESIDENTIAL_DATE)
  }

  def isAfterMar2008AndBeforeMar2016(date: LocalDate): Boolean = {
    date.onOrAfter(Dates.MIN_MIXED_PROPERTY_DATE) && date.isBefore(Dates.MARCH2016_NON_RESIDENTIAL_DATE)
  }

  def isAfterMar2010AndBeforeMar2012(date: LocalDate): Boolean = {
    date.onOrAfter(Dates.MAR2010_RESIDENTIAL_DATE) && date.isBefore(Dates.MAR2012_RESIDENTIAL_DATE)
  }

  def isAfterApr2013AndBeforeDec2014(date: LocalDate): Boolean = {
    date.onOrAfter(Dates.APRIL2013_TAX_YEAR_START_DATE) && date.isBefore(Dates.DECEMBER2014_RESIDENTIAL_DATE)
  }
}
