/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild
import forms.scalabuild.mappings.Mappings
import play.api.data.Form



class NonUkResidentFormProvider extends Mappings {

  def apply(): Form[Boolean] =
    Form(
      "nonUKResident" -> boolean("nonUkResident.error.required")
    )
}