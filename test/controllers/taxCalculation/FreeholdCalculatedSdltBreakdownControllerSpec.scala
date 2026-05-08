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
import models.taxCalculation.*
import models.{FullReturn, Land, ReturnInfo, Transaction, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.taxCalculation.SdltCalculationService
import viewmodels.taxCalculation.CalculationResultViewModel
import views.html.taxCalculation.freeholdTaxCalculated.FreeholdCalculatedSdltBreakdownView

import scala.concurrent.Future

class FreeholdCalculatedSdltBreakdownControllerSpec extends SpecBase with MockitoSugar {

  private val sdltcResult = TaxCalculationResult(
    totalTax = 9000, resultHeading = None, resultHint = None, npv = None,
    taxCalcs = Seq(CalculationDetails(
      TaxTypes.premium, CalcTypes.slice, 9000, None, None, None, None, None,
      Some(Seq(SliceDetails(0, Some(250000), 0, 0), SliceDetails(250000, Some(925000), 5, 9000)))
    ))
  )

  private val freeholdAnswers: UserAnswers = emptyUserAnswers.copy(fullReturn = Some(FullReturn(
    stornId = "STORN", returnResourceRef = "REF",
    returnInfo = Some(ReturnInfo(mainLandID = Some("L1"))),
    transaction = Some(Transaction(
      effectiveDate          = Some("2024-04-01"),
      totalConsideration     = Some(BigDecimal(300000)),
      claimingRelief         = Some("no"),
      transactionDescription = Some("F"),
      isLinked               = Some("no")
    )),
    land = Some(Seq(Land(landID = Some("L1"), propertyType = Some("01"), interestCreatedTransferred = Some("FPF"))))
  )))

  private def appWith(answers: UserAnswers, sdltcStub: Future[Either[MissingDataError, TaxCalculationResult]]) = {
    val mockService = mock[SdltCalculationService]
    when(mockService.calculateStampDutyLandTax(any())(any(), any())).thenReturn(sdltcStub)
    applicationBuilder(userAnswers = Some(answers))
      .overrides(bind[SdltCalculationService].toInstance(mockService))
      .build()
  }

  "FreeholdCalculatedSdltBreakdownController" - {

    "must return OK and render the breakdown view when sdltc and the helper both succeed" in {

      val app = appWith(freeholdAnswers, Future.successful(Right(sdltcResult)))

      running(app) {
        val request = FakeRequest(GET, controllers.taxCalculation.freeholdTaxCalculated.routes.FreeholdCalculatedSdltBreakdownController.onPageLoad().url)
        val result  = route(app, request).value

        val view     = app.injector.instanceOf[FreeholdCalculatedSdltBreakdownView]
        val expected = CalculationResultViewModel.toViewModel(sdltcResult, freeholdAnswers)(messages(app)).toOption.value

        status(result)        mustEqual OK
        contentAsString(result) mustEqual view(expected)(request, messages(app)).toString
      }
    }

    "must redirect to the no-return-reference page when sdltc reports a missing FullReturn" in {

      val app = appWith(freeholdAnswers, Future.successful(Left(MissingFullReturnError)))

      running(app) {
        val request = FakeRequest(GET, controllers.taxCalculation.freeholdTaxCalculated.routes.FreeholdCalculatedSdltBreakdownController.onPageLoad().url)
        val result  = route(app, request).value

        status(result)             mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.NoReturnReferenceController.onPageLoad().url
      }
    }

    "must redirect to the return task list when sdltc reports any other missing-data error" in {

      val app = appWith(freeholdAnswers, Future.successful(Left(MissingAboutTheTransactionError)))

      running(app) {
        val request = FakeRequest(GET, controllers.taxCalculation.freeholdTaxCalculated.routes.FreeholdCalculatedSdltBreakdownController.onPageLoad().url)
        val result  = route(app, request).value

        status(result)             mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }

    "must redirect to the return task list when sdltc succeeds but there is required session data missing" in {

      val brokenAnswers = freeholdAnswers.copy(fullReturn = freeholdAnswers.fullReturn.map(fr =>
        fr.copy(transaction = fr.transaction.map(_.copy(claimingRelief = None)))
      ))

      val app = appWith(brokenAnswers, Future.successful(Right(sdltcResult)))

      running(app) {
        val request = FakeRequest(GET, controllers.taxCalculation.freeholdTaxCalculated.routes.FreeholdCalculatedSdltBreakdownController.onPageLoad().url)
        val result  = route(app, request).value

        status(result)             mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }
  }
}
