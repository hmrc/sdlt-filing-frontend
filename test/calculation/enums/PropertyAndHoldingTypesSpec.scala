/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package calculation.enums

import play.api.libs.json._
import uk.gov.hmrc.play.test.UnitSpec

class PropertyAndHoldingTypesSpec extends UnitSpec {

  "HoldingTypes" should {
    "read 'Leasehold' to Json" in {
      Json.fromJson[HoldingTypes.Value](JsString("Leasehold")) shouldBe JsSuccess(HoldingTypes.leasehold)
    }
    "read 'Freehold' to Json" in {
      Json.fromJson[HoldingTypes.Value](JsString("Freehold")) shouldBe JsSuccess(HoldingTypes.freehold)
    }


    "fail to read 'leaseyholdings' to Json" in {
      Json.fromJson[HoldingTypes.Value](JsString("leaseyholdings")) shouldBe JsError(Seq(JsPath() -> Seq(JsonValidationError("invalid holding type"))))
    }

    "fail to read 3 to Json" in {
      Json.fromJson[HoldingTypes.Value](JsNumber(3)) shouldBe JsError(Seq(JsPath() -> Seq(JsonValidationError("no holding type string provided"))))
    }
  }

  "PropertyTypes" should {
    "read 'Residential' to Json" in {
      Json.fromJson[PropertyTypes.Value](JsString("Residential")) shouldBe JsSuccess(PropertyTypes.residential)
    }
    "read 'Non-residential' to Json" in {
      Json.fromJson[PropertyTypes.Value](JsString("Non-residential")) shouldBe JsSuccess(PropertyTypes.nonResidential)
    }

    "fail to read 'ressssssssidential' to Json" in {
      Json.fromJson[PropertyTypes.Value](JsString("ressssssssidential")) shouldBe JsError(Seq(JsPath() -> Seq(JsonValidationError("invalid property type"))))
    }

    "fail to read 3 to Json" in {
      Json.fromJson[PropertyTypes.Value](JsNumber(3)) shouldBe JsError(Seq(JsPath() -> Seq(JsonValidationError("no property type string provided"))))
    }
  }
}
