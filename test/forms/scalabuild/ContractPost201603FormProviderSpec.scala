/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild

import base.ScalaSpecBase
import play.api.data.FormError

class ContractPost201603FormProviderSpec extends ScalaSpecBase {
  val form = new ContractPost201603FormProvider().apply()

  "bind true" in {
    val data = Map(("contract-varied-post-201603", "true"))
    val result = form.bind(data)
    result.value.value mustBe true
    result.errors mustBe empty
  }

  "bind false" in {
    val data = Map(("contract-varied-post-201603", "false"))
    val result = form.bind(data)
    result.value.value mustBe false
    result.errors mustBe empty
  }

  "return errors on invalid values" in {
    val invalidError = FormError("contract-varied-post-201603", List("error.boolean"), List())
    val data = Map(("contract-varied-post-201603", "Invalid value"))
    val result = form.bind(data)
    result.errors mustBe Seq(invalidError)
  }
}
