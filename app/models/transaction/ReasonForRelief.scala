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

  case object PartExchange extends WithName("partExchange") with ReasonForRelief
  case object RelocationOfEmployment extends WithName("relocationOfEmployment") with ReasonForRelief
  case object CompulsoryDevelopment extends WithName("compulsoryDevelopment") with ReasonForRelief
  case object CompliancePlanning extends WithName("compliancePlanning") with ReasonForRelief
  case object GroupRelief extends WithName("groupRelief") with ReasonForRelief
  case object ReconstructionRelief extends WithName("reconstructionRelief") with ReasonForRelief
  case object AcquisitionReliefTax extends WithName("acquisitionReliefTax") with ReasonForRelief
  case object DemutualisationInsurance extends WithName("demutualisationInsurance") with ReasonForRelief
  case object DemutualisationBuildingSociety extends WithName("demutualisationBuildingSociety") with ReasonForRelief
  case object IncorporationLimitedLiability extends WithName("incorporationLimitedLiability") with ReasonForRelief
  case object TransfersPublic extends WithName("transfersPublic") with ReasonForRelief
  case object TransfersReorganisation extends WithName("transfersReorganisation") with ReasonForRelief
  case object CharitiesRelief extends WithName("charitiesRelief") with ReasonForRelief
  case object AcquisitionByBodies extends WithName("acquisitionByBodies") with ReasonForRelief
  case object RightToBuy extends WithName("rightToBuy") with ReasonForRelief
  case object RegisteredSocial extends WithName("registeredSocial") with ReasonForRelief
  case object AlternativePropertyFinance extends WithName("alternativePropertyFinance") with ReasonForRelief
  case object CollectiveEnfranchisement extends WithName("collectiveEnfranchisement") with ReasonForRelief
  case object CroftingRightToBuy extends WithName("croftingRightToBuy") with ReasonForRelief
  case object DiplomaticPrivileges extends WithName("diplomaticPrivileges") with ReasonForRelief
  case object OtherRelief extends WithName("otherRelief") with ReasonForRelief
  case object CombinationOfReliefs extends WithName("combinationOfReliefs") with ReasonForRelief
  case object AlternativeFinanceInvestment extends WithName("alternativeFinanceInvestment") with ReasonForRelief
  case object FirstTimeBuyer extends WithName("firstTimeBuyer") with ReasonForRelief
  case object MultipleDwellings extends WithName("multipleDwellings") with ReasonForRelief
  case object PreCompletion extends WithName("preCompletion") with ReasonForRelief
  case object ReliefFromRate extends WithName("reliefFromRate") with ReasonForRelief
  case object ReliefForFreeport extends WithName("reliefForFreeport") with ReasonForRelief
  case object ReliefInvestmentZone extends WithName("reliefInvestmentZone") with ReasonForRelief
  case object SeedingRelief extends WithName("seedingRelief") with ReasonForRelief

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

  implicit val enumerable: Enumerable[ReasonForRelief] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
