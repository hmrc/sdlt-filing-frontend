/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package enums

import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsString, Json}

class TaxAndCalcTypesSpec extends PlaySpec {

  "TaxTypes" must {
    "write 'rent' to Json" in {
      Json.toJson(TaxTypes.rent) shouldBe JsString("rent")
    }
    "write 'premium' to Json" in {
      Json.toJson(TaxTypes.premium) shouldBe JsString("premium")
    }
  }

  "CalcTypes" must {
    "write 'slice' to Json" in {
      Json.toJson(CalcTypes.slice) shouldBe JsString("slice")
    }
    "write 'slab' to Json" in {
      Json.toJson(CalcTypes.slab) shouldBe JsString("slab")
    }
  }
}
