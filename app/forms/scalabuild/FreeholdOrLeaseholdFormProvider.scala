/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild

import forms.scalabuild.mappings.Mappings
import models.scalabuild.HoldingTypes
import play.api.data.Form

class FreeholdOrLeaseholdFormProvider extends Mappings {

  def apply(): Form[HoldingTypes] =
    Form(
      "value" -> enumerable[HoldingTypes]("holding.error.required")
    )
}
