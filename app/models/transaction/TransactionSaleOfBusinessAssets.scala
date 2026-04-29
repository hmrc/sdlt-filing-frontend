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

import models.{Enumerable, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.checkbox.*

sealed trait TransactionSaleOfBusinessAssets {
  def order: Int
}

object TransactionSaleOfBusinessAssets extends Enumerable.Implicits {

  case object Stock extends WithName("stock") with TransactionSaleOfBusinessAssets {
    val order = 1
  }
  case object Goodwill extends WithName("goodwill") with TransactionSaleOfBusinessAssets {
    val order = 2
  }
  case object ChattelsAndMoveables extends WithName("chattelsAndMoveables") with TransactionSaleOfBusinessAssets {
    val order = 3
  }
  case object Others extends WithName("others") with TransactionSaleOfBusinessAssets {
    val order = 4
  }

  val values: Seq[TransactionSaleOfBusinessAssets] = Seq(
    Stock,
    Goodwill,
    ChattelsAndMoveables,
    Others
  )

  def checkboxItems(implicit messages: Messages): Seq[CheckboxItem] =
    values.zipWithIndex.map {
      case (value, index) =>
        CheckboxItemViewModel(
          content = Text(messages(s"transaction.transactionSaleOfBusinessAssets.${value.toString}")),
          fieldId = "value",
          index   = index,
          value   = value.toString
        )
    }

  implicit val enumerable: Enumerable[TransactionSaleOfBusinessAssets] =
    Enumerable(values.map(v => v.toString -> v): _*)
}