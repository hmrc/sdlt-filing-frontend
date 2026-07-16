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
import play.api.libs.json.*


case class SubmitRequest(
                          email: Option[String] = None,
                          fullReturn: FullReturn
                        )

object SubmitRequest {
  implicit val format: OFormat[SubmitRequest] = Json.format[SubmitRequest]
}


final case class SubmissionError(
                                  code: Option[String],
                                  message: String,
                                  location: Option[String] = None
                                )

object SubmissionError:
  given Format[SubmissionError] = Json.format[SubmissionError]


sealed trait SubmissionResponse:
  def returnId: String

object SubmissionResponse:
  final case class Submitted(returnId: String, utrn: String, receipt: Boolean) extends SubmissionResponse
  final case class Acknowledged(returnId: String)                              extends SubmissionResponse
  final case class Retryable(returnId: String)                                 extends SubmissionResponse
  final case class Rejected(returnId: String, errors: Seq[SubmissionError])    extends SubmissionResponse
  final case class Failed(returnId: String, errors: Seq[SubmissionError])      extends SubmissionResponse

  private val submittedFormat:    OFormat[Submitted]    = Json.format[Submitted]
  private val acknowledgedFormat: OFormat[Acknowledged] = Json.format[Acknowledged]
  private val retryableFormat:    OFormat[Retryable]    = Json.format[Retryable]
  private val rejectedFormat:     OFormat[Rejected]     = Json.format[Rejected]
  private val failedFormat:       OFormat[Failed]       = Json.format[Failed]

  given Format[SubmissionResponse] = new Format[SubmissionResponse]:
    def reads(json: JsValue): JsResult[SubmissionResponse] =
      (json \ "_type").asOpt[String] match
        case Some("submitted")    => submittedFormat.reads(json)
        case Some("acknowledged") => acknowledgedFormat.reads(json)
        case Some("retryable")    => retryableFormat.reads(json)
        case Some("rejected")     => rejectedFormat.reads(json)
        case Some("failed")       => failedFormat.reads(json)
        case Some(other)          => JsError(s"unknown SubmissionResponse _type: $other")
        case None                 => JsError("missing _type discriminator on SubmissionResponse")

    def writes(value: SubmissionResponse): JsValue = value match
      case s: Submitted    => submittedFormat.writes(s)    ++ Json.obj("_type" -> "submitted")
      case a: Acknowledged => acknowledgedFormat.writes(a) ++ Json.obj("_type" -> "acknowledged")
      case r: Retryable    => retryableFormat.writes(r)    ++ Json.obj("_type" -> "retryable")
      case r: Rejected     => rejectedFormat.writes(r)     ++ Json.obj("_type" -> "rejected")
      case f: Failed       => failedFormat.writes(f)       ++ Json.obj("_type" -> "failed")