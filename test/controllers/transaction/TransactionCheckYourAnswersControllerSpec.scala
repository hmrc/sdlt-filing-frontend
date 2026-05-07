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

package controllers.transaction

import base.SpecBase
import models.UserAnswers
import models.prelimQuestions.TransactionType
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.transaction.*
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import viewmodels.govuk.SummaryListFluency

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class TransactionCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency with MockitoSugar with BeforeAndAfterEach {

  private val mockSessionRepository = mock[SessionRepository]

  implicit val request: FakeRequest[_] = FakeRequest()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository)
  }

  private val baseUserAnswers = UserAnswers(
    id = "12345",
    returnId = Some("AB2346"),
    storn = "TESTSTORN",
    data = Json.obj("key" -> "value")
  )

  // Fully populated grant of lease userAnswers — all mandatory rows are Row (not Missing)
  private val completeUserAnswers = UserAnswers(
    id = "12345",
    returnId = Some("AB2346"),
    storn = "TESTSTORN",
    data = Json.obj()
  )
    .set(TypeOfTransactionPage, TransactionType.GrantOfLease).success.value
    .set(TransactionEffectiveDatePage, LocalDate.of(2024, 1, 1)).success.value
    .set(TransactionAddDateOfContractPage, false).success.value
    .set(TransactionLinkedTransactionsPage, false).success.value
    .set(PurchaserEligibleToClaimReliefPage, false).success.value
    .set(TransactionPartialReliefPage, false).success.value

  "TransactionCheckYourAnswers Controller" - {

    "onPageLoad" - {

      "must redirect to ReturnTaskList when the UserAnswers has no returnId" in {

        val userAnswers = emptyUserAnswers.copy(returnId = None)
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must redirect to JourneyRecovery when session data is not found" in {

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to TransactionBeforeYouStart when transactionCurrent data is empty" in {

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(baseUserAnswers)))

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.transaction.routes.TransactionBeforeYouStartController.onPageLoad().url
        }
      }

      "must return OK and the correct view when UserAnswers contains complete transaction data" in {

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(completeUserAnswers)))

        val application = applicationBuilder(userAnswers = Some(completeUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Check your answers")
        }
      }

      "must redirect to Journey Recovery when no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to first missing page when mandatory data is absent" in {

        val userAnswers = completeUserAnswers.remove(TransactionEffectiveDatePage).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.transaction.routes.TransactionEffectiveDateController.onPageLoad(models.CheckMode).url
        }
      }

      "must not show grant of lease rows when transaction type is grant of lease" in {

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(completeUserAnswers)))

        val application = applicationBuilder(userAnswers = Some(completeUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must not include "transaction.totalConsiderationOfTransaction.checkYourAnswersLabel"
          contentAsString(result) must not include "transaction.transactionVatIncluded.checkYourAnswersLabel"
          contentAsString(result) must not include "transaction.transactionFormsOfConsideration.checkYourAnswersLabel"
        }
      }
    }

    "onSubmit" - {

      "must redirect to ReturnTaskList when session data exists" in {

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(baseUserAnswers)))

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.transaction.routes.TransactionCheckYourAnswersController.onSubmit().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must redirect to JourneyRecovery when session data is not found" in {

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.transaction.routes.TransactionCheckYourAnswersController.onSubmit().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to JourneyRecovery when no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(POST, controllers.transaction.routes.TransactionCheckYourAnswersController.onSubmit().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
