package calculation.enums

import play.api.libs.json.{JsString, JsSuccess, Json}
import uk.gov.hmrc.play.test.UnitSpec

class TaxAndCalcTypesSpec extends UnitSpec {

  "TaxTypes" should {
    "write 'rent' to Json" in {
      Json.toJson(TaxTypes.rent) shouldBe JsString("rent")
    }
    "write 'premium' to Json" in {
      Json.toJson(TaxTypes.premium) shouldBe JsString("premium")
    }
    "read 'rent' from Json" in {
      Json.fromJson[TaxTypes.Value](JsString("rent")) shouldBe JsSuccess(TaxTypes.rent)
    }
    "read 'premium' from Json" in {
      Json.fromJson[TaxTypes.Value](JsString("premium")) shouldBe JsSuccess(TaxTypes.premium)
    }
  }

  "CalcTypes" should {
    "write 'slice' to Json" in {
      Json.toJson(CalcTypes.slice) shouldBe JsString("slice")
    }
    "write 'slab' to Json" in {
      Json.toJson(CalcTypes.slab) shouldBe JsString("slab")
    }
    "read 'slice' from Json" in {
      Json.fromJson[CalcTypes.Value](JsString("slice")) shouldBe JsSuccess(CalcTypes.slice)
    }
    "read 'slab' from Json" in {
      Json.fromJson[CalcTypes.Value](JsString("slab")) shouldBe JsSuccess(CalcTypes.slab)
    }
  }
}
