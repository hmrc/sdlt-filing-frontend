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

package controllers.vendor

import base.SpecBase
import controllers.routes
import models.{FullReturn, UserAnswers, Vendor}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.vendor.PopulateVendorService
import views.html.vendor.VendorIncompleteOverviewView

import scala.concurrent.Future
import scala.util.Success

class VendorIncompleteOverviewControllerSpec extends SpecBase with MockitoSugar {

  private val testStorn     = "TESTSTORN"
  private val testReturnRef = "REF001"

  private val continueUrl = controllers.routes.ReturnTaskListController.onPageLoad(None).url

  private lazy val onPageLoadRoute = controllers.vendor.routes.VendorIncompleteOverviewController.onPageLoad().url
  private def updateVendorRoute(vendorId: String) = controllers.vendor.routes.VendorIncompleteOverviewController.updateVendor(vendorId).url
  private def removeVendorRoute(vendorId: String) = controllers.vendor.routes.VendorIncompleteOverviewController.removeVendor(vendorId).url

  private val completeVendor = Vendor(
    vendorID = Some("VEN-1"),
    name = Some("Smith"),
    address1 = Some("1 Test Street")
  )

  private val incompleteVendor = Vendor(
    vendorID = Some("VEN-2"),
    name = Some("Jones"),
    address1 = None
  )

  private def fullReturnWith(vendors: Seq[Vendor]): FullReturn =
    FullReturn(
      stornId = testStorn,
      returnResourceRef = testReturnRef,
      vendor = Some(vendors)
    )

  private def userAnswersWith(fr: FullReturn): UserAnswers =
    UserAnswers(
      id = userAnswersId,
      returnId = Some("test-return-id"),
      storn = testStorn,
      fullReturn = Some(fr)
    )

  private val testUserAnswers = userAnswersWith(fullReturnWith(Seq(completeVendor, incompleteVendor)))

  "VendorIncompleteOverview Controller" - {

    "onPageLoad" - {

      "must return OK and render the view listing only the incomplete vendors" in {

        val application = applicationBuilder(userAnswers = Some(testUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, onPageLoadRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[VendorIncompleteOverviewView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view(Seq(incompleteVendor), continueUrl)(request, messages(application)).toString
        }
      }

      "must redirect to the return task list when every vendor is complete" in {

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWith(fullReturnWith(Seq(completeVendor))))).build()

        running(application) {
          val request = FakeRequest(GET, onPageLoadRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad(None).url
        }
      }

      "must redirect to the return task list when there are no vendors" in {

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

    "updateVendor" - {

      "must populate the vendor in session and redirect to Before You Start when the vendor is found" in {

        val mockSessionRepository     = mock[SessionRepository]
        val mockPopulateVendorService = mock[PopulateVendorService]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
        when(mockPopulateVendorService.populateVendorInSession(any(), any(), any())) thenReturn Success(testUserAnswers)

        val application =
          applicationBuilder(userAnswers = Some(testUserAnswers))
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository),
              bind[PopulateVendorService].toInstance(mockPopulateVendorService)
            )
            .build()

        running(application) {
          val request = FakeRequest(GET, updateVendorRoute("VEN-2"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.vendor.routes.VendorBeforeYouStartController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery when the vendor is not found" in {

        val application = applicationBuilder(userAnswers = Some(testUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, updateVendorRoute("UNKNOWN"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery when no user answers exist" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, updateVendorRoute("VEN-2"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "removeVendor" - {

      "must set the remove page and redirect to the Remove Vendor controller" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(testUserAnswers))
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request = FakeRequest(GET, removeVendorRoute("VEN-2"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.vendor.routes.RemoveVendorController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery when no user answers exist" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, removeVendorRoute("VEN-2"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}