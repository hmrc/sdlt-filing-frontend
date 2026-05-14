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

package controllers.taxCalculation.leaseholdTaxCalculated

import base.SpecBase
import models.taxCalculation.*
import models.{FullReturn, Land, Lease, Purchaser, Residency, ReturnInfo, Transaction, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
//import org.mockito.Mockito.verify
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.taxCalculation.SdltCalculationService
import viewmodels.taxCalculation.CalculationResultViewModel
import views.html.taxCalculation.CalculatedSdltBreakdownView

import scala.concurrent.Future

class LeaseholdCalculatedSdltBreakdownControllerSpec extends SpecBase with MockitoSugar {

  private val breakdownUrl: String = controllers.taxCalculation.leaseholdTaxCalculated.routes.LeaseholdCalculatedSdltBreakdownController.onPageLoad().url
  private val titleKey: String = "taxCalculation.calculation.leasehold.title"

  private val sdltcResult: TaxCalculationResult = TaxCalculationResult(
    totalTax = 4500, resultHeading = None, resultHint = None, npv = Some(0),
    taxCalcs = Seq(CalculationDetails(
      TaxTypes.rent, CalcTypes.slice, 9000, None, None, None, None, None,
      Some(Seq(SliceDetails(0, Some(250000), 0, 0), SliceDetails(250000, Some(925000), 5, 9000)))
    ), CalculationDetails(
      TaxTypes.premium, CalcTypes.slice, 9000, None, None, None, None, None,
      Some(Seq(SliceDetails(0, Some(250000), 0, 0), SliceDetails(250000, Some(925000), 5, 9000)))
    )
    ))

  private val leaseholdAnswers: UserAnswers = emptyUserAnswers.copy(fullReturn = Some(FullReturn(
    stornId = "STORN",
    returnResourceRef = "leasehold-tax-calculated",
    returnInfo = Some(ReturnInfo(mainLandID = Some("L1"))),
    purchaser = Some(Seq(Purchaser(purchaserID = Some("PurchaserID"), returnID = Some("ReturnID"), isCompany = Some("NO"), surname = Some("OBrian"), forename1 = Some("Liam")))),
    land = Some(Seq(Land(landID = Some("L1"), propertyType = Some("01"), interestCreatedTransferred = Some("FPF")))),
    transaction = Some(Transaction(effectiveDate = Some("2024-04-01"), totalConsideration = Some(BigDecimal(300000)), claimingRelief = Some("no"), transactionDescription = Some("L"), isLinked = Some("no"))),
    lease = Some(Lease(leaseID = Some("LeaseID"), returnID = Some("ReturnID"), contractStartDate = Some("2024-10-01"), contractEndDate = Some("2034-09-30"), startingRent = Some("12000.00"))),
    residency = Some(Residency(residencyID = Some("ResidencyID"), isNonUkResidents = Some("NO")))
  )))

  private def appWith(answers: UserAnswers, sdltcStub: Future[Either[MissingDataError, TaxCalculationResult]]) = {
    val mockService = mock[SdltCalculationService]
    when(mockService.calculateStampDutyLandTax(any())(any(), any())).thenReturn(sdltcStub)
    applicationBuilder(userAnswers = Some(answers))
      .overrides(bind[SdltCalculationService].toInstance(mockService))
      .build()
  }

  "LeaseholdCalculatedSdltBreakdownController" - {

    "must return OK and render the breakdown view when valid user answers given" in {

      val app = appWith(leaseholdAnswers, Future.successful(Right(sdltcResult)))

      running(app) {
        val request = FakeRequest(GET, controllers.taxCalculation.leaseholdTaxCalculated.routes.LeaseholdCalculatedSdltBreakdownController.onPageLoad().url)
        val result = route(app, request).value

        val view = app.injector.instanceOf[CalculatedSdltBreakdownView]
        val expected = CalculationResultViewModel.toViewModel(sdltcResult, leaseholdAnswers)(messages(app)).toOption.value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(expected, breakdownUrl, titleKey)(request, messages(app)).toString
      }
    }
    "must redirect to the no-return-reference page when sdltc reports a missing FullReturn" in {

      val app = appWith(leaseholdAnswers, Future.successful(Left(MissingFullReturnError)))

      running(app) {
        val request = FakeRequest(GET, controllers.taxCalculation.leaseholdTaxCalculated.routes.LeaseholdCalculatedSdltBreakdownController.onPageLoad().url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.NoReturnReferenceController.onPageLoad().url
      }
    }

    "must redirect to the return task list when sdltc reports any other missing-data error" in {

      val app = appWith(leaseholdAnswers, Future.successful(Left(MissingAboutTheTransactionError)))

      running(app) {
        val request = FakeRequest(GET, controllers.taxCalculation.leaseholdTaxCalculated.routes.LeaseholdCalculatedSdltBreakdownController.onPageLoad().url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }

    "must redirect to the return task list when sdltc succeeds but there is required session data missing" in {

      val brokenAnswers = leaseholdAnswers.copy(fullReturn = leaseholdAnswers.fullReturn.map(fr =>
        fr.copy(transaction = fr.transaction.map(_.copy(claimingRelief = None)))
      ))

      val app = appWith(brokenAnswers, Future.successful(Right(sdltcResult)))

      running(app) {
        val request = FakeRequest(GET, controllers.taxCalculation.leaseholdTaxCalculated.routes.LeaseholdCalculatedSdltBreakdownController.onPageLoad().url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
      }
    }
  }
}
