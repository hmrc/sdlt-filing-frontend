/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package pages.scalabuild
import base.ScalaSpecBase
import fixtures.scalabuild.TestObjects
import org.scalatest.wordspec.AnyWordSpec

import java.time.LocalDate

class EffectiveDatePageSpec extends AnyWordSpec with ScalaSpecBase with TestObjects {
  "cleanup" must {
    "must cleanup when Effective Date is before April 2016" when {
      "the route is Freehold" in {
        val userAnswers = emptyUserAnswers.copy(data = freeResNonIndAddMainUaData)
        val updatedAnswer = userAnswers.set(EffectiveDatePage, LocalDate.of(2016, 3, 31)).success.value

        updatedAnswer.get(NonUkResidentPage) should not be defined
        updatedAnswer.get(IsPurchaserIndividualPage) should not be defined
        updatedAnswer.get(PropertyDetailsPage) should not be defined
        updatedAnswer.get(IsAdditionalPropertyPage) should not be defined
        updatedAnswer.get(ReplaceMainResidencePage) should not be defined
        updatedAnswer.get(HoldingPage) shouldBe defined
        updatedAnswer.get(ResidentialOrNonResidentialPage) shouldBe defined
        updatedAnswer.get(ResidentialOrNonResidentialPage) shouldBe defined
      }
      "the route is Leasehold" in {
        val userAnswers = emptyUserAnswers.copy(data = leaseResNonIndAddMainFullUaData)
        val updatedAnswer = userAnswers.set(EffectiveDatePage, LocalDate.of(2016, 3, 31)).success.value

        updatedAnswer.get(NonUkResidentPage) should not be defined
        updatedAnswer.get(IsPurchaserIndividualPage) should not be defined
        updatedAnswer.get(PropertyDetailsPage) should not be defined
        updatedAnswer.get(IsAdditionalPropertyPage) should not be defined
        updatedAnswer.get(ReplaceMainResidencePage) should not be defined
        updatedAnswer.get(HoldingPage) shouldBe defined
        updatedAnswer.get(ResidentialOrNonResidentialPage) shouldBe defined
        updatedAnswer.get(ResidentialOrNonResidentialPage) shouldBe defined
      }
    }
    "must cleanup correct field when Effective Date is after April 2016 and before April 2021" when {
      "the route is Freehold" in {

        val userAnswers = emptyUserAnswers.copy(data = freeResNonIndAddMainUaData)
        val updatedAnswer = userAnswers.set(EffectiveDatePage, LocalDate.of(2016, 4, 30)).success.value

        updatedAnswer.get(NonUkResidentPage) should not be defined

        updatedAnswer.get(IsPurchaserIndividualPage) shouldBe defined
        updatedAnswer.get(IsAdditionalPropertyPage) shouldBe defined
        updatedAnswer.get(IsPurchaserIndividualPage) shouldBe defined
        updatedAnswer.get(ReplaceMainResidencePage) shouldBe defined
        updatedAnswer.get(ResidentialOrNonResidentialPage) shouldBe defined
        updatedAnswer.get(ResidentialOrNonResidentialPage) shouldBe defined
      }
      "the route is Leasehold" in {

        val userAnswers = emptyUserAnswers.copy(data = leaseResNonIndAddMainFullUaData)
        val updatedAnswer = userAnswers.set(EffectiveDatePage, LocalDate.of(2016, 4, 30)).success.value

        updatedAnswer.get(NonUkResidentPage) should not be defined

        updatedAnswer.get(IsPurchaserIndividualPage) shouldBe defined
        updatedAnswer.get(IsAdditionalPropertyPage) shouldBe defined
        updatedAnswer.get(IsPurchaserIndividualPage) shouldBe defined
        updatedAnswer.get(HoldingPage) shouldBe defined
        updatedAnswer.get(ResidentialOrNonResidentialPage) shouldBe defined
      }
    }
  }
}
