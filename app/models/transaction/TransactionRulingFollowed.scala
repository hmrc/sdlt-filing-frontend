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
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait TransactionRulingFollowed

object TransactionRulingFollowed extends Enumerable.Implicits {

  case object Yes extends WithName("yes") with TransactionRulingFollowed
  case object No extends WithName("no") with TransactionRulingFollowed
  case object Divider extends WithName("divider") with TransactionRulingFollowed
  case object RulingNotReceived extends WithName("RulingNotReceived") with TransactionRulingFollowed

  val values: Seq[TransactionRulingFollowed] = Seq(
    Yes, No, Divider, RulingNotReceived
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (Divider, index) =>
      RadioItem(
        content = Text(messages("site.or")),
        id = Some(s"value_$index"),
        divider = Some(messages("site.or"))
      )
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"transaction.rulingFollowed.${value.toString}")),
        value   = Some(value.toString),
        id      = Some(s"value_$index")
      )
  }

  implicit val enumerable: Enumerable[TransactionRulingFollowed] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
