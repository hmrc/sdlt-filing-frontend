/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild

import forms.scalabuild.mappings.Mappings
import play.api.data.Form

class ContractPost201603FormProvider extends Mappings {

  def apply(): Form[Boolean] =
    Form(
      "contract-varied-post-201603" -> boolean("contractPost201603.error.required")
    )
}