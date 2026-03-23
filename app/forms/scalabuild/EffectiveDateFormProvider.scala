/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild

import data.Dates.MAR_22_2012_RESIDENTIAL_DATE
import forms.scalabuild.mappings.Mappings
import models.scalabuild.PropertyType
import play.api.data.Form

import java.time.LocalDate

class EffectiveDateFormProvider extends Mappings {

  def apply(propertyType: PropertyType): Form[LocalDate] =
    Form(
      "effectiveDate" -> localDate(
        invalidKey  = "effectiveDate.error.invalid",
        allRequiredKey = "effectiveDate.error.required.all",
        twoRequiredKey = "effectiveDate.error.required.two",
        requiredKey    = "effectiveDate.error.required"
       ).verifying(earliestDateIfFreehold(MAR_22_2012_RESIDENTIAL_DATE, propertyType,"effectiveDate.error.residentialMinDate"))
  )
}