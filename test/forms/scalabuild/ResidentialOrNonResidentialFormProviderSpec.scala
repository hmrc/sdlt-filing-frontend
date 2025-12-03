/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild
import base.ScalaSpecBase
import models.scalabuild.PropertyType
import play.api.data.FormError

class ResidentialOrNonResidentialFormProviderSpec extends ScalaSpecBase {

  val form = new ResidentialOrNonResidentialFormProvider().apply()

  "bind Residential" in {
    val data = Map(("value", "Residential"))
    val result = form.bind(data)
    result.value.value mustBe PropertyType.Residential
    result.errors mustBe empty
  }

  "bind NonResidential" in {
    val data = Map(("value", "NonResidential"))
    val result = form.bind(data)
    result.value.value mustBe PropertyType.NonResidential
    result.errors mustBe empty
  }

  "return errors on invalid values" in {
    val invalidError = FormError("value", List("error.invalid"), List())
    val data = Map(("value", "Invalid value"))
    val result = form.bind(data)
    result.errors mustBe Seq(invalidError)
  }

}