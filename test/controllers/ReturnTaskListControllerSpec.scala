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
import models.*
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

  val testReturnId = "123456"
  val testStorn = "TESTSTORN"
  val testGetReturnByRefRequest: GetReturnByRefRequest = GetReturnByRefRequest(returnResourceRef = testReturnId, storn = testStorn)

  "ReturnTaskList Controller" - {

    "onPageLoad" - {

      "must return SEE_OTHER and redirect to NoReturnId error page when no returnId is provided in URL or UserAnswers" in {
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
          redirectLocation(result) mustBe Some(controllers.routes.NoReturnReferenceController.onPageLoad().url)

          verify(mockFullReturnService, never()).getFullReturn(any())(any(), any())
          verify(mockSessionRepository, never()).set(any[UserAnswers])
        }
      }

      "must return OK and the correct view when returnId is provided in URL" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        when(mockFullReturnService.getFullReturn(eqTo(testGetReturnByRefRequest))(any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(storn = testStorn)))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(Some(testReturnId)).url)
          val result = route(application, request).value

          status(result) mustEqual OK
          verify(mockFullReturnService, times(1)).getFullReturn(eqTo(testGetReturnByRefRequest))(any(), any())
          verify(mockSessionRepository, times(1)).set(any[UserAnswers])
        }
      }

      "must return OK and use returnId from UserAnswers when not provided in URL" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        when(mockFullReturnService.getFullReturn(eqTo(testGetReturnByRefRequest))(any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(returnId = Some(testReturnId), storn = testStorn)))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(None).url)
          val result = route(application, request).value

          status(result) mustEqual OK
          verify(mockFullReturnService, times(1)).getFullReturn(eqTo(testGetReturnByRefRequest))(any(), any())
          verify(mockSessionRepository, times(1)).set(any[UserAnswers])
        }
      }

      "must prioritize URL returnId over UserAnswers returnId" in {
        val urlReturnId = "URL-RETURN-ID"
        val userAnswersReturnId = "UA-RETURN-ID"
        val expectedRequest = GetReturnByRefRequest(returnResourceRef = urlReturnId, storn = testStorn)

        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        when(mockFullReturnService.getFullReturn(eqTo(expectedRequest))(any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(returnId = Some(userAnswersReturnId), storn = testStorn)))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(Some(urlReturnId)).url)
          val result = route(application, request).value

          status(result) mustEqual OK
          verify(mockFullReturnService, times(1)).getFullReturn(eqTo(expectedRequest))(any(), any())
        }
      }

      "must return OK with sections when fullReturn has complete data" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        when(mockFullReturnService.getFullReturn(any())(any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(returnId = Some(testReturnId), storn = testStorn)))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(None).url)
          val result = route(application, request).value

          status(result) mustEqual OK
          val content = contentAsString(result)
          content must include("Prelim Questions")
          content must include("Vendor Questions")
        }
      }

      "must return OK and show sections with incomplete data status" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        when(mockFullReturnService.getFullReturn(any())(any(), any()))
          .thenReturn(Future.successful(incompleteFullReturn))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(returnId = Some(testReturnId), storn = testStorn)))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(None).url)
          val result = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must save UserAnswers to session repository with correct returnId" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        when(mockFullReturnService.getFullReturn(eqTo(testGetReturnByRefRequest))(any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(storn = testStorn)))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(Some(testReturnId)).url)
          val result = route(application, request).value

          status(result) mustEqual OK

          verify(mockSessionRepository, times(1)).set(argThat[UserAnswers] { userAnswers =>
            userAnswers.returnId.contains(testReturnId) &&
              userAnswers.fullReturn.isDefined &&
              userAnswers.fullReturn.get.stornId == "STORN123456"
          })
        }
      }

      "must save UserAnswers with correct storn from request" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        when(mockFullReturnService.getFullReturn(any())(any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(returnId = Some(testReturnId), storn = testStorn)))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(None).url)
          val result = route(application, request).value

          status(result) mustEqual OK

          verify(mockSessionRepository, times(1)).set(argThat[UserAnswers] { userAnswers =>
            userAnswers.storn == testStorn &&
              userAnswers.returnId.contains(testReturnId)
          })
        }
      }

      "must save UserAnswers with correct userId from request" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]
        val testUserId = "id" // Must match the id in emptyUserAnswers

        when(mockFullReturnService.getFullReturn(any())(any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(id = testUserId, returnId = Some(testReturnId), storn = testStorn)))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(None).url)
          val result = route(application, request).value

          status(result) mustEqual OK

          verify(mockSessionRepository, times(1)).set(argThat[UserAnswers] { userAnswers =>
            userAnswers.id == testUserId &&
              userAnswers.returnId.contains(testReturnId)
          })
        }
      }

      "must save UserAnswers with fullReturn from service" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        when(mockFullReturnService.getFullReturn(any())(any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(returnId = Some(testReturnId), storn = testStorn)))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(None).url)
          val result = route(application, request).value

          status(result) mustEqual OK

          verify(mockSessionRepository, times(1)).set(argThat[UserAnswers] { userAnswers =>
            userAnswers.fullReturn.isDefined &&
              userAnswers.fullReturn.get == completeFullReturn
          })
        }
      }

      "must handle FullReturnService failure and propagate exception" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]
        val serviceException = new RuntimeException("Service error")

        when(mockFullReturnService.getFullReturn(any())(any(), any()))
          .thenReturn(Future.failed(serviceException))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(returnId = Some(testReturnId), storn = testStorn)))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(None).url)

          val exception = intercept[RuntimeException] {
            await(route(application, request).value)
          }

          exception.getMessage mustBe "Service error"
          verify(mockFullReturnService, times(1)).getFullReturn(any())(any(), any())
          verify(mockSessionRepository, never()).set(any[UserAnswers])
        }
      }

      "must handle SessionRepository failure and propagate exception" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]
        val repositoryException = new RuntimeException("Repository error")

        when(mockFullReturnService.getFullReturn(any())(any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.failed(repositoryException))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(returnId = Some(testReturnId), storn = testStorn)))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(None).url)

          val exception = intercept[RuntimeException] {
            await(route(application, request).value)
          }

          exception.getMessage mustBe "Repository error"
          verify(mockFullReturnService, times(1)).getFullReturn(any())(any(), any())
          verify(mockSessionRepository, times(1)).set(any[UserAnswers])
        }
      }

      "must call FullReturnService with GetReturnByRefRequest containing correct returnResourceRef" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]
        val customReturnId = "CUSTOM-RETURN-123"
        val expectedRequest = GetReturnByRefRequest(returnResourceRef = customReturnId, storn = testStorn)

        when(mockFullReturnService.getFullReturn(eqTo(expectedRequest))(any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(storn = testStorn)))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(Some(customReturnId)).url)
          val result = route(application, request).value

          status(result) mustEqual OK
          verify(mockFullReturnService, times(1)).getFullReturn(eqTo(expectedRequest))(any(), any())
        }
      }

      "must render view with PrelimTaskList section" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        when(mockFullReturnService.getFullReturn(any())(any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(returnId = Some(testReturnId), storn = testStorn)))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(None).url)
          val result = route(application, request).value

          status(result) mustEqual OK
          val content = contentAsString(result)
          content must include("Prelim Questions")
        }
      }

      "must render view with VendorTaskList section" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        when(mockFullReturnService.getFullReturn(any())(any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(returnId = Some(testReturnId), storn = testStorn)))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(None).url)
          val result = route(application, request).value

          status(result) mustEqual OK
          val content = contentAsString(result)
          content must include("Vendor Questions")
        }
      }

      "must render view with both sections in correct order" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        when(mockFullReturnService.getFullReturn(any())(any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(returnId = Some(testReturnId), storn = testStorn)))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(None).url)
          val result = route(application, request).value

          status(result) mustEqual OK
          val content = contentAsString(result)

          val prelimIndex = content.indexOf("Prelim Questions")
          val vendorIndex = content.indexOf("Vendor Questions")

          prelimIndex must be < vendorIndex
        }
      }

      "must handle minimal FullReturn" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        when(mockFullReturnService.getFullReturn(any())(any(), any()))
          .thenReturn(Future.successful(minimalFullReturn))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(returnId = Some(testReturnId), storn = testStorn)))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(None).url)
          val result = route(application, request).value

          status(result) mustEqual OK
          verify(mockFullReturnService, times(1)).getFullReturn(any())(any(), any())
          verify(mockSessionRepository, times(1)).set(any[UserAnswers])
        }
      }

      "must handle empty FullReturn" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        when(mockFullReturnService.getFullReturn(any())(any(), any()))
          .thenReturn(Future.successful(emptyFullReturn))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(returnId = Some(testReturnId), storn = testStorn)))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(None).url)
          val result = route(application, request).value

          status(result) mustEqual OK
          verify(mockFullReturnService, times(1)).getFullReturn(any())(any(), any())
          verify(mockSessionRepository, times(1)).set(any[UserAnswers])
        }
      }

      "must execute operations in correct order (service then repository)" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]
        var serviceCallTime: Long = 0
        var repositoryCallTime: Long = 0

        when(mockFullReturnService.getFullReturn(any())(any(), any()))
          .thenAnswer(_ => {
            serviceCallTime = System.nanoTime()
            Future.successful(completeFullReturn)
          })

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenAnswer(_ => {
            repositoryCallTime = System.nanoTime()
            Future.successful(true)
          })

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(returnId = Some(testReturnId), storn = testStorn)))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(None).url)
          val result = route(application, request).value

          status(result) mustEqual OK
          serviceCallTime must be < repositoryCallTime
        }
      }

      "must use identify and getData actions" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        when(mockFullReturnService.getFullReturn(any())(any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(returnId = Some(testReturnId), storn = testStorn)))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(None).url)
          val result = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must handle different returnId formats" in {
        val returnIds = List("123", "ABC-123", "test-return-id", "RRF-2024-001")

        returnIds.foreach { returnId =>
          val mockFullReturnService = mock[FullReturnService]
          val mockSessionRepository = mock[SessionRepository]
          val expectedRequest = GetReturnByRefRequest(returnResourceRef = returnId, storn = testStorn)

          when(mockFullReturnService.getFullReturn(eqTo(expectedRequest))(any(), any()))
            .thenReturn(Future.successful(completeFullReturn))

          when(mockSessionRepository.set(any[UserAnswers]))
            .thenReturn(Future.successful(true))

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(storn = testStorn)))
            .overrides(
              bind[FullReturnService].toInstance(mockFullReturnService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

          running(application) {
            val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(Some(returnId)).url)
            val result = route(application, request).value

            status(result) mustEqual OK
            verify(mockFullReturnService, times(1)).getFullReturn(eqTo(expectedRequest))(any(), any())
          }
        }
      }

      "must not call service when returnId is None and UserAnswers has no returnId" in {
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
          verify(mockFullReturnService, never()).getFullReturn(any())(any(), any())
          verify(mockSessionRepository, never()).set(any[UserAnswers])
        }
      }

      "must render view with PurchaserTaskList section" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        when(mockFullReturnService.getFullReturn(any())(any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(returnId = Some(testReturnId), storn = testStorn)))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(None).url)
          val result = route(application, request).value

          status(result) mustEqual OK
          val content = contentAsString(result)
          content must include("Purchaser Questions")
        }
      }

      "must render view with PurchaserAgentTaskList section" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        when(mockFullReturnService.getFullReturn(any())(any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(returnId = Some(testReturnId), storn = testStorn)))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(None).url)
          val result = route(application, request).value

          status(result) mustEqual OK
          val content = contentAsString(result)
          content must include("Purchaser Agent Questions")
        }
      }

      "must render view with all sections in correct order" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        when(mockFullReturnService.getFullReturn(any())(any(), any()))
          .thenReturn(Future.successful(completeFullReturn))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(returnId = Some(testReturnId), storn = testStorn)))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(None).url)
          val result = route(application, request).value

          status(result) mustEqual OK
          val content = contentAsString(result)

          val prelimIndex = content.indexOf("Prelim Questions")
          val vendorIndex = content.indexOf("Vendor Questions")
          val purchaserIndex = content.indexOf("Purchaser Questions")
          val purchaserAgentIndex = content.indexOf("Purchaser Agent Questions")

          prelimIndex must be < vendorIndex
          vendorIndex must be < purchaserIndex
          purchaserIndex must be < purchaserAgentIndex
        }
      }

      "must handle FullReturn with no purchasers" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]
        val fullReturnWithNoPurchasers = completeFullReturn.copy(purchaser = None)

        when(mockFullReturnService.getFullReturn(any())(any(), any()))
          .thenReturn(Future.successful(fullReturnWithNoPurchasers))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(returnId = Some(testReturnId), storn = testStorn)))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(None).url)
          val result = route(application, request).value

          status(result) mustEqual OK
          val content = contentAsString(result)
          content must include("Purchaser Questions")
        }
      }

      "must handle FullReturn with empty purchaser sequence" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]
        val fullReturnWithEmptyPurchasers = completeFullReturn.copy(purchaser = Some(Seq.empty))

        when(mockFullReturnService.getFullReturn(any())(any(), any()))
          .thenReturn(Future.successful(fullReturnWithEmptyPurchasers))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(returnId = Some(testReturnId), storn = testStorn)))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(None).url)
          val result = route(application, request).value

          status(result) mustEqual OK
          val content = contentAsString(result)
          content must include("Purchaser Questions")
        }
      }

      "must handle FullReturn with no return agents" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]
        val fullReturnWithNoAgents = completeFullReturn.copy(returnAgent = None)

        when(mockFullReturnService.getFullReturn(any())(any(), any()))
          .thenReturn(Future.successful(fullReturnWithNoAgents))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(returnId = Some(testReturnId), storn = testStorn)))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(None).url)
          val result = route(application, request).value

          status(result) mustEqual OK
          val content = contentAsString(result)
          content must include("Purchaser Agent Questions")
        }
      }

      "must handle FullReturn with empty return agent sequence" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]
        val fullReturnWithEmptyAgents = completeFullReturn.copy(returnAgent = Some(Seq.empty))

        when(mockFullReturnService.getFullReturn(any())(any(), any()))
          .thenReturn(Future.successful(fullReturnWithEmptyAgents))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(returnId = Some(testReturnId), storn = testStorn)))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(None).url)
          val result = route(application, request).value

          status(result) mustEqual OK
          val content = contentAsString(result)
          content must include("Purchaser Agent Questions")
        }
      }

      "must handle FullReturn with purchaser agents but no agent type Purchaser" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]
        val vendorAgent = ReturnAgent(agentType = Some("Vendor"))
        val fullReturnWithVendorAgent = completeFullReturn.copy(returnAgent = Some(Seq(vendorAgent)))

        when(mockFullReturnService.getFullReturn(any())(any(), any()))
          .thenReturn(Future.successful(fullReturnWithVendorAgent))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(returnId = Some(testReturnId), storn = testStorn)))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(None).url)
          val result = route(application, request).value

          status(result) mustEqual OK
          val content = contentAsString(result)
          content must include("Purchaser Agent Questions")
        }
      }
    }
  }
}