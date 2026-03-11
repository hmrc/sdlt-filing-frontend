/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package pages.scalabuild
import base.ScalaSpecBase
import fixtures.scalabuild.TestObjects
import org.scalatest.wordspec.AnyWordSpec

class OwnsOtherPropertiesPageSpec extends AnyWordSpec with ScalaSpecBase with TestObjects {

  "cleanup" must {
    "must cleanup correct fields" when {
      "the answer is No" in {
        val userAnswers = emptyUserAnswers.copy(data = freeResNonIndAddMainFullUaData)
        val updatedAnswer = userAnswers.set(OwnsOtherPropertiesPage, false).success.value

        updatedAnswer.get(ReplaceMainResidencePage) should not be defined

        updatedAnswer.get(MainResidencePage) shouldBe defined
        updatedAnswer.get(IsAdditionalPropertyPage) shouldBe defined
        updatedAnswer.get(OwnsOtherPropertiesPage) shouldBe defined
        updatedAnswer.get(HoldingPage) shouldBe defined
        updatedAnswer.get(ResidentialOrNonResidentialPage) shouldBe defined
        updatedAnswer.get(EffectiveDatePage) shouldBe defined
      }
      "the answer is yes" in {
        val userAnswers = emptyUserAnswers.copy(data = leaseResNonIndAddMainFullUaData)
        val updatedAnswer = userAnswers.set(OwnsOtherPropertiesPage, true).success.value

        updatedAnswer.get(ReplaceMainResidencePage) should not be defined
        updatedAnswer.get(MainResidencePage) should not be defined
        updatedAnswer.get(SharedOwnershipPage) should not be defined
        updatedAnswer.get(CurrentValuePage) should not be defined
        updatedAnswer.get(MarketValuePage) should not be defined


        updatedAnswer.get(OwnsOtherPropertiesPage) shouldBe defined
        updatedAnswer.get(HoldingPage) shouldBe defined
        updatedAnswer.get(ResidentialOrNonResidentialPage) shouldBe defined
        updatedAnswer.get(EffectiveDatePage) shouldBe defined
      }
    }
  }
}
