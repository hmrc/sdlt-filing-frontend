/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package forms.scalabuild.taxCalculation



import forms.scalabuild.mappings.Mappings
import play.api.data.Form

import javax.inject.Inject

class PremiumPayableFormProvider @Inject() extends Mappings {

  private val premuiumPayableNonNegativeDecimalRegex = "^[0-9,]*(\\.[0-9]+)?$"
  private val premuiumPayable2dpWholeDecimalRegex = "^[0-9,]+[.]{0,1}[0]{0,2}"
  private val maxLength = 16
  private val maxValue = 999999999

  def apply(): Form[String] =
    Form(
      "premium" -> text("premiumPayable.error")

        .verifying(regexp(premuiumPayableNonNegativeDecimalRegex, "Test1"))
        .verifying(regexp(premuiumPayable2dpWholeDecimalRegex, "Test2"))
        .verifying(maxLength(maxLength, "Test3"))
    )
}
