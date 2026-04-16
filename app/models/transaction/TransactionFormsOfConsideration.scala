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
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import viewmodels.govuk.checkbox.*

sealed trait TransactionFormsOfConsideration

object TransactionFormsOfConsideration extends Enumerable.Implicits {

  case object Cash extends WithName("cash") with TransactionFormsOfConsideration
  case object Debt extends WithName("debt") with TransactionFormsOfConsideration
  case object BuildingWorks extends WithName("buildingWorks") with TransactionFormsOfConsideration
  case object Employment extends WithName("employment") with TransactionFormsOfConsideration
  case object Other extends WithName("other") with TransactionFormsOfConsideration
  case object SharesInAQuotedCompany extends WithName("sharesInAQuotedCompany") with TransactionFormsOfConsideration
  case object SharesInAnUnquotedCompany extends WithName("sharesInAnUnquotedCompany") with TransactionFormsOfConsideration
  case object OtherLand extends WithName("otherLand") with TransactionFormsOfConsideration
  case object Services extends WithName("services") with TransactionFormsOfConsideration
  case object Contingent extends WithName("contingent") with TransactionFormsOfConsideration

  val values: Seq[TransactionFormsOfConsideration] = Seq(
    Cash,
    Debt,
    BuildingWorks,
    Employment,
    Other,
    SharesInAQuotedCompany,
    SharesInAnUnquotedCompany,
    OtherLand,
    Services,
    Contingent
  )

  def checkboxItems(implicit messages: Messages): Seq[CheckboxItem] =
    values.zipWithIndex.map {
      case (value, index) =>
        val hintKey = s"transaction.transactionFormsOfConsideration.${value}.hintText"

        CheckboxItemViewModel(
          content = Text(messages(s"transaction.transactionFormsOfConsideration.${value.toString}")),
          fieldId = "value",
          index   = index,
          value   = value.toString,
        ).withHint(if (messages.isDefinedAt(hintKey)) Hint(content = Text(messages(hintKey))) else Hint())
    }

  implicit val enumerable: Enumerable[TransactionFormsOfConsideration] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
