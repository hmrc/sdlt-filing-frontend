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
import connectors.SdltCalculationConnector
import models.taxCalculation.*
import models.{FullReturn, Land, Lease, Residency, ReturnInfo, Transaction, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.taxCalculation.TaxCalculationFlowPage
import pages.taxCalculation.freeholdSelfAssessed.{FreeholdSelfAssessedAmountPage, FreeholdSelfAssessedPenaltiesAndInterestPage, FreeholdSelfAssessedTotalAmountDuePage}
import pages.taxCalculation.freeholdTaxCalculated.{FreeholdTaxCalculatedPenaltiesAndInterestPage, FreeholdTaxCalculatedSelfAssessedAmountPage, FreeholdTaxCalculatedTotalAmountDuePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import utils.TimeMachine

import java.time.LocalDate
import scala.concurrent.Future

class TaxCalculationCheckYourAnswersControllerSpec extends SpecBase with MockitoSugar {

  private val today              = LocalDate.of(2026, 5, 1)
  private val sdltcResult        = TaxCalculationResult(totalTax = 43750, None, None, None, taxCalcs = Seq.empty)
  private val selfAssessedResult = TaxCalculationResult(totalTax = 0, Some("Self-assessed"), None, None, taxCalcs = Seq.empty)

  private val selfAssessedAnswers: UserAnswers =
    emptyUserAnswers
      .copy(fullReturn = Some(FullReturn(
        stornId = "STORN",
        returnResourceRef = "REF",
        returnInfo = Some(ReturnInfo(mainLandID = Some("L1"))),
        transaction = Some(Transaction(
          effectiveDate = Some(today.minusDays(60).toString),
          totalConsideration = Some("300000"),
          claimingRelief = Some("no"),
          transactionDescription = Some("L"),
          isLinked = Some("no")
        )),
        residency = Some(Residency(isNonUkResidents = Some("no"))),
        land = Some(Seq(Land(landID = Some("L1"), propertyType = Some("01"), interestCreatedTransferred = Some("LG")))),
        lease = Some(Lease(
          contractStartDate = Some(today.minusDays(60).toString),
          contractEndDate = Some(today.plusYears(5).toString),
          netPresentValue = Some("100000"),
          isAnnualRentOver1000 = Some("yes")
        ))
      )))
      .set(TaxCalculationFlowPage, TaxCalculationFlow.FreeholdSelfAssessed).success.value
      .set(FreeholdSelfAssessedAmountPage, "43750").success.value
      .set(FreeholdSelfAssessedTotalAmountDuePage, "43850").success.value
      .set(FreeholdSelfAssessedPenaltiesAndInterestPage, true).success.value

  private val calculatedAnswers: UserAnswers =
    emptyUserAnswers
      .copy(fullReturn = Some(FullReturn(
        stornId = "STORN",
        returnResourceRef = "REF",
        returnInfo = Some(ReturnInfo(mainLandID = Some("L1"))),
        transaction = Some(Transaction(
          effectiveDate = Some(today.minusDays(60).toString),
          totalConsideration = Some("300000"),
          claimingRelief = Some("no"),
          transactionDescription = Some("F"),
          isLinked = Some("no")
        )),
        residency = Some(Residency(isNonUkResidents = Some("no"))),
        land = Some(Seq(Land(landID = Some("L1"), propertyType = Some("01"), interestCreatedTransferred = Some("FPF"))))
      )))
      .set(TaxCalculationFlowPage, TaxCalculationFlow.FreeholdTaxCalculated).success.value
      .set(FreeholdTaxCalculatedSelfAssessedAmountPage, "43750").success.value
      .set(FreeholdTaxCalculatedTotalAmountDuePage, "43850").success.value
      .set(FreeholdTaxCalculatedPenaltiesAndInterestPage, true).success.value

  private def appWith(answers: UserAnswers, sdltcResponse: Future[CalculationResponse] = Future.successful(CalculationResponse(Seq.empty))) = {
    val mockConnector   = mock[SdltCalculationConnector]
    val mockSession     = mock[SessionRepository]
    val mockTimeMachine = mock[TimeMachine]
    when(mockConnector.calculateStampDutyLandTax(any())(any())).thenReturn(sdltcResponse)
    when(mockSession.set(any())).thenReturn(Future.successful(true))
    when(mockTimeMachine.today).thenReturn(today)
    applicationBuilder(userAnswers = Some(answers))
      .overrides(
        bind[SdltCalculationConnector].toInstance(mockConnector),
        bind[SessionRepository].toInstance(mockSession),
        bind[TimeMachine].toInstance(mockTimeMachine)
      )
      .build()
  }

  "TaxCalculationCheckYourAnswersController" - {

    "onPageLoad - freehold self-assessed" - {

      "must return OK and render the rows, declaration and confirm button when all answers are present" in {

        val app = appWith(selfAssessedAnswers)

        running(app) {
          val request = FakeRequest(GET, routes.TaxCalculationCheckYourAnswersController.onPageLoad().url)
          val result  = route(app, request).value

          status(result) mustEqual OK
          val body = contentAsString(result)
          body must include("Check your answers")
          body must include("Self-assessed amount of SDLT")
          body must include("£43750")
          body must include("Penalties due")
          body must include("£100")
          body must include("Amount to be paid")
          body must include("£43850")
          body must include("Does the amount include penalties and interest?")
          body must include("Declaration")
          body must include("Confirm and continue")
        }
      }

      "must redirect to the self-assessment amount page when that answer is missing" in {

        val app = appWith(selfAssessedAnswers.remove(FreeholdSelfAssessedAmountPage).success.value)

        running(app) {
          val request = FakeRequest(GET, routes.TaxCalculationCheckYourAnswersController.onPageLoad().url)
          val result  = route(app, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.taxCalculation.freeholdSelfAssessed.routes.FreeholdSelfAssessedSdltSelfAssessmentController.onPageLoad(models.CheckMode).url
        }
      }
    }

    "onPageLoad - freehold calculated" - {

      "must return OK and render the HMRC-calculated SDLT due plus the remaining rows when the calculation succeeds" in {

        val app = appWith(calculatedAnswers, Future.successful(CalculationResponse(Seq(sdltcResult))))

        running(app) {
          val request = FakeRequest(GET, routes.TaxCalculationCheckYourAnswersController.onPageLoad().url)
          val result  = route(app, request).value

          status(result) mustEqual OK
          val body = contentAsString(result)
          body must include("HMRC calculated SDLT due")
          body must include("£43750")
          body must include("Self-assessed amount of SDLT")
          body must include("Penalties due")
          body must include("£100")
          body must include("Amount to be paid")
          body must include("£43850")
          body must include("Does the amount include penalties and interest?")
          body must include("Declaration")
          body must include("Confirm and continue")
        }
      }

      "must redirect to the return task list when the calculation does not return a calculated result" in {

        val app = appWith(calculatedAnswers, Future.successful(CalculationResponse(Seq(selfAssessedResult))))

        running(app) {
          val request = FakeRequest(GET, routes.TaxCalculationCheckYourAnswersController.onPageLoad().url)
          val result  = route(app, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }
    }

    "onPageLoad - guards" - {

      "must redirect to the return task list when no tax-calculation flow is set" in {

        val app = appWith(selfAssessedAnswers.remove(TaxCalculationFlowPage).success.value)

        running(app) {
          val request = FakeRequest(GET, routes.TaxCalculationCheckYourAnswersController.onPageLoad().url)
          val result  = route(app, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must redirect to the return task list for a flow whose CYA is not yet wired" in {

        val notWired = selfAssessedAnswers.set(TaxCalculationFlowPage, TaxCalculationFlow.LeaseholdTaxCalculated).success.value
        val app      = appWith(notWired)

        running(app) {
          val request = FakeRequest(GET, routes.TaxCalculationCheckYourAnswersController.onPageLoad().url)
          val result  = route(app, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }
    }

    "onSubmit" - {

      "must redirect to the return task list" in {

        val app = appWith(selfAssessedAnswers)

        running(app) {
          val request = FakeRequest(POST, routes.TaxCalculationCheckYourAnswersController.onSubmit().url)
          val result  = route(app, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }
    }
  }
}
