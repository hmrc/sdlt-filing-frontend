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

package models.land

import models.{Enumerable, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait LandInterestTransferredOrCreated

object LandInterestTransferredOrCreated extends Enumerable.Implicits {

  case object FG extends WithName("FG") with LandInterestTransferredOrCreated
  case object FP extends WithName("FP") with LandInterestTransferredOrCreated
  case object FT extends WithName("FT") with LandInterestTransferredOrCreated
  case object LG extends WithName("LG") with LandInterestTransferredOrCreated
  case object LP extends WithName("LP") with LandInterestTransferredOrCreated
  case object LT extends WithName("LT") with LandInterestTransferredOrCreated
  case object OT extends WithName("OT") with LandInterestTransferredOrCreated

  val values: Seq[LandInterestTransferredOrCreated] = Seq(
    FG, FP, FT, LG, LP, LT, OT
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>
      
      val hintKey = s"land.landInterestTransferredOrCreated.${value}.hintText"

      RadioItem(
        content = Text(messages(s"land.landInterestTransferredOrCreated.${value.toString}")),
        value   = Some(value.toString),
        id      = Some(s"value_$index"),
        hint    = if (messages.isDefinedAt(hintKey)) Some(Hint(content = Text(messages(hintKey)))) else None
      )
  }

  implicit val enumerable: Enumerable[LandInterestTransferredOrCreated] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
