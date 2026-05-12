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

package services.taxCalculation

import base.SpecBase
import connectors.SdltCalculationConnector
import models.*
import models.requests.DataRequest
import models.taxCalculation.*
import pages.taxCalculation.TaxCalculationFlowPage
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.taxCalculation.freeholdSelfAssessed.FreeholdSelfAssessedPenaltiesAndInterestPage
import play.api.mvc.{AnyContentAsEmpty, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import models.PenaltiesAndInterest.AmountIncludePenaltiesAndInterestYes

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionException, Future}

class SdltCalculationServiceSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val mockConnector: SdltCalculationConnector = mock[SdltCalculationConnector]
  val mockSessionRepository: SessionRepository = mock[SessionRepository]
  val service = new SdltCalculationService(
    connector = mockConnector,
    sessionRepository = mockSessionRepository)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockConnector)
  }

  val expectedResult: TaxCalculationResult =
    TaxCalculationResult(totalTax = 5000, resultHeading = None, resultHint = None, npv = None, taxCalcs = Seq.empty)


  val validUserAnswers: UserAnswers = UserAnswers(
    id = "id", storn = "STORN",
    fullReturn = Some(FullReturn(
      stornId = "STORN", returnResourceRef = "REF",
      returnInfo = Some(ReturnInfo(mainLandID = Some("L1"))),
      land = Some(Seq(Land(landID = Some("L1"), propertyType = Some("01"), interestCreatedTransferred = Some("FPF")))),
      transaction = Some(Transaction(
        transactionDescription = Some("F"), effectiveDate = Some("2025-06-15"),
        totalConsideration = Some(250000), isLinked = Some("no"), claimingRelief = Some("no")
      )),
      residency = Some(Residency(isNonUkResidents = Some("no")))
    ))
  )

  val taxCalculation = TaxCalculation(includesPenalty = Some("true"))
  val validUserAnswerWithIncludePenalties: UserAnswers = UserAnswers(
    id = "id", storn = "STORN",
    fullReturn = Some(FullReturn(
      taxCalculation = Some(taxCalculation),
      stornId = "STORN",
      returnResourceRef = "REF",
      returnInfo = Some(ReturnInfo(mainLandID = Some("L1"))),
      land = Some(Seq(Land(landID = Some("L1"), propertyType = Some("01"), interestCreatedTransferred = Some("FPF")))),
      transaction = Some(Transaction(
        transactionDescription = Some("F"), effectiveDate = Some("2025-06-15"),
        totalConsideration = Some(250000), isLinked = Some("no")
      )),
      residency = Some(Residency(isNonUkResidents = Some("no")))
    ))
  )

  "calculateStampDutyLandTax" - {

    "must call the connector and return the result" in {
      when(mockConnector.calculateStampDutyLandTax(any())(any()))
        .thenReturn(Future.successful(CalculationResponse(Seq(expectedResult))))

      val result = service.calculateStampDutyLandTax(validUserAnswers).futureValue

      result mustBe Right(expectedResult)
      verify(mockConnector, times(1)).calculateStampDutyLandTax(any())(any())
    }

    "must fail when the connector returns an empty result list" in {
      when(mockConnector.calculateStampDutyLandTax(any())(any()))
        .thenReturn(Future.successful(CalculationResponse(Seq.empty)))

      val result = service.calculateStampDutyLandTax(validUserAnswers).failed.futureValue

      result mustBe an[IllegalStateException]
      result.getMessage mustBe "Calculation response contained no results"
    }

    "must return Left when session data is missing" in {
      val noFullReturn = UserAnswers(id = "id", storn = "STORN", fullReturn = None)

      val result = service.calculateStampDutyLandTax(noFullReturn).futureValue

      result mustBe Left(MissingFullReturnError)
      verify(mockConnector, never()).calculateStampDutyLandTax(any())(any())
    }

    "must fail when a validation error occurs" in {
      val badPropertyType = UserAnswers(
        id = "id", storn = "STORN",
        fullReturn = Some(FullReturn(
          stornId = "STORN", returnResourceRef = "REF",
          returnInfo = Some(ReturnInfo(mainLandID = Some("L1"))),
          land = Some(Seq(Land(landID = Some("L1"), propertyType = Some("99"), interestCreatedTransferred = Some("FPF")))),
          transaction = Some(Transaction(
            transactionDescription = Some("F"), effectiveDate = Some("2025-06-15"),
            totalConsideration = Some(250000), isLinked = Some("no"), claimingRelief = Some("no")
          )),
          residency = Some(Residency(isNonUkResidents = Some("no")))
        ))
      )

      val result = service.calculateStampDutyLandTax(badPropertyType).failed.futureValue

      result mustBe an[IllegalStateException]
      result.getMessage must include("Unknown Property Type")
      verify(mockConnector, never()).calculateStampDutyLandTax(any())(any())
    }

    "must fail when the connector fails" in {
      when(mockConnector.calculateStampDutyLandTax(any())(any()))
        .thenReturn(Future.failed(new RuntimeException("Connector failed")))

      val result = service.calculateStampDutyLandTax(validUserAnswers).failed.futureValue

      result mustBe a[RuntimeException]
      result.getMessage mustBe "Connector failed"
    }
  }

  "whenInFlow" - {

    "must run the onAllowed block when the session has the expected flow recorded" in {
      val answers = emptyUserAnswers.set(TaxCalculationFlowPage, TaxCalculationFlow.FreeholdTaxCalculated).success.value
      implicit val req: DataRequest[?] = DataRequest(FakeRequest(), userAnswersId, answers)

      val result = service.whenInFlow(TaxCalculationFlow.FreeholdTaxCalculated)(Results.Ok("rendered"))

      result.header.status mustBe OK
    }

    "must redirect to the return task list when the session has a different flow recorded" in {
      val answers = emptyUserAnswers.set(TaxCalculationFlowPage, TaxCalculationFlow.LeaseholdSelfAssessed).success.value
      implicit val req: DataRequest[?] = DataRequest(FakeRequest(), userAnswersId, answers)

      val result = service.whenInFlow(TaxCalculationFlow.FreeholdTaxCalculated)(Results.Ok("should not render"))

      result.header.status mustBe SEE_OTHER
      result.header.headers("Location") mustBe controllers.routes.ReturnTaskListController.onPageLoad().url
    }

    "must redirect to the return task list when no flow has been recorded in the session" in {
      implicit val req: DataRequest[?] = DataRequest(FakeRequest(), userAnswersId, emptyUserAnswers)

      val result = service.whenInFlow(TaxCalculationFlow.FreeholdTaxCalculated)(Results.Ok("should not render"))

      result.header.status mustBe SEE_OTHER
      result.header.headers("Location") mustBe controllers.routes.ReturnTaskListController.onPageLoad().url
    }
  }

  "whenInFlowAsync" - {

    "must run the onAllowed block when the session has the expected flow recorded" in {
      val answers = emptyUserAnswers.set(TaxCalculationFlowPage, TaxCalculationFlow.FreeholdTaxCalculated).success.value
      implicit val req: DataRequest[?] = DataRequest(FakeRequest(), userAnswersId, answers)

      val result = service.whenInFlowAsync(TaxCalculationFlow.FreeholdTaxCalculated) {
        Future.successful(Results.Ok("rendered"))
      }

      result.futureValue.header.status mustBe OK
    }

    "must redirect to the return task list when the session has a different flow recorded" in {
      val answers = emptyUserAnswers.set(TaxCalculationFlowPage, TaxCalculationFlow.LeaseholdSelfAssessed).success.value
      implicit val req: DataRequest[?] = DataRequest(FakeRequest(), userAnswersId, answers)

      val result = service.whenInFlowAsync(TaxCalculationFlow.FreeholdTaxCalculated) {
        Future.successful(Results.Ok("should not render"))
      }

      result.futureValue.header.status mustBe SEE_OTHER
      result.futureValue.header.headers("Location") mustBe controllers.routes.ReturnTaskListController.onPageLoad().url
    }

    "must redirect to the return task list when no flow has been recorded in the session" in {
      implicit val req: DataRequest[?] = DataRequest(FakeRequest(), userAnswersId, emptyUserAnswers)

      val result = service.whenInFlowAsync(TaxCalculationFlow.FreeholdTaxCalculated) {
        Future.successful(Results.Ok("should not render"))
      }

      result.futureValue.header.status mustBe SEE_OTHER
      result.futureValue.header.headers("Location") mustBe controllers.routes.ReturnTaskListController.onPageLoad().url
    }
  }

  "savePenaltiesAndInterestYesNoAnswer" - {

    implicit val dataRequest: DataRequest[AnyContentAsEmpty.type] = DataRequest(
      FakeRequest(), "test-id", emptyUserAnswers)

    "call repository to save user PenaltiesAndInterestYesNo choice :: success" in {
      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))
      val result = service.savePenaltiesAndInterestYesNoAnswer(
        key = FreeholdSelfAssessedPenaltiesAndInterestPage,
        value = AmountIncludePenaltiesAndInterestYes).futureValue
      result mustBe true
      verify(mockSessionRepository, times(1)).set(any())
    }

    "call repository to save user PenaltiesAndInterestYesNo choice :: failure" in {
      when(mockSessionRepository.set(any()))
        .thenThrow(Error("ConnectionTimeOut"))
      val result =  service.savePenaltiesAndInterestYesNoAnswer(
          key = FreeholdSelfAssessedPenaltiesAndInterestPage,
          value = AmountIncludePenaltiesAndInterestYes).failed.futureValue
      result mustBe an[ExecutionException]
      result.getCause.getMessage mustBe "ConnectionTimeOut"
    }

  }


}
