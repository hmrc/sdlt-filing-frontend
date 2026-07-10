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

package models.submission

import models.FullReturn
import models.submission.SubmissionResponse.{Accepted, Acknowledge, Rejected}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{EitherValues, OptionValues}
import play.api.libs.json.*

class SubmissionModelsSpec extends AnyFreeSpec with Matchers with EitherValues with OptionValues {

  private val fullReturn = FullReturn(
    stornId = "STORN123456",
    returnResourceRef = "submitted"
  )

  "SubmitRequest" - {

    val completeRequest = SubmitRequest(email = Some("test@example.com"), fullReturn = fullReturn)
    val minimalRequest  = SubmitRequest(fullReturn = fullReturn)

    ".format" - {

      "must be found implicitly" in {
        implicitly[OFormat[SubmitRequest]]
      }

      "must serialize a request with all fields" in {
        val json = Json.toJson(completeRequest)

        (json \ "email").asOpt[String] mustBe Some("test@example.com")
        (json \ "fullReturn" \ "stornId").as[String] mustBe "STORN123456"
        (json \ "fullReturn" \ "returnResourceRef").as[String] mustBe "submitted"
      }

      "must omit email when not provided" in {
        val json = Json.toJson(minimalRequest)

        (json \ "email").toOption mustBe None
        (json \ "fullReturn" \ "stornId").as[String] mustBe "STORN123456"
      }

      "must round-trip a complete request" in {
        Json.toJson(completeRequest).as[SubmitRequest] mustBe completeRequest
      }

      "must round-trip a minimal request" in {
        Json.toJson(minimalRequest).as[SubmitRequest] mustBe minimalRequest
      }

      "must read email as None when absent" in {
        val json = Json.obj("fullReturn" -> Json.toJson(fullReturn))

        json.as[SubmitRequest].email mustBe None
      }

      "must fail to read when fullReturn is missing" in {
        Json.obj("email" -> "test@example.com").validate[SubmitRequest] mustBe a[JsError]
      }
    }

    "case class" - {

      "must create instance with all fields" in {
        completeRequest.email mustBe Some("test@example.com")
        completeRequest.fullReturn mustBe fullReturn
      }

      "must default email to None" in {
        minimalRequest.email must not be defined
      }

      "must support equality" in {
        completeRequest mustEqual completeRequest.copy()
      }

      "must not be equal when fields differ" in {
        completeRequest must not equal completeRequest.copy(email = Some("other@example.com"))
      }
    }
  }

  "SubmissionError" - {

    val completeError = SubmissionError(code = Some("1000"), message = "Invalid return", location = Some("/purchaser/0"))
    val minimalError  = SubmissionError(code = None, message = "Something went wrong")

    ".format" - {

      "must be found implicitly" in {
        implicitly[Format[SubmissionError]]
      }

      "must serialize an error with all fields" in {
        val json = Json.toJson(completeError)

        (json \ "code").asOpt[String] mustBe Some("1000")
        (json \ "message").as[String] mustBe "Invalid return"
        (json \ "location").asOpt[String] mustBe Some("/purchaser/0")
      }

      "must omit optional fields when not provided" in {
        val json = Json.toJson(minimalError)

        (json \ "code").toOption mustBe None
        (json \ "location").toOption mustBe None
        (json \ "message").as[String] mustBe "Something went wrong"
      }

      "must round-trip a complete error" in {
        Json.toJson(completeError).as[SubmissionError] mustBe completeError
      }

      "must round-trip a minimal error" in {
        Json.toJson(minimalError).as[SubmissionError] mustBe minimalError
      }

      "must read optional fields as None when absent" in {
        val result = Json.obj("message" -> "Boom").as[SubmissionError]

        result.code mustBe None
        result.location mustBe None
        result.message mustBe "Boom"
      }

      "must fail to read when message is missing" in {
        Json.obj("code" -> "1000").validate[SubmissionError] mustBe a[JsError]
      }
    }

    "case class" - {

      "must create instance with all fields" in {
        completeError.code mustBe Some("1000")
        completeError.message mustBe "Invalid return"
        completeError.location mustBe Some("/purchaser/0")
      }

      "must default location to None" in {
        SubmissionError(code = None, message = "x").location must not be defined
      }

      "must support equality" in {
        completeError mustEqual completeError.copy()
      }

      "must not be equal when fields differ" in {
        completeError must not equal completeError.copy(message = "Different")
      }
    }
  }

  "SubmissionResponse" - {

    val accepted    = Accepted(returnId = "382966898", utrn = "23456789MCe")
    val acknowledge = Acknowledge(returnId = "382966898")
    val rejected    = Rejected(
      returnId = "382966898",
      errors = Seq(SubmissionError(code = Some("1000"), message = "Invalid return", location = Some("/purchaser/0")))
    )

    ".format (writes)" - {

      "must be found implicitly" in {
        implicitly[Format[SubmissionResponse]]
      }

      "must serialize Accepted with the accepted discriminator" in {
        val json = Json.toJson[SubmissionResponse](accepted)

        (json \ "_type").as[String] mustBe "accepted"
        (json \ "returnId").as[String] mustBe "382966898"
        (json \ "utrn").as[String] mustBe "23456789MCe"
      }

      "must serialize Acknowledge with the acknowledged discriminator" in {
        val json = Json.toJson[SubmissionResponse](acknowledge)

        (json \ "_type").as[String] mustBe "acknowledged"
        (json \ "returnId").as[String] mustBe "382966898"
      }

      "must serialize Rejected with the rejected discriminator" in {
        val json = Json.toJson[SubmissionResponse](rejected)

        (json \ "_type").as[String] mustBe "rejected"
        (json \ "returnId").as[String] mustBe "382966898"
        (json \ "errors" \ 0 \ "message").as[String] mustBe "Invalid return"
      }
    }

    ".format (reads)" - {

      "must read an accepted payload into Accepted" in {
        val json = Json.obj("_type" -> "accepted", "returnId" -> "382966898", "utrn" -> "23456789MCe")

        json.as[SubmissionResponse] mustBe accepted
      }

      "must read an acknowledged payload into Acknowledge" in {
        val json = Json.obj("_type" -> "acknowledged", "returnId" -> "382966898")

        json.as[SubmissionResponse] mustBe acknowledge
      }

      "must read a rejected payload into Rejected" in {
        val json = Json.obj(
          "_type"    -> "rejected",
          "returnId" -> "382966898",
          "errors"   -> Json.arr(Json.obj("code" -> "1000", "message" -> "Invalid return", "location" -> "/purchaser/0"))
        )

        json.as[SubmissionResponse] mustBe rejected
      }

      "must fail with a JsError when the _type is unknown" in {
        val result = Json.obj("_type" -> "something-else", "returnId" -> "382966898").validate[SubmissionResponse]

        result mustBe a[JsError]
        result.asInstanceOf[JsError].errors.head._2.head.message must include("unknown SubmissionResponse _type")
      }

      "must fail with a JsError when the _type discriminator is missing" in {
        val result = Json.obj("returnId" -> "382966898").validate[SubmissionResponse]

        result mustBe a[JsError]
        result.asInstanceOf[JsError].errors.head._2.head.message must include("missing _type discriminator")
      }

      "must fail with a JsError when a matched variant is missing required fields" in {
        // discriminator says accepted, but utrn is absent
        Json.obj("_type" -> "accepted", "returnId" -> "382966898").validate[SubmissionResponse] mustBe a[JsError]
      }
    }

    "round-trip" - {

      "must round-trip Accepted" in {
        Json.toJson[SubmissionResponse](accepted).as[SubmissionResponse] mustBe accepted
      }

      "must round-trip Acknowledge" in {
        Json.toJson[SubmissionResponse](acknowledge).as[SubmissionResponse] mustBe acknowledge
      }

      "must round-trip Rejected" in {
        Json.toJson[SubmissionResponse](rejected).as[SubmissionResponse] mustBe rejected
      }
    }

    "trait" - {

      "must expose returnId for each variant" in {
        (accepted: SubmissionResponse).returnId mustBe "382966898"
        (acknowledge: SubmissionResponse).returnId mustBe "382966898"
        (rejected: SubmissionResponse).returnId mustBe "382966898"
      }
    }

    "case classes" - {

      "must create Accepted with fields" in {
        accepted.returnId mustBe "382966898"
        accepted.utrn mustBe "23456789MCe"
      }

      "must create Acknowledge with fields" in {
        acknowledge.returnId mustBe "382966898"
      }

      "must create Rejected with fields" in {
        rejected.returnId mustBe "382966898"
        rejected.errors must have size 1
        rejected.errors.head.message mustBe "Invalid return"
      }

      "must support equality" in {
        accepted mustEqual accepted.copy()
        acknowledge mustEqual acknowledge.copy()
        rejected mustEqual rejected.copy()
      }

      "must not be equal when fields differ" in {
        accepted must not equal accepted.copy(utrn = "different")
      }
    }
  }
}