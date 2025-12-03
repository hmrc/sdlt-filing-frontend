/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild
import forms.scalabuild.mappings.Mappings
import play.api.data.Form

class MainResidenceFormProvider extends Mappings {

  def apply(): Form[Boolean] =
    Form(
      "mainResidence" -> boolean("mainResidence.error.required")
    )
}