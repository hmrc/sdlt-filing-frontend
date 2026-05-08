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

package controllers.taxCalculation

import models.taxCalculation.*
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.mvc.Call

class TaxCalculationErrorRecoverySpec extends AnyFreeSpec with Matchers {

  private object UnderTest extends TaxCalculationErrorRecovery {
    def dispatch(err: BuildRequestError): Call = errorHandler(err)
  }

  private val noReturnRef   = controllers.routes.NoReturnReferenceController.onPageLoad()
  private val taskList      = controllers.routes.ReturnTaskListController.onPageLoad()

  "errorHandler" - {

    "routes MissingFullReturnError to NoReturnReference" in {
      UnderTest.dispatch(MissingFullReturnError) mustBe noReturnRef
    }

    "routes other MissingDataError types to the return task list" in {
      val others: Seq[MissingDataError] = Seq(
        MissingAboutTheLandError,
        MissingAboutTheTransactionError,
        MissingLandAnswerError("propertyType"),
        MissingTransactionAnswerError("effectiveDate"),
        MissingLeaseAnswerError("contractStartDate")
      )
      others.foreach { err =>
        withClue(s"for $err: ") { UnderTest.dispatch(err) mustBe taskList }
      }
    }

    "routes non-MissingDataError BuildRequestError types to the return task list" in {
      val others: Seq[BuildRequestError] = Seq(
        InvalidDateError("not-a-date"),
        UnknownHoldingTypeError("X"),
        UnknownPropertyTypeError("99"),
        InvalidReliefReasonError("INVALID")
      )
      others.foreach { err =>
        withClue(s"for $err: ") { UnderTest.dispatch(err) mustBe taskList }
      }
    }
  }
}
