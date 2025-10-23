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

package models.prelimQuestions

import models.{Enumerable, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait BusinessOrIndividualRequest

object BusinessOrIndividualRequest extends Enumerable.Implicits {

  case object Option1 extends WithName("Individual") with BusinessOrIndividualRequest
  case object Option2 extends WithName("Business") with BusinessOrIndividualRequest

  val values: Seq[BusinessOrIndividualRequest] = Seq(
    Option1,
    Option2
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map { case (value, index) =>
    RadioItem(
      content = Text(
        value match {
          case Option1 => messages("site.yes")
          case Option2 => messages("site.no")
        }
      ),
      value = Some(value.toString),
      id = Some(s"value_$index")
    )
  }

  implicit val enumerable: Enumerable[BusinessOrIndividualRequest] =
    Enumerable(values.map(v => v.toString -> v): _*)
}