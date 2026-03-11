/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package pages.scalabuild
import base.ScalaSpecBase
import fixtures.scalabuild.TestObjects
import models.scalabuild.HoldingTypes.{Freehold, Leasehold}
import org.scalatest.wordspec.AnyWordSpec

class HoldingPageSpec extends AnyWordSpec with ScalaSpecBase with TestObjects {
  "cleanup" must {
    "must cleanup purchase price when Leasehold is selected" in {
      val userAnswers = emptyUserAnswers.copy(data = freeResNonIndAddMainUaData)

      val updatedAnswer = userAnswers.set(HoldingPage, Leasehold).success.value

      updatedAnswer.get(PurchasePricePage) should not be defined

    }
    "must cleanup leaseDetails object when Freehold is selected" in {
      val userAnswers = emptyUserAnswers.copy(data = leaseResNonIndAddMainFullUaData)
      val updatedAnswer = userAnswers.set(HoldingPage, Freehold).success.value

      updatedAnswer.get(MongoLeaseDetailPage) should not be defined

    }

    "must not cleanup mongoLeaseDetails object and Relevant Rent when Leasehold is selected" in {
      val userAnswers = emptyUserAnswers.copy(data = leaseResNonIndAddMainFullUaData)
      val updatedAnswer = userAnswers.set(HoldingPage, Leasehold).success.value

      updatedAnswer.get(MongoLeaseDetailPage) shouldBe defined
      updatedAnswer.get(RelevantRentDetailPage) shouldBe defined
    }

    "must not cleanup purchase price field when Freehold is selected" in {
      val userAnswers = emptyUserAnswers.copy(data = freeResNonIndAddMainUaData)
      val updatedAnswer = userAnswers.set(HoldingPage, Freehold).success.value

      updatedAnswer.get(PurchasePricePage) shouldBe defined
    }
  }

}
