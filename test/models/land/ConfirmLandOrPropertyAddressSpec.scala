package models.land

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, Json}

class ConfirmLandOrPropertyAddressSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "ConfirmLandOrPropertyAddress" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(ConfirmLandOrPropertyAddress.values.toSeq)

      forAll(gen) {
        confirmLandOrPropertyAddress =>

          JsString(confirmLandOrPropertyAddress.toString).validate[ConfirmLandOrPropertyAddress].asOpt.value mustEqual confirmLandOrPropertyAddress
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!ConfirmLandOrPropertyAddress.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[ConfirmLandOrPropertyAddress] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(ConfirmLandOrPropertyAddress.values.toSeq)

      forAll(gen) {
        confirmLandOrPropertyAddress =>

          Json.toJson(confirmLandOrPropertyAddress) mustEqual JsString(confirmLandOrPropertyAddress.toString)
      }
    }
  }
}
