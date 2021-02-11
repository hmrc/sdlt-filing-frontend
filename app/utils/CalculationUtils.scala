/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package utils

import data.Dates

import java.time.LocalDate

object CalculationUtils extends DateUtil {
  def freeholdNRSDLTOutOfScope(premium: BigDecimal): Boolean = {
    premium < 40000
  }

  def leaseholdNRSDLTOutOfScopeForLeaseAndPremium(premium: BigDecimal, leaseTermYears: Int, highestRent: BigDecimal,
                                                  firstTimeBuyer: Boolean, sharedOwnership: Boolean): Boolean = {
    def outOfScopeForLease: Boolean = {
      (firstTimeBuyer && sharedOwnership) || (premium < 40000 && (highestRent < 1000 || leaseTermYears < 7))
    }

    def outOfScopeForPremium: Boolean = {
      premium < 40000
    }

    outOfScopeForLease && outOfScopeForPremium

    }

  def leaseholdNRSDLTInScopeForLeaseOrPremium(premium: BigDecimal, leaseTermYears: Int, highestRent: BigDecimal,
                                              firstTimeBuyer: Boolean, sharedOwnership: Boolean): Boolean = {

    def inScopeForLease: Boolean = {
      premium >= 40000 && (highestRent >= 1000 || leaseTermYears >= 7) && !(firstTimeBuyer && sharedOwnership)
    }

    def inScopeForPremium: Boolean = {
      premium >= 40000
    }

    inScopeForLease || inScopeForPremium

  }

  def duringNRB500HolidayPeriod(date: LocalDate): Boolean = {
    date.onOrAfter(Dates.JULY2020_RESIDENTIAL_DATE) && date.onOrBefore(Dates.JUNE2021_RESIDENTIAL_DATE)
  }

  def duringNRB250HolidayPeriod(date: LocalDate): Boolean = {
    date.onOrAfter(Dates.JULY2021_RESIDENTIAL_DATE) && date.onOrBefore(Dates.SEPT2021_RESIDENTIAL_DATE)
  }
}
