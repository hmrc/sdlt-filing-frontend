/*
 * Copyright 2025 HM Revenue & Customs
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

package models

import models.vendor.AddVendorAgentContactDetails
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class AddVendorAgentContactDetailsSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "AddVendorAgentContactDetails" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(AddVendorAgentContactDetails.values.toSeq)

      forAll(gen) {
        addVendorAgentContactDetails =>

          JsString(addVendorAgentContactDetails.toString).validate[AddVendorAgentContactDetails].asOpt.value mustEqual addVendorAgentContactDetails
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!AddVendorAgentContactDetails.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[AddVendorAgentContactDetails] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(AddVendorAgentContactDetails.values.toSeq)

      forAll(gen) {
        addVendorAgentContactDetails =>

          Json.toJson(addVendorAgentContactDetails) mustEqual JsString(addVendorAgentContactDetails.toString)
      }
    }
  }
}
