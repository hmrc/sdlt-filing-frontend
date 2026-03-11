/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package pages.scalabuild
import base.ScalaSpecBase
import fixtures.scalabuild.TestObjects
import org.scalatest.wordspec.AnyWordSpec

class IsAdditionalPropertyPageSpec extends AnyWordSpec with ScalaSpecBase with TestObjects{
  "cleanup" must {
    "must cleanup correct fields when Effective Date is not within FTB date boundaries" when {
      "the answer is No" in {
        val userAnswers = emptyUserAnswers.copy(data = freeResNonIndAddMainNotWithinBoundaryUaData)
        val updatedAnswer = userAnswers.set(IsAdditionalPropertyPage, false).success.value

        updatedAnswer.get(ReplaceMainResidencePage) should not be defined
        updatedAnswer.get(OwnsOtherPropertiesPage) should not be defined
        updatedAnswer.get(MainResidencePage) should not be defined
        updatedAnswer.get(SharedOwnershipPage) should not be defined
        updatedAnswer.get(CurrentValuePage) should not be defined
        updatedAnswer.get(MarketValuePage) should not be defined

        updatedAnswer.get(HoldingPage) shouldBe defined
        updatedAnswer.get(ResidentialOrNonResidentialPage) shouldBe defined
        updatedAnswer.get(EffectiveDatePage) shouldBe defined
      }
      "the answer is Yes" in {
        val userAnswers = emptyUserAnswers.copy(data = freeResNonIndAddMainNotWithinBoundaryUaData)
        val updatedAnswer = userAnswers.set(IsAdditionalPropertyPage, true).success.value

        updatedAnswer.get(MainResidencePage) should not be defined
        updatedAnswer.get(OwnsOtherPropertiesPage) should not be defined
        updatedAnswer.get(SharedOwnershipPage) should not be defined
        updatedAnswer.get(CurrentValuePage) should not be defined
        updatedAnswer.get(MarketValuePage) should not be defined

        updatedAnswer.get(HoldingPage) shouldBe defined
        updatedAnswer.get(ResidentialOrNonResidentialPage) shouldBe defined
        updatedAnswer.get(EffectiveDatePage) shouldBe defined
      }
    }
    "must cleanup correct fields when Effective Date is within FTB date boundaries" when {
      "the answer is No" in {

        val userAnswers = emptyUserAnswers.copy(data = freeResNonIndAddMainFullUaData)
        val updatedAnswer = userAnswers.set(IsAdditionalPropertyPage, false).success.value
        updatedAnswer.get(ReplaceMainResidencePage) should not be defined

        updatedAnswer.get(IsPurchaserIndividualPage) shouldBe defined
        updatedAnswer.get(IsAdditionalPropertyPage) shouldBe defined
        updatedAnswer.get(IsPurchaserIndividualPage) shouldBe defined
        updatedAnswer.get(MainResidencePage) shouldBe defined
        updatedAnswer.get(ResidentialOrNonResidentialPage) shouldBe defined
        updatedAnswer.get(ResidentialOrNonResidentialPage) shouldBe defined
      }
      "the answer is yes" in {

        val userAnswers = emptyUserAnswers.copy(data = freeResNonIndAddMainFullUaData)
        val updatedAnswer = userAnswers.set(IsAdditionalPropertyPage, true).success.value
        updatedAnswer.get(MainResidencePage) should not be defined
        updatedAnswer.get(OwnsOtherPropertiesPage) should not be defined
        updatedAnswer.get(SharedOwnershipPage) should not be defined
        updatedAnswer.get(CurrentValuePage) should not be defined
        updatedAnswer.get(MarketValuePage) should not be defined

        updatedAnswer.get(IsAdditionalPropertyPage) shouldBe defined
        updatedAnswer.get(IsPurchaserIndividualPage) shouldBe defined
        updatedAnswer.get(HoldingPage) shouldBe defined
        updatedAnswer.get(ResidentialOrNonResidentialPage) shouldBe defined
        updatedAnswer.get(EffectiveDatePage) shouldBe defined
      }
    }
  }


}
