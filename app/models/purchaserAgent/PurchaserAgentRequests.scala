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

package models.purchaserAgent

import play.api.libs.json.{Json, Reads}

case class Agent(
                  storn:                  String,
                  agentId:                Option[String],
                  name:                   String,
                  houseNumber:            Option[String],
                  address1:               String,
                  address2:               Option[String],
                  address3:               Option[String],
                  address4:               Option[String],
                  postcode:               Option[String],
                  phone:                  Option[String],
                  email:                  Option[String],
                  dxAddress:              Option[String],
                  agentResourceReference: String
                )

object Agent {
  implicit val reads: Reads[Agent] = Json.reads[Agent]
}

case class SdltOrganisationResponse(
                                     storn: String,
                                     version: Option[String],
                                     agents: Seq[Agent]
                                   )

object SdltOrganisationResponse {
  implicit val reads: Reads[SdltOrganisationResponse] = Json.reads[SdltOrganisationResponse]
}
