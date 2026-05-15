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

package controllers.taxCalculation.leaseholdSelfAssessed

import base.SpecBase
import models.taxCalculation.*
import models.{FullReturn, Land, NormalMode, Residency, ReturnInfo, Transaction, UserAnswers}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.taxCalculation.TaxCalculationFlowPage
import pages.taxCalculation.leaseholdSelfAssessed.{LeaseholdSelfAssessedNpvTaxPage, LeaseholdSelfAssessedPremiumPayableTaxPage, LeaseholdSelfAssessedTotalAmountDuePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import utils.TimeMachine
import controllers.taxCalculation.leaseholdSelfAssessed

import java.time.LocalDate

class LeaseholdSelfAssessedTotalAmountDueControllerSpec extends SpecBase with MockitoSugar {

  private lazy val totalAmountDueRoute =
    routes.LeaseholdSelfAssessedTotalAmountDueController.onPageLoad(NormalMode).url

  private val today = LocalDate.of(2026, 5, 1)

  private val leaseholdAnswers: UserAnswers =
    emptyUserAnswers
      .copy(fullReturn = Some(FullReturn(
        stornId           = "STORN",
        returnResourceRef = "REF",
        returnInfo        = Some(ReturnInfo(mainLandID = Some("L1"))),
        transaction       = Some(Transaction(
          effectiveDate          = Some(today.minusDays(60).toString),
          totalConsideration     = Some(BigDecimal(300000)),
          claimingRelief         = Some("no"),
          transactionDescription = Some("L"),
          isLinked               = Some("no")
        )),
        residency = Some(Residency(isNonUkResidents = Some("no"))),
        land      = Some(Seq(Land(landID = Some("L1"), propertyType = Some("01"), interestCreatedTransferred = Some("FPF"))))
      )))
      .set(TaxCalculationFlowPage, TaxCalculationFlow.LeaseholdSelfAssessed).success.value
      .set(LeaseholdSelfAssessedPremiumPayableTaxPage, "20000").success.value
      .set(LeaseholdSelfAssessedNpvTaxPage, "5000").success.value

  private def appWith(answers: UserAnswers) = {
    val mockTimeMachine = mock[TimeMachine]
    when(mockTimeMachine.today).thenReturn(today)
    applicationBuilder(userAnswers = Some(answers))
      .overrides(
        bind[TimeMachine].toInstance(mockTimeMachine)
      )
      .build()
  }

  "LeaseholdSelfAssessedTotalAmountDueController" - {

    "must return OK and render the total-amount-due view when the helper succeeds" in {
      val app = appWith(leaseholdAnswers)

      running(app) {
        val request = FakeRequest(GET, totalAmountDueRoute)

        val result  = route(app, request).value

        status(result) mustEqual OK
        val body = contentAsString(result)
        body must include("£25,000")
        body must include("£100")
        body must include("£25,100")
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val previouslyAnswered = leaseholdAnswers.set(LeaseholdSelfAssessedTotalAmountDuePage, "12345").success.value
      val app = appWith(previouslyAnswered)

      running(app) {
        val request = FakeRequest(GET, totalAmountDueRoute)
        val result = route(app, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("""value="12345"""")
      }
    }

    "must redirect to the return task list when the user is not in the leasehold self-assessed flow" in {
      val outOfFlow = leaseholdAnswers.set(TaxCalculationFlowPage, TaxCalculationFlow.FreeholdTaxCalculated).success.value
      val app = appWith(outOfFlow)

      running(app) {
        val request = FakeRequest(GET, totalAmountDueRoute)
        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }

    "must redirect to the return task list when an answer in a previous flow is missing" in {
      val brokenAnswers = leaseholdAnswers.copy(fullReturn = leaseholdAnswers.fullReturn.map(fr =>
        fr.copy(transaction = fr.transaction.map(_.copy(effectiveDate = None)))
      ))

      val app = appWith(brokenAnswers)

      running(app) {
        val request = FakeRequest(GET, totalAmountDueRoute)
        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }

    "must redirect to the return task list when an answer in the current flow is missing" in {
      val brokenAnswers = leaseholdAnswers.remove(LeaseholdSelfAssessedPremiumPayableTaxPage).success.value

      val app = appWith(brokenAnswers)

      running(app) {
        val request = FakeRequest(GET, totalAmountDueRoute)
        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }

    "must redirect on a valid POST" in {

      val app = appWith(leaseholdAnswers)

      running(app) {
        val request = FakeRequest(POST, routes.LeaseholdSelfAssessedTotalAmountDueController.onPageLoad(NormalMode).url)
          .withFormUrlEncodedBody("value" -> "1000")
        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
      }
    }

    "must return a Bad Request when the form is invalid, re-rendering the view" in {

      val app = appWith(leaseholdAnswers)

      running(app) {
        val request = FakeRequest(POST, routes.LeaseholdSelfAssessedTotalAmountDueController.onPageLoad(NormalMode).url)
          .withFormUrlEncodedBody("value" -> "not-a-number")
        val result = route(app, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include("£25,100")
      }
    }
  }
}
