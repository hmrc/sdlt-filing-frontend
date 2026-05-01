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

import models.transaction.TransactionSaleOfBusinessAssets.{Stock, Goodwill, ChattelsAndMoveables, Others}
import play.api.libs.json.{Format, Json}

case class TransactionSaleOfBusinessAssetsAnswers (
                                                   stock: String,
                                                   goodwill: String,
                                                   chattelsAndMoveables: String,
                                                   others: String,
                                                 )

object TransactionSaleOfBusinessAssetsAnswers {
  implicit val format:Format[TransactionSaleOfBusinessAssetsAnswers] = Json.format[TransactionSaleOfBusinessAssetsAnswers]

  def fromSet(selected: Set[TransactionSaleOfBusinessAssets]): TransactionSaleOfBusinessAssetsAnswers = {
    TransactionSaleOfBusinessAssetsAnswers(
      stock = if (selected.contains(Stock)) "yes" else "no",
      goodwill = if (selected.contains(Goodwill)) "yes" else "no",
      chattelsAndMoveables = if (selected.contains(ChattelsAndMoveables)) "yes" else "no",
      others = if (selected.contains(Others)) "yes" else "no"
    )
  }
  
  def toSet(answers: TransactionSaleOfBusinessAssetsAnswers): Set[TransactionSaleOfBusinessAssets] = {
    val allValues: Map[TransactionSaleOfBusinessAssets, String] = Map(
      Stock -> answers.stock,
      Goodwill -> answers.goodwill,
      ChattelsAndMoveables -> answers.chattelsAndMoveables,
      Others -> answers.others
    )
    
    allValues.collect {
      case (key, value) if value.trim.equalsIgnoreCase("yes") => key
    }.toSet
  }
}

