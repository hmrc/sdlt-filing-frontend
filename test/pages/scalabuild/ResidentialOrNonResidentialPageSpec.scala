/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package pages.scalabuild

import base.ScalaSpecBase
import fixtures.scalabuild.TestObjects
import models.scalabuild.PropertyType.{NonResidential, Residential}
import org.scalatest.wordspec.AnyWordSpec

class ResidentialOrNonResidentialPageSpec extends AnyWordSpec with ScalaSpecBase with TestObjects {

  "cleanup" must {
    "must cleanup correct fields" when {
      "the answer is 'Residential' " in {
        val userAnswers = emptyUserAnswers.copy(data = freeResNonIndAddMainFullUaData)
        val updatedAnswer = userAnswers.set(ResidentialOrNonResidentialPage, NonResidential).success.value

        updatedAnswer.get(NonUkResidentPage) should not be defined
        updatedAnswer.get(MainResidencePage) should not be defined
        updatedAnswer.get(ReplaceMainResidencePage) should not be defined
        updatedAnswer.get(OwnsOtherPropertiesPage) should not be defined
        updatedAnswer.get(SharedOwnershipPage) should not be defined
        updatedAnswer.get(MarketValuePage) should not be defined
        updatedAnswer.get(IsPurchaserIndividualPage) should not be defined
        updatedAnswer.get(PropertyDetailsPage) should not be defined
        updatedAnswer.get(CurrentValuePage) should not be defined

        updatedAnswer.get(HoldingPage) shouldBe defined
        updatedAnswer.get(ResidentialOrNonResidentialPage) shouldBe defined
        updatedAnswer.get(EffectiveDatePage) shouldBe defined
        updatedAnswer.get(PurchasePricePage) shouldBe defined
      }
      "must not cleanup" when {
        "the answer is yes" in {
          val userAnswers = emptyUserAnswers.copy(data = leaseResNonIndAddMainFullUaData)
          val updatedAnswer = userAnswers.set(ResidentialOrNonResidentialPage, Residential).success.value

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
