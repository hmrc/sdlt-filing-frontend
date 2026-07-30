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

package controllers.purchaser

import base.SpecBase
import controllers.routes
import models.{FullReturn, Purchaser, ReturnInfo, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.purchaser.PopulatePurchaserService
import views.html.purchaser.PurchaserIncompleteOverviewView

import scala.concurrent.Future
import scala.util.Success

class PurchaserIncompleteOverviewControllerSpec extends SpecBase with MockitoSugar {

  private val testStorn     = "TESTSTORN"
  private val testReturnRef = "REF001"

  private val continueUrl = controllers.routes.ReturnTaskListController.onPageLoad(None).url

  private lazy val onPageLoadRoute = controllers.purchaser.routes.PurchaserIncompleteOverviewController.onPageLoad().url
  private def updatePurchaserRoute(id: String) = controllers.purchaser.routes.PurchaserIncompleteOverviewController.updatePurchaser(id).url
  private def removePurchaserRoute(id: String) = controllers.purchaser.routes.PurchaserIncompleteOverviewController.removePurchaser(id).url

  private val completeMainPurchaser = Purchaser(
    purchaserID = Some("PUR-1"),
    isCompany = Some("no"),
    address1 = Some("1 Test Street"),
    isTrustee = Some("no"),
    isConnectedToVendor = Some("no"),
    surname = Some("Smith"),
    nino = Some("AB123456C"),
    dateOfBirth = Some("10/03/1992")
  )

  private val incompletePurchaser = Purchaser(
    purchaserID = Some("PUR-2"),
    isCompany = Some("no"),
    address1 = Some("2 Test Road"),
    isTrustee = None,
    isConnectedToVendor = Some("no"),
    surname = Some("Jones")
  )

  private def fullReturnWith(purchasers: Seq[Purchaser]): FullReturn =
    FullReturn(
      stornId = testStorn,
      returnResourceRef = testReturnRef,
      vendor = None,
      purchaser = Some(purchasers),
      returnInfo = Some(ReturnInfo(mainPurchaserID = Some("PUR-1")))
    )

  private def userAnswersWith(fr: FullReturn): UserAnswers =
    UserAnswers(
      id = userAnswersId,
      returnId = Some("test-return-id"),
      storn = testStorn,
      fullReturn = Some(fr)
    )

  private val testUserAnswers = userAnswersWith(fullReturnWith(Seq(completeMainPurchaser, incompletePurchaser)))

  "PurchaserIncompleteOverview Controller" - {

    "onPageLoad" - {

      "must return OK and render the view listing only the incomplete purchasers" in {

        val application = applicationBuilder(userAnswers = Some(testUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, onPageLoadRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[PurchaserIncompleteOverviewView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view(Seq(incompletePurchaser), continueUrl)(request, messages(application)).toString
        }
      }

      "must redirect to the return task list when every purchaser is complete" in {

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWith(fullReturnWith(Seq(completeMainPurchaser))))).build()

        running(application) {
          val request = FakeRequest(GET, onPageLoadRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad(None).url
        }
      }

      "must redirect to the return task list when there are no purchasers" in {

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWith(fullReturnWith(Seq.empty)))).build()

        running(application) {
          val request = FakeRequest(GET, onPageLoadRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad(None).url
        }
      }

      "must redirect to Journey Recovery when no user answers exist" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, onPageLoadRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "updatePurchaser" - {

      "must populate the purchaser in session and redirect to Check Your Answers when the purchaser is found" in {

        val mockSessionRepository        = mock[SessionRepository]
        val mockPopulatePurchaserService = mock[PopulatePurchaserService]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
        when(mockPopulatePurchaserService.populatePurchaserInSession(any(), any(), any())) thenReturn Success(testUserAnswers)

        val application =
          applicationBuilder(userAnswers = Some(testUserAnswers))
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository),
              bind[PopulatePurchaserService].toInstance(mockPopulatePurchaserService)
            )
            .build()

        running(application) {
          val request = FakeRequest(GET, updatePurchaserRoute("PUR-2"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaser.routes.PurchaserCheckYourAnswersController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery when the purchaser is not found" in {

        val application = applicationBuilder(userAnswers = Some(testUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, updatePurchaserRoute("UNKNOWN"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery when no user answers exist" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, updatePurchaserRoute("PUR-2"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "removePurchaser" - {

      "must set the remove page and redirect to the Purchaser Remove controller" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(testUserAnswers))
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request = FakeRequest(GET, removePurchaserRoute("PUR-2"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaser.routes.PurchaserRemoveController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery when no user answers exist" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, removePurchaserRoute("PUR-2"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}