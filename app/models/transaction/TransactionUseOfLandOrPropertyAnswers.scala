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

package models.transaction

import TransactionUseOfLandOrProperty.*
import play.api.libs.json.{Format, Json}

case class TransactionUseOfLandOrPropertyAnswers(
                                                  office: String,
                                                  hotel: String,
                                                  shop: String,
                                                  warehouse: String,
                                                  factory: String,
                                                  otherIndustrialUnit: String,
                                                  other: String
                                                )
object TransactionUseOfLandOrPropertyAnswers {
  implicit val format: Format[TransactionUseOfLandOrPropertyAnswers] = Json.format[TransactionUseOfLandOrPropertyAnswers]
  
  def fromSet(selected: Set[TransactionUseOfLandOrProperty]): TransactionUseOfLandOrPropertyAnswers = {
    TransactionUseOfLandOrPropertyAnswers(
      office = if(selected.contains(Office)) "yes" else "no",
      hotel = if(selected.contains(Hotel)) "yes" else "no",
      shop = if(selected.contains(Shop)) "yes" else "no",
      warehouse = if(selected.contains(Warehouse)) "yes" else "no",
      factory = if(selected.contains(Factory)) "yes" else "no",
      otherIndustrialUnit = if(selected.contains(OtherIndustrialUnit)) "yes" else "no",
      other = if(selected.contains(Other)) "yes" else "no",
    )
  }

  def toSet(answers: TransactionUseOfLandOrPropertyAnswers): Set[TransactionUseOfLandOrProperty] = {
    val allValues: Map[TransactionUseOfLandOrProperty, String] = Map(
      Office -> answers.office,
      Hotel -> answers.hotel,
      Shop -> answers.shop,
      Warehouse -> answers.warehouse,
      Factory -> answers.factory,
      OtherIndustrialUnit -> answers.otherIndustrialUnit,
      Other -> answers.other,
    )

    allValues.collect {
      case (key, value) if value.trim.equalsIgnoreCase("yes") => key
    }.toSet
  }
  
}
