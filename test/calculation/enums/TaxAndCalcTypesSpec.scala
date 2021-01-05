/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package calculation.enums

import play.api.libs.json.{JsString, Json}
import uk.gov.hmrc.play.test.UnitSpec

class TaxAndCalcTypesSpec extends UnitSpec {

  "TaxTypes" should {
    "write 'rent' to Json" in {
      Json.toJson(TaxTypes.rent) shouldBe JsString("rent")
    }
    "write 'premium' to Json" in {
      Json.toJson(TaxTypes.premium) shouldBe JsString("premium")
    }
  }

  "CalcTypes" should {
    "write 'slice' to Json" in {
      Json.toJson(CalcTypes.slice) shouldBe JsString("slice")
    }
    "write 'slab' to Json" in {
      Json.toJson(CalcTypes.slab) shouldBe JsString("slab")
    }
  }
}
