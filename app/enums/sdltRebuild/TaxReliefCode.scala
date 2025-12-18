/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package enums.sdltRebuild

import play.api.libs.json.{JsError, JsNumber, JsSuccess, Reads, Writes}

sealed abstract class TaxReliefCode(val code: Int) {
  override def toString: String = s"TaxReliefCode:$code"

  val fromCode: (Int, TaxReliefCode) = code -> this
}

sealed trait ZeroRate
sealed trait FreeportRelief

/* ------------- Tax codes associated to rate: 0% --------------------------- */

case object PartExchange extends TaxReliefCode(8) with ZeroRate
case object ReLocationEmployment extends TaxReliefCode(9) with ZeroRate
case object CompulsoryPurchaseFacilitatingDevelopment extends TaxReliefCode(10) with ZeroRate
case object ComplianceWithPlanningObligations extends TaxReliefCode(11) with ZeroRate
case object GroupRelief extends TaxReliefCode(12) with ZeroRate
case object ReConstructionRelief extends TaxReliefCode(13) with ZeroRate
case object DemutualisationOfInsuranceCompany extends TaxReliefCode(15) with ZeroRate
case object DemutualisationOfBuildingSociety extends TaxReliefCode(16) with ZeroRate
case object IncorporationOfLimitedLiabilityPartnership extends TaxReliefCode(17) with ZeroRate
case object TransfersInvolvingPublicBodies extends TaxReliefCode(18) with ZeroRate
case object TransferInConsequenceOfReorganisationOfParliamentaryConstituencies extends TaxReliefCode(19) with ZeroRate
case object CharitiesTaxReliefs extends TaxReliefCode(20) with ZeroRate
case object AcquisitionByBodiesEstablishedForNationalPurposes extends TaxReliefCode(21) with ZeroRate
case object RegisteredSocialLandlords extends TaxReliefCode(23) with ZeroRate
case object AlternativePropertyFinance extends TaxReliefCode(24) with ZeroRate
case object CroftingCommunityRightToBuy extends TaxReliefCode(26) with ZeroRate
case object DiplomaticPrivileges extends TaxReliefCode(27) with ZeroRate
case object OtherTaxReliefs extends TaxReliefCode(28) with ZeroRate
case object CombinationOfReliefs extends TaxReliefCode(29) with ZeroRate
case object AlternativeFinanceInvestmentBondsRelief extends TaxReliefCode(31) with ZeroRate
case object FreeportsTaxSiteRelief extends TaxReliefCode(36) with FreeportRelief
case object InvestmentZonesTaxSiteRelief extends TaxReliefCode(37) with FreeportRelief
case object SeedingRelief extends TaxReliefCode(38) with ZeroRate

/* ------------- Tax codes associated to rate: x% --------------------------- */

/* ------------- Tax codes associated to rate: y% --------------------------- */

object TaxReliefCode {
  val toName: Map[Int, TaxReliefCode] = Map(
    PartExchange.fromCode,
    ReLocationEmployment.fromCode,
    CompulsoryPurchaseFacilitatingDevelopment.fromCode,
    ComplianceWithPlanningObligations.fromCode,
    GroupRelief.fromCode,
    ReConstructionRelief.fromCode,
    DemutualisationOfInsuranceCompany.fromCode,
    DemutualisationOfBuildingSociety.fromCode,
    IncorporationOfLimitedLiabilityPartnership.fromCode,
    TransfersInvolvingPublicBodies.fromCode,
    TransferInConsequenceOfReorganisationOfParliamentaryConstituencies.fromCode,
    CharitiesTaxReliefs.fromCode,
    AcquisitionByBodiesEstablishedForNationalPurposes.fromCode,
    RegisteredSocialLandlords.fromCode,
    AlternativePropertyFinance.fromCode,
    CroftingCommunityRightToBuy.fromCode,
    DiplomaticPrivileges.fromCode,
    OtherTaxReliefs.fromCode,
    CombinationOfReliefs.fromCode,
    AlternativeFinanceInvestmentBondsRelief.fromCode,
    FreeportsTaxSiteRelief.fromCode,
    InvestmentZonesTaxSiteRelief.fromCode,
    SeedingRelief.fromCode
  )

  val zeroRateCodes: Set[TaxReliefCode] = toName.values.toSet.filter(_.isInstanceOf[ZeroRate])
  val freeportRelief: Set[TaxReliefCode] = toName.values.toSet.filter(_.isInstanceOf[FreeportRelief])

  implicit val reads: Reads[TaxReliefCode] =
    Reads {
      case JsNumber(n) if toName.contains(n.toInt) => JsSuccess(toName(n.toInt))
      case JsNumber(n) => JsError(s"Unknown TaxReliefCode: $n")
      case _ => JsError("TaxReliefCode must be a number")
    }

  implicit val writes: Writes[TaxReliefCode] =
    Writes(tc => JsNumber(tc.code))
}
