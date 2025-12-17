/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild

import forms.scalabuild.mappings.Mappings
import play.api.data.Form

class PremiumFormProvider extends Mappings {
  def apply(): Form[BigDecimal] =
    Form(
      "premium" -> bigDecimal(
        2,
        "premium.error.required",
        "premium.error.nonNumeric",
        "premium.error.nonNumeric"
      )
        .verifying(minimumValue(BigDecimal(0.00), "premium.error.nonNumeric"))
        .verifying(maximumValue(BigDecimal(9999999999.99), "premium.error.nonNumeric"))
    )
}
