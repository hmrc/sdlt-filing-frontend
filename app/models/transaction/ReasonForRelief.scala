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
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait ReasonForRelief

object ReasonForRelief extends Enumerable.Implicits {

  case object PartExchange extends WithName("08") with ReasonForRelief
  case object RelocationOfEmployment extends WithName("09") with ReasonForRelief
  case object CompulsoryDevelopment extends WithName("10") with ReasonForRelief
  case object CompliancePlanning extends WithName("11") with ReasonForRelief
  case object GroupRelief extends WithName("12") with ReasonForRelief
  case object ReconstructionRelief extends WithName("13") with ReasonForRelief
  case object AcquisitionReliefTax extends WithName("14") with ReasonForRelief
  case object DemutualisationInsurance extends WithName("15") with ReasonForRelief
  case object DemutualisationBuildingSociety extends WithName("16") with ReasonForRelief
  case object IncorporationLimitedLiability extends WithName("17") with ReasonForRelief
  case object TransfersPublic extends WithName("18") with ReasonForRelief
  case object TransfersReorganisation extends WithName("19") with ReasonForRelief
  case object CharitiesRelief extends WithName("20") with ReasonForRelief
  case object AcquisitionByBodies extends WithName("21") with ReasonForRelief
  case object RightToBuy extends WithName("22") with ReasonForRelief
  case object RegisteredSocial extends WithName("23") with ReasonForRelief
  case object AlternativePropertyFinance extends WithName("24") with ReasonForRelief
  case object CollectiveEnfranchisement extends WithName("25") with ReasonForRelief
  case object CroftingRightToBuy extends WithName("26") with ReasonForRelief
  case object DiplomaticPrivileges extends WithName("27") with ReasonForRelief
  case object OtherRelief extends WithName("28") with ReasonForRelief
  case object CombinationOfReliefs extends WithName("29") with ReasonForRelief
  case object AlternativeFinanceInvestment extends WithName("31") with ReasonForRelief
  case object FirstTimeBuyer extends WithName("32") with ReasonForRelief
  case object MultipleDwellings extends WithName("33") with ReasonForRelief
  case object PreCompletion extends WithName("34") with ReasonForRelief
  case object ReliefFromRate extends WithName("35") with ReasonForRelief
  case object ReliefForFreeport extends WithName("36") with ReasonForRelief
  case object ReliefInvestmentZone extends WithName("37") with ReasonForRelief
  case object SeedingRelief extends WithName("38") with ReasonForRelief

  val values: Seq[ReasonForRelief] = Seq(
    PartExchange,
    RelocationOfEmployment,
    CompulsoryDevelopment,
    CompliancePlanning,
    GroupRelief,
    ReconstructionRelief,
    AcquisitionReliefTax,
    DemutualisationInsurance,
    DemutualisationBuildingSociety,
    IncorporationLimitedLiability,
    TransfersPublic,
    TransfersReorganisation,
    CharitiesRelief,
    AcquisitionByBodies,
    RightToBuy,
    RegisteredSocial,
    AlternativePropertyFinance,
    CollectiveEnfranchisement,
    CroftingRightToBuy,
    DiplomaticPrivileges,
    OtherRelief,
    CombinationOfReliefs,
    AlternativeFinanceInvestment,
    FirstTimeBuyer,
    MultipleDwellings,
    PreCompletion,
    ReliefFromRate,
    ReliefForFreeport,
    ReliefInvestmentZone,
    SeedingRelief
  )

  def fromString(value: String): ReasonForRelief =
    values.find(_.toString == value)
      .get
  
  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>

      val hintKey = s"transaction.ReasonForRelief.${value}.hint"

      RadioItem(
        content = Text(messages(s"transaction.ReasonForRelief.${value.toString}")),
        value = Some(value.toString),
        id = Some(s"value_$index"),
        hint = if (messages.isDefinedAt(hintKey)) Some(Hint(content = Text(messages(hintKey)))) else None
      )
  }

  def parse(s: Option[String]): Option[ReasonForRelief] = s.flatMap { code =>
    values.find(_.toString == code)
  }

  def isValid(value: String): Boolean =
    values.exists(_.toString == value)

  implicit val enumerable: Enumerable[ReasonForRelief] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
