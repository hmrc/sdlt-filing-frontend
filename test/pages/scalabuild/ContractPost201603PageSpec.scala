/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package pages.scalabuild
import base.ScalaSpecBase
import fixtures.scalabuild.TestObjects
import org.scalatest.wordspec.AnyWordSpec

class ContractPost201603PageSpec extends AnyWordSpec with ScalaSpecBase with TestObjects {
  "cleanup" must {
    "must cleanup relevant rent field when Yes is selected" in {
      val userAnswers = emptyUserAnswers.copy(data = leaseResNonIndAddMainFullUaData)
      val updatedAnswer = userAnswers.set(ContractPost201603Page, true).success.value

      updatedAnswer.get(RelevantRentPage) should not be defined

    }
    "must not cleanup relevant rent field when No is selected" in {
      val userAnswers = emptyUserAnswers.copy(data = leaseResNonIndAddMainFullUaData)
      val updatedAnswer = userAnswers.set(ContractPost201603Page, true).success.value

      updatedAnswer.get(RelevantRentPage) should not be defined    }
  }
}
