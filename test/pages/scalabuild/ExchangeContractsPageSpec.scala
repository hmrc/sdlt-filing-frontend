/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package pages.scalabuild
import base.ScalaSpecBase
import fixtures.scalabuild.TestObjects
import org.scalatest.wordspec.AnyWordSpec

class ExchangeContractsPageSpec extends AnyWordSpec with ScalaSpecBase with TestObjects {
  "cleanup" must {
    "must cleanup correct fields when No is selected" in {
      val userAnswers = emptyUserAnswers.copy(data = leaseResNonIndAddMainFullUaData)
      val updatedAnswer = userAnswers.set(ExchangeContractsPage, false).success.value

      updatedAnswer.get(RelevantRentPage) should not be defined
      updatedAnswer.get(ContractPost201603Page) should not be defined
    }

    "must not cleanup relevant rent field when Yes is selected" in {
      val userAnswers = emptyUserAnswers.copy(data = leaseResNonIndAddMainFullUaData)
      val updatedAnswer = userAnswers.set(ExchangeContractsPage, true).success.value

      updatedAnswer.get(RelevantRentPage) shouldBe defined
      updatedAnswer.get(ContractPost201603Page) shouldBe defined
    }
  }

}
