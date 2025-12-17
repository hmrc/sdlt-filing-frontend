/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild

import forms.scalabuild.mappings.Mappings
import play.api.data.Form

class SharedOwnershipFormProvider extends Mappings {

  def apply(): Form[Boolean] =
    Form(
      "sharedOwnership" -> boolean("sharedOwnership.error.required")
    )
}