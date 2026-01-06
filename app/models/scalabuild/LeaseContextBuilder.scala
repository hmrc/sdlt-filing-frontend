/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package models.scalabuild

import services.scalabuild.LeaseTermService

import java.time.LocalDate
import javax.inject.Inject


class LeaseContextBuilder @Inject()(
                                     leaseTermService: LeaseTermService
                                   ) {

  def build(
             effectiveDate: LocalDate,
             leaseStart: LocalDate,
             leaseEnd: LocalDate
           ): LeaseContext = {

    val term =
      leaseTermService.calculateTermOfLease(
        effectiveDate,
        leaseStart,
        leaseEnd
      )

    val totalPeriod = term.years + (if (term.days > 0) 1 else 0)
    LeaseContext(
      periodCount = math.min(totalPeriod, 5)
    )
  }
}