/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild

import base.ScalaSpecBase
import play.api.data.FormError

class NonUkResidentFormProviderSpec extends ScalaSpecBase {
  val form = new NonUkResidentFormProvider().apply()

  "bind true" in {
    val data = Map(("nonUKResident", "true"))
    val result = form.bind(data)
    result.value.value mustBe true
    result.errors mustBe empty
  }

  "bind false" in {
    val data = Map(("nonUKResident", "false"))
    val result = form.bind(data)
    result.value.value mustBe false
    result.errors mustBe empty
  }

  "return errors on invalid values" in {
    val invalidError = FormError("nonUKResident", List("error.boolean"), List())
    val data = Map(("nonUKResident", "Invalid value"))
    val result = form.bind(data)
    result.errors mustBe Seq(invalidError)
  }

}
