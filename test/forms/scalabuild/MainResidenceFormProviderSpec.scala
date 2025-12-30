/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild
import base.ScalaSpecBase
import play.api.data.{Form, FormError}


class MainResidenceFormProviderSpec extends ScalaSpecBase {
  val form:Form[Boolean] = new MainResidenceFormProvider().apply()

  "bind true" in {
    val data = Map(("replaceMainResidence", "true"))
    val result = form.bind(data)
    result.value.value mustBe true
    result.errors mustBe empty
  }

  "bind false" in {
    val data = Map(("replaceMainResidence", "false"))
    val result = form.bind(data)
    result.value.value mustBe false
    result.errors mustBe empty
  }

  "return errors on invalid values" in {
    val invalidError = FormError("replaceMainResidence", List("error.boolean"), List())
    val data = Map(("replaceMainResidence", "Invalid value"))
    val result = form.bind(data)
    result.errors mustBe Seq(invalidError)
  }
}