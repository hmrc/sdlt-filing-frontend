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

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, Json}

class TransactionTypeSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "TransactionType" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(TransactionType.values.toSeq)

      forAll(gen) {
        transactionType =>

          JsString(transactionType.toString).validate[TransactionType].asOpt.value mustEqual transactionType
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!TransactionType.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[TransactionType] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(TransactionType.values.toSeq)

      forAll(gen) {
        transactionType =>

          Json.toJson(transactionType) mustEqual JsString(transactionType.toString)
      }
    }

    "parse" - {
      "must return a TransactionType when a valid string is parsed" in {
        val gen = Gen.oneOf(TransactionType.values)

        forAll(gen) {
          transactionType =>

            TransactionType.parse(Some(transactionType.toString)) mustBe Some(transactionType)
        }
      }

      "must return None when an invalid string is parsed" in {
        val invalidTransactionString: Option[String] = Some("randomType")

        TransactionType.parse(invalidTransactionString) mustBe None
      }

      "must return None when no string is parsed" in {

        TransactionType.parse(None) mustBe None
      }
    }
  }
}
