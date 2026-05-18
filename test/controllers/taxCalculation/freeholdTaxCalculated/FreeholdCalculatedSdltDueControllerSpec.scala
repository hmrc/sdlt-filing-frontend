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

package controllers.taxCalculation.freeholdTaxCalculated

import base.SpecBase
import connectors.SdltCalculationConnector
import models.taxCalculation.{CalculationResponse, TaxCalculationFlow, TaxCalculationResult}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.taxCalculation.TaxCalculationFlowPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.taxCalculation.freeholdTaxCalculated.FreeholdCalculatedSdltDueView
import models.{FullReturn, Land, Residency, ReturnInfo, Transaction, UserAnswers}

import java.time.LocalDate
import scala.concurrent.Future

class FreeholdCalculatedSdltDueControllerSpec extends SpecBase with MockitoSugar {

  private val today = LocalDate.of(2026, 5, 1)
  private val sdltcResult = TaxCalculationResult(totalTax = 43750, None, None, None, taxCalcs = Seq.empty)
  private val sdltDue = "£43,750"
  private val sectionKey = "site.taxCalculation.freeholdSdltCalculated.section"

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
          transactionDescription = Some("F"),
          isLinked = Some("no")
        )),
        residency = Some(Residency(isNonUkResidents = Some("no"))),
        land = Some(Seq(Land(landID = Some("L1"), propertyType = Some("01"), interestCreatedTransferred = Some("FPF"))))
      )))
      .set(TaxCalculationFlowPage, TaxCalculationFlow.FreeholdTaxCalculated).success.value

  private def appWith(answers: UserAnswers, sdltcResponse: Future[CalculationResponse]) = {
    val mockConnector = mock[SdltCalculationConnector]
    when(mockConnector.calculateStampDutyLandTax(any())(any())).thenReturn(sdltcResponse)
    applicationBuilder(userAnswers = Some(answers))
      .overrides(
        bind[SdltCalculationConnector].toInstance(mockConnector)
      )
      .build()
  }

  "FreeholdCalculatedSdltDueController Controller" - {

    "must return OK and the correct view for a GET" in {

      val app = appWith(freeholdAnswers, Future.successful(CalculationResponse(Seq(sdltcResult))))

      running(app) {
        val request = FakeRequest(GET, controllers.taxCalculation.freeholdTaxCalculated.routes.FreeholdCalculatedSdltDueController.onPageLoad().url)

        val result = route(app, request).value

        val view = app.injector.instanceOf[FreeholdCalculatedSdltDueView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(sdltDue, sectionKey)(request, messages(app)).toString
      }
    }

    "must redirect to the return task list when the user is not in the freehold tax calculated flow" in {

      val app = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(app) {
        val request = FakeRequest(GET, controllers.taxCalculation.freeholdTaxCalculated.routes.FreeholdCalculatedSdltDueController.onPageLoad().url)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }

    "must redirect to the return task list when sdltc cannot calculate (validation rejects the request)" in {

      val brokenAnswers = freeholdAnswers.copy(fullReturn = freeholdAnswers.fullReturn.map(fr =>
        fr.copy(transaction = fr.transaction.map(_.copy(transactionDescription = None)))
      ))

      val app = appWith(brokenAnswers, Future.successful(CalculationResponse(Seq(sdltcResult))))

      running(app) {
        val request = FakeRequest(GET, controllers.taxCalculation.freeholdTaxCalculated.routes.FreeholdCalculatedSdltDueController.onPageLoad().url)
        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }
  }
}
