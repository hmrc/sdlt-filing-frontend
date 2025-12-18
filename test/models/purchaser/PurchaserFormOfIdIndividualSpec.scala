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

package models.purchaser

import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsObject, Json, Reads, Writes}

class PurchaserFormOfIdIndividualSpec extends AnyFreeSpec with Matchers with EitherValues {

  "PurchaserFormOfIdIndividual" - {

    val validJson: JsObject = Json.obj(
      "idNumberOrReference" -> "123456",
      "countryIssued" -> "Germany"
    )

    val invalidJson: JsObject = Json.obj(
      "idNumberOrReference" -> true,
      "countryIssued" -> "Germany"
    )

    val missingRequiredFieldJson: JsObject = Json.obj(
      "countryIssued" -> "Germany"
    )

    val missingRequiredCountryFieldJson: JsObject = Json.obj(
      "idNumberOrReference" -> "123456"
    )
    
    val purchaserFormOfIdIndividualComplete: PurchaserFormOfIdIndividual = PurchaserFormOfIdIndividual(
      idNumberOrReference = "123456",
      countryIssued = "Germany"
    )

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[PurchaserFormOfIdIndividual]]
      }

      "must deserialize valid JSON" - {

        "with all required values" in {
          val result = Json.fromJson[PurchaserFormOfIdIndividual](validJson).asEither.value

          result mustBe PurchaserFormOfIdIndividual(
            idNumberOrReference = "123456",
            countryIssued = "Germany"
          )
        }
      }

      "must fail when field has wrong type" in {
        val result = Json.fromJson[PurchaserFormOfIdIndividual](invalidJson).asEither

        result.isLeft mustBe true
      }

      "must fail when required ID field is missing" in {
        val result = Json.fromJson[PurchaserFormOfIdIndividual](missingRequiredFieldJson).asEither

        result.isLeft mustBe true
      }

      "must fail when required country field is missing" in {
        val result = Json.fromJson[PurchaserFormOfIdIndividual](missingRequiredCountryFieldJson).asEither

        result.isLeft mustBe true
      }
    }

    ".writes" - {

      "must be found implicitly" in {
        implicitly[Writes[PurchaserFormOfIdIndividual]]
      }

      "must serialize PurchaserFormOfIdIndividual" - {

        "with all values" in {
          val json = Json.toJson(purchaserFormOfIdIndividualComplete)

          (json \ "idNumberOrReference").as[String] mustBe "123456"
          (json \ "countryIssued").as[String] mustBe "Germany"
        }
      }
    }

    ".formats" - {

      "must round-trip" - {

        "with all required values" in {
          val json = Json.toJson(validJson)
          val result = Json.fromJson[PurchaserFormOfIdIndividual](json).asEither.value

          result mustEqual purchaserFormOfIdIndividualComplete
        }
      }
    }
  }
}