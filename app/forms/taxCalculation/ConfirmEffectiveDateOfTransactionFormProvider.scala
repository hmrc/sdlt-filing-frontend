package forms.taxCalculation

import forms.mappings.Mappings
import models.ConfirmEffectiveDateOfTransaction
import play.api.data.Form

import javax.inject.Inject

class ConfirmEffectiveDateOfTransactionFormProvider @Inject() extends Mappings {

  def apply(): Form[ConfirmEffectiveDateOfTransaction] =
    Form(
      "value" -> enumerable[ConfirmEffectiveDateOfTransaction]("taxCalculation.confirmEffectiveDateOfTransaction.error.required")
    )

}
