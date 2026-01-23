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

sealed trait StandardZeroRate
sealed trait ZeroRate
sealed trait ZeroPointFiveSlabRate
sealed trait SelfAssessed

case object PartExchange                                                       extends TaxReliefCode(8)  with StandardZeroRate
case object ReLocationEmployment                                               extends TaxReliefCode(9)  with StandardZeroRate
case object CompulsoryPurchaseFacilitatingDevelopment                          extends TaxReliefCode(10) with StandardZeroRate
case object ComplianceWithPlanningObligations                                  extends TaxReliefCode(11) with StandardZeroRate
case object GroupRelief                                                        extends TaxReliefCode(12) with StandardZeroRate
case object ReConstructionRelief                                               extends TaxReliefCode(13) with StandardZeroRate
case object AcquisitionRelief                                                  extends TaxReliefCode(14) with ZeroPointFiveSlabRate
case object DemutualisationOfInsuranceCompany                                  extends TaxReliefCode(15) with StandardZeroRate
case object DemutualisationOfBuildingSociety                                   extends TaxReliefCode(16) with StandardZeroRate
case object IncorporationOfLimitedLiabilityPartnership                         extends TaxReliefCode(17) with StandardZeroRate
case object TransfersInvolvingPublicBodies                                     extends TaxReliefCode(18) with StandardZeroRate
case object TransferInConsequenceOfReorganisationOfParliamentaryConstituencies extends TaxReliefCode(19) with StandardZeroRate
case object CharitiesTaxReliefs                                                extends TaxReliefCode(20) with StandardZeroRate
case object AcquisitionByBodiesEstablishedForNationalPurposes                  extends TaxReliefCode(21) with StandardZeroRate
case object RightToBuy                                                         extends TaxReliefCode(22) with SelfAssessed
case object RegisteredSocialLandlords                                          extends TaxReliefCode(23) with StandardZeroRate
case object AlternativePropertyFinance                                         extends TaxReliefCode(24) with StandardZeroRate
case object CroftingCommunityRightToBuy                                        extends TaxReliefCode(26) with StandardZeroRate
case object DiplomaticPrivileges                                               extends TaxReliefCode(27) with StandardZeroRate
case object OtherTaxReliefs                                                    extends TaxReliefCode(28) with StandardZeroRate
case object CombinationOfReliefs                                               extends TaxReliefCode(29) with StandardZeroRate
case object AlternativeFinanceInvestmentBondsRelief                            extends TaxReliefCode(31) with StandardZeroRate
case object PreCompletionTransaction                                           extends TaxReliefCode(34) with ZeroRate
case object FirstTimeBuyersRelief                                              extends TaxReliefCode(35) with SelfAssessed
case object FreeportsTaxSiteRelief                                             extends TaxReliefCode(36) with ZeroRate with SelfAssessed
case object InvestmentZonesTaxSiteRelief                                       extends TaxReliefCode(37) with ZeroRate with SelfAssessed
case object SeedingRelief                                                      extends TaxReliefCode(38) with StandardZeroRate

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
    RightToBuy.fromCode,
    RegisteredSocialLandlords.fromCode,
    AlternativePropertyFinance.fromCode,
    CroftingCommunityRightToBuy.fromCode,
    DiplomaticPrivileges.fromCode,
    OtherTaxReliefs.fromCode,
    CombinationOfReliefs.fromCode,
    AlternativeFinanceInvestmentBondsRelief.fromCode,
    PreCompletionTransaction.fromCode,
    FirstTimeBuyersRelief.fromCode,
    SeedingRelief.fromCode,
    AcquisitionRelief.fromCode,
    FreeportsTaxSiteRelief.fromCode,
    InvestmentZonesTaxSiteRelief.fromCode
  )

  val zeroRateCodes: Set[TaxReliefCode] = toName.values.toSet.filter(_.isInstanceOf[ZeroRate])

  val standardZeroRateFreeholdReliefCodes: Set[TaxReliefCode] = toName.values.toSet.filter(_.isInstanceOf[StandardZeroRate])

  val standardZeroRateLeaseholdReliefCodes: Set[TaxReliefCode] = toName.values.toSet.filter(_.isInstanceOf[StandardZeroRate])

  val selfAssessedFreeHold: Set[TaxReliefCode] = Set(
    PartExchange,
    ReLocationEmployment,
    CompulsoryPurchaseFacilitatingDevelopment,
    ComplianceWithPlanningObligations,
    GroupRelief,
    ReConstructionRelief,
    DemutualisationOfInsuranceCompany,
    DemutualisationOfBuildingSociety,
    IncorporationOfLimitedLiabilityPartnership,
    TransfersInvolvingPublicBodies,
    TransferInConsequenceOfReorganisationOfParliamentaryConstituencies,
    CharitiesTaxReliefs,
    AcquisitionByBodiesEstablishedForNationalPurposes,
    RegisteredSocialLandlords,
    AlternativePropertyFinance,
    CroftingCommunityRightToBuy,
    DiplomaticPrivileges,
    OtherTaxReliefs,
    CombinationOfReliefs,
    AlternativeFinanceInvestmentBondsRelief
  )

  val selfAssessedCodes: Set[TaxReliefCode] = toName.values.toSet.filter(_.isInstanceOf[SelfAssessed])

  val ACQUISITION_RATE_FRACTION = 5

  implicit val reads: Reads[TaxReliefCode] =
    Reads {
      case JsNumber(n) if toName.contains(n.toInt) => JsSuccess(toName(n.toInt))
      case JsNumber(n) => JsError(s"Unknown TaxReliefCode: $n")
      case _ => JsError("TaxReliefCode must be a number")
    }

  implicit val writes: Writes[TaxReliefCode] =
    Writes(tc => JsNumber(tc.code))
}
