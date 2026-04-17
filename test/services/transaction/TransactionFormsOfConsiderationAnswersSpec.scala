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

package services.transaction

import models.transaction.{TransactionFormsOfConsideration, TransactionFormsOfConsiderationAnswers}
import models.transaction.TransactionFormsOfConsideration.{BuildingWorks, Cash, Contingent, Debt, Employment, Other, OtherLand, Services, SharesInAQuotedCompany, SharesInAnUnquotedCompany}
import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{Format, JsValue, Json, Reads, Writes}

class TransactionFormsOfConsiderationAnswersSpec extends AnyFreeSpec with Matchers with EitherValues {

  private val allFormsOfConsideration: Set[TransactionFormsOfConsideration] = Set(
    Cash,
    Debt,
    BuildingWorks,
    Employment,
    Other,
    SharesInAQuotedCompany,
    SharesInAnUnquotedCompany,
    OtherLand,
    Services,
    Contingent
  )

  private val someFormsOfConsideration: Set[TransactionFormsOfConsideration] = Set(
    Cash,
    Debt,
    BuildingWorks,
    Employment
  )

  private val noFormsOfConsideration: Set[TransactionFormsOfConsideration] = Set.empty

  private val allFormsOfConsiderationAnswers = TransactionFormsOfConsiderationAnswers(
    cash = "yes",
    debt = "yes",
    buildingWorks = "yes",
    employment = "yes",
    other = "yes",
    sharesInAQuotedCompany = "yes",
    sharesInAnUnquotedCompany = "yes",
    otherLand = "yes",
    services = "yes",
    contingent = "yes",
  )

  private val someFormsOfConsiderationAnswers = TransactionFormsOfConsiderationAnswers(
    cash = "yes",
    debt = "yes",
    buildingWorks = "yes",
    employment = "yes",
    other = "no",
    sharesInAQuotedCompany = "no",
    sharesInAnUnquotedCompany = "no",
    otherLand = "no",
    services = "no",
    contingent = "no",
  )

  private val noFormsOfConsiderationAnswers = TransactionFormsOfConsiderationAnswers(
    cash = "no",
    debt = "no",
    buildingWorks = "no",
    employment = "no",
    other = "no",
    sharesInAQuotedCompany = "no",
    sharesInAnUnquotedCompany = "no",
    otherLand = "no",
    services = "no",
    contingent = "no",
  )

  private val someFormsOfConsiderationAnswersJson = Json.obj(
    "cash" -> "yes",
    "debt" -> "yes",
    "buildingWorks" -> "yes",
    "employment" -> "yes",
    "other" -> "no",
    "sharesInAQuotedCompany" -> "no",
    "sharesInAnUnquotedCompany" -> "no",
    "otherLand" -> "no",
    "services" -> "no",
    "contingent" -> "no"
  )

  "TransactionFormsOfConsiderationAnswers" - {
    ".fromSet" - {
      "must convert a set of all forms of consideration to an answers object with all 'yes'" in {
        val answers = TransactionFormsOfConsiderationAnswers.fromSet(allFormsOfConsideration)
        answers mustEqual allFormsOfConsiderationAnswers
      }

      "must convert a set of some forms of consideration to an answers object with corresponding 'yes' and 'no'" in {
        val answers = TransactionFormsOfConsiderationAnswers.fromSet(someFormsOfConsideration)
        answers mustEqual someFormsOfConsiderationAnswers
      }

      "must convert an empty set to an answers object with all 'no'" in {
        val answers = TransactionFormsOfConsiderationAnswers.fromSet(noFormsOfConsideration)
        answers mustEqual noFormsOfConsiderationAnswers
      }
    }

    ".toSet" - {
      "must convert an answers object with all 'yes' to a set of all forms of consideration" in {
        val formsOfConsiderationSet = TransactionFormsOfConsiderationAnswers.toSet(allFormsOfConsiderationAnswers)
        formsOfConsiderationSet mustEqual allFormsOfConsideration
      }

      "must convert an answers object with some 'yes' and some 'no' to a set of corresponding forms of consideration" in {
        val formsOfConsiderationSet = TransactionFormsOfConsiderationAnswers.toSet(someFormsOfConsiderationAnswers)
        formsOfConsiderationSet mustEqual someFormsOfConsideration
      }

      "must convert an answers object with all 'no' to an empty set" in {
        val formsOfConsiderationSet = TransactionFormsOfConsiderationAnswers.toSet(noFormsOfConsiderationAnswers)
        formsOfConsiderationSet mustEqual noFormsOfConsideration
      }

      "must omit invalid values and treat them as 'no'" in {
        val invalidAnswers = someFormsOfConsiderationAnswers.copy(cash = "true")
        val formsOfConsiderationSet = TransactionFormsOfConsiderationAnswers.toSet(invalidAnswers)
        formsOfConsiderationSet mustEqual (someFormsOfConsideration - Cash)
      }
    }

    ".writes" - {
      "must be found implicitly" in {
        implicitly[Writes[TransactionFormsOfConsiderationAnswers]]
      }

      "must serialize TransactionFormsOfConsiderationAnswers" in {
        val json: JsValue = Json.toJson(someFormsOfConsiderationAnswers)
        (json \ "cash").as[String] mustBe "yes"
        (json \ "debt").as[String] mustBe "yes"
        (json \ "buildingWorks").as[String] mustBe "yes"
        (json \ "employment").as[String] mustBe "yes"
        (json \ "other").as[String] mustBe "no"
        (json \ "sharesInAQuotedCompany").as[String] mustBe "no"
        (json \ "sharesInAnUnquotedCompany").as[String] mustBe "no"
        (json \ "otherLand").as[String] mustBe "no"
        (json \ "services").as[String] mustBe "no"
        (json \ "contingent").as[String] mustBe "no"
      }
    }

    ".reads" - {
      "must be found implicitly" in {
        implicitly[Reads[TransactionFormsOfConsiderationAnswers]]
      }

      "must deserialize valid JSON to TransactionFormsOfConsiderationAnswers" in {
        val result = Json.fromJson[TransactionFormsOfConsiderationAnswers](someFormsOfConsiderationAnswersJson).asEither.value
        result mustEqual someFormsOfConsiderationAnswers
      }
    }

    ".formats" - {

      "must be found implicitly" in {
        implicitly[Format[TransactionFormsOfConsiderationAnswers]]
      }
    }
  }
}
