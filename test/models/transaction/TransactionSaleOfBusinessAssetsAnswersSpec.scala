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

import models.transaction.TransactionSaleOfBusinessAssets.{Goodwill, Others, Stock, ChattelsAndMoveables}
import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{Format, JsValue, Json, Reads, Writes}

class TransactionSaleOfBusinessAssetsAnswersSpec extends AnyFreeSpec with Matchers with EitherValues {

  private val allBusinessAssets: Set[TransactionSaleOfBusinessAssets] = Set(
    Stock,
    Goodwill,
    ChattelsAndMoveables,
    Others
  )

  private val someBusinessAssets: Set[TransactionSaleOfBusinessAssets] = Set(
    Goodwill,
    ChattelsAndMoveables
  )

  private val noBusinessAssets: Set[TransactionSaleOfBusinessAssets] = Set.empty

  private val allBusinessAssetsAnswers = TransactionSaleOfBusinessAssetsAnswers(
    stock = "yes",
    goodwill = "yes",
    chattelsAndMoveables = "yes",
    others = "yes"
  )

  private val someBusinessAssetsAnswers = TransactionSaleOfBusinessAssetsAnswers(
    stock = "no",
    goodwill = "yes",
    chattelsAndMoveables = "yes",
    others = "no"
  )

  private val noBusinessAssetsAnswers = TransactionSaleOfBusinessAssetsAnswers(
    stock = "no",
    goodwill = "no",
    chattelsAndMoveables = "no",
    others = "no"
  )

  private val someBusinessAssetsAnswersJson = Json.obj(
    "stock" -> "no",
    "goodwill" -> "yes",
    "chattelsAndMoveables" -> "yes",
    "others" -> "no"
  )

  "TransactionSaleOfBusinessAssetsAnswers" - {
    ".fromSet" - {
      "must convert a set of all business assets to an answers object with all 'yes'" in {
        val answers = TransactionSaleOfBusinessAssetsAnswers.fromSet(allBusinessAssets)
        answers mustEqual allBusinessAssetsAnswers
      }

      "must convert a set of some business assets to an answers object with corresponding 'yes' and 'no'" in {
        val answers = TransactionSaleOfBusinessAssetsAnswers.fromSet(someBusinessAssets)
        answers mustEqual someBusinessAssetsAnswers
      }

      "must convert an empty set to an answers object with all 'no'" in {
        val answers = TransactionSaleOfBusinessAssetsAnswers.fromSet(noBusinessAssets)
        answers mustEqual noBusinessAssetsAnswers
      }
    }

    ".toSet" - {
      "must convert an answers object with all 'yes' to a set of all business assets" in {
        val businessAssetsSet = TransactionSaleOfBusinessAssetsAnswers.toSet(allBusinessAssetsAnswers)
        businessAssetsSet mustEqual allBusinessAssets
      }

      "must convert an answers object with some 'yes' and some 'no' to a set of corresponding business assets" in {
        val businessAssetsSet = TransactionSaleOfBusinessAssetsAnswers.toSet(someBusinessAssetsAnswers)
        businessAssetsSet mustEqual someBusinessAssets
      }

      "must convert an answers object with all 'no' to an empty set" in {
        val businessAssetsSet = TransactionSaleOfBusinessAssetsAnswers.toSet(noBusinessAssetsAnswers)
        businessAssetsSet mustEqual noBusinessAssets
      }

      "must omit invalid values and treat them as 'no'" in {
        val invalidAnswers = someBusinessAssetsAnswers.copy(stock = "true")
        val businessAssetsSet = TransactionSaleOfBusinessAssetsAnswers.toSet(invalidAnswers)
        businessAssetsSet mustEqual (someBusinessAssets - Stock)
      }
    }

    ".writes" - {
      "must be found implicitly" in {
        implicitly[Writes[TransactionSaleOfBusinessAssetsAnswers]]
      }

      "must serialize TransactionSaleOfBusinessAssetsAnswers" in {
        val json: JsValue = Json.toJson(someBusinessAssetsAnswers)
        (json \ "stock").as[String] mustBe "no"
        (json \ "goodwill").as[String] mustBe "yes"
        (json \ "chattelsAndMoveables").as[String] mustBe "yes"
        (json \ "others").as[String] mustBe "no"
      }
    }

    ".reads" - {
      "must be found implicitly" in {
        implicitly[Reads[TransactionSaleOfBusinessAssetsAnswers]]
      }

      "must deserialize valid JSON to TransactionSaleOfBusinessAssetsAnswers" in {
        val result = Json.fromJson[TransactionSaleOfBusinessAssetsAnswers](someBusinessAssetsAnswersJson).asEither.value
        result mustEqual someBusinessAssetsAnswers
      }
    }

    ".formats" - {

      "must be found implicitly" in {
        implicitly[Format[TransactionSaleOfBusinessAssetsAnswers]]
      }
    }
  }
}
