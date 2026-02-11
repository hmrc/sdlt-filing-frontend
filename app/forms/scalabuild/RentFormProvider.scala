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
  def apply(): Form[RentPeriods] =
    Form[RentPeriods](
      mapping(
        "year1Rent" -> bigDecimal(
          2,
          "rents.error.required",
          "rents.error.nonNumeric",
          "rents.error.nonNumeric"
        ),
        "year2Rent" -> mandatoryIfExists("year2Rent",bigDecimal(
          2,
          "rents.error.required",
          "rents.error.nonNumeric",
          "rents.error.nonNumeric"
        )),
        "year3Rent" -> mandatoryIfExists("year3Rent",bigDecimal(
          2,
          "rents.error.required",
          "rents.error.nonNumeric",
          "rents.error.nonNumeric"
        )),
        "year4Rent" -> mandatoryIfExists("year4Rent",bigDecimal(
          2,
          "rents.error.required",
          "rents.error.nonNumeric",
          "rents.error.nonNumeric"
        )),
        "year5Rent" -> mandatoryIfExists("year5Rent",bigDecimal(
          2,
          "rents.error.required",
          "rents.error.nonNumeric",
          "rents.error.nonNumeric"
        ))
      )(RentPeriods.apply)(RentPeriods.unapply)
    )
}
