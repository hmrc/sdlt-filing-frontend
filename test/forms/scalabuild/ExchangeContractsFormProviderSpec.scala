/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild

import base.ScalaSpecBase
import org.scalatest.freespec.AnyFreeSpec
import play.api.data.FormError
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper

class ExchangeContractsFormProviderSpec extends AnyFreeSpec with ScalaSpecBase {
  val form = new ExchangeContractsFormProvider().apply()

  "bind true" in {
    val data = Map(("contract-pre-201603", "true"))
    val result = form.bind(data)
    result.value.value mustBe true
    result.errors mustBe empty
  }

  "bind false" in {
    val data = Map(("contract-pre-201603", "false"))
    val result = form.bind(data)
    result.value.value mustBe false
    result.errors mustBe empty
  }

  "return errors on invalid values" in {
    val invalidError = FormError("contract-pre-201603", List("error.boolean"), List())
    val data = Map(("contract-pre-201603", "Invalid value"))
    val result = form.bind(data)
    result.errors mustBe Seq(invalidError)
  }

}
