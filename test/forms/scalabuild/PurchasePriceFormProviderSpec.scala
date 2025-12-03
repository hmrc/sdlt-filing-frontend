/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild
import base.ScalaSpecBase
import play.api.data.{Form, FormError}

class PurchasePriceFormProviderSpec extends ScalaSpecBase {
  val form:Form[_] = new PurchasePriceFormProvider().apply()

  "bind valid amount" in {
    val data = Map(("premium", "100000"))
    val result = form.bind(data)
    result.value.value mustBe 100000
    result.errors mustBe empty
  }

  "return errors on invalid values" in {
    val invalidError = FormError("premium", List("error.nonNumeric"), List())
    val data = Map(("premium", "Invalid value"))
    val result = form.bind(data)
    result.errors mustBe Seq(invalidError)
  }
}
