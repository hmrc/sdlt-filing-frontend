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

package controllers.taxCalculation.freeholdSelfAssessed

import base.SpecBase
import models.taxCalculation.*
import models.{FullReturn, Land, Lease, NormalMode, Residency, ReturnInfo, Transaction, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.taxCalculation.TaxCalculationFlowPage
import pages.taxCalculation.freeholdSelfAssessed.{FreeholdSelfAssessedAmountPage, FreeholdSelfAssessedTotalAmountDuePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import utils.TimeMachine

import java.time.LocalDate
import scala.concurrent.Future

class FreeholdSdltNotCalculatedTotalDueControllerSpec extends SpecBase with MockitoSugar {

  private val today = LocalDate.of(2026, 5, 1)

  private val freeholdAnswers: UserAnswers =
    emptyUserAnswers
      .copy(fullReturn = Some(FullReturn(
        stornId = "STORN",
        returnResourceRef = "REF",
        returnInfo = Some(ReturnInfo(mainLandID = Some("L1"))),
        transaction = Some(Transaction(
          effectiveDate = Some(today.minusDays(60).toString),
          totalConsideration = Some(BigDecimal(300000)),
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

  private def appWith(answers: UserAnswers) = {
    val mockSession = mock[SessionRepository]
    val mockTimeMachine = mock[TimeMachine]
    when(mockSession.set(any())).thenReturn(Future.successful(true))
    when(mockTimeMachine.today).thenReturn(today)
    applicationBuilder(userAnswers = Some(answers))
      .overrides(
        bind[SessionRepository].toInstance(mockSession),
        bind[TimeMachine].toInstance(mockTimeMachine)
      )
      .build()
  }

  "FreeholdSdltNotCalculatedTotalDueController" - {

    "must return OK and render the total-amount-due view when sdltc and the helper both succeed" in {

      val app = appWith(freeholdAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.FreeholdSdltNotCalculatedTotalDueController.onPageLoad(NormalMode).url)
        val result = route(app, request).value

        status(result) mustEqual OK
        val body = contentAsString(result)
        body must include("£43,750")
        body must include("£100")
        body must include("£43,850")
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val previouslyAnswered = freeholdAnswers.set(FreeholdSelfAssessedTotalAmountDuePage, "12345").success.value
      val app = appWith(previouslyAnswered)

      running(app) {
        val request = FakeRequest(GET, routes.FreeholdSdltNotCalculatedTotalDueController.onPageLoad(NormalMode).url)
        val result = route(app, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("""value="12345"""")
      }
    }

    "must redirect to the return task list when the user is not in the freehold-sdlt-not-calculated flow" in {

      val outOfFlow = freeholdAnswers.set(TaxCalculationFlowPage, TaxCalculationFlow.FreeholdTaxCalculated).success.value
      val app = appWith(outOfFlow)

      running(app) {
        val request = FakeRequest(GET, routes.FreeholdSdltNotCalculatedTotalDueController.onPageLoad(NormalMode).url)
        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }

    "must redirect to return task list when retrieving value from FreeholdSelfAssessedAmountPage fails" in {

      val brokenAnswers = freeholdAnswers.remove(FreeholdSelfAssessedAmountPage).success.value

      val app = appWith(brokenAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.FreeholdSdltNotCalculatedTotalDueController.onPageLoad(NormalMode).url)
        val result = route(app, request).value
        status(result) mustEqual SEE_OTHER
      }
    }


    "must redirect to return task list when constructViewModel fails" in {

      val brokenAnswers = freeholdAnswers.copy(fullReturn = freeholdAnswers.fullReturn.map(_.copy(transaction = Some(Transaction()))))

      val app = appWith(brokenAnswers)

      running(app) {
        val request = FakeRequest(GET, routes.FreeholdSdltNotCalculatedTotalDueController.onPageLoad(NormalMode).url)
        val result = route(app, request).value
        status(result) mustEqual SEE_OTHER
      }
    }

    "must redirect on a valid POST" in {

      val app = appWith(freeholdAnswers)

      running(app) {
        val request = FakeRequest(POST, routes.FreeholdSdltNotCalculatedTotalDueController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody("value" -> "1000")
        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
      }
    }

    "must return BAD_REQUEST when form is invalid" in {

      val app = appWith(freeholdAnswers)

      running(app) {
        val request = FakeRequest(POST, routes.FreeholdSdltNotCalculatedTotalDueController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody("value" -> "not-a-number")

        val result = route(app, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) must include("£43,750")
      }
    }

    "must redirect when invalid form and constructViewModel fails" in {

      val brokenAnswers = freeholdAnswers.copy(fullReturn = freeholdAnswers.fullReturn.map(_.copy(transaction = Some(Transaction()))))
      val app = appWith(brokenAnswers)
      running(app) {
        val request =
          FakeRequest(POST, routes.FreeholdSdltNotCalculatedTotalDueController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody("value" -> "invalid")

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
      }
    }
  }
}
