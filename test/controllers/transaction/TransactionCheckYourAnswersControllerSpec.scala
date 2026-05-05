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
import models.transaction.ReasonForRelief
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
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.govuk.SummaryListFluency

import scala.concurrent.{ExecutionContext, Future}

class TransactionCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency with MockitoSugar with BeforeAndAfterEach {

  private val mockSessionRepository = mock[SessionRepository]

  implicit val hc: HeaderCarrier = HeaderCarrier()
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

  "TransactionCheckYourAnswers Controller" - {

    "onPageLoad" - {

      "must redirect to ReturnTaskList when the UserAnswers has no returnId" in {

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must redirect to ReturnTaskList when session data is not found" in {

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must redirect to BeforeStartReturnController when returnId exists but data is empty" in {

        val userAnswers = UserAnswers(
          id = "12345",
          returnId = Some("AB2346"),
          storn = "TESTSTORN",
          data = Json.obj()
        )

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.preliminary.routes.BeforeStartReturnController.onPageLoad().url
        }
      }

      "must return OK and the correct view when UserAnswers contains valid data" in {

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(baseUserAnswers)))

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
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

      "must return OK and show contract date row when contractDateKnown is true" in {

        val userAnswers = baseUserAnswers
          .set(TransactionAddDateOfContractPage, true).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Do you know the date of contract or conclusion of missives?")
          contentAsString(result) must include("Contract date or conclusion of missives")
        }
      }

      "must return OK and show VAT amount row when vatIncluded is true" in {

        val userAnswers = baseUserAnswers
          .set(TransactionVatIncludedPage, true).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Is VAT included in the total consideration?")
          contentAsString(result) must include("What is the amount of VAT included in the total consideration?")
        }
      }

      "must return OK and show total consideration of linked transactions row when transaction is linked" in {

        val userAnswers = baseUserAnswers
          .set(TransactionLinkedTransactionsPage, true).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must return OK and show reason for relief row when purchaser is claiming relief" in {

        val userAnswers = baseUserAnswers
          .set(PurchaserEligibleToClaimReliefPage, true).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Claiming relief")
          contentAsString(result) must include("Reason for claiming relief")
        }
      }

      "must return OK and show charity number row when relief reason is CharitiesRelief" in {

        val userAnswers = baseUserAnswers
          .set(PurchaserEligibleToClaimReliefPage, true).success.value
          .set(ReasonForReliefPage, ReasonForRelief.CharitiesRelief).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Registered charity number")
        }
      }

      "must return OK and show charity registered number row when knowsCharityNumber is true" in {

        val userAnswers = baseUserAnswers
          .set(PurchaserEligibleToClaimReliefPage, true).success.value
          .set(ReasonForReliefPage, ReasonForRelief.CharitiesRelief).success.value
          .set(AddRegisteredCharityNumberPage, true).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Charity Registered Number")
        }
      }

      "must return OK and show partial relief amount row when isPartialRelief is true" in {

        val userAnswers = baseUserAnswers
          .set(TransactionPartialReliefPage, true).success.value

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Is the purchaser claiming relief on part of the land or property?")
          contentAsString(result) must include("How much relief is being claimed on part of the land or property?")
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
