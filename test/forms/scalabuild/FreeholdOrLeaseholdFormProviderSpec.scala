/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild

import base.ScalaSpecBase
import models.scalabuild.HoldingTypes.{freehold, leasehold}
import play.api.data.FormError

class FreeholdOrLeaseholdFormProviderSpec extends ScalaSpecBase {

  val form = new FreeholdOrLeaseholdFormProvider().apply()

  "bind Freehold" in {
    val data = Map(("value", "freehold"))
    val result = form.bind(data)
    result.value.value mustBe freehold
    result.errors mustBe empty
  }

  "bind Leasehold" in {
    val data = Map(("value", "leasehold"))
    val result = form.bind(data)
    result.value.value mustBe leasehold
    result.errors mustBe empty
  }

  "return errors on invalid values" in {
    val invalidError = FormError("value", List("error.invalid"), List())
    val data = Map(("value", "Invalid value"))
    val result = form.bind(data)
    result.errors mustBe Seq(invalidError)
  }

}

