/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild
import base.ScalaSpecBase
import play.api.data.{Form, FormError}

class IsPurchaserIndividualFormProviderSpec extends ScalaSpecBase {
  val form:Form[Boolean] = new IsPurchaserIndividualFormProvider().apply()

  "bind true" in {
    val data = Map(("individual", "true"))
    val result = form.bind(data)
    result.value.value mustBe true
    result.errors mustBe empty
  }

  "bind false" in {
    val data = Map(("individual", "false"))
    val result = form.bind(data)
    result.value.value mustBe false
    result.errors mustBe empty
  }

  "return errors on invalid values" in {
    val invalidError = FormError("individual", List("error.boolean"), List())
    val data = Map(("individual", "Invalid value"))
    val result = form.bind(data)
    result.errors mustBe Seq(invalidError)
  }
}
