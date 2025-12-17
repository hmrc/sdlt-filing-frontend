/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild

import base.ScalaSpecBase
import models.scalabuild.CurrentValue
import play.api.data.FormError

class CurrentValueFormProviderSpec extends ScalaSpecBase {

  val form = new CurrentValueFormProvider().apply()

  "bind AtOrBelowThreshold" in {
    val data = Map(("value", "AtOrBelowThreshold"))
    val result = form.bind(data)
    result.value.value mustBe CurrentValue.AtOrBelowThreshold
    result.errors mustBe empty
  }

  "bind AboveThreshold" in {
    val data = Map(("value", "AboveThreshold"))
    val result = form.bind(data)
    result.value.value mustBe CurrentValue.AboveThreshold
    result.errors mustBe empty
  }

  "return errors on invalid values" in {
    val invalidError = FormError("value", List("error.invalid"), List())
    val data = Map(("value", "Invalid value"))
    val result = form.bind(data)
    result.errors mustBe Seq(invalidError)
  }

}