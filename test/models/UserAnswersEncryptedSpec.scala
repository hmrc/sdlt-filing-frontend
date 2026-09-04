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

package models

import constants.FullReturnConstants.completeFullReturn
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{EitherValues, OptionValues}
import play.api.libs.json.*
import uk.gov.hmrc.crypto.{Decrypter, Encrypter, SymmetricCryptoFactory}

import java.time.Instant
import java.time.temporal.ChronoUnit

class UserAnswersEncryptedSpec extends AnyFreeSpec with Matchers with EitherValues with OptionValues {

  private implicit val crypto: Encrypter with Decrypter =
    SymmetricCryptoFactory.aesGcmCrypto("z9WMSuFsHqfY5F2wgIcEvcnwyRTRB4dyPWfMbCbCXfM=")

  private val instant = Instant.now.truncatedTo(ChronoUnit.MILLIS)

  private val data = Json.obj(
    "purchaserCurrent" -> Json.obj(
      "whoIsMakingThePurchase" -> "Individual",
      "nationalInsuranceNumber" -> "AA123456A"
    )
  )

  private val userAnswers = UserAnswers(
    id = "test-session-id",
    storn = "test-storn-123",
    returnId = Some("12345"),
    fullReturn = Some(completeFullReturn),
    data = data,
    lastUpdated = instant
  )

  private val userAnswersEncrypted = UserAnswersEncrypted.fromUserAnswers(userAnswers)

  ".fromUserAnswers" - {

    "must wrap the sensitive fields and copy the rest across" in {
      userAnswersEncrypted.id mustBe "test-session-id"
      userAnswersEncrypted.storn mustBe "test-storn-123"
      userAnswersEncrypted.returnId mustBe Some("12345")
      userAnswersEncrypted.fullReturn.value.decryptedValue mustBe completeFullReturn
      userAnswersEncrypted.data.decryptedValue mustBe data
      userAnswersEncrypted.lastUpdated mustBe instant
    }

    "must leave fullReturn unwrapped when it is not present" in {
      val result = UserAnswersEncrypted.fromUserAnswers(userAnswers.copy(fullReturn = None))

      result.fullReturn must not be defined
    }
  }

  ".toUserAnswers" - {

    "must unwrap the sensitive fields and copy the rest across" in {
      userAnswersEncrypted.toUserAnswers mustEqual userAnswers
    }

    "must produce a None fullReturn when it is not present" in {
      val withoutFullReturn = userAnswers.copy(fullReturn = None)

      UserAnswersEncrypted.fromUserAnswers(withoutFullReturn).toUserAnswers mustEqual withoutFullReturn
    }
  }

  "UserAnswersEncrypted" - {

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[UserAnswersEncrypted]]
      }

      "must deserialize and decrypt a stored document" in {
        val json = Json.toJson(userAnswersEncrypted)
        val result = Json.fromJson[UserAnswersEncrypted](json).asEither.value

        result.fullReturn.value.decryptedValue mustBe completeFullReturn
        result.data.decryptedValue mustBe data
      }

      "must deserialize a document with no returnId or fullReturn" in {
        val minimal = userAnswers.copy(returnId = None, fullReturn = None)
        val json = Json.toJson(UserAnswersEncrypted.fromUserAnswers(minimal))
        val result = Json.fromJson[UserAnswersEncrypted](json).asEither.value

        result.returnId must not be defined
        result.fullReturn must not be defined
      }

      "must fail to deserialize when storn is missing" in {
        val json = Json.toJson(userAnswersEncrypted).as[JsObject] - "storn"
        val result = Json.fromJson[UserAnswersEncrypted](json).asEither

        result.isLeft mustBe true
      }

      "must fail to deserialize when the ciphertext is not valid" in {
        val json = Json.toJson(userAnswersEncrypted).as[JsObject] ++ Json.obj("data" -> "not-encrypted")

        assertThrows[Exception] {
          Json.fromJson[UserAnswersEncrypted](json)
        }
      }
    }

    ".writes" - {

      "must be found implicitly" in {
        implicitly[Writes[UserAnswersEncrypted]]
      }

      "must write the sensitive fields as opaque strings" in {
        val json = Json.toJson(userAnswersEncrypted)

        (json \ "data").get mustBe a[JsString]
        (json \ "fullReturn").get mustBe a[JsString]
      }

      "must leave the queryable fields in plaintext" in {
        val json = Json.toJson(userAnswersEncrypted)

        (json \ "_id").as[String] mustBe "test-session-id"
        (json \ "storn").as[String] mustBe "test-storn-123"
        (json \ "returnId").as[String] mustBe "12345"
      }

      "must not leak sensitive values into the serialized document" in {
        val raw = Json.toJson(userAnswersEncrypted).toString

        raw must not include "AA123456A"
        raw must not include "purchaserCurrent"
        raw must not include completeFullReturn.stornId
      }

      "must omit fullReturn when it is not present" in {
        val json = Json.toJson(UserAnswersEncrypted.fromUserAnswers(userAnswers.copy(fullReturn = None)))

        (json \ "fullReturn").isDefined mustBe false
      }
    }

    ".formats" - {

      "must be found implicitly" in {
        implicitly[Format[UserAnswersEncrypted]]
      }

      "must round-trip a full UserAnswers without losing anything" in {
        val json = Json.toJson(UserAnswersEncrypted.fromUserAnswers(userAnswers))
        val result = Json.fromJson[UserAnswersEncrypted](json).asEither.value

        result.toUserAnswers mustEqual userAnswers
      }

      "must round-trip a minimal UserAnswers" in {
        val minimal = UserAnswers(id = "id", storn = "storn", lastUpdated = instant)
        val json = Json.toJson(UserAnswersEncrypted.fromUserAnswers(minimal))
        val result = Json.fromJson[UserAnswersEncrypted](json).asEither.value

        result.toUserAnswers mustEqual minimal
      }

      "must produce different ciphertext for the same value on each write" in {
        val first = (Json.toJson(UserAnswersEncrypted.fromUserAnswers(userAnswers)) \ "data").as[String]
        val second = (Json.toJson(UserAnswersEncrypted.fromUserAnswers(userAnswers)) \ "data").as[String]

        first must not equal second
      }
    }
  }
}