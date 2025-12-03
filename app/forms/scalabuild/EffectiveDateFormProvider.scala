/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild

import forms.scalabuild.mappings.Mappings
import play.api.data.Form

import java.time.LocalDate

class EffectiveDateFormProvider extends Mappings {

  def apply(): Form[LocalDate] =
    Form(
      "effectiveDate" -> localDate(
        invalidKey  = "effectiveDate.error.invalid",
        allRequiredKey = "effectiveDate.error.required.all",
        twoRequiredKey = "effectiveDate.error.required.two",
        requiredKey    = "effectiveDate.error.required"
       )
  )
}