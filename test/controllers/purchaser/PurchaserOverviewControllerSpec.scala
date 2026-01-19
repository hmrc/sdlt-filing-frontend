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

package controllers.purchaser

import base.SpecBase
import forms.purchaser.PurchaserOverviewFormProvider
import models.{FullReturn, GetReturnByRefRequest, NormalMode, Purchaser, UserAnswers}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.FullReturnService
import services.purchaser.PopulatePurchaserService
import views.html.purchaser.PurchaserOverview

import scala.concurrent.Future
import scala.util.Success

class PurchaserOverviewControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new PurchaserOverviewFormProvider()
  private val form = formProvider()

  def createPurchaser(id: String,
                      forename1: Option[String] = None,
                      forename2: Option[String] = None,
                      name: Option[String] = None,
                      companyName: Option[String] = None): Purchaser = {
    Purchaser(
      purchaserID = Some(id),
      forename1 = forename1,
      forename2 = forename2,
      surname = name,
      address1 = Some("123 Street")
    )
  }

  val testid = "PUR001"
  val testStorn = "VEN001"
  val testReturnRef = "REF001"

  private val testPurchaser = createPurchaser(
    testid,
    forename1 = Some("John"),
    forename2 = Some("Michael"),
    name = Some("Smith")
  )

  private val testFullReturn = FullReturn(
    stornId = testStorn,
    returnResourceRef = testReturnRef,
    vendor = None,
    purchaser = Some(Seq(testPurchaser))
  )

  private val testUserAnswers = UserAnswers(
    id = userAnswersId,
    returnId = Some("test-return-id"),
    storn = testStorn,
    fullReturn = Some(testFullReturn)
  )

  lazy val purchaserOverviewRoute: String = routes.PurchaserOverviewController.onPageLoad(1).url
  lazy val purchaserOverviewSubmitRoute: String = routes.PurchaserOverviewController.onSubmit().url
  lazy val changePurchaserRoute: String = routes.PurchaserOverviewController.changePurchaser("PUR001").url
  lazy val removePurchaserRoute: String = routes.PurchaserOverviewController.removePurchaser("REF001").url

  "PurchaserOverviewController" - {

    "onPageLoad" - {

      "must return OK and the correct view when no vendors exist" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        val emptyFullReturn = FullReturn(stornId = testStorn,
          returnResourceRef = testReturnRef, vendor = None, purchaser = None)
        val emptyUserAnswers = testUserAnswers.copy(fullReturn = Some(emptyFullReturn))

        when(mockFullReturnService.getFullReturn(any[GetReturnByRefRequest])(any(), any()))
          .thenReturn(Future.successful(emptyFullReturn))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, purchaserOverviewRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[PurchaserOverview]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(None, None, None, routes.PurchaserBeforeYouStartController.onPageLoad(), form, NormalMode, false)(request, messages(application)).toString
        }
      }

      "must return OK and the correct view with vendors" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        when(mockFullReturnService.getFullReturn(any[GetReturnByRefRequest])(any(), any()))
          .thenReturn(Future.successful(testFullReturn))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(testUserAnswers))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, purchaserOverviewRoute)

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("John Michael Smith")
        }
      }

      "must calculate errorCalc correctly when vendors + purchasers > 99" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        val purchasers = (1 to 60).map(i => createPurchaser(
          s"PUR$i",
          name = Some(s"Purchaser$i")
        ))

        val vendors = (1 to 50).map(_ => models.Vendor())

        val fullReturnWithMany = FullReturn(stornId = testStorn,
          returnResourceRef = testReturnRef, vendor = Some(vendors), purchaser = Some(purchasers))

        when(mockFullReturnService.getFullReturn(any[GetReturnByRefRequest])(any(), any()))
          .thenReturn(Future.successful(fullReturnWithMany))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(testUserAnswers))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, purchaserOverviewRoute)

          val result = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must return OK with pagination when more than 15 purchasers exist" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        val purchasers = (1 to 20).map(i => createPurchaser(
          s"VEN$i",
          name = Some(s"Vendor$i")
        ))

        val fullReturnWithManyPurchasers = FullReturn(stornId = testStorn,
          returnResourceRef = testReturnRef, vendor = None, purchaser = Some(purchasers))

        when(mockFullReturnService.getFullReturn(any[GetReturnByRefRequest])(any(), any()))
          .thenReturn(Future.successful(fullReturnWithManyPurchasers))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(testUserAnswers))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, purchaserOverviewRoute)

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("govuk-pagination")
        }
      }

      "must redirect to BeforeStartReturn when no returnId exists" in {
        val noReturnIdUserAnswers = testUserAnswers.copy(returnId = None)

        val application = applicationBuilder(userAnswers = Some(noReturnIdUserAnswers))
          .build()

        running(application) {
          val request = FakeRequest(GET, purchaserOverviewRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.preliminary.routes.BeforeStartReturnController.onPageLoad().url
        }
      }

      "must redirect to JourneyRecovery when fullReturnService fails" in {
        val mockFullReturnService = mock[FullReturnService]

        when(mockFullReturnService.getFullReturn(any[GetReturnByRefRequest])(any(), any()))
          .thenReturn(Future.failed(new RuntimeException("Service error")))

        val application = applicationBuilder(userAnswers = Some(testUserAnswers))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, purchaserOverviewRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to ReturnTaskList when vendors missing PurchaserId" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        val purchaserWithoutRef = Purchaser(purchaserID = None, surname = Some("Jones"))
        val fullReturnWithBadVendor = FullReturn(stornId = testStorn,
          returnResourceRef = testReturnRef, vendor = None, purchaser = Some(Seq(purchaserWithoutRef)))

        when(mockFullReturnService.getFullReturn(any[GetReturnByRefRequest])(any(), any()))
          .thenReturn(Future.successful(fullReturnWithBadVendor))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(testUserAnswers))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, purchaserOverviewRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must handle pagination index correctly" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        val purchasers = (1 to 30).map(i => createPurchaser(
          s"PUR$i",
          name = Some(s"Purchaser$i")
        ))

        val fullReturnWithManyPurchasers = FullReturn(stornId = testStorn,
          returnResourceRef = testReturnRef, vendor = None, purchaser = Some(purchasers))

        when(mockFullReturnService.getFullReturn(any[GetReturnByRefRequest])(any(), any()))
          .thenReturn(Future.successful(fullReturnWithManyPurchasers))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(testUserAnswers))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val page2Route = routes.PurchaserOverviewController.onPageLoad(2).url
          val request = FakeRequest(GET, page2Route)

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Purchaser16")
        }
      }
    }

    "onSubmit" - {

      "must redirect to Before you start when user answers yes (true)" in {
        val mockSessionRepository = mock[SessionRepository]

        val application = applicationBuilder(userAnswers = Some(testUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, purchaserOverviewSubmitRoute)
            .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.PurchaserBeforeYouStartController.onPageLoad().url
        }
      }

      "must redirect to ReturnTaskList when user answers no (false)" in {
        val mockSessionRepository = mock[SessionRepository]

        val application = applicationBuilder(userAnswers = Some(testUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, purchaserOverviewSubmitRoute)
            .withFormUrlEncodedBody(("value", "false"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must return BadRequest when invalid data is submitted with no vendors" in {
        val emptyFullReturn = FullReturn(stornId = testStorn,
          returnResourceRef = testReturnRef, vendor = None, purchaser = None)
        val emptyUserAnswers = testUserAnswers.copy(fullReturn = Some(emptyFullReturn))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()

        running(application) {
          val request = FakeRequest(POST, purchaserOverviewSubmitRoute)
            .withFormUrlEncodedBody(("value", ""))

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
        }
      }

      "must return BadRequest when invalid data is submitted with vendors" in {
        val application = applicationBuilder(userAnswers = Some(testUserAnswers))
          .build()

        running(application) {
          val request = FakeRequest(POST, purchaserOverviewSubmitRoute)
            .withFormUrlEncodedBody(("value", "invalid"))

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) must include("John Michael Smith")
        }
      }

      "must calculate errorCalc correctly on submit with form errors" in {
        val purchasers = (1 to 60).map(i => createPurchaser(
          s"VEN$i",
          name = Some(s"Vendor$i")
        ))

        val vendors = (1 to 50).map(_ => models.Vendor())

        val fullReturnWithMany = FullReturn(stornId = testStorn,
          returnResourceRef = testReturnRef, vendor = Some(vendors), purchaser = Some(purchasers))
        val userAnswersWithMany = testUserAnswers.copy(fullReturn = Some(fullReturnWithMany))

        val application = applicationBuilder(userAnswers = Some(userAnswersWithMany))
          .build()

        running(application) {
          val request = FakeRequest(POST, purchaserOverviewSubmitRoute)
            .withFormUrlEncodedBody(("value", ""))

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          // errorCalc should be true (60 + 50 = 110 > 99)
        }
      }
    }

    "changePurchaser" - {

      "must populate session and redirect to VendorCYA when vendor found" in {
        val mockPopulatePurchaserService = mock[PopulatePurchaserService]
        val mockSessionRepository = mock[SessionRepository]

        when(mockPopulatePurchaserService.populatePurchaserInSession(any(), any(), any()))
          .thenReturn(Success(testUserAnswers))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(testUserAnswers))
          .overrides(
            bind[PopulatePurchaserService].toInstance(mockPopulatePurchaserService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, changePurchaserRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.purchaser.routes.PurchaserBeforeYouStartController.onPageLoad().url

          verify(mockPopulatePurchaserService, times(1)).populatePurchaserInSession(eqTo(testPurchaser), eqTo("PUR001"), any())
          verify(mockSessionRepository, times(1)).set(any())
        }
      }

      "must redirect to JourneyRecovery when Purchaser not found" in {
        val application = applicationBuilder(userAnswers = Some(testUserAnswers))
          .build()

        running(application) {
          val nonExistentPurchaserRoute = routes.PurchaserOverviewController.changePurchaser("NONEXISTENT").url
          val request = FakeRequest(GET, nonExistentPurchaserRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must handle purchasers with different IDs" in {
        val purchaser2 = createPurchaser(
          "PUR002",
          forename1 = Some("Jane"),
          name = Some("Doe")
        )

        val fullReturnWithMultiple = FullReturn(stornId = testStorn,
          returnResourceRef = testReturnRef, vendor = None, purchaser = Some(Seq(testPurchaser, purchaser2)))
        val userAnswersMultiple = testUserAnswers.copy(fullReturn = Some(fullReturnWithMultiple))

        val mockPopulatePurchaserService = mock[PopulatePurchaserService]
        val mockSessionRepository = mock[SessionRepository]

        when(mockPopulatePurchaserService.populatePurchaserInSession(any(), any(), any()))
          .thenReturn(Success(userAnswersMultiple))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(userAnswersMultiple))
          .overrides(
            bind[PopulatePurchaserService].toInstance(mockPopulatePurchaserService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val changeVendor2Route = routes.PurchaserOverviewController.changePurchaser("PUR002").url
          val request = FakeRequest(GET, changeVendor2Route)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          verify(mockPopulatePurchaserService, times(1)).populatePurchaserInSession(eqTo(purchaser2), eqTo("PUR002"), any())
        }
      }
    }

    "removePurchaser" - {

      "must set vendorId in session and redirect to Remove Vendor" in {
        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(testUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, removePurchaserRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.PurchaserRemoveController.onPageLoad().url

          verify(mockSessionRepository, times(1)).set(any())
        }
      }

      "must handle different vendor IDs" in {
        val vendorIds = Seq("REF001", "REF002", "REF003")

        vendorIds.foreach { vendorId =>
          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

          val application = applicationBuilder(userAnswers = Some(testUserAnswers))
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

          running(application) {
            val removeRoute = routes.PurchaserOverviewController.removePurchaser(vendorId).url
            val request = FakeRequest(GET, removeRoute)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.PurchaserRemoveController.onPageLoad().url
          }
        }
      }
    }
  }
}