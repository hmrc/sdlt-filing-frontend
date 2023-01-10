/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package fixtures

import java.time.LocalDate

import models.{LeaseDetails, LeaseTerm}

trait LeaseDetailsFixture {

  protected val testLeaseDetailsJuly2020: LeaseDetails = LeaseDetails(
    startDate = LocalDate.of(2020, 7, 10),
    endDate = LocalDate.of(2120, 7, 9),
    leaseTerm = LeaseTerm(
      years = 100,
      days = 0,
      daysInPartialYear = 365
    ),
    year1Rent = 18150,
    year2Rent = Some(18150),
    year3Rent = Some(18150),
    year4Rent = Some(18150),
    year5Rent = Some(18150)
  )

  protected val testLeaseDetailsSept2022: LeaseDetails = LeaseDetails(
    startDate = LocalDate.of(2020, 7, 10),
    endDate = LocalDate.of(2120, 7, 9),
    leaseTerm = LeaseTerm(
      years = 100,
      days = 0,
      daysInPartialYear = 365
    ),
    year1Rent = 25000,
    year2Rent = Some(25000),
    year3Rent = Some(25000),
    year4Rent = Some(25000),
    year5Rent = Some(25000)
  )

  protected val testLeaseDetails: LeaseDetails = LeaseDetails(
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

  protected val testLeaseDetailsAllRentsUnder2000: LeaseDetails = LeaseDetails(
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
