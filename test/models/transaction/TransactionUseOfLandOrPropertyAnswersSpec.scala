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

package models.transaction

import models.transaction.TransactionUseOfLandOrProperty.{Factory, Hotel, Office, Other, OtherIndustrialUnit, Shop, Warehouse}
import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.*

class TransactionUseOfLandOrPropertyAnswersSpec extends AnyFreeSpec with Matchers with EitherValues {

  private val allUsesOfLand: Set[TransactionUseOfLandOrProperty] = Set(
    Office,
    Hotel,
    Shop,
    Warehouse,
    Factory,
    OtherIndustrialUnit,
    Other
  )

  private val someUsesOfLand: Set[TransactionUseOfLandOrProperty] = Set(
    Office,
    Hotel,
    Shop
  )

  private val noUsesOfLand: Set[TransactionUseOfLandOrProperty] = Set.empty

  private val allUsesOfLandAnswers = TransactionUseOfLandOrPropertyAnswers(
    office = "yes",
    hotel = "yes",
    shop = "yes",
    warehouse = "yes",
    factory = "yes",
    otherIndustrialUnit = "yes",
    other = "yes"
  )

  private val someUsesOfLandAnswers = TransactionUseOfLandOrPropertyAnswers(
    office = "yes",
    hotel = "yes",
    shop = "yes",
    warehouse = "no",
    factory = "no",
    otherIndustrialUnit = "no",
    other = "no"
  )

  private val noUsesOfLandAnswers = TransactionUseOfLandOrPropertyAnswers(
    office = "no",
    hotel = "no",
    shop = "no",
    warehouse = "no",
    factory = "no",
    otherIndustrialUnit = "no",
    other = "no"
  )

  private val someUsesOfLandAnswersJson = Json.obj(
    "office" -> "yes",
    "hotel" -> "yes",
    "shop" -> "yes",
    "warehouse" -> "no",
    "factory" -> "no",
    "otherIndustrialUnit" -> "no",
    "other" -> "no",
  )

  "TransactionUseOfLandOrPropertyAnswers" - {
    ".fromSet" - {
      "must convert a set of all uses of land to an answers object with all 'yes'" in {
        val answers = TransactionUseOfLandOrPropertyAnswers.fromSet(allUsesOfLand)
        answers mustEqual allUsesOfLandAnswers
      }

      "must convert a set of some uses of land to an answers object with corresponding 'yes' and 'no'" in {
        val answers = TransactionUseOfLandOrPropertyAnswers.fromSet(someUsesOfLand)
        answers mustEqual someUsesOfLandAnswers
      }

      "must convert an empty set to an answers object with all 'no'" in {
        val answers = TransactionUseOfLandOrPropertyAnswers.fromSet(noUsesOfLand)
        answers mustEqual noUsesOfLandAnswers
      }
    }

    ".toSet" - {
      "must convert an answers object with all 'yes' to a set of all uses of land" in {
        val usesOfLandSet = TransactionUseOfLandOrPropertyAnswers.toSet(allUsesOfLandAnswers)
        usesOfLandSet mustEqual allUsesOfLand
      }

      "must convert an answers object with some 'yes' and some 'no' to a set of corresponding uses of land" in {
        val usesOfLandSet = TransactionUseOfLandOrPropertyAnswers.toSet(someUsesOfLandAnswers)
        usesOfLandSet mustEqual someUsesOfLand
      }

      "must convert an answers object with all 'no' to an empty set" in {
        val usesOfLandSet = TransactionUseOfLandOrPropertyAnswers.toSet(noUsesOfLandAnswers)
        usesOfLandSet mustEqual noUsesOfLand
      }

      "must omit invalid values and treat them as 'no'" in {
        val invalidAnswers = someUsesOfLandAnswers.copy(office = "true")
        val usesOfLandSet = TransactionUseOfLandOrPropertyAnswers.toSet(invalidAnswers)
        usesOfLandSet mustEqual (someUsesOfLand - Office)
      }
    }

    ".writes" - {
      "must be found implicitly" in {
        implicitly[Writes[TransactionUseOfLandOrPropertyAnswers]]
      }

      "must serialize TransactionUseOfLandOrPropertyAnswers" in {
        val json: JsValue = Json.toJson(someUsesOfLandAnswers)
        (json \ "office").as[String] mustBe "yes"
        (json \ "hotel").as[String] mustBe "yes"
        (json \ "shop").as[String] mustBe "yes"
        (json \ "warehouse").as[String] mustBe "no"
        (json \ "factory").as[String] mustBe "no"
        (json \ "otherIndustrialUnit").as[String] mustBe "no"
        (json \ "other").as[String] mustBe "no"
      }
    }

    ".reads" - {
      "must be found implicitly" in {
        implicitly[Reads[TransactionUseOfLandOrPropertyAnswers]]
      }

      "must deserialize valid JSON to TransactionUseOfLandOrPropertyAnswers" in {
        val result = Json.fromJson[TransactionUseOfLandOrPropertyAnswers](someUsesOfLandAnswersJson).asEither.value
        result mustEqual someUsesOfLandAnswers
      }
    }

    ".formats" - {

      "must be found implicitly" in {
        implicitly[Format[TransactionUseOfLandOrPropertyAnswers]]
      }
    }
  }
}