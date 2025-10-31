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
import models.{FullReturn, PrelimReturn, VendorReturn}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.FullReturnService

import scala.concurrent.Future

class ReturnTaskListControllerSpec extends SpecBase with MockitoSugar {

  "ReturnTaskList Controller" - {

    "onPageLoad" - {

      "must return OK and the correct view when no returnId is provided" in {

        val mockFullReturnService = mock[FullReturnService]
        val mockFullReturn = new FullReturn(None, None)

        when(mockFullReturnService.getFullReturn(eqTo(None))(any(), any()))
          .thenReturn(Future.successful(mockFullReturn))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(None).url)
          val result = route(application, request).value

          status(result) mustEqual OK
          verify(mockFullReturnService, times(1)).getFullReturn(eqTo(None))(any(), any())
        }
      }

      "must return OK and the correct view when returnId is provided" in {

        val mockFullReturnService = mock[FullReturnService]
        val mockPrelimReturn = new PrelimReturn(
          stornId = "id",
          purchaserIsCompany = "YES",
          surNameOrCompanyName = "Test",
          houseNumber = Some(34),
          addressLine1 = "test address",
          addressLine2 = None,
          addressLine3 = None,
          addressLine4 = None,
          transactionType = "O",
          postcode = None
        )

        val mockVendorReturn = new VendorReturn(
          stornId = "12345",
          returnResourceRef = "124",
          title = "Mr",
          forename1 = "Test",
          forename2 = Some("Man"),
          surName = "Test",
          houseNumber = Some(1),
          addressLine1 = "Test Street",
          addressLine2 = Some("Apartment 5"),
          addressLine3 = None,
          addressLine4 = None,
          postcode = Some("TE23 5TT"),
          isRepresentedByAgent = "No"
        )
        
        val mockFullReturn = new FullReturn(Some(mockPrelimReturn), Some(mockVendorReturn))
        val testReturnId = Some("123456")

        when(mockFullReturnService.getFullReturn(eqTo(testReturnId))(any(), any()))
          .thenReturn(Future.successful(mockFullReturn))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(testReturnId).url)
          val result = route(application, request).value

          status(result) mustEqual OK
          verify(mockFullReturnService, times(1)).getFullReturn(eqTo(testReturnId))(any(), any())
        }
      }

      "must return OK with sections when fullReturn has prelimReturn" in {

        val mockFullReturnService = mock[FullReturnService]
        val mockPrelimReturn = new PrelimReturn(
          stornId = "id",
          purchaserIsCompany = "YES",
          surNameOrCompanyName = "Test",
          houseNumber = Some(34),
          addressLine1 = "test address",
          addressLine2 = None,
          addressLine3 = None,
          addressLine4 = None,
          transactionType = "O",
          postcode = None
        )
        
        val mockVendorReturn = new VendorReturn(
          stornId = "12345",
          returnResourceRef = "124",
          title = "Mr",
          forename1 = "Test",
          forename2 = Some("Man"),
          surName = "Test",
          houseNumber = Some(1),
          addressLine1 = "Test Street",
          addressLine2 = Some("Apartment 5"),
          addressLine3 = None,
          addressLine4 = None,
          postcode = Some("TE23 5TT"),
          isRepresentedByAgent = "No"
        )
        
        val mockFullReturn = new FullReturn(Some(mockPrelimReturn), Some(mockVendorReturn))

        when(mockFullReturnService.getFullReturn(any())(any(), any()))
          .thenReturn(Future.successful(mockFullReturn))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(None).url)
          val result = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must return OK and show prelim questions section as 'Not Started' when fullReturn has no prelimReturn" in {

        val mockFullReturnService = mock[FullReturnService]
        val mockFullReturn = new FullReturn(None, None)

        when(mockFullReturnService.getFullReturn(any())(any(), any()))
          .thenReturn(Future.successful(mockFullReturn))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService)
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

      "must handle service failure gracefully" in {

        val mockFullReturnService = mock[FullReturnService]

        when(mockFullReturnService.getFullReturn(any())(any(), any()))
          .thenReturn(Future.failed(new RuntimeException("Service error")))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(None).url)

          intercept[RuntimeException] {
            await(route(application, request).value)
          }

          verify(mockFullReturnService, times(1)).getFullReturn(any())(any(), any())
        }
      }

      "must call FullReturnService with correct returnId parameter" in {

        val mockFullReturnService = mock[FullReturnService]
        val mockPrelimReturn = new PrelimReturn(
          stornId = "id",
          purchaserIsCompany = "YES",
          surNameOrCompanyName = "Test",
          houseNumber = Some(34),
          addressLine1 = "test address",
          addressLine2 = None,
          addressLine3 = None,
          addressLine4 = None,
          transactionType = "O",
          postcode = None
        )
        
        val mockVendorReturn = new VendorReturn(
          stornId = "12345",
          returnResourceRef = "124",
          title = "Mr",
          forename1 = "Test",
          forename2 = Some("Man"),
          surName = "Test",
          houseNumber = Some(1),
          addressLine1 = "Test Street",
          addressLine2 = Some("Apartment 5"),
          addressLine3 = None,
          addressLine4 = None,
          postcode = Some("TE23 5TT"),
          isRepresentedByAgent = "No"
        )
        
        val mockFullReturn = new FullReturn(Some(mockPrelimReturn), Some(mockVendorReturn))
        val testReturnId = Some("TEST-123")

        when(mockFullReturnService.getFullReturn(eqTo(testReturnId))(any(), any()))
          .thenReturn(Future.successful(mockFullReturn))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[FullReturnService].toInstance(mockFullReturnService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnTaskListController.onPageLoad(testReturnId).url)
          val result = route(application, request).value

          status(result) mustEqual OK
          verify(mockFullReturnService, times(1)).getFullReturn(eqTo(testReturnId))(any(), any())
        }
      }
    }
  }
}