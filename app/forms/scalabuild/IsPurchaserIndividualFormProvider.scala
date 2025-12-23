/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild
import forms.scalabuild.mappings.Mappings
import play.api.data.Form

class IsPurchaserIndividualFormProvider extends Mappings {

  def apply(): Form[Boolean] =
    Form(
      "individual" -> boolean("isPurchaserIndividual.error.required")
    )
}