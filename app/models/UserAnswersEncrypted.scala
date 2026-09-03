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

import play.api.libs.json.*
import uk.gov.hmrc.crypto.{Decrypter, Encrypter}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant
import scala.language.implicitConversions

/*
 * UserAnswersEncrypted is a wrapper for UserAnswers that encrypts the data and fullReturn fields
 * for secure storage in MongoDB. It is only used in the repository layer, and converted to/from
 * UserAnswers when reading/writing to the database. The encryption/decryption is handled
 * automatically by the implicit Reads and Writes using the provided Encrypter/Decrypter.
 */

final case class UserAnswersEncrypted(
                                       id: String,
                                       storn: String,
                                       returnId: Option[String] = None,
                                       fullReturn: Option[SensitiveWrapper[FullReturn]] = None,
                                       data: SensitiveWrapper[JsObject] = SensitiveWrapper(Json.obj()),
                                       lastUpdated: Instant = Instant.now
                                     ) {

  def toUserAnswers: UserAnswers =
    UserAnswers(
      id = id,
      storn = storn,
      returnId = returnId,
      fullReturn = fullReturn.map(_.decryptedValue),
      data = data.decryptedValue,
      lastUpdated = lastUpdated
    )
}

object UserAnswersEncrypted {

  def fromUserAnswers(userAnswers: UserAnswers): UserAnswersEncrypted =
    UserAnswersEncrypted(
      id = userAnswers.id,
      storn = userAnswers.storn,
      returnId = userAnswers.returnId,
      fullReturn = userAnswers.fullReturn.map(SensitiveWrapper(_)),
      data = SensitiveWrapper(userAnswers.data),
      lastUpdated = userAnswers.lastUpdated
    )

  implicit def reads(using crypto: Encrypter & Decrypter): Reads[UserAnswersEncrypted] = {
    import play.api.libs.functional.syntax.*

    (
      (__ \ "_id").read[String] and
        (__ \ "storn").read[String] and
        (__ \ "returnId").readNullable[String] and
        (__ \ "fullReturn").readNullable[SensitiveWrapper[FullReturn]] and
        (__ \ "data").read[SensitiveWrapper[JsObject]] and
        (__ \ "lastUpdated").read(MongoJavatimeFormats.instantFormat)
      )(UserAnswersEncrypted.apply _)
  }

  implicit def writes(using crypto: Encrypter & Decrypter): Writes[UserAnswersEncrypted] = {
    import play.api.libs.functional.syntax.*

    (
      (__ \ "_id").write[String] and
        (__ \ "storn").write[String] and
        (__ \ "returnId").writeNullable[String] and
        (__ \ "fullReturn").writeNullable[SensitiveWrapper[FullReturn]] and
        (__ \ "data").write[SensitiveWrapper[JsObject]] and
        (__ \ "lastUpdated").write[Instant](MongoJavatimeFormats.instantFormat)
      )(ua => (ua.id, ua.storn, ua.returnId, ua.fullReturn, ua.data, ua.lastUpdated))
  }

  implicit def format(using crypto: Encrypter & Decrypter): Format[UserAnswersEncrypted] =
    Format(reads, writes)
}