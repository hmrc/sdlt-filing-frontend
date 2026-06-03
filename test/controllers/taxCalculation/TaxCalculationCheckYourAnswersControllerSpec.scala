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
import models.requests.DataRequest
import models.taxCalculation.{CalculationResponse, TaxCalculationFlow, TaxCalculationResult}
import models.{CheckMode, FullReturn, Land, Lease, Residency, ReturnInfo, Transaction, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.taxCalculation.TaxCalculationFlowPage
import pages.taxCalculation.freeholdSelfAssessed.{FreeholdSelfAssessedAmountPage, FreeholdSelfAssessedPenaltiesAndInterestPage, FreeholdSelfAssessedTotalAmountDuePage}
import pages.taxCalculation.freeholdTaxCalculated.{FreeholdTaxCalculatedPenaltiesAndInterestPage, FreeholdTaxCalculatedSelfAssessedAmountPage, FreeholdTaxCalculatedTotalAmountDuePage}
import play.api.Application
import play.api.inject.bind
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import utils.TimeMachine

import java.time.LocalDate
import scala.concurrent.Future

class TaxCalculationCheckYourAnswersControllerSpec extends SpecBase with MockitoSugar {

  private val today = LocalDate.of(2026, 5, 1)
  private val calculatedResult = TaxCalculationResult(totalTax = 43750, None, None, None, taxCalcs = Seq.empty)
  private val noCalcResult = TaxCalculationResult(totalTax = 0, Some("Self-assessed"), None, None, taxCalcs = Seq.empty)

  private val freeholdTaxCalculatedAnswers: UserAnswers =
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

  private val freeholdSelfAssessedAnswers: UserAnswers = emptyUserAnswers.copy(
      fullReturn = Some(FullReturn(
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
        land = Some(Seq(Land(landID = Some("L1"), propertyType = Some("01"), interestCreatedTransferred = Some("LG"))))
      )))
    .set(TaxCalculationFlowPage, TaxCalculationFlow.FreeholdSelfAssessed).success.value
    .set(FreeholdSelfAssessedAmountPage, "45000").success.value
    .set(FreeholdSelfAssessedTotalAmountDuePage, "45100").success.value
    .set(FreeholdSelfAssessedPenaltiesAndInterestPage, true).success.value

  private val selfAssessedAnswers: UserAnswers =
    emptyUserAnswers.set(TaxCalculationFlowPage, TaxCalculationFlow.FreeholdSelfAssessed).success.value

  private def appWith(answers: UserAnswers, calcResponse: Future[CalculationResponse] = Future.successful(CalculationResponse(Seq.empty))): Application = {
    val connector = mock[SdltCalculationConnector]
    val session   = mock[SessionRepository]
    when(connector.calculateStampDutyLandTax(any())(any())).thenReturn(calcResponse)
    when(session.set(any())).thenReturn(Future.successful(true))
    applicationBuilder(userAnswers = Some(answers))
      .overrides(
        bind[SdltCalculationConnector].toInstance(connector),
        bind[SessionRepository].toInstance(session)
      )
      .build()
  }

  private def onPageLoad(app: Application) =
    route(app, FakeRequest(GET, routes.TaxCalculationCheckYourAnswersController.onPageLoad().url)).value

  "TaxCalculationCheckYourAnswersController" - {

    "onPageLoad" - {

      "freehold tax calculated" - {

        "returns OK for a calculated flow when the calculation succeeds" in {
          val app = appWith(freeholdTaxCalculatedAnswers, Future.successful(CalculationResponse(Seq(calculatedResult))))
          running(app) {
            status(onPageLoad(app)) mustEqual OK
          }
        }

        "redirects to the change page of the first incomplete row in a calculated flow" in {
          val incomplete = freeholdTaxCalculatedAnswers.remove(FreeholdTaxCalculatedSelfAssessedAmountPage).success.value
          val app = appWith(incomplete, Future.successful(CalculationResponse(Seq(calculatedResult))))
          running(app) {
            val result = onPageLoad(app)
            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual
              controllers.taxCalculation.freeholdTaxCalculated.routes.FreeholdTaxCalculatedSdltSelfAssessmentController.onPageLoad(CheckMode).url
          }
        }

        "redirects to the task list when the calculation does not return a calculated result" in {
          val app = appWith(freeholdTaxCalculatedAnswers, Future.successful(CalculationResponse(Seq(noCalcResult))))
          running(app) {
            val result = onPageLoad(app)
            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
          }
        }

        "redirects to the task list when no tax-calculation flow is set" in {
          val app = appWith(emptyUserAnswers)
          running(app) {
            val result = onPageLoad(app)
            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
          }
        }
      }
      "freehold self assessed" - {
        "returns Ok for freehold self assessed flow" in {
          val app = appWith(freeholdSelfAssessedAnswers)
          running(app) {
            status(onPageLoad(app)) mustEqual OK
          }
        }
        "redirect to SDLT self-assessment Page when SDLT due is missing" in {
          val incompleteUserAnswers = freeholdSelfAssessedAnswers.remove(FreeholdSelfAssessedAmountPage).success.value
          val app = appWith(incompleteUserAnswers)
          running(app) {
            val result = onPageLoad(app)
            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual
              controllers.taxCalculation.freeholdSelfAssessed.routes.FreeholdSelfAssessedSdltSelfAssessmentController.onPageLoad(CheckMode).url
          }
        }
        "redirect to Total amount due Page when FreeholdSelfAssessedTotalAmountDue is missing" in {
          val incompleteUserAnswers = freeholdSelfAssessedAnswers.remove(FreeholdSelfAssessedTotalAmountDuePage).success.value
          val app = appWith(incompleteUserAnswers)
          running(app) {
            val result = onPageLoad(app)
            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual
              controllers.taxCalculation.freeholdSelfAssessed.routes.FreeholdSelfAssessedTotalAmountDueController.onPageLoad(CheckMode).url
          }
        }
        "redirect to Penalties and Interest Page when FreeholdSelfAssessedPenaltiesAndInterestPage is missing" in {
          val incompleteUserAnswers = freeholdSelfAssessedAnswers.remove(FreeholdSelfAssessedPenaltiesAndInterestPage).success.value
          val app = appWith(incompleteUserAnswers)
          running(app) {
            val result = onPageLoad(app)
            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual
              controllers.taxCalculation.freeholdSelfAssessed.routes
                .FreeholdSelfAssessedPenaltiesAndInterestController.onPageLoad(CheckMode).url
          }
        }
      }
    }
    "onSubmit" - {

      "redirects to the task list" in {
        val app = appWith(selfAssessedAnswers)
        running(app) {
          val result = route(app, FakeRequest(POST, routes.TaxCalculationCheckYourAnswersController.onSubmit().url)).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }
    }

    "buildRowResult" - {
      "freehold tax calculated" - {
        "return Seq[SummaryRowResult] when the case is freehold tax calculated" in {
          val app = appWith(freeholdTaxCalculatedAnswers)
          running(app) {
            val controller = app.injector.instanceOf[TaxCalculationCheckYourAnswersController]
            implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "123", freeholdTaxCalculatedAnswers)

            val row = controller.buildRowResults(calculatedResult)

            row.size mustEqual 5
          }
        }

        "freehold self assessed" - {
          "return Seq[SummaryRowResult] when the case is freehold self assessed" in {
            val mockTimeMachine = mock[TimeMachine]
            when(mockTimeMachine.today).thenReturn(today)
            val app = appWith(freeholdSelfAssessedAnswers)
            running(app) {
              val controller = app.injector.instanceOf[TaxCalculationCheckYourAnswersController]

              implicit val request: DataRequest[AnyContent] =
                DataRequest(FakeRequest(), "123", freeholdSelfAssessedAnswers)

              val row = controller.buildRowResults(noCalcResult)
              row.size mustEqual 4
            }
          }
        }
      }
    }
  }
}
