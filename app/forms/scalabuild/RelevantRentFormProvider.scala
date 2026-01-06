/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild

import forms.scalabuild.mappings.Mappings
import play.api.data.Form

class RelevantRentFormProvider extends Mappings {
  def apply(): Form[BigDecimal] =
    Form(
      "relevantRent" -> bigDecimal(
        2,
        "relevantRent.error.required",
        "relevantRent.error.nonNumeric",
        "relevantRent.error.nonNumeric"
      )
        .verifying(minimumValue(BigDecimal(0.00), "relevantRent.error.nonNumeric"))
        .verifying(maximumValue(BigDecimal(9999999999.99), "relevantRent.error.nonNumeric"))
    )
}