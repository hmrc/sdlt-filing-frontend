/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild

import forms.scalabuild.mappings.Mappings
import models.scalabuild.CurrentValue
import play.api.data.Form

class CurrentValueFormProvider extends Mappings {

  def apply(): Form[CurrentValue] =
    Form(
      "value" -> enumerable[CurrentValue]("currentValue.error.required")
    )
}