/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package services.scalabuild

import base.ScalaSpecBase
import models.LeaseTerm

import java.time.LocalDate

class LeaseTermServiceSpec extends ScalaSpecBase {
  val leaseTermService = new LeaseTermService
  val testCases = Seq(
    // TestCase(#, effectiveDate, leaseStart, leaseEnd, expectedYears, expectedDays, expectedDaysInPartialYear, notes)
    (1, "2025-01-01", "2025-01-01", "2028-01-01", 3, 1, 366, "Exact 3-year lease"),
    (2, "2025-06-15", "2025-01-01", "2028-03-15", 2, 275, 366, "Partial year at end, start after leaseStart"),
    (3, "2020-02-29", "2020-02-29", "2023-02-28", 3, 1, 365, "Leap year start, Feb 29 adjustment"),
    (4, "2025-05-01", "2025-05-01", "2025-12-31", 0, 245, 365, "Less than one year lease"),
    (5, "2025-01-01", "2025-01-01", "2031-03-01", 6, 60, 365, "More than 5 years, test UI capping at 5"),
    (6, "2024-02-29", "2024-02-29", "2027-02-28", 3, 1, 365, "Leap year start, non-leap end"),
    (7, "2023-03-01", "2023-03-01", "2023-08-15", 0, 168, 366, "Partial year less than 1 year"),
    (8, "2025-12-31", "2025-01-01", "2026-12-31", 1, 1, 365, "Start after leaseStart → picks effective date"),
    (9, "2025-02-28", "2025-02-28", "2029-02-27", 4, 0, 0, "Exact 4 years minus 1 day"),
    (10, "2024-02-29", "2024-02-29", "2025-02-28", 1, 1, 365, "One-year lease with leap year start")
  )
  for ((id, eff, start, end, expectedYears, expectedDays, expectedPartialDays, notes) <- testCases) {
    s"return correct lease term for test case #$id: $notes" in {
      val effectiveDate = LocalDate.parse(eff)
      val leaseStart = LocalDate.parse(start)
      val leaseEnd = LocalDate.parse(end)
      val result: LeaseTerm = leaseTermService.calculateTermOfLease(effectiveDate, leaseStart, leaseEnd)
      result.years mustEqual expectedYears
      result.days mustEqual expectedDays
      result.daysInPartialYear mustEqual expectedPartialDays
    }
  }
}
