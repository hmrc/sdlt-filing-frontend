/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild
import forms.scalabuild.mappings.Mappings
import models.scalabuild.PropertyType
import play.api.data.Form

class ResidentialOrNonResidentialFormProvider extends Mappings {

  def apply(): Form[PropertyType] =
    Form(
      "value" -> enumerable[PropertyType]("propertyType.error.required")
    )
}
