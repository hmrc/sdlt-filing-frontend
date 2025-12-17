/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild

import base.ScalaSpecBase
import play.api.data.FormError

class SharedOwnershipFormProviderSpec extends ScalaSpecBase {
  val form = new SharedOwnershipFormProvider().apply()

  "bind true" in {
    val data = Map(("sharedOwnership", "true"))
    val result = form.bind(data)
    result.value.value mustBe true
    result.errors mustBe empty
  }

  "bind false" in {
    val data = Map(("sharedOwnership", "false"))
    val result = form.bind(data)
    result.value.value mustBe false
    result.errors mustBe empty
  }

  "return errors on invalid values" in {
    val invalidError = FormError("sharedOwnership", List("error.boolean"), List())
    val data = Map(("sharedOwnership", "Invalid value"))
    val result = form.bind(data)
    result.errors mustBe Seq(invalidError)
  }

}