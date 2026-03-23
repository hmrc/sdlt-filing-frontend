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
        "purchasePrice.error.nonNumeric",
        "purchasePrice.error.nonNumeric")
    )
}

