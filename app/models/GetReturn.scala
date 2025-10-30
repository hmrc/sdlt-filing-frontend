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

package models

import play.api.libs.json.{Json, OFormat}

class GetReturn (
                  stornId: Option[String],
                  returnResourceRef: Option[String],
                  sdltOrganisation: Option[String],
                  returnInfo: Option[FullReturn],
                  purchaser: Option[Seq[String]],
                  companyDetails: Option[String],
                  vendor: Option[Seq[String]],
                  land: Option[Seq[String]],
                  transaction: Option[String],
                  returnAgent: Option[String],
                  agent: Option[String],
                  lease: Option[String],
                  taxCalculation: Option[String],
                  submission: Option[String],
                  submissionErrorDetails: Option[String],
                  residency: Option[String]
                )