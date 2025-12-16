/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild
import forms.scalabuild.mappings.Mappings
import play.api.data.Form


class OwnsOtherPropertiesFormProvider extends Mappings {

  def apply(): Form[Boolean] =
    Form(
      "ownedOtherProperties" -> boolean("ownsOtherProperties.error.required")
    )
}
