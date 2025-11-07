/*
 * Copyright 2025 HM Revenue & Customs
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
import constants.FullReturnConstants.*
import models.UserAnswers
import org.mockito.ArgumentMatchers.{any, argThat, eq as eqTo}
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.FullReturnService

import scala.concurrent.Future

class ReturnTaskListControllerSpec extends SpecBase with MockitoSugar {

  "ReturnTaskList Controller" - {

    "onPageLoad" - {

      "must return SEE_OTHER and the correct view when no returnId is provided" in {

        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        when(mockFullReturnService.getFullReturn(eqTo(None))(any(), any()))
          .thenReturn(Future.successful(emptyFullReturn))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(None).url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
        }
      }

      "must return OK and the correct view when returnId is provided" in {

        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]
        val testReturnId = Some("123456")

        when(mockFullReturnService.getFullReturn(eqTo(testReturnId))(any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(testReturnId).url)
          val result = route(application, request).value

          status(result) mustEqual OK
          verify(mockFullReturnService, times(1)).getFullReturn(eqTo(testReturnId))(any(), any())
          verify(mockSessionRepository, times(1)).set(any[UserAnswers])
        }
      }

      "must return OK with sections when fullReturn has complete data" in {

        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        when(mockFullReturnService.getFullReturn(any())(any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(returnId = Some("12345"))))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(None).url)
          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Prelim Questions")
          contentAsString(result) must include("Vendor Questions")
        }
      }

      "must return OK and show prelim questions section as 'Not Started' when fullReturn has minimal data" in {

        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        when(mockFullReturnService.getFullReturn(any())(any(), any()))
          .thenReturn(Future.successful(incompleteFullReturn))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(returnId = Some("12345"))))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(None).url)
          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Prelim Questions")
          contentAsString(result) must include("Not Started")
        }
      }

      "must save UserAnswers to session repository with correct data" in {

        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]
        val testReturnId = Some("TEST-123")

        when(mockFullReturnService.getFullReturn(eqTo(testReturnId))(any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(testReturnId).url)
          val result = route(application, request).value

          status(result) mustEqual OK

          verify(mockSessionRepository, times(1)).set(argThat[UserAnswers] { userAnswers =>
            userAnswers.fullReturn.isDefined &&
              userAnswers.fullReturn.get.stornId == Some("STORN123456")
          })
        }
      }

      "must handle service failure gracefully" in {

        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        when(mockFullReturnService.getFullReturn(any())(any(), any()))
          .thenReturn(Future.failed(new RuntimeException("Service error")))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(Some("123345")).url)

          intercept[RuntimeException] {
            await(route(application, request).value)
          }

          verify(mockFullReturnService, times(1)).getFullReturn(any())(any(), any())
          verify(mockSessionRepository, never()).set(any[UserAnswers])
        }
      }

      "must redirect when no return id is provided" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(None).url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.preliminary.routes.BeforeStartReturnController.onPageLoad().url)

          verify(mockFullReturnService, never()).getFullReturn(any())(any(), any())
          verify(mockSessionRepository, never()).set(any[UserAnswers])
        }
      }

      "must handle session repository failure gracefully" in {

        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        when(mockFullReturnService.getFullReturn(any())(any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.failed(new RuntimeException("Repository error")))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(Some("123345")).url)

          intercept[RuntimeException] {
            await(route(application, request).value)
          }

          verify(mockFullReturnService, times(1)).getFullReturn(any())(any(), any())
          verify(mockSessionRepository, times(1)).set(any[UserAnswers])
        }
      }

      "must call FullReturnService with correct returnId parameter" in {

        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]
        val testReturnId = Some("TEST-123")

        when(mockFullReturnService.getFullReturn(eqTo(testReturnId))(any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(testReturnId).url)
          val result = route(application, request).value

          status(result) mustEqual OK
          verify(mockFullReturnService, times(1)).getFullReturn(eqTo(testReturnId))(any(), any())
        }
      }

      "must render two sections (Prelim and Vendor)" in {

        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        when(mockFullReturnService.getFullReturn(any())(any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(Some("123345")).url)
          val result = route(application, request).value

          status(result) mustEqual OK
          val content = contentAsString(result)
          content must include("Prelim Questions")
          content must include("Vendor Questions")
        }
      }

      "must create UserAnswers with userId from request" in {

        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]
        val testUserId = "test-user-123"

        when(mockFullReturnService.getFullReturn(any())(any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(id = testUserId)))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(Some("123345")).url)
          val result = route(application, request).value

          status(result) mustEqual OK

          verify(mockSessionRepository, times(1)).set(argThat[UserAnswers] { userAnswers =>
            userAnswers.fullReturn.isDefined &&
              userAnswers.fullReturn.get.stornId.contains("STORN123456") &&
              userAnswers.id.nonEmpty
          })
        }
      }

      "must handle minimal FullReturn with only stornId" in {

        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        when(mockFullReturnService.getFullReturn(any())(any(), any()))
          .thenReturn(Future.successful(minimalFullReturn))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(Some("123345")).url)
          val result = route(application, request).value

          status(result) mustEqual OK
          verify(mockFullReturnService, times(1)).getFullReturn(any())(any(), any())
          verify(mockSessionRepository, times(1)).set(any[UserAnswers])
        }
      }
    }
  }
}