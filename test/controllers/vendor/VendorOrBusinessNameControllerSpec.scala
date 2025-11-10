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
import forms.vendor.VendorOrBusinessNameFormProvider
import models.vendor.{VendorName, whoIsTheVendor}
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.vendor.{VendorOrBusinessNamePage, WhoIsTheVendorPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.vendor.VendorOrBusinessNameView

import scala.concurrent.Future

class VendorOrBusinessNameControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new VendorOrBusinessNameFormProvider()
  val form = formProvider()

  lazy val vendorOrBusinessNameRoute = controllers.vendor.routes.VendorOrBusinessNameController.onPageLoad(NormalMode).url

  "VendorOrBusinessName Controller" - {
    "when the vendor is an Individual" - {

      val individualVendor = UserAnswers(userAnswersId, storn = "TESTSTORN").set(WhoIsTheVendorPage, whoIsTheVendor.Individual).success.value

      val vendor = "Individual"

      val vendorName = VendorName(forename1 = Some("First name"), forename2 = Some("Middle name"), name = "Surname")

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(individualVendor)).build()

        running(application) {
          val request = FakeRequest(GET, vendorOrBusinessNameRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[VendorOrBusinessNameView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode, vendorOrBusiness = vendor)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = individualVendor.set(VendorOrBusinessNamePage, vendorName).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, vendorOrBusinessNameRoute)

          val view = application.injector.instanceOf[VendorOrBusinessNameView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(vendorName), NormalMode, vendorOrBusiness = vendor)(request, messages(application)).toString
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
            FakeRequest(POST, vendorOrBusinessNameRoute)
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
            FakeRequest(POST, vendorOrBusinessNameRoute)
              .withFormUrlEncodedBody(("name", ""))

          val boundForm = form.bind(Map("name" -> ""))

          val view = application.injector.instanceOf[VendorOrBusinessNameView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode, vendorOrBusiness = vendor)(request, messages(application)).toString
        }
      }
    }

    "when the vendor is a Business" - {

      val businessVendor = UserAnswers(userAnswersId, storn = "TESTSTORN").set(WhoIsTheVendorPage, whoIsTheVendor.Business).success.value

      val vendor = "Business"

      val vendorName = VendorName(forename1 = None, forename2 = None, name = "Business name")

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(businessVendor)).build()

        running(application) {
          val request = FakeRequest(GET, vendorOrBusinessNameRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[VendorOrBusinessNameView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode, vendor)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = businessVendor.set(VendorOrBusinessNamePage, vendorName).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
        
        running(application) {
          val request = FakeRequest(GET, vendorOrBusinessNameRoute)

          val view = application.injector.instanceOf[VendorOrBusinessNameView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(vendorName), NormalMode, vendorOrBusiness = vendor)(request, messages(application)).toString
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
            FakeRequest(POST, vendorOrBusinessNameRoute)
              .withFormUrlEncodedBody(("name", "Business name"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, vendorOrBusinessNameRoute)
              .withFormUrlEncodedBody(("name", ""))

          val boundForm = form.bind(Map("name" -> ""))

          val view = application.injector.instanceOf[VendorOrBusinessNameView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode, vendorOrBusiness = vendor)(request, messages(application)).toString
        }
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, vendorOrBusinessNameRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, vendorOrBusinessNameRoute)
            .withFormUrlEncodedBody(("name", "name"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
