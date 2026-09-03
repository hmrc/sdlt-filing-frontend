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

package repositories

import config.FrontendAppConfig
import constants.FullReturnConstants
import models.{FullReturn, UserAnswers, UserAnswersEncrypted}
import org.mockito.Mockito.when
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.Filters
import org.scalactic.source.Position
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.slf4j.MDC
import play.api.libs.json.Json
import uk.gov.hmrc.crypto.{Decrypter, Encrypter, SymmetricCryptoFactory}
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport
import uk.gov.hmrc.play.bootstrap.dispatchers.MDCPropagatingExecutorService

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, ZoneId}
import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}

class SessionRepositoryISpec
  extends AnyFreeSpec
    with Matchers
    with DefaultPlayMongoRepositorySupport[UserAnswersEncrypted]
    with ScalaFutures
    with IntegrationPatience
    with OptionValues
    with MockitoSugar {

  private val instant = Instant.now.truncatedTo(ChronoUnit.MILLIS)
  private val stubClock: Clock = Clock.fixed(instant, ZoneId.systemDefault)
  private val fullReturn: FullReturn = FullReturnConstants.completeFullReturn

  private implicit val crypto: Encrypter with Decrypter =
    SymmetricCryptoFactory.aesGcmCrypto("z9WMSuFsHqfY5F2wgIcEvcnwyRTRB4dyPWfMbCbCXfM=")

  private val testReturnId = "123456"

  private val userAnswers =
    UserAnswers("id", storn = "TESTSTORN", data = Json.obj("foo" -> "bar"), lastUpdated = Instant.ofEpochSecond(1))

  private val userAnswersWithReturnId =
    UserAnswers(
      "id",
      storn = "TESTSTORN",
      Some(testReturnId),
      Some(fullReturn),
      Json.obj("foo" -> "bar"),
      Instant.ofEpochSecond(1)
    )

  private val mockAppConfig = mock[FrontendAppConfig]
  when(mockAppConfig.cacheTtl) thenReturn 1L

  protected override val repository: SessionRepository = new SessionRepository(
    mongoComponent = mongoComponent,
    appConfig      = mockAppConfig,
    clock          = stubClock,
    crypto         = crypto
  )(scala.concurrent.ExecutionContext.Implicits.global)

  private def rawDocument(id: String): Future[Option[BsonDocument]] =
    mongoComponent.database
      .getCollection[BsonDocument]("user-answers")
      .find(Filters.equal("_id", id))
      .headOption()

  ".set" - {

    "must set the last updated time on the supplied user answers to `now`, and save them" in {

      val expectedResult = userAnswers copy (lastUpdated = instant)

      repository.set(userAnswers).futureValue

      val updatedRecord = find(Filters.equal("_id", userAnswers.id)).futureValue.headOption.value

      updatedRecord.toUserAnswers mustEqual expectedResult
    }

    "must save the fullReturn and read it back intact" in {

      repository.set(userAnswersWithReturnId).futureValue

      val result = repository.get(userAnswersWithReturnId.id).futureValue

      result.value.fullReturn.value mustEqual fullReturn
    }

    "must store data and fullReturn as encrypted strings" in {

      repository.set(userAnswersWithReturnId).futureValue

      val document = rawDocument(userAnswersWithReturnId.id).futureValue.value

      document.get("data").isString mustBe true
      document.get("fullReturn").isString mustBe true
    }

    "must not write the plaintext values to the database" in {

      repository.set(userAnswersWithReturnId).futureValue

      val raw = rawDocument(userAnswersWithReturnId.id).futureValue.value.toJson

      raw must not include "bar"
      raw must not include fullReturn.stornId
    }

    "must leave the queryable fields in plaintext" in {

      repository.set(userAnswersWithReturnId).futureValue

      val document = rawDocument(userAnswersWithReturnId.id).futureValue.value

      document.getString("_id").getValue mustEqual "id"
      document.getString("storn").getValue mustEqual "TESTSTORN"
      document.getString("returnId").getValue mustEqual testReturnId
    }

    "must overwrite an existing record rather than duplicating it" in {

      repository.set(userAnswersWithReturnId).futureValue
      repository.set(userAnswers).futureValue

      val result = repository.get(userAnswers.id).futureValue

      result.value.fullReturn must not be defined
      result.value.returnId must not be defined
    }

    mustPreserveMdc(repository.set(userAnswers))
  }

  ".get" - {

    "when there is a record for this id" - {

      "must update the lastUpdated time and get the record no return id" in {

        insert(UserAnswersEncrypted.fromUserAnswers(userAnswers)).futureValue

        val result         = repository.get(userAnswers.id).futureValue
        val expectedResult = userAnswers copy (lastUpdated = instant)

        result.value mustEqual expectedResult
      }

      "must update the lastUpdated time and get the record with return id" in {

        insert(UserAnswersEncrypted.fromUserAnswers(userAnswersWithReturnId)).futureValue

        val result = repository.get(userAnswersWithReturnId.id).futureValue
        val expectedResult = userAnswersWithReturnId copy (lastUpdated = instant)

        result.value mustEqual expectedResult
        result.value.returnId mustBe Some(testReturnId)
        result.value.fullReturn.value mustEqual fullReturn
      }
    }

    "when there is no record for this id" - {

      "must return None" in {

        repository.get("id that does not exist").futureValue must not be defined
      }
    }

    mustPreserveMdc(repository.get(userAnswers.id))
  }

  ".clear" - {

    "must remove a record" in {

      insert(UserAnswersEncrypted.fromUserAnswers(userAnswers)).futureValue

      repository.clear(userAnswers.id).futureValue

      repository.get(userAnswers.id).futureValue must not be defined
    }

    "must return true when there is no record to remove" in {
      val result = repository.clear("id that does not exist").futureValue

      result mustEqual true
    }

    mustPreserveMdc(repository.clear(userAnswers.id))
  }

  ".keepAlive" - {

    "when there is a record for this id" - {

      "must update its lastUpdated to `now` and return true" in {

        insert(UserAnswersEncrypted.fromUserAnswers(userAnswers)).futureValue

        repository.keepAlive(userAnswers.id).futureValue

        val expectedUpdatedAnswers = userAnswers copy (lastUpdated = instant)

        val updatedAnswers = find(Filters.equal("_id", userAnswers.id)).futureValue.headOption.value
        updatedAnswers.toUserAnswers mustEqual expectedUpdatedAnswers
      }

      "must not disturb the encrypted fields" in {

        insert(UserAnswersEncrypted.fromUserAnswers(userAnswersWithReturnId)).futureValue

        repository.keepAlive(userAnswersWithReturnId.id).futureValue

        repository.get(userAnswersWithReturnId.id).futureValue.value.fullReturn.value mustEqual fullReturn
      }
    }

    "when there is no record for this id" - {

      "must return true" in {

        repository.keepAlive("id that does not exist").futureValue mustEqual true
      }
    }

    mustPreserveMdc(repository.keepAlive(userAnswers.id))
  }

  private def mustPreserveMdc[A](f: => Future[A])(implicit pos: Position): Unit =
    "must preserve MDC" in {

      implicit lazy val ec: ExecutionContext =
        ExecutionContext.fromExecutor(new MDCPropagatingExecutorService(Executors.newFixedThreadPool(2)))

      MDC.put("test", "foo")

      f.map { _ =>
        MDC.get("test") mustEqual "foo"
      }.futureValue
    }
}