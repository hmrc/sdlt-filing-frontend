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

package models.purchaserAgent

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsSuccess, Json}

class PurchaserAgentsContactDetailsSpec extends AnyWordSpec with Matchers {

  "PurchaserAgentsContactDetails.isAtLeastOneProvided" should {

    "return false when neither phone number nor email address is provided" in {
      val model = PurchaserAgentsContactDetails(None, None)
      model.isAtLeastOneProvided mustBe false
    }

    "return false when both phone number and email address are empty or whitespace" in {
      val model = PurchaserAgentsContactDetails(
        phoneNumber = Some("   "),
        emailAddress = Some("")
      )
      model.isAtLeastOneProvided mustBe false
    }

    "return true when a phone number is provided" in {
      val model = PurchaserAgentsContactDetails(
        phoneNumber = Some("0123456789"),
        emailAddress = None
      )
      model.isAtLeastOneProvided mustBe true
    }

    "return true when an email address is provided" in {
      val model = PurchaserAgentsContactDetails(
        phoneNumber = None,
        emailAddress = Some("test@example.com")
      )
      model.isAtLeastOneProvided mustBe true
    }

    "return true when both phone number and email address are provided" in {
      val model = PurchaserAgentsContactDetails(
        phoneNumber = Some("0123456789"),
        emailAddress = Some("test@example.com")
      )
      model.isAtLeastOneProvided mustBe true
    }
  }

  "PurchaserAgentsContactDetails JSON format" should {

    "write to JSON correctly" in {
      val model = PurchaserAgentsContactDetails(
        phoneNumber = Some("0123456789"),
        emailAddress = Some("test@example.com")
      )

      val json = Json.toJson(model)

      json mustBe Json.obj(
        "phoneNumber"   -> "0123456789",
        "emailAddress"  -> "test@example.com"
      )
    }

    "read from JSON correctly" in {
      val json = Json.obj(
        "phoneNumber"  -> "0123456789",
        "emailAddress" -> "test@example.com"
      )

      val result = json.validate[PurchaserAgentsContactDetails]

      result mustBe JsSuccess(
        PurchaserAgentsContactDetails(
          phoneNumber = Some("0123456789"),
          emailAddress = Some("test@example.com")
        )
      )
    }

    "handle missing optional fields when reading JSON" in {
      val json = Json.obj()

      val result = json.validate[PurchaserAgentsContactDetails]

      result mustBe JsSuccess(
        PurchaserAgentsContactDetails(None, None)
      )
    }
  }
}
