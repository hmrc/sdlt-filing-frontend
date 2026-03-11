/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package pages.scalabuild
import base.ScalaSpecBase
import fixtures.scalabuild.TestObjects
import org.scalatest.wordspec.AnyWordSpec

class ReplaceMainResidencePageSpec extends AnyWordSpec with ScalaSpecBase with TestObjects {

  "cleanup" must {
    "must cleanup correct fields" when {
      "the answer is No" in {
        val userAnswers = emptyUserAnswers.copy(data = freeResNonIndAddMainFullUaData)
        val updatedAnswer = userAnswers.set(ReplaceMainResidencePage, false).success.value

        updatedAnswer.get(MainResidencePage) should not be defined
        updatedAnswer.get(SharedOwnershipPage) should not be defined
        updatedAnswer.get(CurrentValuePage) should not be defined
        updatedAnswer.get(MarketValuePage) should not be defined

        updatedAnswer.get(HoldingPage) shouldBe defined
        updatedAnswer.get(ResidentialOrNonResidentialPage) shouldBe defined
        updatedAnswer.get(EffectiveDatePage) shouldBe defined
        updatedAnswer.get(NonUkResidentPage) shouldBe defined
        updatedAnswer.get(ReplaceMainResidencePage) shouldBe defined
        updatedAnswer.get(IsPurchaserIndividualPage) shouldBe defined
        updatedAnswer.get(PurchasePricePage) shouldBe defined
      }
      "must not cleanup" when {
        "the answer is yes" in {
          val userAnswers = emptyUserAnswers.copy(data = leaseResNonIndAddMainFullUaData)
          val updatedAnswer = userAnswers.set(IsPurchaserIndividualPage, true).success.value

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


}
