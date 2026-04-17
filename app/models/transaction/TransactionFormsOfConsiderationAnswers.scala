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

import models.transaction.TransactionFormsOfConsideration.{BuildingWorks, Cash, Contingent, Debt, Employment, Other, OtherLand, Services, SharesInAQuotedCompany, SharesInAnUnquotedCompany}
import play.api.libs.json.{Format, Json}

case class TransactionFormsOfConsiderationAnswers(
                                                   cash: String,
                                                   debt: String,
                                                   buildingWorks: String,
                                                   employment: String,
                                                   other: String,
                                                   sharesInAQuotedCompany: String,
                                                   sharesInAnUnquotedCompany: String,
                                                   otherLand: String,
                                                   services: String,
                                                   contingent: String,
                                                 )


object TransactionFormsOfConsiderationAnswers {
  implicit val format: Format[TransactionFormsOfConsiderationAnswers] = Json.format[TransactionFormsOfConsiderationAnswers]

  def fromSet(selected: Set[TransactionFormsOfConsideration]): TransactionFormsOfConsiderationAnswers = {
    TransactionFormsOfConsiderationAnswers(
      cash = if (selected.contains(Cash)) "yes" else "no",
      debt = if (selected.contains(Debt)) "yes" else "no",
      buildingWorks = if (selected.contains(BuildingWorks)) "yes" else "no",
      employment = if (selected.contains(Employment)) "yes" else "no",
      other = if (selected.contains(Other)) "yes" else "no",
      sharesInAQuotedCompany = if (selected.contains(SharesInAQuotedCompany)) "yes" else "no",
      sharesInAnUnquotedCompany = if (selected.contains(SharesInAnUnquotedCompany)) "yes" else "no",
      otherLand = if (selected.contains(OtherLand)) "yes" else "no",
      services = if (selected.contains(Services)) "yes" else "no",
      contingent = if (selected.contains(Contingent)) "yes" else "no"
    )
  }

  def toSet(answers: TransactionFormsOfConsiderationAnswers): Set[TransactionFormsOfConsideration] = {
    val allValues: Map[TransactionFormsOfConsideration, String] = Map(
      Cash -> answers.cash,
      Debt -> answers.debt,
      BuildingWorks -> answers.buildingWorks,
      Employment -> answers.employment,
      Other -> answers.other,
      SharesInAQuotedCompany -> answers.sharesInAQuotedCompany,
      SharesInAnUnquotedCompany -> answers.sharesInAnUnquotedCompany,
      OtherLand -> answers.otherLand,
      Services -> answers.services,
      Contingent -> answers.contingent
    )

    allValues.collect {
      case (key, value) if value.trim.equalsIgnoreCase("yes") => key
    }.toSet
  }
}