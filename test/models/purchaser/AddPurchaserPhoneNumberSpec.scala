package models.purchaser

import models.purchaser.AddPurchaserPhoneNumber
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, Json}

class AddPurchaserPhoneNumberSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "AddPurchaserPhoneNumber" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(AddPurchaserPhoneNumber.values.toSeq)

      forAll(gen) {
        addPurchaserPhoneNumber =>

          JsString(addPurchaserPhoneNumber.toString).validate[AddPurchaserPhoneNumber].asOpt.value mustEqual addPurchaserPhoneNumber
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!AddPurchaserPhoneNumber.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[AddPurchaserPhoneNumber] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(AddPurchaserPhoneNumber.values.toSeq)

      forAll(gen) {
        addPurchaserPhoneNumber =>

          Json.toJson(addPurchaserPhoneNumber) mustEqual JsString(addPurchaserPhoneNumber.toString)
      }
    }
  }
}
