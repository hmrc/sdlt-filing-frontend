/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild

import forms.scalabuild.mappings.Mappings
import play.api.data.Form

class PurchasePriceFormProvider  extends Mappings {

  def apply(): Form[BigDecimal] =
    Form(
      "premium" -> currency(
        "purchasePrice.error.twoDecimalPlaces",
        "purchasePrice.error.nonNumeric")
      // Todo: Lan can a purchase price be negative? Is there a max value?
    )
}

