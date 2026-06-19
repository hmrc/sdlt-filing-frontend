/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package services.lease

import base.SpecBase
import constants.FullReturnConstants.completeTransaction
import models.{FullReturn, UserAnswers}
import models.prelimQuestions.TransactionType.{ConveyanceTransferLease, GrantOfLease}
import org.scalatest.matchers.must.Matchers

class LeaseServiceSpec extends SpecBase with Matchers {

  private val service = new LeaseService()

  private def userAnswersWithTransactionType(
                                              transactionDescription: Option[String],
                                              returnId:               Option[String] = None
                                            ): UserAnswers = {
    val fullReturn = FullReturn(
      stornId           = "1",
      returnResourceRef = "ref",
      transaction       = Some(completeTransaction.copy(
        transactionDescription = transactionDescription))
    )

    emptyUserAnswers.copy(
      fullReturn = Some(fullReturn),
      returnId   = returnId
    )
  }

  "transactionType" - {

    "must return GrantOfLease when transaction description is 'L'" in {
      val userAnswers = userAnswersWithTransactionType(Some("L"))

      service.transactionType(userAnswers) mustBe Some(GrantOfLease)
    }

    "must return ConveyanceTransferLease when transaction description is 'A'" in {
      val userAnswers = userAnswersWithTransactionType(Some("A"))

      service.transactionType(userAnswers) mustBe Some(ConveyanceTransferLease)
    }

    "must return None when transaction description is None" in {
      val userAnswers = userAnswersWithTransactionType(None)

      service.transactionType(userAnswers) mustBe None
    }

    "must return None when transaction description is unrecognised" in {
      val userAnswers = userAnswersWithTransactionType(Some("Z"))

      service.transactionType(userAnswers) mustBe None
    }

    "must return None when fullReturn is None" in {
      val userAnswers = emptyUserAnswers.copy(fullReturn = None)

      service.transactionType(userAnswers) mustBe None
    }
  }

  "leaseFlowValidationCheck" - {

    "must return None when transaction type is GrantOfLease" in {
      val userAnswers = userAnswersWithTransactionType(Some("L"))

      service.leaseFlowValidationCheck(userAnswers) mustBe None
    }

    "must return None when transaction type is ConveyanceTransferLease" in {
      val userAnswers = userAnswersWithTransactionType(Some("A"))

      service.leaseFlowValidationCheck(userAnswers) mustBe None
    }

    "must return Some redirect to ReturnTaskList when transaction type is not a lease and returnId is present" in {
      val userAnswers = userAnswersWithTransactionType(
        transactionDescription = Some("F"),
        returnId               = Some("RE12345")
      )

      service.leaseFlowValidationCheck(userAnswers) mustBe
        Some(controllers.routes.ReturnTaskListController.onPageLoad())
    }

    "must return Some redirect to JourneyRecovery when transaction type is not a lease and returnId is absent" in {
      val userAnswers = userAnswersWithTransactionType(
        transactionDescription = Some("F"),
        returnId               = None
      )

      service.leaseFlowValidationCheck(userAnswers) mustBe
        Some(controllers.routes.JourneyRecoveryController.onPageLoad())
    }

    "must return Some redirect to ReturnTaskList when transaction type is missing and returnId is present" in {
      val userAnswers = userAnswersWithTransactionType(
        transactionDescription = None,
        returnId               = Some("RE12345")
      )

      service.leaseFlowValidationCheck(userAnswers) mustBe
        Some(controllers.routes.ReturnTaskListController.onPageLoad())
    }

    "must return Some redirect to JourneyRecovery when transaction type is missing and returnId is absent" in {
      val userAnswers = userAnswersWithTransactionType(
        transactionDescription = None,
        returnId               = None
      )

      service.leaseFlowValidationCheck(userAnswers) mustBe
        Some(controllers.routes.JourneyRecoveryController.onPageLoad())
    }

    "must return Some redirect to JourneyRecovery when there is no fullReturn at all" in {
      val userAnswers = emptyUserAnswers.copy(fullReturn = None, returnId = None)

      service.leaseFlowValidationCheck(userAnswers) mustBe
        Some(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }
}