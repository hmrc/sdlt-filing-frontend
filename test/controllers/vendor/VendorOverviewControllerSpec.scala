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

package controllers.vendor

import base.SpecBase
import forms.vendor.VendorOverviewFormProvider
import models.{FullReturn, GetReturnByRefRequest, NormalMode, UserAnswers, Vendor}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.FullReturnService
import services.vendor.PopulateVendorService
import views.html.vendor.VendorOverview

import scala.concurrent.Future
import scala.util.Success

class VendorOverviewControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new VendorOverviewFormProvider()
  private val form = formProvider()

  private def createVendor(id: String, forename1: Option[String] = None, forename2: Option[String] = None,
                           name: Option[String] = None, vendorResourceRef: Option[String] = None): Vendor = {
    Vendor(
      vendorID = Some(id),
      forename1 = forename1,
      forename2 = forename2,
      name = name,
      address1 = Some("123 Street"),
      vendorResourceRef = vendorResourceRef
    )
  }

  val testStorn = "VEN001"
  val testReturnRef = "REF001"

  private val testVendor = createVendor(
    testStorn,
    forename1 = Some("John"),
    forename2 = Some("Michael"),
    name = Some("Smith"),
    vendorResourceRef = Some(testReturnRef)
  )

  private val testFullReturn = FullReturn(
    stornId = testStorn,
    returnResourceRef = testReturnRef,
    vendor = Some(Seq(testVendor)),
    purchaser = None
  )

  private val testUserAnswers = UserAnswers(
    id = userAnswersId,
    returnId = Some("test-return-id"),
    storn = "TESTSTORN",
    fullReturn = Some(testFullReturn)
  )

  lazy val vendorOverviewRoute: String = routes.VendorOverviewController.onPageLoad(1).url
  lazy val vendorOverviewSubmitRoute: String = routes.VendorOverviewController.onSubmit().url
  lazy val changeVendorRoute: String = routes.VendorOverviewController.changeVendor("REF001").url
  lazy val removeVendorRoute: String = routes.VendorOverviewController.removeVendor("REF001").url

  "VendorOverviewController" - {

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
          val request = FakeRequest(GET, vendorOverviewRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[VendorOverview]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(None, None, None, routes.VendorBeforeYouStartController.onPageLoad(), form, NormalMode, false)(request, messages(application)).toString
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
          val request = FakeRequest(GET, vendorOverviewRoute)

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("John Michael Smith")
        }
      }

      "must calculate errorCalc correctly when vendors + purchasers > 98" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        val vendors = (1 to 49).map(i => createVendor(
          s"VEN$i",
          name = Some(s"Vendor$i"),
          vendorResourceRef = Some(s"REF$i")
        ))

        val purchasers = (1 to 50).map(_ => models.Purchaser())

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
          val request = FakeRequest(GET, vendorOverviewRoute)

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("To add a new vendor, you must remove a purchaser or vendor")
        }
      }

      "must return OK with pagination when more than 15 vendors exist" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        val vendors = (1 to 20).map(i => createVendor(
          s"VEN$i",
          name = Some(s"Vendor$i"),
          vendorResourceRef = Some(s"REF$i")
        ))

        val fullReturnWithManyVendors = FullReturn(stornId = testStorn,
          returnResourceRef = testReturnRef, vendor = Some(vendors), purchaser = None)

        when(mockFullReturnService.getFullReturn(any[GetReturnByRefRequest])(any(), any()))
          .thenReturn(Future.successful(fullReturnWithManyVendors))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(testUserAnswers))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, vendorOverviewRoute)

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
          val request = FakeRequest(GET, vendorOverviewRoute)

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
          val request = FakeRequest(GET, vendorOverviewRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to ReturnTaskList when vendors missing vendorResourceRef" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        val vendorWithoutRef = createVendor("VEN002", name = Some("Jones"), vendorResourceRef = None)
        val fullReturnWithBadVendor = FullReturn(stornId = testStorn,
          returnResourceRef = testReturnRef, vendor = Some(Seq(vendorWithoutRef)), purchaser = None)

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
          val request = FakeRequest(GET, vendorOverviewRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ReturnTaskListController.onPageLoad().url
        }
      }

      "must handle pagination index correctly" in {
        val mockFullReturnService = mock[FullReturnService]
        val mockSessionRepository = mock[SessionRepository]

        val vendors = (1 to 30).map(i => createVendor(
          s"VEN$i",
          name = Some(s"Vendor$i"),
          vendorResourceRef = Some(s"REF$i")
        ))

        val fullReturnWithManyVendors = FullReturn(stornId = testStorn,
          returnResourceRef = testReturnRef, vendor = Some(vendors), purchaser = None)

        when(mockFullReturnService.getFullReturn(any[GetReturnByRefRequest])(any(), any()))
          .thenReturn(Future.successful(fullReturnWithManyVendors))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(testUserAnswers))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val page2Route = routes.VendorOverviewController.onPageLoad(2).url
          val request = FakeRequest(GET, page2Route)

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Vendor16")
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
          val request = FakeRequest(POST, vendorOverviewSubmitRoute)
            .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.VendorBeforeYouStartController.onPageLoad().url
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
          val request = FakeRequest(POST, vendorOverviewSubmitRoute)
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
          val request = FakeRequest(POST, vendorOverviewSubmitRoute)
            .withFormUrlEncodedBody(("value", ""))

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
        }
      }

      "must return BadRequest when invalid data is submitted with vendors" in {
        val application = applicationBuilder(userAnswers = Some(testUserAnswers))
          .build()

        running(application) {
          val request = FakeRequest(POST, vendorOverviewSubmitRoute)
            .withFormUrlEncodedBody(("value", "invalid"))

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) must include("John Michael Smith")
        }
      }

      "must calculate errorCalc correctly on submit with form errors" in {
        val vendors = (1 to 60).map(i => createVendor(
          s"VEN$i",
          name = Some(s"Vendor$i"),
          vendorResourceRef = Some(s"REF$i")
        ))

        val purchasers = (1 to 50).map(_ => models.Purchaser())

        val fullReturnWithMany = FullReturn(stornId = testStorn,
          returnResourceRef = testReturnRef, vendor = Some(vendors), purchaser = Some(purchasers))
        val userAnswersWithMany = testUserAnswers.copy(fullReturn = Some(fullReturnWithMany))

        val application = applicationBuilder(userAnswers = Some(userAnswersWithMany))
          .build()

        running(application) {
          val request = FakeRequest(POST, vendorOverviewSubmitRoute)
            .withFormUrlEncodedBody(("value", ""))

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
        }
      }
    }

    "changeVendor" - {

      "must populate session and redirect to VendorCYA when vendor found" in {
        val mockPopulateVendorService = mock[PopulateVendorService]
        val mockSessionRepository = mock[SessionRepository]

        when(mockPopulateVendorService.populateVendorInSession(any(), any(), any()))
          .thenReturn(Success(testUserAnswers))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(testUserAnswers))
          .overrides(
            bind[PopulateVendorService].toInstance(mockPopulateVendorService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, changeVendorRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.vendor.routes.VendorCheckYourAnswersController.onPageLoad().url

          verify(mockPopulateVendorService, times(1)).populateVendorInSession(eqTo(testVendor), eqTo("REF001"), any())
          verify(mockSessionRepository, times(1)).set(any())
        }
      }

      "must redirect to JourneyRecovery when vendor not found" in {
        val application = applicationBuilder(userAnswers = Some(testUserAnswers))
          .build()

        running(application) {
          val nonExistentVendorRoute = routes.VendorOverviewController.changeVendor("NONEXISTENT").url
          val request = FakeRequest(GET, nonExistentVendorRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must handle vendors with different IDs" in {
        val vendor2 = createVendor(
          "VEN002",
          forename1 = Some("Jane"),
          name = Some("Doe"),
          vendorResourceRef = Some("REF002")
        )

        val fullReturnWithMultiple = FullReturn(stornId = testStorn,
          returnResourceRef = testReturnRef, vendor = Some(Seq(testVendor, vendor2)), purchaser = None)
        val userAnswersMultiple = testUserAnswers.copy(fullReturn = Some(fullReturnWithMultiple))

        val mockPopulateVendorService = mock[PopulateVendorService]
        val mockSessionRepository = mock[SessionRepository]

        when(mockPopulateVendorService.populateVendorInSession(any(), any(), any()))
          .thenReturn(Success(userAnswersMultiple))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(userAnswersMultiple))
          .overrides(
            bind[PopulateVendorService].toInstance(mockPopulateVendorService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val changeVendor2Route = routes.VendorOverviewController.changeVendor("REF002").url
          val request = FakeRequest(GET, changeVendor2Route)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          verify(mockPopulateVendorService, times(1)).populateVendorInSession(eqTo(vendor2), eqTo("REF002"), any())
        }
      }
    }

    "removeVendor" - {

      "must set vendorId in session and redirect to Remove Vendor" in {
        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(testUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, removeVendorRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.RemoveVendorController.onPageLoad().url

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
            val removeRoute = routes.VendorOverviewController.removeVendor(vendorId).url
            val request = FakeRequest(GET, removeRoute)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.RemoveVendorController.onPageLoad().url
          }
        }
      }
    }
  }
}