package forms.purchaser

import forms.behaviours.OptionFieldBehaviours
import forms.purchaser.AddPurchaserPhoneNumberFormProvider
import models.purchaser.AddPurchaserPhoneNumber
import play.api.data.FormError

class AddPurchaserPhoneNumberFormProviderSpec extends OptionFieldBehaviours {

  val form = new AddPurchaserPhoneNumberFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "addPurchaserPhoneNumber.error.required"

    behave like optionsField[AddPurchaserPhoneNumber](
      form,
      fieldName,
      validValues  = AddPurchaserPhoneNumber.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
