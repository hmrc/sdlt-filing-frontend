/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild

import forms.scalabuild.mappings.Mappings
import models.scalabuild.Tenancy
import play.api.data.Form

class FreeholdOrLeaseholdFormProvider extends Mappings {

  def apply(): Form[Tenancy] =
    Form(
      "value" -> enumerable[Tenancy]("tenancy.error.required")
    )
}
