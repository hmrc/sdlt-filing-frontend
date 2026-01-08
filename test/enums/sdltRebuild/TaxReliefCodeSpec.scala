/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package enums.sdltRebuild

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsError, JsNumber, JsString, Json}

class TaxReliefCodeSpec extends AnyFreeSpec with Matchers {

  private val allCodes: Seq[TaxReliefCode] = TaxReliefCode.toName.values.toSeq

  "TaxReliefCode.toName" - {

    "should contain an entry for every TaxReliefCode" in {
      allCodes.foreach { trc =>
        withClue(s"Missing or incorrect mapping for code ${trc.code} ($trc): ") {
          TaxReliefCode.toName.get(trc.code) mustBe Some(trc)
        }
      }
    }

    "should not contain duplicate codes (Map keys must be unique)" in {
      TaxReliefCode.toName.size mustBe allCodes.size
    }

    "should only contain values that are in our canonical list (no unexpected extras)" in {
      TaxReliefCode.toName.values.toSet mustBe allCodes.toSet
    }
  }

  "TaxReliefCode.zeroRateCodes" - {

    "should equal all TaxReliefCode values that mix in ZeroRate" in {
      val expected = allCodes.collect { case z: ZeroRate => z.asInstanceOf[TaxReliefCode] }.toSet
      TaxReliefCode.zeroRateCodes mustBe expected
    }
  }

  "TaxReliefCode.selfAssessedCodes" - {

    "should equal all TaxReliefCode values that mix in SelfAssessed" in {
      val expected = allCodes.collect { case s: SelfAssessed => s.asInstanceOf[SelfAssessed] }.toSet
      TaxReliefCode.selfAssessedCodes mustBe expected
    }
  }

  "TaxReliefCode JSON" - {

    "should read a known numeric code into the correct TaxReliefCode" in {
      val json = JsNumber(8)
      json.as[TaxReliefCode] mustBe PartExchange
    }

    "should write a TaxReliefCode as its numeric code" in {
      Json.toJson(PartExchange: TaxReliefCode) mustBe JsNumber(8)
    }

    "should write then read for every code" in {
      allCodes.foreach { trc =>
        withClue(s"write then read failed for ${trc.code} ($trc): ") {
          val json = Json.toJson(trc: TaxReliefCode)
          json.as[TaxReliefCode] mustBe trc
        }
      }
    }

    "should fail to read an unknown numeric code" in {
      val json = JsNumber(999)

      json.validate[TaxReliefCode] match {
        case JsError(errors) => errors.toString must include ("Unknown TaxReliefCode")
        case _               => fail("Expected JsError for unknown code")
      }
    }

    "should fail to read a non-numeric value" in {
      val result = JsString("8").validate[TaxReliefCode]

      result mustBe a [JsError]
      result.asInstanceOf[JsError].errors.toString must include ("must be a number")
    }
  }
}
