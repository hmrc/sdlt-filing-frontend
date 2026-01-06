/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild

import forms.scalabuild.mappings.Mappings
import play.api.data.Form

class ExchangeContractsFormProvider extends Mappings {

  def apply(): Form[Boolean] =
    Form(
      "contract-pre-201603" -> boolean("exchangeContracts.error.required")
    )
}