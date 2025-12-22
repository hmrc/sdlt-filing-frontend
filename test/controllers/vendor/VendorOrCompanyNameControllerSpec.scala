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
import controllers.routes
import forms.vendor.VendorOrCompanyNameFormProvider
import models.vendor.{VendorName, whoIsTheVendor}
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.vendor.{VendorOrCompanyNamePage, WhoIsTheVendorPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.vendor.VendorOrCompanyNameView

import scala.concurrent.Future

class VendorOrCompanyNameControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new VendorOrCompanyNameFormProvider()
  val form = formProvider()

  lazy val vendorOrCompanyNameRoute = controllers.vendor.routes.VendorOrCompanyNameController.onPageLoad(NormalMode).url

  "VendorOrCompanyName Controller" - {
    "when the vendor is an Individual" - {

      val individualVendor = UserAnswers(userAnswersId, storn = "TESTSTORN").set(WhoIsTheVendorPage, whoIsTheVendor.Individual).success.value

      val vendor = "Individual"

      val vendorName = VendorName(forename1 = Some("First name"), forename2 = Some("Middle name"), name = "Surname")

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(individualVendor)).build()

        running(application) {
          val request = FakeRequest(GET, vendorOrCompanyNameRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[VendorOrCompanyNameView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode, vendorOrCompany = vendor)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = individualVendor.set(VendorOrCompanyNamePage, vendorName).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, vendorOrCompanyNameRoute)

          val view = application.injector.instanceOf[VendorOrCompanyNameView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(vendorName), NormalMode, vendorOrCompany = vendor)(request, messages(application)).toString
        }
      }

      "must redirect to the next page when valid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, vendorOrCompanyNameRoute)
              .withFormUrlEncodedBody(("forename1", "First name"), ("forename2", "Middle name"), ("name", "Surname"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(individualVendor)).build()

        running(application) {
          val request =
            FakeRequest(POST, vendorOrCompanyNameRoute)
              .withFormUrlEncodedBody(("name", ""))

          val boundForm = form.bind(Map("name" -> ""))

          val view = application.injector.instanceOf[VendorOrCompanyNameView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode, vendorOrCompany = vendor)(request, messages(application)).toString
        }
      }
    }

    "when the vendor is a Company" - {

      val companyVendor = UserAnswers(userAnswersId, storn = "TESTSTORN").set(WhoIsTheVendorPage, whoIsTheVendor.Company).success.value

      val vendor = "Company"

      val vendorName = VendorName(forename1 = None, forename2 = None, name = "Company name")

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(companyVendor)).build()

        running(application) {
          val request = FakeRequest(GET, vendorOrCompanyNameRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[VendorOrCompanyNameView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode, vendor)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = companyVendor.set(VendorOrCompanyNamePage, vendorName).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
        
        running(application) {
          val request = FakeRequest(GET, vendorOrCompanyNameRoute)

          val view = application.injector.instanceOf[VendorOrCompanyNameView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(vendorName), NormalMode, vendorOrCompany = vendor)(request, messages(application)).toString
        }
      }

      "must redirect to the next page when valid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, vendorOrCompanyNameRoute)
              .withFormUrlEncodedBody(("name", "Company name"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must redirect to the next page when purchaser is Company" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(companyVendor))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, vendorOrCompanyNameRoute)
              .withFormUrlEncodedBody(("name", "answer"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, vendorOrCompanyNameRoute)
              .withFormUrlEncodedBody(("name", ""))

          val boundForm = form.bind(Map("name" -> ""))

          val view = application.injector.instanceOf[VendorOrCompanyNameView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode, vendorOrCompany = vendor)(request, messages(application)).toString
        }
      }
    }

    "must handle missing purchaser type on GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, vendorOrCompanyNameRoute)
        val result = route(application, request).value

        val view = application.injector.instanceOf[VendorOrCompanyNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, "")(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, vendorOrCompanyNameRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, vendorOrCompanyNameRoute)
            .withFormUrlEncodedBody(("name", "name"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
