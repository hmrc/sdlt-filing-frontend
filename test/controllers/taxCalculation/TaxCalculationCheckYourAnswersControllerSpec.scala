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
import models.taxCalculation.{CalculationResponse, TaxCalculationFlow, TaxCalculationResult}
import models.{FullReturn, Land, Residency, ReturnInfo, Transaction, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.taxCalculation.TaxCalculationFlowPage
import pages.taxCalculation.freeholdTaxCalculated.{FreeholdTaxCalculatedPenaltiesAndInterestPage, FreeholdTaxCalculatedSelfAssessedAmountPage, FreeholdTaxCalculatedTotalAmountDuePage}
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository

import java.time.LocalDate
import scala.concurrent.Future

class TaxCalculationCheckYourAnswersControllerSpec extends SpecBase with MockitoSugar {

  private val today            = LocalDate.of(2026, 5, 1)
  private val calculatedResult = TaxCalculationResult(totalTax = 43750, None, None, None, taxCalcs = Seq.empty)
  private val noCalcResult     = TaxCalculationResult(totalTax = 0, Some("Self-assessed"), None, None, taxCalcs = Seq.empty)

  // A complete return so the calculation request can be built for a calculated flow.
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

      "returns OK for a calculated flow when the calculation succeeds" in {
        val app = appWith(calculatedAnswers, Future.successful(CalculationResponse(Seq(calculatedResult))))
        running(app) {
          status(onPageLoad(app)) mustEqual OK
        }
      }

      "returns OK for a self-assessed flow" in {
        val app = appWith(selfAssessedAnswers)
        running(app) {
          status(onPageLoad(app)) mustEqual OK
        }
      }

      "redirects to the task list when the calculation does not return a calculated result" in {
        val app = appWith(calculatedAnswers, Future.successful(CalculationResponse(Seq(noCalcResult))))
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
  }
}
