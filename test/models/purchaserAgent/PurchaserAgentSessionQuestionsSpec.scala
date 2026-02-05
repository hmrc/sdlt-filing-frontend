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

package models.purchaserAgent

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.shouldBe
import org.scalatest.{EitherValues, OptionValues}
import play.api.libs.json.{JsNull, JsObject, Json, Reads}

class PurchaserAgentSessionQuestionsSpec extends AnyFreeSpec with Matchers with EitherValues with OptionValues {

  object TestData {
    def purchaserAgentSessionQuestionsJsonComplete: JsObject = Json.obj(
      "purchaserAgentName" -> "Agent name",
      "purchaserAgentAddress" -> Json.obj(
        "houseNumber" -> 1,
        "line1" -> "Street 1",
        "line2" -> "Street 2",
        "line3" -> "Street 3",
        "line4" -> "Street 4",
        "line5" -> "Street 5",
        "postcode" ->  "W9 2NP",
        "country" -> Json.obj(
          "code" -> "GB",
          "name" -> "UK"
        ),
        "addressValidated" -> false
      ),
      "purchaserAgentContactDetails" -> Json.obj(
        "phoneNumber" -> "1234567890",
        "emailAddress" -> "test@example.com"
      ),
      "purchaserAgentReference" -> "1234",
      "purchaserAgentAuthorised" -> "YES"
    )


    def purchaserAgentSessionQuestionsJsonWithNoOptional: JsObject = Json.obj(
      "purchaserAgentName" -> "Agent name",
      "purchaserAgentAddress" -> Json.obj(
        "houseNumber" -> JsNull,
        "line1" -> "Street 1",
        "line2" -> JsNull,
        "line3" -> JsNull,
        "line4" -> JsNull,
        "line5" -> JsNull ,
        "postcode" ->  "W9 2NP",
        "country" -> JsNull,
        "addressValidated" -> false
      ),
      "purchaserAgentAuthorised" -> "YES"
    )
  }

  "PurchaserAgentSessionQuestions" - {

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[PurchaserAgentSessionQuestions]]
      }

      "must deserialize valid JSON with all fields" in {
        val result = Json.fromJson[PurchaserAgentSessionQuestions](TestData.purchaserAgentSessionQuestionsJsonComplete).asEither.value

        val expectedResult: PurchaserAgentSessionQuestions =
          PurchaserAgentSessionQuestions(
            purchaserAgentName = "Agent name",
            purchaserAgentAddress = PurchaserAgentSessionAddress(
              houseNumber = Some(1),
              line1 = "Street 1",
              line2 = Some("Street 2"),
              line3 = Some("Street 3"),
              line4 = Some("Street 4"),
              line5 = Some("Street 5"),
              postcode = "W9 2NP",
              country = Some(PurchaserAgentSessionCountry(
                code = Some("GB"),
                name = Some("UK")
              )),
              addressValidated = false
            ),
            purchaserAgentContactDetails = Some(PurchaserAgentsContactDetails(
              phoneNumber = Some("1234567890"),
              emailAddress = Some("test@example.com")
            )),
            purchaserAgentReference = Some("1234"),
            purchaserAgentAuthorised = "YES"
          )

        result shouldBe expectedResult
      }

      "must deserialize valid JSON with minimal fields" in {
        val result = Json.fromJson[PurchaserAgentSessionQuestions](TestData.purchaserAgentSessionQuestionsJsonWithNoOptional).asEither.value

        val expectedResult: PurchaserAgentSessionQuestions =
          PurchaserAgentSessionQuestions(
            purchaserAgentName = "Agent name",
            purchaserAgentAddress = PurchaserAgentSessionAddress(
              houseNumber = None,
              line1 = "Street 1",
              line2 = None,
              line3 = None,
              line4 = None,
              line5 = None,
              postcode = "W9 2NP",
              country = None,
              addressValidated = false
            ),
            purchaserAgentAuthorised = "YES"
          )

        result shouldBe expectedResult
      }
    }
  }
}
