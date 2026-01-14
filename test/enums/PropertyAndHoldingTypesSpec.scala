/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package enums

import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.play.PlaySpec
import play.api.libs.json._

class PropertyAndHoldingTypesSpec extends PlaySpec {

  "HoldingTypes" must {
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

  "PropertyTypes" must {
    "read 'Residential' to Json" in {
      Json.fromJson[PropertyTypes.Value](JsString("Residential")) shouldBe JsSuccess(PropertyTypes.residential)
    }
    "read 'Non-residential' to Json" in {
      Json.fromJson[PropertyTypes.Value](JsString("Non-residential")) shouldBe JsSuccess(PropertyTypes.nonResidential)
    }
    "read 'Mixed' to Json" in {
      Json.fromJson[PropertyTypes.Value](JsString("Mixed")) shouldBe JsSuccess(PropertyTypes.mixed)
    }

    "fail to read 'ressssssssidential' to Json" in {
      Json.fromJson[PropertyTypes.Value](JsString("ressssssssidential")) shouldBe JsError(Seq(JsPath() -> Seq(JsonValidationError("invalid property type"))))
    }

    "fail to read 3 to Json" in {
      Json.fromJson[PropertyTypes.Value](JsNumber(3)) shouldBe JsError(Seq(JsPath() -> Seq(JsonValidationError("no property type string provided"))))
    }
  }
}
