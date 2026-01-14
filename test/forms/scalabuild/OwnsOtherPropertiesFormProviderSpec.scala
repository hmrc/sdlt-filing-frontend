/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild

import base.ScalaSpecBase
import org.scalatest.freespec.AnyFreeSpec
import play.api.data.{Form, FormError}
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper

class OwnsOtherPropertiesFormProviderSpec extends AnyFreeSpec with ScalaSpecBase {
  val form:Form[Boolean] = new OwnsOtherPropertiesFormProvider().apply()

  "bind true" in {
    val data = Map(("ownedOtherProperties", "true"))
    val result = form.bind(data)
    result.value.value mustBe true
    result.errors mustBe empty
  }

  "bind false" in {
    val data = Map(("ownedOtherProperties", "false"))
    val result = form.bind(data)
    result.value.value mustBe false
    result.errors mustBe empty
  }

  "return errors on invalid values" in {
    val invalidError = FormError("ownedOtherProperties", List("error.boolean"), List())
    val data = Map(("ownedOtherProperties", "Invalid value"))
    val result = form.bind(data)
    result.errors mustBe Seq(invalidError)
  }
}