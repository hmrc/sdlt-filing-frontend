package calculation.fixtures

import java.time.LocalDate

import calculation.models.{LeaseDetails, LeaseTerm}

trait LeaseDetailsFixture {

  protected val testLeaseDetails = LeaseDetails(
    startDate = LocalDate.of(2000, 1, 1),
    endDate = LocalDate.of(2020, 1, 1),
    leaseTerm = LeaseTerm(
      years = 3,
      days = 0,
      daysInPartialYear = 365
    ),
    year1Rent = 100,
    year2Rent = Some(100),
    year3Rent = Some(100),
    year4Rent = None,
    year5Rent = None
  )

}
