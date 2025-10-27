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

package models.prelimQuestions

import base.SpecBase
import org.scalacheck.Gen
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, Json}

class BusinessOrIndividualRequestSpec extends SpecBase with ScalaCheckPropertyChecks {

  "BusinessOrIndividualRequest" - {

    "must deserialise valid values" in {
      val gen = Gen.oneOf(BusinessOrIndividualRequest.values)

      forAll(gen) { option =>
        JsString(option.toString).validate[BusinessOrIndividualRequest].asOpt.value mustEqual option
      }
    }

    "must fail to deserialise invalid values" in {
      val gen = arbitrary[String] suchThat (!BusinessOrIndividualRequest.values.map(_.toString).contains(_))

      forAll(gen) { invalidValue =>
          JsString(invalidValue).validate[BusinessOrIndividualRequest] mustEqual(JsError("error.invalid"))
      }
    }

    "must serialise values correctly" in {
      val gen = Gen.oneOf(BusinessOrIndividualRequest.values)

      forAll(gen) { option =>
        Json.toJson(option) mustEqual JsString(option.toString)
      }
    }

    "must fail to deserialise whitespace or empty strings" in {
      Seq("", " ", "\t").foreach { invalidInput =>
        JsString(invalidInput).validate[BusinessOrIndividualRequest] mustEqual JsError("error.invalid")
      }
    }
  }
}