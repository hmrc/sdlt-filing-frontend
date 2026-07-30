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

package controllers

import base.SpecBase
import models.{FullReturn, Lease, Transaction, UserAnswers}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.lease.PopulateLeaseService
import services.transaction.PopulateTransactionService

import scala.concurrent.Future
import scala.util.{Failure, Success}

class ResumeSectionControllerSpec extends SpecBase with MockitoSugar with ScalaFutures {

  val testStorn     = "STORN123456"
  val testReturnRef = "123456"

  private val testTransaction = Transaction(
    transactionID          = Some("TXN001"),
    transactionDescription = Some("F")
  )

  private val testLease = Lease(
    leaseID = Some("LSE001")
  )

  private val fullReturnWithBoth = FullReturn(
    stornId           = testStorn,
    returnResourceRef = testReturnRef,
    transaction       = Some(testTransaction),
    lease             = Some(testLease)
  )

  private val fullReturnEmpty = FullReturn(
    stornId           = testStorn,
    returnResourceRef = testReturnRef,
    transaction       = None,
    lease             = None
  )

  private def userAnswersWith(fullReturn: FullReturn): UserAnswers =
    UserAnswers(
      id         = userAnswersId,
      returnId   = Some("test-return-id"),
      storn      = "TESTSTORN",
      fullReturn = Some(fullReturn)
    )

  private val userAnswersBoth  = userAnswersWith(fullReturnWithBoth)
  private val userAnswersEmpty = userAnswersWith(fullReturnEmpty)

  private lazy val journeyRecoveryUrl =
    controllers.routes.JourneyRecoveryController.onPageLoad().url

  private def resumeRoute(section: String): String =
    controllers.routes.ResumeSectionController.resume(section, None).url

  "ResumeSectionController" - {

    ".resume" - {

      "for the transaction section" - {

        "must populate the transaction into session and redirect to Transaction Check Your Answers" in {
          val mockPopulateTransactionService = mock[PopulateTransactionService]
          val mockPopulateLeaseService       = mock[PopulateLeaseService]
          val mockSessionRepository          = mock[SessionRepository]

          when(mockPopulateTransactionService.populateTransactionInSession(any(), any()))
            .thenReturn(Success(userAnswersBoth))
          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

          val application = applicationBuilder(userAnswers = Some(userAnswersBoth))
            .overrides(
              bind[PopulateTransactionService].toInstance(mockPopulateTransactionService),
              bind[PopulateLeaseService].toInstance(mockPopulateLeaseService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

          running(application) {
            val request = FakeRequest(GET, resumeRoute("transaction"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual
              controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad().url

            verify(mockPopulateTransactionService, times(1))
              .populateTransactionInSession(eqTo(testTransaction), any())
            verify(mockSessionRepository, times(1)).set(any())
            verify(mockPopulateLeaseService, never()).populateLeaseInSession(any(), any())
          }
        }

        "must match the section name case-insensitively" in {
          val mockPopulateTransactionService = mock[PopulateTransactionService]
          val mockSessionRepository          = mock[SessionRepository]

          when(mockPopulateTransactionService.populateTransactionInSession(any(), any()))
            .thenReturn(Success(userAnswersBoth))
          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

          val application = applicationBuilder(userAnswers = Some(userAnswersBoth))
            .overrides(
              bind[PopulateTransactionService].toInstance(mockPopulateTransactionService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

          running(application) {
            val request = FakeRequest(GET, resumeRoute("TRANSACTION"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual
              controllers.transaction.routes.TransactionCheckYourAnswersController.onPageLoad().url
          }
        }

        "must redirect to Journey Recovery when the FullReturn has no transaction" in {
          val mockPopulateTransactionService = mock[PopulateTransactionService]
          val mockSessionRepository          = mock[SessionRepository]

          val application = applicationBuilder(userAnswers = Some(userAnswersEmpty))
            .overrides(
              bind[PopulateTransactionService].toInstance(mockPopulateTransactionService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

          running(application) {
            val request = FakeRequest(GET, resumeRoute("transaction"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual journeyRecoveryUrl

            verify(mockPopulateTransactionService, never()).populateTransactionInSession(any(), any())
            verify(mockSessionRepository, never()).set(any())
          }
        }
      }

      "for the lease section" - {

        "must populate the lease into session and redirect to Lease Check Your Answers" in {
          val mockPopulateTransactionService = mock[PopulateTransactionService]
          val mockPopulateLeaseService       = mock[PopulateLeaseService]
          val mockSessionRepository          = mock[SessionRepository]

          when(mockPopulateLeaseService.populateLeaseInSession(any(), any()))
            .thenReturn(Success(userAnswersBoth))
          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

          val application = applicationBuilder(userAnswers = Some(userAnswersBoth))
            .overrides(
              bind[PopulateTransactionService].toInstance(mockPopulateTransactionService),
              bind[PopulateLeaseService].toInstance(mockPopulateLeaseService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

          running(application) {
            val request = FakeRequest(GET, resumeRoute("lease"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual
              controllers.lease.routes.LeaseCheckYourAnswersController.onPageLoad().url

            verify(mockPopulateLeaseService, times(1))
              .populateLeaseInSession(eqTo(testLease), any())
            verify(mockSessionRepository, times(1)).set(any())
            verify(mockPopulateTransactionService, never()).populateTransactionInSession(any(), any())
          }
        }

        "must redirect to Journey Recovery when the FullReturn has no lease" in {
          val mockPopulateLeaseService = mock[PopulateLeaseService]
          val mockSessionRepository    = mock[SessionRepository]

          val application = applicationBuilder(userAnswers = Some(userAnswersEmpty))
            .overrides(
              bind[PopulateLeaseService].toInstance(mockPopulateLeaseService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

          running(application) {
            val request = FakeRequest(GET, resumeRoute("lease"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual journeyRecoveryUrl

            verify(mockPopulateLeaseService, never()).populateLeaseInSession(any(), any())
            verify(mockSessionRepository, never()).set(any())
          }
        }
      }

      "for an unrecognised section" - {

        "must redirect to Journey Recovery and populate nothing" in {
          val mockPopulateTransactionService = mock[PopulateTransactionService]
          val mockPopulateLeaseService       = mock[PopulateLeaseService]
          val mockSessionRepository          = mock[SessionRepository]

          val application = applicationBuilder(userAnswers = Some(userAnswersBoth))
            .overrides(
              bind[PopulateTransactionService].toInstance(mockPopulateTransactionService),
              bind[PopulateLeaseService].toInstance(mockPopulateLeaseService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

          running(application) {
            val request = FakeRequest(GET, resumeRoute("something-else"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual journeyRecoveryUrl

            verify(mockPopulateTransactionService, never()).populateTransactionInSession(any(), any())
            verify(mockPopulateLeaseService, never()).populateLeaseInSession(any(), any())
            verify(mockSessionRepository, never()).set(any())
          }
        }
      }

      "when the populate service fails" - {

        "must not persist to session and must fail the request rather than redirecting" in {
          val mockPopulateTransactionService = mock[PopulateTransactionService]
          val mockSessionRepository          = mock[SessionRepository]

          when(mockPopulateTransactionService.populateTransactionInSession(any(), any()))
            .thenReturn(Failure(new RuntimeException("populate failed")))

          val application = applicationBuilder(userAnswers = Some(userAnswersBoth))
            .overrides(
              bind[PopulateTransactionService].toInstance(mockPopulateTransactionService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

          running(application) {
            val request = FakeRequest(GET, resumeRoute("transaction"))

            val result = route(application, request).value

            whenReady(result.failed) { e =>
              e mustBe a[RuntimeException]
            }

            verify(mockSessionRepository, never()).set(any())
          }
        }
      }
    }
  }
}