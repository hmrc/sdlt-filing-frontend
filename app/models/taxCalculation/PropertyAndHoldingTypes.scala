/*
 * Copyright 2023 HM Revenue & Customs
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

package models.taxCalculation

import models.land.LandTypeOfProperty
import models.prelimQuestions.TransactionType
import play.api.libs.json._

object HoldingTypes extends Enumeration {

  val leasehold = Value
  val freehold  = Value

  def fromCode(description: String): Option[HoldingTypes.Value] =
    TransactionType.parse(Some(description)).map {
      case TransactionType.GrantOfLease => leasehold
      case _                            => freehold
    }

  implicit val writes: Writes[HoldingTypes.Value] =
    Writes(value => JsString(value.toString))
}

object PropertyTypes extends Enumeration {
  val residential    = Value
  val nonResidential = Value
  val mixed          = Value

  def fromCode(code: String): Option[PropertyTypes.Value] = code match {
    case LandTypeOfProperty.Residential.toString    => Some(residential)
    case LandTypeOfProperty.Additional.toString     => Some(residential)
    case LandTypeOfProperty.Mixed.toString          => Some(mixed)
    case LandTypeOfProperty.NonResidential.toString => Some(nonResidential)
    case _                                          => None
  }

  implicit val writes: Writes[PropertyTypes.Value] = Writes {
    case `residential`    => JsString("Residential")
    case `nonResidential` => JsString("NonResidential")
    case `mixed`          => JsString("Mixed")
  }
}
