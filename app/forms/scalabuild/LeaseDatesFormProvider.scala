/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild

import forms.scalabuild.mappings.Mappings
import models.scalabuild.LeaseDates
import play.api.data.Form
import play.api.data.Forms.mapping

import java.time.LocalDate

class LeaseDatesFormProvider extends Mappings {

  def apply(effectiveDate: LocalDate): Form[LeaseDates] =
    Form(
      mapping(
        "leaseStartDate" -> localDate(
          invalidKey  = "leaseDate.error.invalid",
          allRequiredKey = "leaseStartDate.error.required.all",
          twoRequiredKey = "leaseDate.error.invalid",
          requiredKey    = "leaseDate.error.invalid"
        ),
        "leaseEndDate" -> localDate(
          invalidKey  = "leaseDate.error.invalid",
          allRequiredKey = "leaseEndDate.error.required.all",
          twoRequiredKey = "leaseDate.error.invalid",
          requiredKey    = "leaseDate.error.invalid"
        )
      )(LeaseDates.apply)(LeaseDates.unapply)
        .transform(
          identity[LeaseDates],
          identity[LeaseDates]
        ).verifying("leaseEndDate.error.beforeStartDate",date => !date.endDate.isBefore(date.startDate))
        .verifying("leaseEndDate.error.beforeEffectiveDate", date => !date.endDate.isBefore(effectiveDate))
    )
}