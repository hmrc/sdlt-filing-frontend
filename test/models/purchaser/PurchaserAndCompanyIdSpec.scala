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

class PurchaserAndCompanyIdSpec extends AnyFreeSpec with Matchers with EitherValues {

  "PurchaserAndCompanyId" - {

    val validJson: JsObject = Json.obj(
      "purchaserID" -> "123456",
      "companyDetailsID" -> "CD001"
    )

    val invalidJson: JsObject = Json.obj(
      "purchaserID" -> true,
      "companyDetailsID" -> "CD001"
    )

    val missingRequiredFieldJson: JsObject = Json.obj(
      "companyDetailsID" -> "CD001"
    )
    
    val PurchaserAndCompanyIdComplete: PurchaserAndCompanyId = PurchaserAndCompanyId(
      purchaserID = "123456",
      companyDetailsID = Some("CD001")
    )

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[PurchaserAndCompanyId]]
      }

      "must deserialize valid JSON" - {

        "with all required values" in {
          val result = Json.fromJson[PurchaserAndCompanyId](validJson).asEither.value

          result mustBe PurchaserAndCompanyId(
            purchaserID = "123456",
            companyDetailsID = Some("CD001")
          )
        }
      }

      "must fail when field has wrong type" in {
        val result = Json.fromJson[PurchaserAndCompanyId](invalidJson).asEither

        result.isLeft mustBe true
      }

      "must fail when purchaserID field is missing" in {
        val result = Json.fromJson[PurchaserAndCompanyId](missingRequiredFieldJson).asEither

        result.isLeft mustBe true
      }
    }

    ".writes" - {

      "must be found implicitly" in {
        implicitly[Writes[PurchaserAndCompanyId]]
      }

      "must serialize PurchaserAndCompanyId" - {

        "with all values" in {
          val json = Json.toJson(PurchaserAndCompanyIdComplete)

          (json \ "purchaserID").as[String] mustBe "123456"
          (json \ "companyDetailsID").as[String] mustBe "CD001"
        }
      }
    }

    ".formats" - {

      "must round-trip" - {

        "with all required values" in {
          val json = Json.toJson(validJson)
          val result = Json.fromJson[PurchaserAndCompanyId](json).asEither.value

          result mustEqual PurchaserAndCompanyIdComplete
        }
      }
    }
  }
}