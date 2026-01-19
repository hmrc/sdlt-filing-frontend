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

package models.purchaser

import models.{Enumerable, WithName}
import play.api.libs.json._

sealed trait PurchaserRemove

object PurchaserRemove extends Enumerable.Implicits {

  case class Remove(purchaserId: String) extends PurchaserRemove
  case object No extends WithName("no-action") with PurchaserRemove
  case object Keep extends WithName("no-action") with PurchaserRemove
  case class SelectNewMain(purchaserId: String) extends PurchaserRemove

  val values: Seq[PurchaserRemove] = Seq(No, Keep)

  private val RemovePrefix = "REMOVE-"
  private val PromotePrefix = "PROMOTE-"

  implicit val enumerable: Enumerable[PurchaserRemove] =
    Enumerable(values.map(v => v.toString -> v): _*)

  implicit val reads: Reads[PurchaserRemove] = Reads {
    case JsString("no") => JsSuccess(No)
    case JsString("keep") => JsSuccess(Keep)
    case JsString(value) if value.startsWith(RemovePrefix) =>
      JsSuccess(Remove(value.drop(RemovePrefix.length)))
    case JsString(value) if value.startsWith(PromotePrefix) =>
      JsSuccess(SelectNewMain(value.drop(PromotePrefix.length)))
    case _ => JsError("Invalid purchaser remove value")
  }

  implicit val writes: Writes[PurchaserRemove] = Writes {
    case No => JsString("no")
    case Keep => JsString("keep")
    case Remove(id) => JsString(s"$RemovePrefix$id")
    case SelectNewMain(id) => JsString(s"$PromotePrefix$id")
  }
}