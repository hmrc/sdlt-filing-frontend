/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package pages.scalabuild
import base.ScalaSpecBase
import fixtures.scalabuild.TestObjects
import models.scalabuild.RentPeriods
import org.scalatest.wordspec.AnyWordSpec

class RentPageSpec extends AnyWordSpec with ScalaSpecBase with TestObjects {
  "cleanup" must {
    "remove the correct userAnswer fields" when {
      "premium is less than £150,000 and rents are not all below £2000" in {
        val userAnswers = emptyUserAnswers.copy(data = leaseResNonIndAddMainFullPremAndRentBelowThresholdUaData)
        val updatedAnswer = userAnswers
          .set(
            RentPage,
            RentPeriods(
              year1Rent = 1900,
              year2Rent = Some(1900),
              year3Rent = Some(1900),
              year4Rent = Some(1900),
              year5Rent = Some(5000)
            )
          )
          .success
          .value

        updatedAnswer.get(ExchangeContractsPage) should not be defined
        updatedAnswer.get(ContractPost201603Page) should not be defined
        updatedAnswer.get(RelevantRentPage) should not be defined

        updatedAnswer.get(NonUkResidentPage) shouldBe defined
        updatedAnswer.get(OwnsOtherPropertiesPage) shouldBe defined
        updatedAnswer.get(SharedOwnershipPage) shouldBe defined
        updatedAnswer.get(CurrentValuePage) shouldBe defined
        updatedAnswer.get(MarketValuePage) shouldBe defined
        updatedAnswer.get(HoldingPage) shouldBe defined
        updatedAnswer.get(ResidentialOrNonResidentialPage) shouldBe defined
        updatedAnswer.get(EffectiveDatePage) shouldBe defined

      }

      "premium is more than £150,000 and rents are all below £2000" in {
        val userAnswers = emptyUserAnswers.copy(data = leaseResNonIndAddMainFullPremAndRentAboveThresholdUaData)
        val updatedAnswer = userAnswers
          .set(
            RentPage,
            RentPeriods(
              year1Rent = 1900,
              year2Rent = Some(1900),
              year3Rent = Some(1900),
              year4Rent = Some(1900),
              year5Rent = Some(1900)
            )
          )
          .success
          .value

        updatedAnswer.get(ExchangeContractsPage) should not be defined
        updatedAnswer.get(ContractPost201603Page) should not be defined
        updatedAnswer.get(RelevantRentPage) should not be defined

        updatedAnswer.get(NonUkResidentPage) shouldBe defined
        updatedAnswer.get(OwnsOtherPropertiesPage) shouldBe defined
        updatedAnswer.get(SharedOwnershipPage) shouldBe defined
        updatedAnswer.get(CurrentValuePage) shouldBe defined
        updatedAnswer.get(MarketValuePage) shouldBe defined
        updatedAnswer.get(HoldingPage) shouldBe defined
        updatedAnswer.get(ResidentialOrNonResidentialPage) shouldBe defined
        updatedAnswer.get(EffectiveDatePage) shouldBe defined
      }
    }
    "Not remove fields" when {
      "premium is less than £150,000 and rents are all below £2000" in {
        val userAnswers = emptyUserAnswers.copy(data = leaseResNonIndAddMainFullPremAndRentBelowThresholdUaData)
        val updatedAnswer = userAnswers
          .set(
            RentPage,
            RentPeriods(
              year1Rent = 1900,
              year2Rent = Some(1900),
              year3Rent = Some(1900),
              year4Rent = Some(1900),
              year5Rent = Some(1900)
            )
          )
          .success
          .value

        updatedAnswer.get(ExchangeContractsPage) shouldBe defined
        updatedAnswer.get(ContractPost201603Page) shouldBe defined
        updatedAnswer.get(RelevantRentPage) shouldBe defined

        updatedAnswer.get(NonUkResidentPage) shouldBe defined
        updatedAnswer.get(OwnsOtherPropertiesPage) shouldBe defined
        updatedAnswer.get(SharedOwnershipPage) shouldBe defined
        updatedAnswer.get(CurrentValuePage) shouldBe defined
        updatedAnswer.get(MarketValuePage) shouldBe defined
        updatedAnswer.get(HoldingPage) shouldBe defined
        updatedAnswer.get(ResidentialOrNonResidentialPage) shouldBe defined
        updatedAnswer.get(EffectiveDatePage) shouldBe defined

      }
    }
  }
}
