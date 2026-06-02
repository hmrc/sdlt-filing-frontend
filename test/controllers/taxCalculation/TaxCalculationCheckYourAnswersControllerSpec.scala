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
import connectors.{SdltCalculationConnector, StampDutyLandTaxConnector}
import models.requests.DataRequest
import models.taxCalculation.{CalculationResponse, TaxCalculationFlow, TaxCalculationResult, UpdateTaxCalculationReturn}
import models.{CheckMode, FullReturn, Land, Residency, ReturnInfo, ReturnVersionUpdateReturn, TaxCalculation, Transaction, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.taxCalculation.TaxCalculationFlowPage
import pages.taxCalculation.freeholdSelfAssessed.{FreeholdSelfAssessedAmountPage, FreeholdSelfAssessedPenaltiesAndInterestPage, FreeholdSelfAssessedTotalAmountDuePage}
import pages.taxCalculation.freeholdTaxCalculated.{FreeholdTaxCalculatedPenaltiesAndInterestPage, FreeholdTaxCalculatedSelfAssessedAmountPage, FreeholdTaxCalculatedTotalAmountDuePage}
import pages.taxCalculation.leaseholdTaxCalculated.{LeaseholdTaxCalculatedPenaltiesAndInterestPage, LeaseholdTaxCalculatedSelfAssessedAmountPage, LeaseholdTaxCalculatedTotalAmountDuePage}
import play.api.Application
import play.api.inject.bind
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.taxCalculation.PopulateTaxCalculationService
import utils.TimeMachine

import java.time.LocalDate
import scala.concurrent.Future
import scala.util.Failure

class TaxCalculationCheckYourAnswersControllerSpec extends SpecBase with MockitoSugar {

  private val today = LocalDate.of(2026, 5, 1)
  private val calculatedResult = TaxCalculationResult(totalTax = 43750, None, None, None, taxCalcs = Seq.empty)
  private val noCalcResult = TaxCalculationResult(totalTax = 0, Some("Self-assessed"), None, None, taxCalcs = Seq.empty)

  private val freeholdFullReturn = FullReturn(
    stornId = "STORN",
    returnResourceRef = "REF",
    returnInfo = Some(ReturnInfo(version = Some("1"), mainLandID = Some("L1"))),
    transaction = Some(Transaction(
      effectiveDate = Some(today.minusDays(60).toString),
      totalConsideration = Some("300000"),
      claimingRelief = Some("no"),
      transactionDescription = Some("F"),
      isLinked = Some("no")
    )),
    residency = Some(Residency(isNonUkResidents = Some("no"))),
    land = Some(Seq(Land(landID = Some("L1"), propertyType = Some("01"), interestCreatedTransferred = Some("FPF"))))
  )

  private val freeholdTaxCalculatedAnswers: UserAnswers =
    emptyUserAnswers
      .copy(returnId = Some("REF"), fullReturn = Some(freeholdFullReturn))
      .set(TaxCalculationFlowPage, TaxCalculationFlow.FreeholdTaxCalculated).success.value
      .set(FreeholdTaxCalculatedSelfAssessedAmountPage, "43750").success.value
      .set(FreeholdTaxCalculatedTotalAmountDuePage, "43850").success.value
      .set(FreeholdTaxCalculatedPenaltiesAndInterestPage, true).success.value

  private val freeholdSelfAssessedAnswers: UserAnswers = emptyUserAnswers.copy(
      returnId = Some("REF"),
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
    emptyUserAnswers
      .copy(returnId = Some("REF"), fullReturn = Some(FullReturn(
        stornId = "STORN",
        returnResourceRef = "REF",
        returnInfo = Some(ReturnInfo(version = Some("1"))),
        transaction = Some(Transaction(effectiveDate = Some(today.minusDays(60).toString)))
      )))
      .set(TaxCalculationFlowPage, TaxCalculationFlow.FreeholdSelfAssessed).success.value
      .set(FreeholdSelfAssessedAmountPage, "43750").success.value
      .set(FreeholdSelfAssessedTotalAmountDuePage, "43850").success.value
      .set(FreeholdSelfAssessedPenaltiesAndInterestPage, true).success.value

  private val answersToPopulate: UserAnswers =
    emptyUserAnswers
      .copy(returnId = Some("REF"), fullReturn = Some(freeholdFullReturn.copy(
        taxCalculation = Some(TaxCalculation(amountPaid = Some("43850"), includesPenalty = Some("yes"), taxDue = Some("43750")))
      )))
      .set(TaxCalculationFlowPage, TaxCalculationFlow.FreeholdTaxCalculated).success.value

  private val answersWithoutFlow: UserAnswers =
    emptyUserAnswers
      .copy(returnId = Some("REF"), fullReturn = Some(freeholdFullReturn.copy(
        taxCalculation = Some(TaxCalculation(amountPaid = Some("43850"), includesPenalty = Some("yes"), taxDue = Some("43750")))
      )))

  private def appWith(answers: UserAnswers,
                      calcResponse: Future[CalculationResponse] = Future.successful(CalculationResponse(Seq.empty)),
                      versionReturn: ReturnVersionUpdateReturn = ReturnVersionUpdateReturn(newVersion = Some(2)),
                      taxCalcReturn: UpdateTaxCalculationReturn = UpdateTaxCalculationReturn(updated = true)): Application = {
    val connector = mock[SdltCalculationConnector]
    val session   = mock[SessionRepository]
    val backend   = mock[StampDutyLandTaxConnector]
    when(connector.calculateStampDutyLandTax(any())(any())).thenReturn(calcResponse)
    when(session.get(any())).thenReturn(Future.successful(Some(answers)))
    when(session.set(any())).thenReturn(Future.successful(true))
    when(backend.updateReturnVersion(any())(any(), any())).thenReturn(Future.successful(versionReturn))
    when(backend.updateTaxCalculationInfo(any())(any(), any())).thenReturn(Future.successful(taxCalcReturn))
    applicationBuilder(userAnswers = Some(answers))
      .overrides(
        bind[SdltCalculationConnector].toInstance(connector),
        bind[SessionRepository].toInstance(session),
        bind[StampDutyLandTaxConnector].toInstance(backend)
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

      "populates the answers from the full return when the section has not been started" in {
        val app = appWith(answersToPopulate, Future.successful(CalculationResponse(Seq(calculatedResult))))
        running(app) {
          status(onPageLoad(app)) mustEqual OK
        }
      }

      "redirects to the start of the tax calculation journey when populating the section from the full return fails" in {
        val populateService = mock[PopulateTaxCalculationService]
        when(populateService.populateTaxCalculationInSession(any(), any(), any()))
          .thenReturn(Failure(new RuntimeException("populate failed")))

        val session = mock[SessionRepository]
        when(session.get(any())).thenReturn(Future.successful(Some(answersToPopulate)))
        when(session.set(any())).thenReturn(Future.successful(true))

        val app = applicationBuilder(userAnswers = Some(answersToPopulate))
          .overrides(
            bind[PopulateTaxCalculationService].toInstance(populateService),
            bind[SessionRepository].toInstance(session)
          )
          .build()

        running(app) {
          val result = onPageLoad(app)
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.TaxCalculationBeforeYouStartController.onPageLoad().url
        }
      }

      "redirects to the task list when the return id is missing" in {
        val app = appWith(freeholdTaxCalculatedAnswers.copy(returnId = None))
        running(app) {
          val result = onPageLoad(app)
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "derives the flow from the calculation when it is not set in session, then returns OK" in {
        val app = appWith(answersWithoutFlow, Future.successful(CalculationResponse(Seq(calculatedResult))))
        running(app) {
          status(onPageLoad(app)) mustEqual OK
        }
      }
    }
    "onSubmit" - {

      "submits the tax calculation and redirects to the task list when the backend confirms the update" in {
        val app = appWith(freeholdTaxCalculatedAnswers, Future.successful(CalculationResponse(Seq(calculatedResult))))
        running(app) {
          val result = route(app, FakeRequest(POST, routes.TaxCalculationCheckYourAnswersController.onSubmit().url)).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "redirects back to check your answers when the backend does not confirm the update" in {
        val app = appWith(
          freeholdTaxCalculatedAnswers,
          Future.successful(CalculationResponse(Seq(calculatedResult))),
          taxCalcReturn = UpdateTaxCalculationReturn(updated = false)
        )
        running(app) {
          val result = route(app, FakeRequest(POST, routes.TaxCalculationCheckYourAnswersController.onSubmit().url)).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.TaxCalculationCheckYourAnswersController.onPageLoad().url
        }
      }

      "submits a self-assessed flow and redirects to the task list when the backend confirms the update" in {
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

            val row = controller.buildRowResults(calculatedResult, freeholdTaxCalculatedAnswers)

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

              val row = controller.buildRowResults(noCalcResult, freeholdSelfAssessedAnswers)
              row.size mustEqual 4
            }
          }
        }
      }
    }
  }
}
