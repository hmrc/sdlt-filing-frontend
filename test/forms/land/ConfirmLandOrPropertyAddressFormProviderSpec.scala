package forms.land

import forms.behaviours.OptionFieldBehaviours
import forms.land.ConfirmLandOrPropertyAddressFormProvider
import models.land.ConfirmLandOrPropertyAddress
import play.api.data.FormError

class ConfirmLandOrPropertyAddressFormProviderSpec extends OptionFieldBehaviours {

  val form = new ConfirmLandOrPropertyAddressFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "confirmLandOrPropertyAddress.error.required"

    behave like optionsField[ConfirmLandOrPropertyAddress](
      form,
      fieldName,
      validValues  = ConfirmLandOrPropertyAddress.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
