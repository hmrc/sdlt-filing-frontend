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

import models.purchaser.ConfirmNameOfThePurchaser
import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.libs.json.{JsString, Json}

class ConfirmNameOfThePurchaserSpec extends AnyFreeSpec with Matchers with EitherValues with OptionValues with MockitoSugar {

  "ConfirmNameOfThePurchaser" - {

    "must deserialise valid values" - {

      "Yes" in {
        val json = JsString("Yes")
        json.validate[ConfirmNameOfThePurchaser].asOpt.value mustEqual ConfirmNameOfThePurchaser.Yes
      }

      "No" in {
        val json = JsString("No")
        json.validate[ConfirmNameOfThePurchaser].asOpt.value mustEqual ConfirmNameOfThePurchaser.No
      }
    }

    "must fail to deserialise invalid values" in {
      val json = JsString("invalid")
      json.validate[ConfirmNameOfThePurchaser].asOpt must not be defined
    }
    ".values" - {

      "must contain all values" in {
        ConfirmNameOfThePurchaser.values must contain theSameElementsAs Seq(
          ConfirmNameOfThePurchaser.Yes,
          ConfirmNameOfThePurchaser.No
        )
      }
    }

    ".options" - {

      "must return correct radio items" in {
        implicit val messages: Messages = mock[Messages]

        org.mockito.Mockito.when(messages.apply("site.Yes"))
          .thenReturn("Yes")
        org.mockito.Mockito.when(messages.apply("site.No"))
          .thenReturn("No")

        val result = ConfirmNameOfThePurchaser.options

        result.size mustBe 2
        result.head.value.value mustBe "Yes"
        result.head.id.value mustBe "value_0"
        result(1).value.value mustBe "No"
        result(1).id.value mustBe "value_1"
      }
    }
  }
}