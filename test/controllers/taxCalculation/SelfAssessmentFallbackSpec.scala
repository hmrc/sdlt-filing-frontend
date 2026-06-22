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

import base.SpecBase
import models.taxCalculation.TaxCalculationFlow
import models.{FullReturn, NormalMode, Transaction, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.taxCalculation.TaxCalculationFlowPage
import play.api.mvc.Results.{Ok, Redirect}
import repositories.SessionRepository

import scala.concurrent.{ExecutionContext, Future}

class SelfAssessmentFallbackSpec extends SpecBase with MockitoSugar {

  private implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  private def answersFor(transactionDescription: Option[String]): UserAnswers =
    emptyUserAnswers.copy(fullReturn = Some(FullReturn(
      stornId           = "TESTSTORN",
      returnResourceRef = "REF001",
      transaction       = transactionDescription.map(d => Transaction(transactionDescription = Some(d)))
    )))

  private def repoCapturingSets: (SessionRepository, ArgumentCaptor[UserAnswers]) = {
    val repo   = mock[SessionRepository]
    val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
    when(repo.set(captor.capture())).thenReturn(Future.successful(true))
    (repo, captor)
  }

  "byHoldingType" - {

    "must persist the freehold self-assessed flow and render with the freehold section key and continue url for a freehold return" in {
      val (repo, captor) = repoCapturingSets
      val page           = Ok("freehold")

      val result = SelfAssessmentFallback.byHoldingType(answersFor(Some("F")), repo) { (sectionKey, continueUrl) =>
        sectionKey  mustBe "site.taxCalculation.freeholdSelfAssessed.section"
        continueUrl mustBe controllers.taxCalculation.freeholdSelfAssessed.routes.FreeholdSelfAssessedSdltSelfAssessmentController.onPageLoad(NormalMode).url
        page
      }.futureValue

      result mustEqual page
      captor.getValue.get(TaxCalculationFlowPage) mustBe Some(TaxCalculationFlow.FreeholdSelfAssessed)
    }

    "must persist the leasehold self-assessed flow and render with the leasehold section key and continue url for a leasehold return" in {
      val (repo, captor) = repoCapturingSets
      val page           = Ok("leasehold")

      val result = SelfAssessmentFallback.byHoldingType(answersFor(Some("L")), repo) { (sectionKey, continueUrl) =>
        sectionKey  mustBe "site.taxCalculation.leaseholdSelfAssessed.caption"
        continueUrl mustBe controllers.taxCalculation.leaseholdSelfAssessed.routes.LeaseholdSelfAssessedPremiumPayableTaxController.onPageLoad(NormalMode).url
        page
      }.futureValue

      result mustEqual page
      captor.getValue.get(TaxCalculationFlowPage) mustBe Some(TaxCalculationFlow.LeaseholdSelfAssessed)
    }

    "must redirect to the task list without rendering or touching the session when the holding type cannot be determined" in {
      val repo = mock[SessionRepository]

      val result = SelfAssessmentFallback.byHoldingType(answersFor(None), repo) { (_, _) =>
        fail("render should not be called when the holding type cannot be determined")
      }.futureValue

      result mustEqual Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
      verify(repo, never()).set(any[UserAnswers]())
    }
  }
}
