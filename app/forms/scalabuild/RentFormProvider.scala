/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild

import forms.scalabuild.mappings.Mappings
import models.scalabuild.RentPeriods
import play.api.data.Form
import play.api.data.Forms._

class RentFormProvider extends Mappings {
  def apply(count: Int): Form[RentPeriods] =
    Form(
      mapping(
        "rents" -> list(bigDecimal(
          2,
          "rents.error.required",
          "rents.error.nonNumeric",
          "rents.error.nonNumeric"
        )).verifying("rents.error.required",(rents: List[BigDecimal]) => rents.length == count)//additional error to prevent manual tampering of input years
      )(RentPeriods.apply)(RentPeriods.unapply)
    )
}
