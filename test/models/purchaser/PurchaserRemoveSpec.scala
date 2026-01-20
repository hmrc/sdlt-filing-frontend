/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models.purchaser

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, JsSuccess, Json}

class PurchaserRemoveSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "PurchaserRemove" - {

    "must deserialise valid values" - {

      "for No" in {
        JsString("no").validate[PurchaserRemove] mustEqual JsSuccess(PurchaserRemove.No)
      }

      "for Keep" in {
        JsString("keep").validate[PurchaserRemove] mustEqual JsSuccess(PurchaserRemove.Keep)
      }

      "for Remove with purchaser ID" in {
        val purchaserId = "PUR-001"
        JsString(s"REMOVE-$purchaserId").validate[PurchaserRemove] mustEqual JsSuccess(PurchaserRemove.Remove(purchaserId))
      }

      "for SelectNewMain with purchaser ID" in {
        val purchaserId = "PUR-002"
        JsString(s"PROMOTE-$purchaserId").validate[PurchaserRemove] mustEqual JsSuccess(PurchaserRemove.SelectNewMain(purchaserId))
      }

      "for Remove with various purchaser IDs" in {
        val purchaserIdGen = Gen.alphaNumStr.suchThat(_.nonEmpty)

        forAll(purchaserIdGen) { purchaserId =>
          val result = JsString(s"REMOVE-$purchaserId").validate[PurchaserRemove]
          result.asOpt.value mustEqual PurchaserRemove.Remove(purchaserId)
        }
      }

      "for SelectNewMain with various purchaser IDs" in {
        val purchaserIdGen = Gen.alphaNumStr.suchThat(_.nonEmpty)

        forAll(purchaserIdGen) { purchaserId =>
          val result = JsString(s"PROMOTE-$purchaserId").validate[PurchaserRemove]
          result.asOpt.value mustEqual PurchaserRemove.SelectNewMain(purchaserId)
        }
      }
    }

    "must fail to deserialise invalid values" - {

      "for empty string" in {
        JsString("").validate[PurchaserRemove] mustBe a[JsError]
      }

      "for random invalid strings" in {
        val invalidGen = arbitrary[String].suchThat { s =>
          s != "no" &&
            s != "keep" &&
            !s.startsWith("REMOVE-") &&
            !s.startsWith("PROMOTE-")
        }

        forAll(invalidGen) { invalidValue =>
          JsString(invalidValue).validate[PurchaserRemove] mustBe a[JsError]
        }
      }

      "for REMOVE prefix without ID" in {
        JsString("REMOVE-").validate[PurchaserRemove].asOpt.value mustEqual PurchaserRemove.Remove("")
      }

      "for PROMOTE prefix without ID" in {
        JsString("PROMOTE-").validate[PurchaserRemove].asOpt.value mustEqual PurchaserRemove.SelectNewMain("")
      }

      "for wrong case" in {
        JsString("NO").validate[PurchaserRemove] mustBe a[JsError]
        JsString("KEEP").validate[PurchaserRemove] mustBe a[JsError]
      }

      "for wrong prefix" in {
        JsString("DELETE-PUR-001").validate[PurchaserRemove] mustBe a[JsError]
        JsString("UPDATE-PUR-001").validate[PurchaserRemove] mustBe a[JsError]
      }
    }

    "must serialise" - {

      "No to 'no'" in {
        val value: PurchaserRemove = PurchaserRemove.No
        Json.toJson(value) mustEqual JsString("no")
      }

      "Keep to 'keep'" in {
        val value: PurchaserRemove = PurchaserRemove.Keep
        Json.toJson(value) mustEqual JsString("keep")
      }

      "Remove to 'REMOVE-{id}'" in {
        val purchaserId = "PUR-001"
        val value: PurchaserRemove = PurchaserRemove.Remove(purchaserId)
        Json.toJson(value) mustEqual JsString(s"REMOVE-$purchaserId")
      }

      "SelectNewMain to 'PROMOTE-{id}'" in {
        val purchaserId = "PUR-002"
        val value: PurchaserRemove = PurchaserRemove.SelectNewMain(purchaserId)
        Json.toJson(value) mustEqual JsString(s"PROMOTE-$purchaserId")
      }

      "Remove with various purchaser IDs" in {
        val purchaserIdGen = Gen.alphaNumStr.suchThat(_.nonEmpty)

        forAll(purchaserIdGen) { purchaserId =>
          val value: PurchaserRemove = PurchaserRemove.Remove(purchaserId)
          Json.toJson(value) mustEqual JsString(s"REMOVE-$purchaserId")
        }
      }

      "SelectNewMain with various purchaser IDs" in {
        val purchaserIdGen = Gen.alphaNumStr.suchThat(_.nonEmpty)

        forAll(purchaserIdGen) { purchaserId =>
          val value: PurchaserRemove = PurchaserRemove.SelectNewMain(purchaserId)
          Json.toJson(value) mustEqual JsString(s"PROMOTE-$purchaserId")
        }
      }
    }

    "must round-trip serialisation and deserialisation" - {

      "for No" in {
        val original: PurchaserRemove = PurchaserRemove.No
        val json = Json.toJson(original)
        json.validate[PurchaserRemove].asOpt.value mustEqual original
      }

      "for Keep" in {
        val original: PurchaserRemove = PurchaserRemove.Keep
        val json = Json.toJson(original)
        json.validate[PurchaserRemove].asOpt.value mustEqual original
      }

      "for Remove" in {
        val purchaserIdGen = Gen.alphaNumStr.suchThat(_.nonEmpty)

        forAll(purchaserIdGen) { purchaserId =>
          val original: PurchaserRemove = PurchaserRemove.Remove(purchaserId)
          val json = Json.toJson(original)
          json.validate[PurchaserRemove].asOpt.value mustEqual original
        }
      }

      "for SelectNewMain" in {
        val purchaserIdGen = Gen.alphaNumStr.suchThat(_.nonEmpty)

        forAll(purchaserIdGen) { purchaserId =>
          val original: PurchaserRemove = PurchaserRemove.SelectNewMain(purchaserId)
          val json = Json.toJson(original)
          json.validate[PurchaserRemove].asOpt.value mustEqual original
        }
      }
    }
  }
}